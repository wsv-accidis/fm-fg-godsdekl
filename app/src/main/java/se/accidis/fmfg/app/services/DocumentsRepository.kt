package se.accidis.fmfg.app.services

import android.content.Context
import androidx.core.util.AtomicFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.json.JSONObject
import se.accidis.fmfg.app.model.Document
import se.accidis.fmfg.app.model.DocumentBuilder
import se.accidis.fmfg.app.model.DocumentLink
import se.accidis.fmfg.app.model.mutate
import se.accidis.fmfg.app.utils.Resource
import timber.log.Timber
import java.io.FileNotFoundException
import java.util.UUID

/**
 * Repository for documents using a single-store auto-save model.
 */
class DocumentsRepository private constructor(context: Context) {
    private val context: Context = context.applicationContext
    private val ioDispatcher = Dispatchers.IO.limitedParallelism(1)
    private var loadJob: Job? = null
    private val prefs = Preferences.getInstance(this.context)
    private val repositoryScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val _currentDocument = MutableStateFlow<Document?>(null)
    val currentDocumentFlow: Flow<Document> = _currentDocument.filterNotNull()
    val currentDocumentOrNull: Document?
        get() = _currentDocument.value

    private val _documents = MutableStateFlow<Resource<List<DocumentLink>>>(Resource.Loading)
    val documents: StateFlow<Resource<List<DocumentLink>>> = _documents.asStateFlow()

    private val initialDocumentLoad: Deferred<Document> = repositoryScope.async(ioDispatcher) {
        val document = migrateLegacyCurrentDocument() ?: loadActiveDocument()
            ?: DocumentBuilder.createNew(author = prefs.defaultAuthor)
        _currentDocument.compareAndSet(null, document)
        val current = _currentDocument.value ?: document
        Timber.d("Current document initialized with ID: %s", current.id)
        current
    }

    suspend fun awaitCurrentDocument(): Document =
        _currentDocument.value ?: initialDocumentLoad.await()

    fun beginLoad() {
        if (_documents.value is Resource.Success) {
            Timber.d("Documents already loaded, nothing to do.")
            return
        }
        if (loadJob?.isActive == true) {
            Timber.d("Documents load already in progress.")
            return
        }

        Timber.d("Loading documents...")
        _documents.value = Resource.Loading
        loadJob = repositoryScope.launch(ioDispatcher) {
            try {
                val list = context.fileList()
                    .filter {
                        it.startsWith(SAVED_DOCUMENT_PREFIX) && it.endsWith(SAVED_DOCUMENT_SUFFIX)
                    }
                    .mapNotNull { fileName ->
                        try {
                            readDocumentLink(fileName)
                        } catch (ex: Exception) {
                            Timber.w(ex, "Skipping malformed document: %s", fileName)
                            null
                        }
                    }
                    .sorted()

                _documents.value = Resource.Success(list)
                Timber.i("Finished loading documents (%d documents loaded).", list.size)
            } catch (ex: Exception) {
                Timber.e(ex, "Failed to load documents.")
                _documents.value = Resource.Error(ex)
            }
        }
    }

    fun createNewDocument() {
        repositoryScope.launch {
            val current = awaitCurrentDocument()
            if (current.isEmpty) {
                Timber.d("Current document is already empty, nothing to do.")
                return@launch
            }

            val newDoc = DocumentBuilder.createNew(author = prefs.defaultAuthor)
            Timber.d("Created new document with ID: %s", newDoc.id)
            _currentDocument.value = newDoc
            prefs.currentDocumentId = null
        }
    }

    fun deleteDocument(id: UUID) {
        Timber.d("Deleting document with ID: %s", id)
        repositoryScope.launch {
            if (awaitCurrentDocument().id == id) {
                _currentDocument.value = DocumentBuilder.createNew(author = prefs.defaultAuthor)
                prefs.currentDocumentId = null
            }

            withContext(ioDispatcher) {
                val file = context.getFileStreamPath(getFilenameByDocumentId(id))
                AtomicFile(file).delete()
                removeDocumentLink(id)
            }
        }
    }

    fun mutateCurrentDocument(transform: DocumentBuilder.() -> Unit) {
        repositoryScope.launch {
            val current = awaitCurrentDocument()
            val updated = current.mutate(transform)
            if (updated == current) {
                return@launch
            }
            updateCurrentDocument(updated)
        }
    }

    fun openDocument(id: UUID) {
        repositoryScope.launch {
            if (awaitCurrentDocument().id == id) {
                Timber.d("Document with ID %s is already open.", id)
                return@launch
            }

            try {
                val loadedDoc = withContext(ioDispatcher) {
                    Timber.d("Loading document with ID: %s", id)
                    readDocument(getFilenameByDocumentId(id))
                }
                _currentDocument.value = loadedDoc
                prefs.currentDocumentId = loadedDoc.id
            } catch (ex: Exception) {
                Timber.e(ex, "Failed to load document with ID: %s", id)
            }
        }
    }

    fun updateCurrentDocument(document: Document) {
        if (document.isEmpty) {
            val wasSaved = document.isSaved
            val emptyDoc = if (wasSaved) document.copy(timestamp = null) else document
            Timber.d("Current document is empty, skipping auto-save for ID: %s", emptyDoc.id)
            _currentDocument.value = emptyDoc
            if (prefs.currentDocumentId != null) {
                prefs.currentDocumentId = null
            }

            if (wasSaved) {
                repositoryScope.launch(ioDispatcher) {
                    val file = context.getFileStreamPath(getFilenameByDocumentId(emptyDoc.id))
                    if (file.exists()) {
                        AtomicFile(file).delete()
                        removeDocumentLink(emptyDoc.id)
                    }
                }
            }
            return
        }

        val savedDoc = document.copy(timestamp = DateTime.now())
        Timber.d("Auto-saving document with ID: %s", savedDoc.id)
        _currentDocument.value = savedDoc
        prefs.currentDocumentId = savedDoc.id

        repositoryScope.launch(ioDispatcher) {
            try {
                writeDocument(savedDoc)
                upsertDocumentLink(DocumentLink.fromDocument(savedDoc))
            } catch (ex: Exception) {
                Timber.e(ex, "Exception while auto-saving document with ID: %s", savedDoc.id)
            }
        }
    }

    private fun loadActiveDocument(): Document? {
        val activeId = prefs.currentDocumentId ?: return null
        return try {
            readDocument(getFilenameByDocumentId(activeId))
        } catch (_: FileNotFoundException) {
            Timber.w("Active document with ID %s not found, starting blank.", activeId)
            prefs.currentDocumentId = null
            null
        } catch (ex: Exception) {
            Timber.e(ex, "Exception while reading active document with ID: %s", activeId)
            prefs.currentDocumentId = null
            null
        }
    }

    private fun migrateLegacyCurrentDocument(): Document? {
        val legacyFile = context.getFileStreamPath(LEGACY_CURRENT_DOCUMENT)
        if (!legacyFile.exists()) {
            return null
        }

        val legacyDoc = try {
            readDocument(LEGACY_CURRENT_DOCUMENT)
        } catch (ex: Exception) {
            Timber.e(ex, "Exception while migrating legacy current document.")
            null
        } finally {
            context.deleteFile(LEGACY_CURRENT_DOCUMENT)
        }

        if (legacyDoc == null || legacyDoc.isEmpty) {
            return null
        }

        val migratedDoc = if (legacyDoc.timestamp != null) {
            legacyDoc
        } else {
            legacyDoc.copy(timestamp = DateTime.now())
        }

        return try {
            writeDocument(migratedDoc)
            prefs.currentDocumentId = migratedDoc.id
            Timber.i("Migrated legacy current document with ID: %s", migratedDoc.id)
            migratedDoc
        } catch (ex: Exception) {
            Timber.e(ex, "Failed to persist migrated legacy document with ID: %s", migratedDoc.id)
            migratedDoc
        }
    }

    private fun readDocument(fileName: String): Document {
        val file = context.getFileStreamPath(fileName)
        val str = AtomicFile(file).openRead().bufferedReader().use { it.readText() }
        return Document.fromJson(JSONObject(str))
    }

    private fun readDocumentLink(fileName: String): DocumentLink {
        val file = context.getFileStreamPath(fileName)
        return AtomicFile(file).openRead().use { DocumentLink.fromStream(it) }
    }

    private fun removeDocumentLink(id: UUID) {
        _documents.update { resource ->
            if (resource is Resource.Success) {
                Resource.Success(resource.data.filterNot { it.id == id })
            } else {
                resource
            }
        }
    }

    private fun upsertDocumentLink(link: DocumentLink) {
        _documents.update { resource ->
            if (resource is Resource.Success) {
                val updated = resource.data
                    .filterNot { it.id == link.id }
                    .plus(link)
                    .sorted()
                Resource.Success(updated)
            } else {
                resource
            }
        }
    }

    private fun writeDocument(document: Document) {
        val file = context.getFileStreamPath(getFilenameByDocumentId(document.id))
        val atomicFile = AtomicFile(file)
        val outputStream = atomicFile.startWrite()
        try {
            outputStream.write(document.toJson().toString().toByteArray(Charsets.UTF_8))
            atomicFile.finishWrite(outputStream)
        } catch (ex: Exception) {
            atomicFile.failWrite(outputStream)
            throw ex
        }
    }

    companion object {
        private const val LEGACY_CURRENT_DOCUMENT = "CurrentDocument.json"
        private const val SAVED_DOCUMENT_PREFIX = "Saved_"
        private const val SAVED_DOCUMENT_SUFFIX = ".json"

        @Volatile
        private var singleton: DocumentsRepository? = null

        @JvmStatic
        fun getInstance(context: Context): DocumentsRepository =
            singleton ?: synchronized(this) {
                singleton ?: DocumentsRepository(context).also { singleton = it }
            }

        private fun getFilenameByDocumentId(documentId: UUID): String =
            "$SAVED_DOCUMENT_PREFIX$documentId$SAVED_DOCUMENT_SUFFIX"
    }
}

package se.accidis.fmfg.app.services

import android.content.Context
import android.content.Context.MODE_PRIVATE
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import se.accidis.fmfg.app.model.*
import se.accidis.fmfg.app.utils.Resource
import timber.log.Timber
import java.io.FileNotFoundException
import java.util.UUID

/**
 * Repository for documents.
 */
class DocumentsRepository private constructor(private val context: Context) {
    private val prefs = Preferences(context)

    private val _currentDocument = MutableStateFlow<Document?>(null)
    val currentDocumentFlow: StateFlow<Document>
        get() {
            ensureDocument()
            @Suppress("UNCHECKED_CAST")
            return _currentDocument as StateFlow<Document>
        }

    val currentDocument: Document
        get() = ensureDocument()

    private val _documents = MutableStateFlow<Resource<List<DocumentLink>>>(Resource.Loading)
    val documents: StateFlow<Resource<List<DocumentLink>>> = _documents.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun beginLoad() {
        val current = _documents.value
        if (current is Resource.Success && current.data.isNotEmpty()) {
            Timber.d("Documents already loaded, nothing to do.")
            return
        }

        Timber.d("Loading documents.")
        _documents.value = Resource.Loading
        repositoryScope.launch {
            try {
                val list = withContext(Dispatchers.IO) {
                    context.fileList()
                        .filter { it.startsWith(SAVED_DOCUMENT_PREFIX) }
                        .map { readDocumentLink(it) }
                        .sorted()
                }

                _documents.value = Resource.Success(list)
                Timber.i("Finished loading documents (%d documents loaded).", list.size)
            } catch (ex: Exception) {
                Timber.e(ex, "Failed to load documents.")
                _documents.value = Resource.Error(ex)
            }
        }
    }

    val isLoaded: Boolean
        get() = _documents.value is Resource.Success

    fun updateCurrentDocument(document: Document) {
        Timber.d("Updating current document with ID: %s", document.id)
        _currentDocument.value = document
        repositoryScope.launch(Dispatchers.IO) {
            try {
                val json = document.toJson().toString()
                context.openFileOutput(CURRENT_DOCUMENT, MODE_PRIVATE).bufferedWriter().use {
                    it.write(json)
                }
            } catch (ex: Exception) {
                Timber.e(ex, "Exception while writing current document.")
            }
        }
    }

    fun saveCurrentDocument(name: String) {
        val document = ensureDocument()
        Timber.d("Saving current document with ID: %s, name = %s", document.id, name)
        val savedDoc = document.mutate { this.name = name }.save()
        repositoryScope.launch(Dispatchers.IO) {
            writeDocument(savedDoc)
            invalidateListOfDocuments()
        }
    }

    suspend fun loadDocument(id: UUID): Document = withContext(Dispatchers.IO) {
        Timber.d("Loading document with ID: %s", id)
        val filename = getFilenameByDocumentId(id)
        readDocument(filename)
    }

    fun deleteDocument(id: UUID) {
        Timber.d("Deleting document with ID: %s", id)
        repositoryScope.launch(Dispatchers.IO) {
            val filename = getFilenameByDocumentId(id)
            context.deleteFile(filename)
            invalidateListOfDocuments()
        }
    }

    private fun ensureDocument(): Document {
        val current = _currentDocument.value
        if (current != null) return current

        val document = try {
            // Using runBlocking here for synchronous initialization when called from property getter
            runBlocking(Dispatchers.IO) {
                readDocument(CURRENT_DOCUMENT)
            }
        } catch (_: FileNotFoundException) {
            Timber.w("Current document not found, might be first startup.")
            null
        } catch (ex: Exception) {
            Timber.e(ex, "Exception while reading current document.")
            null
        } ?: DocumentBuilder.createNew().mutate { author = prefs.defaultAuthor }

        _currentDocument.value = document
        Timber.d("Current document initialized with ID: %s", document.id)
        return document
    }

    private fun invalidateListOfDocuments() {
        _documents.value = Resource.Loading
        beginLoad()
    }

    private fun readDocument(fileName: String): Document {
        val str = context.openFileInput(fileName).bufferedReader().use { it.readText() }
        return Document.fromJson(JSONObject(str))
    }

    private fun writeDocument(document: Document) {
        val fileName = getFilenameByDocumentId(document.id)
        val json = document.toJson().toString()
        context.openFileOutput(fileName, Context.MODE_PRIVATE).bufferedWriter().use {
            it.write(json)
        }
    }

    private fun readDocumentLink(fileName: String): DocumentLink {
        val str = context.openFileInput(fileName).bufferedReader().use { it.readText() }
        return DocumentLink.fromJson(JSONObject(str))
    }

    companion object {
        private const val CURRENT_DOCUMENT = "CurrentDocument.json"
        private const val SAVED_DOCUMENT_PREFIX = "Saved_"

        @Volatile
        private var singleton: DocumentsRepository? = null

        @JvmStatic
        fun getInstance(context: Context): DocumentsRepository =
            singleton ?: synchronized(this) {
                singleton ?: DocumentsRepository(context).also { singleton = it }
            }

        private fun getFilenameByDocumentId(documentId: UUID): String =
            "Saved_$documentId.json"
    }
}

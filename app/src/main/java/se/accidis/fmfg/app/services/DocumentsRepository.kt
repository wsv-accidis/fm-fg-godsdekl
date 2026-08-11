package se.accidis.fmfg.app.services

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import se.accidis.fmfg.app.model.*
import se.accidis.fmfg.app.utils.Resource
import se.accidis.fmfg.app.utils.TAG
import java.io.FileNotFoundException
import java.util.UUID

/**
 * Repository for documents.
 */
class DocumentsRepository private constructor(private val context: Context) {
    private val prefs = Preferences(context)
    
    private val _currentDocument = MutableStateFlow<Document?>(null)
    //val currentDocumentFlow: StateFlow<Document?> = _currentDocument.asStateFlow()

    val currentDocument: Document
        get() = ensureDocument()

    private val _documents = MutableStateFlow<Resource<List<DocumentLink>>>(Resource.Loading)
    val documents: StateFlow<Resource<List<DocumentLink>>> = _documents.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun beginLoad() {
        val current = _documents.value
        if (current is Resource.Success && current.data.isNotEmpty()) {
            Log.d(TAG, "Documents already loaded, nothing to do.")
            return
        }

        Log.d(TAG, "Loading documents.")
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
                Log.i(TAG, "Finished loading documents (${list.size} documents loaded).")
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to load documents.", ex)
                _documents.value = Resource.Error(ex)
            }
        }
    }

    fun changeCurrentDocument(document: Document) {
        Log.d(TAG, "Replacing current document with ID: ${document.id}")
        _currentDocument.value = document
        repositoryScope.launch(Dispatchers.IO) {
            commitDocumentInternal(document)
        }
    }

    fun commitCurrentDocument() {
        _currentDocument.value?.let { doc ->
            repositoryScope.launch(Dispatchers.IO) {
                commitDocumentInternal(doc)
            }
        }
    }

    private fun commitDocumentInternal(document: Document) {
        try {
            val json = document.toJson().toString()
            context.openFileOutput(CURRENT_DOCUMENT, Context.MODE_PRIVATE).bufferedWriter().use {
                it.write(json)
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Exception while writing current document.", ex)
        }
    }

    fun deleteDocument(id: UUID) {
        Log.d(TAG, "Deleting document with ID: $id")
        repositoryScope.launch(Dispatchers.IO) {
            val filename = getFilenameByDocumentId(id)
            context.deleteFile(filename)
            invalidateListOfDocuments()
        }
    }

    /**
     * Gets the current document, initializing it if necessary.
     */
    private fun ensureDocument(): Document {
        val current = _currentDocument.value
        if (current != null) return current

        val document = try {
            // Using runBlocking here for synchronous initialization when called from property getter
            runBlocking(Dispatchers.IO) {
                readDocument(CURRENT_DOCUMENT)
            }
        } catch (_: FileNotFoundException) {
            Log.w(TAG, "Current document not found, might be first startup.")
            null
        } catch (ex: Exception) {
            Log.e(TAG, "Exception while reading current document.", ex)
            null
        } ?: DocumentBuilder.createNew().mutate { author = prefs.defaultAuthor }

        _currentDocument.value = document
        Log.d(TAG, "Current document initialized with ID: ${document.id}")
        return document
    }

    val isLoaded: Boolean
        get() = _documents.value is Resource.Success

    suspend fun loadDocument(id: UUID): Document = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading document with ID: $id")
        val filename = getFilenameByDocumentId(id)
        readDocument(filename)
    }

    fun saveCurrentDocument(name: String) {
        val document = ensureDocument()
        Log.d(TAG, "Saving current document with ID: ${document.id}, name = $name")
        val savedDoc = document.mutate { this.name = name }.save()
        repositoryScope.launch(Dispatchers.IO) {
            writeDocument(savedDoc)
            invalidateListOfDocuments()
        }
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

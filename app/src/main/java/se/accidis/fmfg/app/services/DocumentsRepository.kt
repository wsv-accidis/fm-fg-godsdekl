package se.accidis.fmfg.app.services

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import org.joda.time.DateTime
import org.json.JSONException
import org.json.JSONObject
import se.accidis.fmfg.app.model.Document
import se.accidis.fmfg.app.model.DocumentLink
import se.accidis.fmfg.app.utils.IOUtils
import se.accidis.fmfg.app.utils.TAG
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Collections
import java.util.UUID

/**
 * Repository for documents.
 */
class DocumentsRepository private constructor(context: Context) {
    private val context: Context = context.applicationContext
    private val prefs: Preferences = Preferences(this@DocumentsRepository.context)
    private var openDocument: Document? = null
    private var documents: MutableList<DocumentLink>? = null
    private var onLoadedListener: OnLoadedListener? = null

    fun beginLoad() {
        if (this.isLoaded) {
            Log.d(TAG, "Documents already loaded, nothing to do.")
            if (null != onLoadedListener) {
                onLoadedListener!!.onLoaded(documents!!)
            }
            return
        }

        Log.d(TAG, "Loading documents.")
        val loadTask = LoadTask()
        loadTask.execute()
    }

    fun changeCurrentDocument(document: Document) {
        Log.d(TAG, "Replacing current document with ID: ${document.id}")
        openDocument = document
        commitCurrentDocument()
    }

    fun commitCurrentDocument() {
        try {
            val json = openDocument!!.toJson().toString()
            IOUtils.writeToStream(
                context.openFileOutput(CURRENT_DOCUMENT, Context.MODE_PRIVATE),
                json
            )
        } catch (ex: Exception) {
            Log.e(TAG, "Exception while writing current document.", ex)
        }
    }

    fun deleteDocument(id: UUID) {
        Log.d(TAG, "Deleting document with ID: $id")
        val filename: String = getFilenameByDocumentId(id)
        context.deleteFile(filename)
        invalidate()
    }

    fun ensureCurrentDocumentLoaded() {
        if (null == openDocument) {
            try {
                openDocument = readDocument(CURRENT_DOCUMENT)
            } catch (_: FileNotFoundException) {
                Log.w(TAG, "Current document not found, might be first startup.")
            } catch (ex: Exception) {
                Log.e(TAG, "Exception while reading current document.", ex)
            }

            if (null == openDocument) {
                openDocument = createNewDocument()
                Log.d(TAG, "Created a document with ID: ${openDocument!!.id}")
            } else {
                Log.d(TAG, "Loaded current document with ID: ${openDocument!!.id}")
            }
        }
    }

    val currentDocument: Document?
        get() {
            ensureCurrentDocumentLoaded()
            return openDocument
        }

    val isLoaded: Boolean
        get() = (null != documents)

    @Throws(IOException::class, JSONException::class)
    fun loadDocument(id: UUID): Document {
        Log.d(TAG, "Loading document with ID: $id")
        val filename: String = getFilenameByDocumentId(id)
        return readDocument(filename)
    }

    @Throws(IOException::class, JSONException::class)
    fun saveCurrentDocument(name: String?) {
        Log.d(
            TAG,
            "Saving current document with ID: ${openDocument!!.id}, name = ${openDocument!!.name}"
        )
        ensureCurrentDocumentLoaded()
        openDocument!!.name = name
        openDocument!!.timestamp = DateTime.now()
        openDocument!!.setHasUnsavedChanges(false)
        writeDocument(openDocument!!)
        invalidate()
    }

    fun setOnLoadedListener(listener: OnLoadedListener?) {
        onLoadedListener = listener
    }

    private fun createNewDocument(): Document {
        val document = Document()
        document.author = prefs.defaultAuthor
        return document
    }

    private fun invalidate() {
        documents = null
    }

    @Throws(IOException::class, JSONException::class)
    private fun readDocument(fileName: String?): Document {
        val str = IOUtils.readToEnd(context.openFileInput(fileName))
        val json = JSONObject(str)
        return Document.fromJson(json)
    }

    @Throws(IOException::class, JSONException::class)
    private fun writeDocument(document: Document) {
        val fileName: String = getFilenameByDocumentId(document.id)
        val json = document.toJson().toString()
        IOUtils.writeToStream(context.openFileOutput(fileName, Context.MODE_PRIVATE), json)
    }

    interface OnLoadedListener {
        fun onException(ex: Exception)

        fun onLoaded(list: List<DocumentLink>)
    }

    private inner class LoadTask : AsyncTask<Void?, Void?, Void?>() {
        override fun doInBackground(vararg params: Void?): Void? {
            try {
                val fileList = filterDocuments(context.fileList())

                val list = ArrayList<DocumentLink>()
                for (file in fileList) {
                    val docLink = readDocumentLink(file)
                    list.add(docLink)
                }

                Collections.sort(list)
                documents = list

                Log.i(TAG, "Finished loading documents (${documents!!.size} documents loaded).")
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to load documents.", ex)

                if (null == documents && null != onLoadedListener) {
                    onLoadedListener!!.onException(ex)
                }
            }

            if (null != documents && null != onLoadedListener) {
                onLoadedListener!!.onLoaded(documents!!)
            }

            return null
        }

        fun filterDocuments(files: Array<String>): List<String> {
            val filtered = ArrayList<String>()
            for (fileName in files) {
                if (fileName.startsWith(SAVED_DOCUMENT_PREFIX)) {
                    filtered.add(fileName)
                }
            }
            return filtered
        }

        @Throws(IOException::class, JSONException::class)
        fun readDocumentLink(fileName: String): DocumentLink {
            val str = IOUtils.readToEnd(context.openFileInput(fileName))
            val json = JSONObject(str)
            return DocumentLink.fromJson(json)
        }
    }

    companion object {
        private const val CURRENT_DOCUMENT = "CurrentDocument.json"
        private const val SAVED_DOCUMENT_FORMAT = $$"Saved_%1$s.json"
        private const val SAVED_DOCUMENT_PREFIX = "Saved_"
        private var singleton: DocumentsRepository? = null

        @JvmStatic
        fun getInstance(context: Context): DocumentsRepository {
            return (
                    if (null == singleton)
                        (DocumentsRepository(context).also { singleton = it })
                    else
                        singleton
                    )!!
        }

        private fun getFilenameByDocumentId(documentId: UUID): String {
            return String.format(SAVED_DOCUMENT_FORMAT, documentId.toString())
        }
    }
}

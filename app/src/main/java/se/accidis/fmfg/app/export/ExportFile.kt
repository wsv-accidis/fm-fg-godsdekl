package se.accidis.fmfg.app.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.model.Document
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * Encapsulates file operations related to exporting documents to PDF.
 */
data class ExportFile(
    val filename: String,
    val file: File,
    val uri: Uri,
) {
    companion object {
        private const val EXPORTED_DOC_DIRECTORY = "exported_doc"
        private const val EXPORTED_FILE_MAX_AGE_MS = 1000L * 60 * 60 * 24
        private const val FILENAME_ALLOWED_CHARS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_-"
        private const val FILE_PROVIDER_AUTHORITY = "se.accidis.fmfg.fileprovider"
        private const val SEPARATOR_CHAR = '_'

        fun cleanUpOldExports(context: Context) {
            try {
                val baseDir = File(context.filesDir, EXPORTED_DOC_DIRECTORY)
                if (!baseDir.isDirectory) {
                    return
                }

                val limit = System.currentTimeMillis() - EXPORTED_FILE_MAX_AGE_MS
                baseDir.listFiles()?.forEach { file ->
                    val lastModified = file.lastModified()
                    if (lastModified < limit) {
                        Timber.d(
                            "Deleting old export \"%s\", last modified %d, limit %d.",
                            file.name,
                            lastModified,
                            limit
                        )
                        if (!file.delete()) {
                            Timber.e("Error deleting export \"%s\".", file.name)
                        }
                    }
                }
            } catch (ex: Exception) {
                Timber.e(ex, "Exception while cleaning up old exports:")
            }
        }

        @Throws(IOException::class)
        fun fromDocument(document: Document, extension: String, context: Context): ExportFile {
            val baseDir = File(context.filesDir, EXPORTED_DOC_DIRECTORY)
            if (!baseDir.isDirectory && !baseDir.mkdirs()) {
                throw IOException("Could not create the base directory \"${baseDir.path}\".")
            }

            val fileName = createUniqueFilename(
                baseDir = baseDir,
                documentName = document.name,
                extension = extension,
                defaultName = context.getString(R.string.document_export_default_name)
            )
            val file = File(baseDir, fileName)
            val uri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
            return ExportFile(filename = fileName, file = file, uri = uri)
        }

        private fun createUniqueFilename(
            baseDir: File,
            documentName: String,
            extension: String,
            defaultName: String
        ): String {
            val trimmedName = documentName.trim()
            val baseName = if (trimmedName.isNotEmpty()) {
                sanitizeName(trimmedName)
            } else {
                defaultName
            }

            var fileName = baseName + extension
            var candidate = File(baseDir, fileName)

            var i = 1
            while (candidate.exists()) {
                fileName = "$baseName$SEPARATOR_CHAR${i++}$extension"
                candidate = File(baseDir, fileName)
            }

            return fileName
        }

        private fun sanitizeName(documentName: String): String =
            buildString(documentName.length) {
                for (ch in documentName) {
                    append(if (ch in FILENAME_ALLOWED_CHARS) ch else SEPARATOR_CHAR)
                }
            }
    }
}

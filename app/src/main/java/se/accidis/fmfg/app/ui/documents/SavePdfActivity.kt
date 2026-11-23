package se.accidis.fmfg.app.ui.documents

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.export.PdfGenerator
import timber.log.Timber

/**
 * Translucent helper activity launched from the Android 14+ Sharesheet custom action
 * to save an exported PDF via the Storage Access Framework without requiring storage permissions.
 */
class SavePdfActivity : ComponentActivity() {

    private var sourceUri: Uri? = null

    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument(PdfGenerator.PDF_CONTENT_TYPE)
    ) { destinationUri ->
        val srcUri = sourceUri
        if (destinationUri == null || srcUri == null) {
            finish()
            return@registerForActivityResult
        }

        lifecycleScope.launch {
            val saved = withContext(Dispatchers.IO) {
                copyUriToDestination(srcUri, destinationUri)
            }
            Toast.makeText(
                applicationContext,
                if (saved) R.string.document_export_saved else R.string.document_export_error,
                if (saved) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = IntentCompat.getParcelableExtra(intent, EXTRA_SOURCE_URI, Uri::class.java)
        val filename = intent.getStringExtra(EXTRA_FILENAME)
            ?.takeIf { it.isNotBlank() }
            ?: (getString(R.string.document_export_default_name) + PdfGenerator.PDF_EXTENSION)

        if (uri == null) {
            Timber.e("SavePdfActivity started without source URI.")
            finish()
            return
        }

        sourceUri = uri

        if (savedInstanceState == null) {
            createDocumentLauncher.launch(filename)
        }
    }

    private fun copyUriToDestination(srcUri: Uri, destinationUri: Uri): Boolean = try {
        contentResolver.openInputStream(srcUri)?.use { input ->
            contentResolver.openOutputStream(destinationUri)?.use { output ->
                input.copyTo(output)
                output.flush()
            } ?: return false
        } ?: return false
        true
    } catch (ex: Exception) {
        Timber.e(ex, "Exception while saving exported PDF to destination URI.")
        false
    }

    companion object {
        private const val EXTRA_SOURCE_URI = "se.accidis.fmfg.app.extra.SOURCE_URI"
        private const val EXTRA_FILENAME = "se.accidis.fmfg.app.extra.FILENAME"

        fun createIntent(context: Context, sourceUri: Uri, filename: String): Intent =
            Intent(context, SavePdfActivity::class.java).apply {
                putExtra(EXTRA_SOURCE_URI, sourceUri)
                putExtra(EXTRA_FILENAME, filename)
            }
    }
}

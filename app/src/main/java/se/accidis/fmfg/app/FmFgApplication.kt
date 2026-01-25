package se.accidis.fmfg.app

import android.app.Application
import se.accidis.fmfg.app.export.ExportFile
import se.accidis.fmfg.app.services.DocumentsRepository
import se.accidis.fmfg.app.old.materials.ValueHelper
import java.util.Locale

/**
 * Application class.
 */
class FmFgApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ValueHelper.initializeLocale(Locale.forLanguageTag("sv-SE"))
        DocumentsRepository.getInstance(applicationContext).ensureCurrentDocumentLoaded()
        ExportFile.cleanUpOldExports(applicationContext)
    }
}

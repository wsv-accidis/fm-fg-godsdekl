package se.accidis.fmfg.app

import android.app.Application
import se.accidis.fmfg.app.export.ExportFile
import se.accidis.fmfg.app.old.materials.ValueHelper
import timber.log.Timber
import java.util.Locale

/**
 * Application class.
 */
class FmFgApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        ValueHelper.initializeLocale(Locale.forLanguageTag("sv-SE"))
        ExportFile.cleanUpOldExports(applicationContext)
    }
}

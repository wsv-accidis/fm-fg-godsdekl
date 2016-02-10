package se.accidis.fmfg.app;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.Locale;

import se.accidis.fmfg.app.export.ExportFile;
import se.accidis.fmfg.app.services.DocumentsRepository;
import se.accidis.fmfg.app.ui.materials.ValueHelper;

/**
 * Application class.
 */
public final class FmFgApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		ValueHelper.initializeLocale(new Locale("sv", "SE"));
		JodaTimeAndroid.init(this);
		DocumentsRepository.getInstance(getApplicationContext()).ensureCurrentDocumentLoaded();
		ExportFile.cleanUpOldExports(getApplicationContext());
	}
}

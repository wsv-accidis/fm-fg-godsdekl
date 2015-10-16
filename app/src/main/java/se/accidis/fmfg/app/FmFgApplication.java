package se.accidis.fmfg.app;

import android.app.Application;

import java.util.Locale;

import se.accidis.fmfg.app.services.DocumentsRepository;

/**
 * Application class.
 */
public final class FmFgApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Locale.setDefault(new Locale("sv", "SE"));
        DocumentsRepository.getInstance(getApplicationContext()).ensureInitialized();
    }
}

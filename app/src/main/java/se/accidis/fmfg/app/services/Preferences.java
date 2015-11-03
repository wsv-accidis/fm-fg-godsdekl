package se.accidis.fmfg.app.services;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Wrapper for Android shared preferences. Used to store application data between launches.
 */
public final class Preferences {
    private static final String PREFERENCES_FILE = "ThePreferences";
    private static final String TAG = Preferences.class.getSimpleName();
    private final SharedPreferences mPrefs;

    public Preferences(Context context) {
        mPrefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    public Set<String> getFavoriteMaterials() {
        Set<String> set = mPrefs.getStringSet(Keys.FAVORITE_MATERIALS, null);
        return (set != null ? new HashSet<>(set) : new HashSet<String>());
    }

    public void setFavoriteMaterials(Set<String> materialsKeys) {
        mPrefs.edit().putStringSet(Keys.FAVORITE_MATERIALS, materialsKeys).apply();
    }

    public void setShowFbetInDocument(boolean value) {
        mPrefs.edit().putBoolean(Keys.SHOW_FBET_IN_DOCUMENT, value).apply();
    }

    public boolean shouldShowFbetInDocument() {
        return mPrefs.getBoolean(Keys.SHOW_FBET_IN_DOCUMENT, false);
    }

    private static class Keys {
        public static final String FAVORITE_MATERIALS = "FavoriteMaterials";
        public static final String SHOW_FBET_IN_DOCUMENT = "ShowFbetInDocument";

        private Keys() {
        }
    }
}

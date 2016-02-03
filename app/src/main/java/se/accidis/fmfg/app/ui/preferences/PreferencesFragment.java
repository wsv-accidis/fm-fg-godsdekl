package se.accidis.fmfg.app.ui.preferences;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.services.Preferences;
import se.accidis.fmfg.app.ui.MainActivity;

/**
 * Allows the user to edit app preferences.
 */
public final class PreferencesFragment extends PreferenceFragmentCompat implements MainActivity.HasNavigationItem, MainActivity.HasTitle {
	@Override
	public int getItemId() {
		return R.id.nav_preferences;
	}

	@Override
	public String getTitle(Context context) {
		return context.getString(R.string.preferences_nav_title);
	}

	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		PreferenceManager preferenceManager = getPreferenceManager();
		preferenceManager.setSharedPreferencesName(Preferences.PREFERENCES_FILE);
		preferenceManager.setSharedPreferencesMode(Context.MODE_PRIVATE);
		addPreferencesFromResource(R.xml.preferences);
	}
}

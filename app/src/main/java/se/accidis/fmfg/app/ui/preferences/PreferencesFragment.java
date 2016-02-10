package se.accidis.fmfg.app.ui.preferences;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.services.Preferences;
import se.accidis.fmfg.app.ui.MainActivity;

/**
 * Allows the user to edit app preferences.
 */
public final class PreferencesFragment extends PreferenceFragmentCompat implements MainActivity.HasNavigationItem, MainActivity.HasTitle, PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback {
	private static final String DIALOG_FRAGMENT_TAG = "android.support.v7.preference.PreferenceFragment.DIALOG";

	@Override
	public Fragment getCallbackFragment() {
		return this;
	}

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

	@Override
	public boolean onPreferenceDisplayDialog(PreferenceFragmentCompat preferenceFragment, Preference preference) {
		if (preference instanceof AddressDialogPreference) {
			FragmentManager fragmentManager = preferenceFragment.getFragmentManager();
			if (null == fragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG)) {
				AddressDialogPreferenceFragment dialogFragment = AddressDialogPreferenceFragment.newInstance(preference);
				dialogFragment.setTargetFragment(preferenceFragment, 0);
				dialogFragment.show(fragmentManager, DIALOG_FRAGMENT_TAG);
			}
			return true;
		}

		return false;
	}
}

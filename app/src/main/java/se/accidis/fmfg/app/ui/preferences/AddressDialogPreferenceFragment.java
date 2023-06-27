package se.accidis.fmfg.app.ui.preferences;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;

import se.accidis.fmfg.app.R;

/**
 * Preference fragment for the address dialog.
 */
public final class AddressDialogPreferenceFragment extends PreferenceDialogFragmentCompat {
	private EditText mAddressText;

	public static AddressDialogPreferenceFragment newInstance(Preference preference) {
		AddressDialogPreferenceFragment fragment = new AddressDialogPreferenceFragment();
		Bundle bundle = new Bundle(1);
		bundle.putString(ARG_KEY, preference.getKey());
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			String value = mAddressText.getText().toString();
			if (getCustomPreference().callChangeListener(value)) {
				getCustomPreference().setText(value);
			}
		}
	}

	@Override
	protected boolean needInputMethod() {
		return true;
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		mAddressText = (EditText) view.findViewById(R.id.address_text);
		mAddressText.setText(getCustomPreference().getText());

		TextView headingText = (TextView) view.findViewById(R.id.address_heading);
		headingText.setText(getCustomPreference().getHeadingResourceId());
	}

	private AddressDialogPreference getCustomPreference() {
		return (AddressDialogPreference) getPreference();
	}
}

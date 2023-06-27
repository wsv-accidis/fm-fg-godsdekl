package se.accidis.fmfg.app.ui.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.EditTextPreference;

import se.accidis.fmfg.app.R;

/**
 * Shows the address dialog as a preference item.
 */
public final class AddressDialogPreference extends EditTextPreference {
	private int mHeadingResId;

	public AddressDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initializeDialog(context, attrs);
	}

	public AddressDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initializeDialog(context, attrs);
	}

	public AddressDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initializeDialog(context, attrs);
	}

	public AddressDialogPreference(Context context) {
		super(context);
		initializeDialog(context, null);
	}

	public int getHeadingResourceId() {
		return mHeadingResId;
	}

	private void initializeDialog(Context context, AttributeSet attrs) {
		setDialogTitle("");
		setDialogIcon(null);

		setDialogLayoutResource(R.layout.dialog_address);
		setPositiveButtonText(R.string.generic_save);
		setNegativeButtonText(R.string.generic_cancel);

		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AddressDialogPreference);
		try {
			mHeadingResId = ta.getResourceId(R.styleable.AddressDialogPreference_heading, 0);
		} finally {
			ta.recycle();
		}
	}
}

package se.accidis.fmfg.app.ui.documents;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.utils.AndroidUtils;

/**
 * Dialog fragment for entering/editing an address.
 */
public final class AddressDialogFragment extends DialogFragment {
	public static final String ARG_CURRENT_ADDRESS = "address";
	public static final String ARG_POSITION = "position";
	private EditText mAddressText;
	private AddressDialogListener mListener;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Bundle args = getArguments();
		final int position = args.getInt(ARG_POSITION);
		final String address = args.getString(ARG_CURRENT_ADDRESS);

		final LayoutInflater inflater = getActivity().getLayoutInflater();
		final View view = inflater.inflate(R.layout.dialog_address, null);

		final TextView headingText = (TextView) view.findViewById(R.id.address_heading);
		headingText.setText(getHeadingByPosition(position));

		mAddressText = (EditText) view.findViewById(R.id.address_text);
		mAddressText.setText(address);

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(view)
			.setPositiveButton(R.string.generic_save, new SaveClickedListener())
			.setNegativeButton(R.string.generic_cancel, null);

		final Dialog dialog = builder.create();
		AndroidUtils.showSoftKeyboardForDialog(dialog);
		return dialog;
	}

	public void setDialogListener(AddressDialogListener listener) {
		mListener = listener;
	}

	private int getHeadingByPosition(int position) {
		switch (position) {
			case DocumentAdapter.SENDER_POSITION:
				return R.string.address_sender_heading;
			case DocumentAdapter.RECIPIENT_POSITION:
				return R.string.address_recipient_heading;
			case DocumentAdapter.AUTHOR_POSITION:
				return R.string.address_author_heading;
			default:
				throw new IllegalArgumentException();
		}
	}

	public interface AddressDialogListener {
		void onDismiss(String address);
	}

	private final class SaveClickedListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			final String address = mAddressText.getText().toString();
			if (null != mListener) {
				mListener.onDismiss(address);
			}
		}
	}
}

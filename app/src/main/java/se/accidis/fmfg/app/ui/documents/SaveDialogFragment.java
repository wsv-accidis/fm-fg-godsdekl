package se.accidis.fmfg.app.ui.documents;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.utils.AndroidUtils;

/**
 * Dialog fragment for saving a document.
 */
public final class SaveDialogFragment extends DialogFragment {
	public static final String ARG_IS_SAVED = "isSaved";
	public static final String ARG_NAME = "name";
	private SaveDialogListener mListener;
	private EditText mNameText;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Bundle args = getArguments();
		final boolean isSaved = args.getBoolean(ARG_IS_SAVED);
		final String name = args.getString(ARG_NAME);

		final LayoutInflater inflater = getActivity().getLayoutInflater();
		final View view = inflater.inflate(R.layout.dialog_save, null);

		mNameText = (EditText) view.findViewById(R.id.document_name);
		mNameText.setText(name);

		final View alreadySavedView = view.findViewById(R.id.document_already_saved);
		alreadySavedView.setVisibility(isSaved ? View.VISIBLE : View.GONE);

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(view)
			.setPositiveButton(R.string.generic_save, new SaveClickedListener(false))
			.setNegativeButton(R.string.generic_cancel, null);

		if (isSaved) {
			builder.setNeutralButton(R.string.document_save_as_new, new SaveClickedListener(true));
		}

		final Dialog dialog = builder.create();
		AndroidUtils.showSoftKeyboardForDialog(dialog);
		return dialog;
	}

	public void setDialogListener(SaveDialogListener listener) {
		mListener = listener;
	}

	public interface SaveDialogListener {
		void onDismiss(String name, boolean asCopy);
	}

	private final class SaveClickedListener implements DialogInterface.OnClickListener {
		private final boolean mAsCopy;

		public SaveClickedListener(boolean asCopy) {
			mAsCopy = asCopy;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			final String name = mNameText.getText().toString();
			if (null != mListener) {
				mListener.onDismiss(name, mAsCopy);
			}
		}
	}
}

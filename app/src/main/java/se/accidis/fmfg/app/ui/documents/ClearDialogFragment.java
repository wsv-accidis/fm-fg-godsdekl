package se.accidis.fmfg.app.ui.documents;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import se.accidis.fmfg.app.R;

/**
 * Dialog fragment for prompting the user on whether to clear data.
 */
public final class ClearDialogFragment extends DialogFragment {
    private ClearDialogListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.document_clear_heading)
                .setPositiveButton(R.string.document_clear_everything, new ClearClickedListener(false))
                .setNeutralButton(R.string.document_clear_keep_addresses, new ClearClickedListener(true))
                .setNegativeButton(R.string.generic_cancel, null);

        return builder.create();
    }

    public void setDialogListener(ClearDialogListener listener) {
        mListener = listener;
    }

    public interface ClearDialogListener {
        void onDismiss(boolean keepAddresses);
    }

    private final class ClearClickedListener implements DialogInterface.OnClickListener {
        private final boolean mKeepAddresses;

        public ClearClickedListener(boolean keepAddresses) {
            mKeepAddresses = keepAddresses;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (null != mListener) {
                mListener.onDismiss(mKeepAddresses);
            }
        }
    }
}

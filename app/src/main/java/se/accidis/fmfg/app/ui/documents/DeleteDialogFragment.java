package se.accidis.fmfg.app.ui.documents;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import se.accidis.fmfg.app.R;

/**
 * Dialog fragment for prompting the user on whether to delete a document.
 */
public final class DeleteDialogFragment extends DialogFragment {
    private DeleteDialogListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.document_delete_heading)
                .setPositiveButton(R.string.document_delete, new DeleteClickedListener())
                .setNegativeButton(R.string.generic_cancel, null);

        return builder.create();
    }

    public void setDialogListener(DeleteDialogListener listener) {
        mListener = listener;
    }

    public interface DeleteDialogListener {
        void onDismiss();
    }

    private final class DeleteClickedListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (null != mListener) {
                mListener.onDismiss();
            }
        }
    }
}

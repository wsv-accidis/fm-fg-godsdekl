package se.accidis.fmfg.app.old.documents;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import se.accidis.fmfg.app.R;

/**
 * Dialog fragment for prompting the user on whether to edit data.
 */
public final class EditUnsavedDialogFragment extends DialogFragment {
    private EditUnsavedDialogListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.document_edit_unsaved_heading)
                .setPositiveButton(R.string.document_edit_unsaved_yes, new EditClickedListener())
                .setNegativeButton(R.string.document_edit_unsaved_no, null);

        return builder.create();
    }

    public void setDialogListener(EditUnsavedDialogListener listener) {
        mListener = listener;
    }

    public interface EditUnsavedDialogListener {
        void onDismiss();
    }

    private final class EditClickedListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (null != mListener) {
                mListener.onDismiss();
            }
        }
    }
}

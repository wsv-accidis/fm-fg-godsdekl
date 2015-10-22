package se.accidis.fmfg.app.ui.documents;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import se.accidis.fmfg.app.R;

/**
 * Fragment for entering/editing an address.
 */
public final class AddressDialogFragment extends DialogFragment {
    public static final String ARG_IS_SENDER = "isSender";
    public static final String ARG_CURRENT_ADDRESS = "address";
    private OnDismissListener mOnDismissListener;
    private EditText mAddressText;
    private boolean mIsSaving;

    public interface OnDismissListener {
        void onDismiss(DialogInterface dialog, String address);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments();
        boolean isSender = args.getBoolean(ARG_IS_SENDER);
        String address = args.getString(ARG_CURRENT_ADDRESS);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_address, null);

        TextView headingText = (TextView) view.findViewById(R.id.address_heading);
        headingText.setText(isSender ? R.string.address_sender_heading : R.string.address_recipient_heading);

        mAddressText = (EditText) view.findViewById(R.id.address_text);
        mAddressText.setText(address);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(R.string.generic_save, new SaveClickedListener())
                .setNegativeButton(R.string.generic_cancel, null);

        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (null != mOnDismissListener) {
            String address = (mIsSaving ? mAddressText.getText().toString() : null);
            mOnDismissListener.onDismiss(dialog, address);
        }
    }

    private final class SaveClickedListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mIsSaving = true;
        }
    }
}

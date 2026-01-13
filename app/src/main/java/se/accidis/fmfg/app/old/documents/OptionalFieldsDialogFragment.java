package se.accidis.fmfg.app.old.documents;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Document;

/**
 * Fragment for editing optional fields on a document.
 */
public final class OptionalFieldsDialogFragment extends DialogFragment {
	public static final String ARG_PROTECTED_TRANSPORT = "protectedTransport";
	public static final String ARG_READONLY = "readOnly";
	public static final String ARG_VEHICLE_REG = "vehicleReg";
	public static final String ARG_VEHICLE_TYPE = "vehicleType";
	public static final int PROTECTED_TRANSPORT_NO = 2;
	public static final int PROTECTED_TRANSPORT_UNKNOWN = 0;
	public static final int PROTECTED_TRANSPORT_YES = 1;
	private Spinner mProtectedTpSpinner;
	private EditText mVehicleRegField;
	private AutoCompleteTextView mVehicleTypeField;
	private DocumentOptFieldsDialogListener mListener;
	private boolean mIsReadOnly;
	private Bundle mOutArgs;

	public static OptionalFieldsDialogFragment createInstance(Document document, boolean readOnly) {
		Bundle args = new Bundle();
		args.putInt(ARG_PROTECTED_TRANSPORT, !document.isProtectedTransportSpecified() ? PROTECTED_TRANSPORT_UNKNOWN : (document.isProtectedTransport() ? PROTECTED_TRANSPORT_YES : PROTECTED_TRANSPORT_NO));
		args.putBoolean(ARG_READONLY, readOnly);
		args.putString(ARG_VEHICLE_REG, document.getVehicleReg());
		args.putString(ARG_VEHICLE_TYPE, document.getVehicleType());

		OptionalFieldsDialogFragment fragment = new OptionalFieldsDialogFragment();
		fragment.setArguments(args);
		return fragment;
	}

	public void setDialogListener(DocumentOptFieldsDialogListener listener) {
		mListener = listener;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Bundle args = getArguments();

		String vehicleType = args.getString(ARG_VEHICLE_TYPE, "");
		String vehicleReg = args.getString(ARG_VEHICLE_REG, "");
		int protectedTransport = args.getInt(ARG_PROTECTED_TRANSPORT, PROTECTED_TRANSPORT_UNKNOWN);
		mIsReadOnly = args.getBoolean(ARG_READONLY, false);

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_optional_fields, null);

		mVehicleTypeField = (AutoCompleteTextView) view.findViewById(R.id.document_vehicle_type);
		if (!mIsReadOnly) {
			ArrayAdapter<String> vehicleTypeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.document_vehicle_types));
			mVehicleTypeField.setThreshold(1);
			mVehicleTypeField.setAdapter(vehicleTypeAdapter);
		}
		mVehicleTypeField.setText(vehicleType);
		mVehicleTypeField.setEnabled(!mIsReadOnly);

		mVehicleRegField = (EditText) view.findViewById(R.id.document_vehicle_reg);
		mVehicleRegField.setText(vehicleReg);
		mVehicleRegField.setEnabled(!mIsReadOnly);

		mProtectedTpSpinner = (Spinner) view.findViewById(R.id.document_protected_transport);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.generic_yes_no_unknown, R.layout.spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mProtectedTpSpinner.setAdapter(adapter);
		mProtectedTpSpinner.setSelection(protectedTransport);
		mProtectedTpSpinner.setEnabled(!mIsReadOnly);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(view);

		if (!mIsReadOnly) {
			builder.setPositiveButton(R.string.generic_save, new SaveClickedListener());
			builder.setNegativeButton(R.string.generic_cancel, null);
		} else {
			builder.setPositiveButton(R.string.generic_close, null);
		}

		return builder.create();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);

		if (null != mListener) {
			mListener.onDismiss(mIsReadOnly ? null : mOutArgs);
		}
	}

	public interface DocumentOptFieldsDialogListener {
		void onDismiss(Bundle outArgs);
	}

	private final class SaveClickedListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			mOutArgs = new Bundle();
			mOutArgs.putInt(ARG_PROTECTED_TRANSPORT, mProtectedTpSpinner.getSelectedItemPosition());
			mOutArgs.putString(ARG_VEHICLE_REG, mVehicleRegField.getText().toString().trim());
			mOutArgs.putString(ARG_VEHICLE_TYPE, mVehicleTypeField.getText().toString().trim());
		}
	}
}

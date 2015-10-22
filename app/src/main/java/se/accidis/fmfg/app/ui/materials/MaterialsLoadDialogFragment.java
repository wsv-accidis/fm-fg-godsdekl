package se.accidis.fmfg.app.ui.materials;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.math.BigDecimal;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Document;
import se.accidis.fmfg.app.model.DocumentRow;
import se.accidis.fmfg.app.model.Material;
import se.accidis.fmfg.app.services.DocumentsRepository;

/**
 * Fragment for creating/editing a document row (loading materials).
 */
public final class MaterialsLoadDialogFragment extends DialogFragment {
    private BigDecimal mAmount;
    private BigDecimal mDocumentTotalValue;
    private Material mMaterial;
    private BigDecimal mMultiplier;
    private TextView mNEMView;
    private EditText mNumberPkgsField;
    private DialogInterface.OnDismissListener mOnDismissListener;
    private DocumentsRepository mRepository;
    private TextView mTotalValueView;
    private EditText mTypePkgsField;
    private TextView mValueView;
    private BigDecimal mWeightVolume;
    private boolean mWeightVolumeIsVolume;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments();
        mMaterial = Material.fromBundle(args);
        int multiplier = ValueHelper.getMultiplierByTpKat(mMaterial.getTpKat());
        mMultiplier = new BigDecimal(multiplier);

        mRepository = DocumentsRepository.getInstance(getContext());
        Document document = mRepository.getCurrentDocument();
        DocumentRow row = document.getRowByMaterial(mMaterial);
        mDocumentTotalValue = document.getCalculatedTotalValue();
        if (null != row) {
            mDocumentTotalValue = mDocumentTotalValue.subtract(row.getCalculatedValue());
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_material_load, null);

        TextView multiplierView = (TextView) view.findViewById(R.id.material_load_multiplier);
        multiplierView.setText(String.valueOf(multiplier));

        Spinner weightVolumeUnitSpinner = (Spinner) view.findViewById(R.id.material_load_weight_volume_unit);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.unit_weight_volume, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weightVolumeUnitSpinner.setAdapter(adapter);
        weightVolumeUnitSpinner.setOnItemSelectedListener(new WeightOrVolumeSelectedListener());
        if (null != row) {
            mWeightVolumeIsVolume = row.isVolume();
            weightVolumeUnitSpinner.setSelection(row.isVolume() ? 1 : 0);
        }

        mValueView = (TextView) view.findViewById(R.id.material_load_value);
        mTotalValueView = (TextView) view.findViewById(R.id.material_load_total_value);

        if (!mMaterial.hasNEM()) {
            TextView amountHeading = (TextView) view.findViewById(R.id.material_load_amount_heading);
            LinearLayout amountLayout = (LinearLayout) view.findViewById(R.id.material_load_amount_layout);
            TextView nemHeading = (TextView) view.findViewById(R.id.material_load_nem_heading);
            TextView nemView = (TextView) view.findViewById(R.id.material_load_nem);
            amountHeading.setVisibility(View.GONE);
            amountLayout.setVisibility(View.GONE);
            nemHeading.setVisibility(View.GONE);
            nemView.setVisibility(View.GONE);
        } else {
            mNEMView = (TextView) view.findViewById(R.id.material_load_nem);
            EditText amountField = (EditText) view.findViewById(R.id.material_load_amount);
            amountField.addTextChangedListener(new AmountChangedListener());
            if (null != row) {
                amountField.setText(String.valueOf(row.getAmount()));
            }
        }

        mNumberPkgsField = (EditText) view.findViewById(R.id.material_load_number_pkgs);
        mTypePkgsField = (EditText) view.findViewById(R.id.material_load_type_pkgs);
        EditText weightVolumeField = (EditText) view.findViewById(R.id.material_load_weight_volume);
        weightVolumeField.addTextChangedListener(new WeightVolumeChangedListener());
        if (null != row) {
            mNumberPkgsField.setText(String.valueOf(row.getNumberOfPackages()));
            mTypePkgsField.setText(row.getTypeOfPackages());
            weightVolumeField.setText(row.getWeightVolume().toString());
        }

        calculate();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(R.string.generic_save, new SaveClickedListener())
                .setNegativeButton(R.string.generic_cancel, null);

        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (null != mOnDismissListener) {
            mOnDismissListener.onDismiss(dialog);
        }
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    private void calculate() {
        if (null == mAmount) {
            mAmount = BigDecimal.ZERO;
        }
        if (null == mWeightVolume) {
            mWeightVolume = BigDecimal.ZERO;
        }

        BigDecimal value;
        if (mMaterial.hasNEM()) {
            value = mMaterial.getNEMkg().multiply(mAmount);
            mNEMView.setText(String.format(getString(R.string.unit_kg_format), ValueHelper.formatValue(value)));
        } else {
            value = mWeightVolume;
        }

        value = value.multiply(mMultiplier);
        mValueView.setText(String.format(getString(R.string.unit_points_format), ValueHelper.formatValue(value)));
        mTotalValueView.setText(String.format(getString(R.string.unit_points_format), ValueHelper.formatValue(value.add(mDocumentTotalValue))));
    }

    private final class AmountChangedListener implements TextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mAmount = ValueHelper.parseValue(s.toString());
            calculate();
        }
    }

    private final class SaveClickedListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            DocumentRow row = new DocumentRow(mMaterial);

            int numberPkgs = parseIntOrZero(mNumberPkgsField.getText().toString());
            row.setNumberOfPackages(numberPkgs);
            String typePkgs = mTypePkgsField.getText().toString();
            row.setTypeOfPackages(typePkgs);
            row.setAmount(mAmount);
            row.setWeightVolume(mWeightVolume);
            row.setIsVolume(mWeightVolumeIsVolume);

            mRepository.getCurrentDocument().addOrUpdateRow(row);
            mRepository.commit();
        }

        private int parseIntOrZero(String str) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
    }

    private final class WeightOrVolumeSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mWeightVolumeIsVolume = (1 == position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private final class WeightVolumeChangedListener implements TextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mWeightVolume = ValueHelper.parseValue(s.toString());
            calculate();
        }
    }
}

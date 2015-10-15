package se.accidis.fmfg.app.ui.materials;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.math.BigDecimal;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Material;

/**
 * Fragment for creating/editing a document row (loading materials).
 */
public final class MaterialsLoadDialogFragment extends DialogFragment {
    private BigDecimal mAmount;
    private Material mMaterial;
    private BigDecimal mMultiplier;
    private TextView mNEMView;
    private TextView mTotalValueView;
    private TextView mValueView;
    private BigDecimal mWeightVolume;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments();
        mMaterial = Material.fromBundle(args);
        int multiplier = ValueHelper.getMultiplierByTpKat(mMaterial.getTpKat());
        mMultiplier = new BigDecimal(multiplier);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_material_load, null);

        TextView multiplierView = (TextView) view.findViewById(R.id.material_load_multiplier);
        multiplierView.setText(String.valueOf(multiplier));

        Spinner weightVolumeUnitSpinner = (Spinner) view.findViewById(R.id.material_load_weight_volume_unit);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.unit_weight_volume, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weightVolumeUnitSpinner.setAdapter(adapter);

        if (!shouldUseNEM()) {
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
        }

        mValueView = (TextView) view.findViewById(R.id.material_load_value);
        mTotalValueView = (TextView) view.findViewById(R.id.material_load_total_value);
        calculate();

        EditText weightVolumeField = (EditText) view.findViewById(R.id.material_load_weight_volume);
        weightVolumeField.addTextChangedListener(new WeightVolumeChangedListener());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(R.string.material_load_save, null)
                .setNegativeButton(R.string.material_load_cancel, null);

        return builder.create();
    }

    private void calculate() {
        if (null == mAmount) {
            mAmount = BigDecimal.ZERO;
        }
        if (null == mWeightVolume) {
            mWeightVolume = BigDecimal.ZERO;
        }

        BigDecimal value;
        if (shouldUseNEM()) {
            value = mMaterial.getNEMkg().multiply(mAmount);
            mNEMView.setText(String.format(getString(R.string.unit_kg_format), ValueHelper.formatValue(value)));
        } else {
            value = mWeightVolume;
        }

        value = value.multiply(mMultiplier);
        mValueView.setText(String.format(getString(R.string.unit_points_format), ValueHelper.formatValue(value)));
        // TODO
        mTotalValueView.setText(String.format(getString(R.string.unit_points_format), ValueHelper.formatValue(value)));
    }

    private boolean shouldUseNEM() {
        return (0 != mMaterial.getNEMmg());
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

package se.accidis.fmfg.app.ui.materials;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Document;
import se.accidis.fmfg.app.model.DocumentRow;
import se.accidis.fmfg.app.model.Material;
import se.accidis.fmfg.app.services.DocumentsRepository;
import se.accidis.fmfg.app.utils.AndroidUtils;

/**
 * Fragment for creating/editing a document row (loading materials).
 */
public final class MaterialsLoadDialogFragment extends DialogFragment {
	private static final BigDecimal MG_PER_KG = new BigDecimal(1000000);

	private BigDecimal mAmount;
	private EditText mCustomNEMField;
	private BigDecimal mCustomNEMkg;
	private BigDecimal mDocumentTotalValue;
	private Spinner mFmSpinner;
	private CheckBox mMiljoCheckbox;
	private MaterialsLoadDialogListener mListener;
	private Material mMaterial;
	private BigDecimal mMultiplier;
	private TextView mNEMView;
	private int mSelectedFmIndex = -1;
	private EditText mNumberPkgsField;
	private DocumentsRepository mRepository;
	private TextView mTotalValueView;
	private AutoCompleteTextView mTypePkgsField;
	private TextView mValueView;
	private BigDecimal mWeightVolume;
	private boolean mWeightVolumeIsVolume;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Bundle args = getArguments();
		mMaterial = Material.fromBundle(args);
		mSelectedFmIndex = mMaterial.getSelectedFmIndex();
		AndroidUtils.assertIsTrue(!mMaterial.isCustom(), "Materials load dialog loaded with custom material.");

		mRepository = DocumentsRepository.getInstance(getContext());
		Document document = mRepository.getCurrentDocument();
		DocumentRow row = document.getRowByMaterial(mMaterial);

		mDocumentTotalValue = document.getCalculatedTotalValue();
		boolean hasExistingRow = false;
		if (null != row) {
			hasExistingRow = true;
			mMaterial = row.getMaterial();
			mSelectedFmIndex = mMaterial.getSelectedFmIndex();
			mDocumentTotalValue = mDocumentTotalValue.subtract(row.getCalculatedValue());
		}

		int multiplier = ValueHelper.getMultiplierByTpKat(mMaterial.getTpKat());
		mMultiplier = new BigDecimal(multiplier);

		@SuppressLint("InflateParams")
		View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_material_load, null);
		initializeFmSpinner(view);
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
		mNEMView = (TextView) view.findViewById(R.id.material_load_nem);

		EditText amountField = (EditText) view.findViewById(R.id.material_load_amount);
		amountField.addTextChangedListener(new AmountChangedListener());
		if (null != row) {
			amountField.setText(String.valueOf(row.getAmount()));
			mAmount = row.getAmount();
		}

		mCustomNEMField = (EditText) view.findViewById(R.id.material_load_custom_nem);
		View customNEMHeading = view.findViewById(R.id.material_load_custom_nem_heading);
		View customNEMLayout = view.findViewById(R.id.material_load_custom_nem_layout);
		if (mMaterial.hasPresetNEMValue()) {
			customNEMHeading.setVisibility(View.GONE);
			customNEMLayout.setVisibility(View.GONE);
		} else {
			customNEMHeading.setVisibility(View.VISIBLE);
			customNEMLayout.setVisibility(View.VISIBLE);
			mCustomNEMField.addTextChangedListener(new CustomNEMChangedListener());
			if (null != row && null != row.getCustomNEMmg()) {
				mCustomNEMkg = row.getCustomNEMmg().divide(MG_PER_KG, 6, BigDecimal.ROUND_FLOOR);
				mCustomNEMField.setText(mCustomNEMkg.stripTrailingZeros().toPlainString());
			}
		}

		mNumberPkgsField = (EditText) view.findViewById(R.id.material_load_number_pkgs);
		mTypePkgsField = (AutoCompleteTextView) view.findViewById(R.id.material_load_type_pkgs);
		ArrayAdapter<String> typePkgsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.material_pkg_types));
		mTypePkgsField.setThreshold(1);
		mTypePkgsField.setAdapter(typePkgsAdapter);

		EditText weightVolumeField = (EditText) view.findViewById(R.id.material_load_weight_volume);
		weightVolumeField.addTextChangedListener(new WeightVolumeChangedListener());
		if (null != row) {
			mNumberPkgsField.setText(String.valueOf(row.getNumberOfPackages()));
			mTypePkgsField.setText(row.getTypeOfPackages());
			weightVolumeField.setText(row.getWeightVolume().toString());
			if (null == mAmount) {
				mAmount = row.getAmount();
			}
		}

		mMiljoCheckbox = (CheckBox) view.findViewById(R.id.material_load_miljo);
		initializeMiljoCheckbox(row);

		calculate();

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(view)
			.setPositiveButton(R.string.generic_save, new SaveClickedListener())
			.setNegativeButton(R.string.generic_cancel, null);

		if (hasExistingRow) {
			// The button to remove the material is only visible if there is prior data
			builder.setNeutralButton(R.string.material_remove, new RemoveClickedListener());
		}

		return builder.create();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (null != mListener) {
			mListener.onDismiss();
		}
	}

	public void setDialogListener(MaterialsLoadDialogListener listener) {
		mListener = listener;
	}

	private void initializeFmSpinner(View view) {
		mFmSpinner = (Spinner) view.findViewById(R.id.material_load_fm_spinner);
		View fmHeading = view.findViewById(R.id.material_load_fm_heading);
		List<Material.FM> fmEntries = mMaterial.getFM();

		if (fmEntries.size() > 1) {
			ArrayAdapter<Material.FM> adapter = new ArrayAdapter<Material.FM>(getContext(), R.layout.spinner_two_line_item, fmEntries) {
				@NonNull
				@Override
				public View getView(int position, View convertView, @NonNull ViewGroup parent) {
					return createView(position, convertView, parent, R.layout.spinner_two_line_item);
				}

				@Override
				public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
					return createView(position, convertView, parent, R.layout.spinner_two_line_item);
				}

				private View createView(int position, View convertView, ViewGroup parent, int layoutResource) {
					View row = convertView;
					if (null == row) {
						LayoutInflater inflater = LayoutInflater.from(getContext());
						row = inflater.inflate(layoutResource, parent, false);
					}
					Material.FM entry = getItem(position);
					if (null != entry) {
						TextView line1 = (TextView) row.findViewById(R.id.fm_spinner_line1);
						TextView line2 = (TextView) row.findViewById(R.id.fm_spinner_line2);
						if (null != line1) {
							line1.setText(entry.getFbet() + " " + entry.getFben());
						}
						if (null != line2) {
							BigDecimal nemKg = new BigDecimal(entry.getNEMmgAsInt()).divide(MG_PER_KG, 6, BigDecimal.ROUND_FLOOR);
							line2.setText(getString(R.string.material_nem_kg_format, ValueHelper.formatValue(nemKg)));
						}
					}
					return row;
				}
			};
			mFmSpinner.setAdapter(adapter);
			int initialSelection = (mSelectedFmIndex >= 0 && mSelectedFmIndex < fmEntries.size()) ? mSelectedFmIndex : 0;
			mSelectedFmIndex = initialSelection;
			mMaterial = mMaterial.withSelectedFmIndex(initialSelection);
			mFmSpinner.setSelection(initialSelection);
			mFmSpinner.setOnItemSelectedListener(new FmSelectedListener());
			fmHeading.setVisibility(View.VISIBLE);
			mFmSpinner.setVisibility(View.VISIBLE);
		} else {
			if (1 == fmEntries.size()) {
				mSelectedFmIndex = 0;
				mMaterial = mMaterial.withSelectedFmIndex(mSelectedFmIndex);
			}
			if (null != fmHeading) {
				fmHeading.setVisibility(View.GONE);
			}
			if (null != mFmSpinner) {
				mFmSpinner.setVisibility(View.GONE);
			}
		}
	}

	private void initializeMiljoCheckbox(DocumentRow row) {
		if (null == mMiljoCheckbox) {
			return;
		}
		if (mMaterial.hasMiljoValue()) {
			mMiljoCheckbox.setVisibility(View.GONE);
			return;
		}
		mMiljoCheckbox.setVisibility(View.VISIBLE);
		boolean isChecked = (null != row && row.hasMiljoOverride());
		mMiljoCheckbox.setChecked(isChecked);
	}

	private void calculate() {
		if (null == mAmount) {
			mAmount = BigDecimal.ZERO;
		}
		if (null == mWeightVolume) {
			mWeightVolume = BigDecimal.ZERO;
		}

		BigDecimal value;
		BigDecimal nemPerAmount = getActiveNEMPerAmountKg();
		if (null != nemPerAmount) {
			value = nemPerAmount.multiply(mAmount);
			mNEMView.setText(String.format(getString(R.string.unit_kg_format), ValueHelper.formatValue(value)));
		} else {
			value = mWeightVolume;
			if (null != mNEMView) {
				mNEMView.setText(getString(R.string.material_no_data));
			}
		}

		value = value.multiply(mMultiplier);
		mValueView.setText(String.format(getString(R.string.unit_points_format), ValueHelper.formatValue(value)));
		mTotalValueView.setText(String.format(getString(R.string.unit_points_format), ValueHelper.formatValue(value.add(mDocumentTotalValue))));
	}

	private BigDecimal getActiveNEMPerAmountKg() {
		if (mMaterial.hasNEM()) {
			return mMaterial.getNEMkg();
		} else if (null != mCustomNEMkg) {
			return mCustomNEMkg;
		}
		return null;
	}

	public interface MaterialsLoadDialogListener {
		void onDismiss();
	}

	private final class FmSelectedListener implements AdapterView.OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			if (position != mSelectedFmIndex) {
				mSelectedFmIndex = position;
				mMaterial = mMaterial.withSelectedFmIndex(position);
				calculate();
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
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

	private final class RemoveClickedListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			Document document = mRepository.getCurrentDocument();
			document.removeRowByMaterial(mMaterial);
			document.setHasUnsavedChanges(true);
			mRepository.commitCurrentDocument();
		}
	}

	private final class SaveClickedListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			DocumentRow row = new DocumentRow(mMaterial);

			int numberPkgs = parseIntOrZero(mNumberPkgsField.getText().toString());
			row.setNumberOfPackages(numberPkgs);
			String typePkgs = mTypePkgsField.getText().toString().trim();
			row.setTypeOfPackages(typePkgs);
			row.setAmount(mAmount);
			row.setCustomNEMmg(convertKgToMg(mCustomNEMkg));
			row.setWeightVolume(mWeightVolume);
			row.setIsVolume(mWeightVolumeIsVolume);
			if (null != mMiljoCheckbox && mMiljoCheckbox.getVisibility() == View.VISIBLE) {
				row.setMiljoOverride(mMiljoCheckbox.isChecked());
			} else {
				row.setMiljoOverride(null);
			}

			Document document = mRepository.getCurrentDocument();
			document.addOrUpdateRow(row);
			document.setHasUnsavedChanges(true);
			mRepository.commitCurrentDocument();
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

	private final class CustomNEMChangedListener implements TextWatcher {
		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			BigDecimal parsed = ValueHelper.parseValue(s.toString());
			mCustomNEMkg = (parsed.compareTo(BigDecimal.ZERO) > 0) ? parsed : null;
			calculate();
		}
	}

	private BigDecimal convertKgToMg(BigDecimal valueKg) {
		if (null == valueKg) {
			return null;
		}
		return valueKg.multiply(MG_PER_KG).setScale(0, RoundingMode.HALF_UP);
	}
}

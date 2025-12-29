package se.accidis.fmfg.app.ui.materials;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Document;
import se.accidis.fmfg.app.model.DocumentRow;
import se.accidis.fmfg.app.model.Material;
import se.accidis.fmfg.app.services.DocumentsRepository;
import se.accidis.fmfg.app.services.LabelsRepository;
import se.accidis.fmfg.app.ui.MainActivity;

/**
 * Fragment showing information about a material.
 */
public final class MaterialsInfoFragment extends Fragment implements MainActivity.HasNavigationItem {
	private Button mLoadButton;
	private Material mMaterial;
	private Button mRemoveButton;
	private DocumentsRepository mRepository;

	public static MaterialsInfoFragment createInstance(Material material) {
		Bundle bundle = material.toBundle();
		MaterialsInfoFragment fragment = new MaterialsInfoFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public int getItemId() {
		return R.id.nav_materials;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_materials_info, container, false);

		Bundle args = getArguments();
		mMaterial = Material.fromBundle(args);
		mRepository = DocumentsRepository.getInstance(getContext());
		DocumentRow existingRow = mRepository.getCurrentDocument().getRowByMaterial(mMaterial);
		if (null != existingRow) {
			mMaterial = existingRow.getMaterial();
		}

		TextView fbenHeading = (TextView) view.findViewById(R.id.material_fben_heading);
		LinearLayout fmListLayout = (LinearLayout) view.findViewById(R.id.material_fm_list);
		populateFmList(fbenHeading, fmListLayout);

		TextView namnView = (TextView) view.findViewById(R.id.material_namn);
		namnView.setText(mMaterial.getNamn());

		TextView unNrView = (TextView) view.findViewById(R.id.material_unnr);
		if (!TextUtils.isEmpty(mMaterial.getUNnr())) {
			unNrView.setText(String.format(getString(R.string.material_un_format), mMaterial.getUNnr()));
		} else {
			unNrView.setText(R.string.material_no_data);
		}

		TextView etiketterView = (TextView) view.findViewById(R.id.material_etiketter);
		if (!TextUtils.isEmpty(mMaterial.getEtiketterAsString())) {
			etiketterView.setText(mMaterial.getEtiketterAsString());
		} else {
			etiketterView.setText(R.string.material_no_data);
		}

		TextView frpGrpHeading = (TextView) view.findViewById(R.id.material_frpgrp_heading);
		TextView frpGrpView = (TextView) view.findViewById(R.id.material_frpgrp);
		if (!TextUtils.isEmpty(mMaterial.getFrpGrp())) {
			frpGrpView.setText(mMaterial.getFrpGrp());
		} else {
			frpGrpHeading.setVisibility(View.GONE);
			frpGrpView.setVisibility(View.GONE);
		}

		TextView tunnelKodHeading = (TextView) view.findViewById(R.id.material_tunnelkod_heading);
		TextView tunnelKodView = (TextView) view.findViewById(R.id.material_tunnelkod);
		if (!TextUtils.isEmpty(mMaterial.getTunnelkod())) {
			tunnelKodView.setText(mMaterial.getTunnelkod());
		} else {
			tunnelKodHeading.setVisibility(View.GONE);
			tunnelKodView.setVisibility(View.GONE);
		}

		TextView tpKatView = (TextView) view.findViewById(R.id.material_tpkat);
		if (0 != mMaterial.getTpKat()) {
			tpKatView.setText(String.valueOf(mMaterial.getTpKat()));
		} else {
			tpKatView.setText(R.string.material_no_data);
		}

		LinearLayout labelsLayout = (LinearLayout) view.findViewById(R.id.material_layout_labels);
		if (!mMaterial.getEtiketter().isEmpty()) {
			populateLabelsView(labelsLayout);
		} else {
			labelsLayout.setVisibility(View.GONE);
		}

		mLoadButton = (Button) view.findViewById(R.id.material_button_load);
		mLoadButton.setOnClickListener(new LoadButtonClickListener());
		mRemoveButton = (Button) view.findViewById(R.id.material_button_remove);
		mRemoveButton.setOnClickListener(new RemoveButtonClickListener());
		refreshDocumentState();

		return view;
	}

	private void populateLabelsView(LinearLayout layout) {
		Context context = getContext();
		Resources resources = getResources();
		int size = resources.getDimensionPixelSize(R.dimen.material_label_size);
		int margin = resources.getDimensionPixelSize(R.dimen.material_label_margin);

		List<Integer> labels = LabelsRepository.getLabelsByMaterial(mMaterial, false);

		for (Integer label : labels) {
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
			layoutParams.setMargins(0, 0, 0, margin);

			ImageView imageView = new ImageView(context);
			imageView.setLayoutParams(layoutParams);
			imageView.setImageDrawable(ContextCompat.getDrawable(context, label));
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

			layout.addView(imageView);
		}
	}

	private void refreshDocumentState() {
		DocumentRow row = mRepository.getCurrentDocument().getRowByMaterial(mMaterial);
		if (null != row) {
			mLoadButton.setText(R.string.material_change);
			mRemoveButton.setVisibility(View.VISIBLE);
		} else {
			mLoadButton.setText(R.string.material_load);
			mRemoveButton.setVisibility(View.GONE);
		}
	}

	private void populateFmList(TextView headingView, LinearLayout fmListLayout) {
		int rowSpacing = getResources().getDimensionPixelSize(R.dimen.material_fm_row_spacing);
		boolean hasData = false;

		for (Material.FM entry : mMaterial.getFM()) {
			CharSequence rowText = createFmRowText(entry);
			if (TextUtils.isEmpty(rowText)) {
				continue;
			}

			hasData = true;
			TextView rowView = new TextView(getContext());
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params.setMargins(0, 0, 0, rowSpacing);
			rowView.setLayoutParams(params);
			TextViewCompat.setTextAppearance(rowView, R.style.DocumentRowPrimary);
			rowView.setText(rowText);
			fmListLayout.addView(rowView);
		}

		if (!hasData) {
			headingView.setVisibility(View.GONE);
			fmListLayout.setVisibility(View.GONE);
		}
	}

	private CharSequence createFmRowText(Material.FM entry) {
		List<String> labelParts = new ArrayList<>();
		if (!TextUtils.isEmpty(entry.getFbet())) {
			labelParts.add(entry.getFbet());
		}
		if (!TextUtils.isEmpty(entry.getFben())) {
			labelParts.add(entry.getFben());
		}

		String nemValue = formatNemKg(entry.getNEMmg());
		boolean hasNem = !TextUtils.isEmpty(nemValue);

		if (labelParts.isEmpty() && !hasNem) {
			return "";
		}

		SpannableStringBuilder builder = new SpannableStringBuilder();
		if (!labelParts.isEmpty()) {
			builder.append(TextUtils.join(" ", labelParts));
		}

		if (hasNem) {
			if (builder.length() > 0) {
				builder.append('\n');
			}
			int nemStart = builder.length();
			builder.append("NEM ");
			builder.append(nemValue);
			builder.append(" kg");
			builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.darkgray)), nemStart, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		return builder;
	}

	private String formatNemKg(Integer nemMg) {
		if (null == nemMg) {
			return null;
		}
		BigDecimal nemKg = new BigDecimal(nemMg).divide(new BigDecimal(1000000), 6, BigDecimal.ROUND_FLOOR);
		return ValueHelper.formatValue(nemKg);
	}

	private final class LoadButtonClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			MaterialsLoadDialogFragment dialog = new MaterialsLoadDialogFragment();
			dialog.setArguments(mMaterial.toBundle());
			dialog.setDialogListener(new MaterialsLoadDialogListener());
			dialog.show(getFragmentManager(), MaterialsLoadDialogFragment.class.getSimpleName());
		}
	}

	private final class MaterialsLoadDialogListener implements MaterialsLoadDialogFragment.MaterialsLoadDialogListener {
		@Override
		public void onDismiss() {
			refreshDocumentState();
		}
	}

	private final class RemoveButtonClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			Document document = mRepository.getCurrentDocument();
			document.removeRowByMaterial(mMaterial);
			document.setHasUnsavedChanges(true);
			mRepository.commitCurrentDocument();
			refreshDocumentState();
		}
	}
}

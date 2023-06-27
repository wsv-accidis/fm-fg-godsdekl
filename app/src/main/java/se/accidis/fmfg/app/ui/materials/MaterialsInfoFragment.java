package se.accidis.fmfg.app.ui.materials;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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

		TextView fbenHeading = (TextView) view.findViewById(R.id.material_fben_heading);
		TextView fbenView = (TextView) view.findViewById(R.id.material_fben);
		TextView fbetView = (TextView) view.findViewById(R.id.material_fbet);
		if (!TextUtils.isEmpty(mMaterial.getFben()) || !TextUtils.isEmpty(mMaterial.getFbet())) {
			fbenView.setText(mMaterial.getFben());
			if (!TextUtils.isEmpty(mMaterial.getFbet())) {
				fbetView.setText(mMaterial.getFbet());
			} else {
				fbetView.setVisibility(View.GONE);
			}
		} else {
			fbenHeading.setVisibility(View.GONE);
			fbenView.setVisibility(View.GONE);
			fbetView.setVisibility(View.GONE);
		}

		TextView namnView = (TextView) view.findViewById(R.id.material_namn);
		namnView.setText(mMaterial.getNamn());

		TextView unNrView = (TextView) view.findViewById(R.id.material_unnr);
		if (!TextUtils.isEmpty(mMaterial.getUNnr())) {
			unNrView.setText(String.format(getString(R.string.material_un_format), mMaterial.getUNnr()));
		} else {
			unNrView.setText(R.string.material_no_data);
		}

		TextView klassKodView = (TextView) view.findViewById(R.id.material_klasskod);
		if (!TextUtils.isEmpty(mMaterial.getKlassKodAsString())) {
			klassKodView.setText(mMaterial.getKlassKodAsString());
		} else {
			klassKodView.setText(R.string.material_no_data);
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

		TextView nemHeading = (TextView) view.findViewById(R.id.material_nem_heading);
		TextView nemView = (TextView) view.findViewById(R.id.material_nem);
		if (mMaterial.hasNEM()) {
			nemView.setText(String.format(getString(R.string.unit_kg_format), ValueHelper.formatValue(mMaterial.getNEMkg())));
		} else {
			nemHeading.setVisibility(View.GONE);
			nemView.setVisibility(View.GONE);
		}

		LinearLayout labelsLayout = (LinearLayout) view.findViewById(R.id.material_layout_labels);
		if (!mMaterial.getKlassKod().isEmpty()) {
			populateLabelsView(labelsLayout);
		} else {
			labelsLayout.setVisibility(View.GONE);
		}

		mRepository = DocumentsRepository.getInstance(getContext());
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

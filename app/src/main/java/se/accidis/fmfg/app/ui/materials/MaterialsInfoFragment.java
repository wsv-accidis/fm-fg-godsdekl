package se.accidis.fmfg.app.ui.materials;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Material;

/**
 * Fragment showing information about a material.
 */
public final class MaterialsInfoFragment extends Fragment {
    private Material mMaterial;

    public static MaterialsInfoFragment createInstance(Material material) {
        Bundle bundle = material.toBundle();
        MaterialsInfoFragment fragment = new MaterialsInfoFragment();
        fragment.setArguments(bundle);
        return fragment;
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
        if(0 != mMaterial.getNEMmg()) {
            DecimalFormat format = new DecimalFormat();
            format.setMaximumFractionDigits(5);
            format.setMinimumFractionDigits(0);
            nemView.setText(String.format(getString(R.string.material_nem_format), format.format(mMaterial.getNEMkg())));
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

        return view;
    }

    private void populateLabelsView(LinearLayout layout) {
        Context context = getContext();
        Resources resources = getResources();
        int size = resources.getDimensionPixelSize(R.dimen.material_label_size);
        int margin = resources.getDimensionPixelSize(R.dimen.material_label_margin);

        List<Integer> labels = MaterialLabels.getLabelsByMaterial(mMaterial);

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
}

package se.accidis.fmfg.app.ui.materials;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        return view;
    }
}

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
    public static MaterialsInfoFragment createInstance(Material material) {
        Bundle bundle = new Bundle();
        bundle.putString(Material.Keys.FBET, material.getFbet());
        bundle.putString(Material.Keys.FBEN, material.getFben());
        bundle.putString(Material.Keys.UNNR, material.getUNnr());
        bundle.putString(Material.Keys.NAMN, material.getNamn());
        bundle.putStringArray(Material.Keys.KLASSKOD, material.getKlassKod().toArray(new String[material.getKlassKod().size()]));
        bundle.putInt(Material.Keys.NEMMG, material.getNEMmg());
        bundle.putInt(Material.Keys.TPKAT, material.getTpKat());
        bundle.putString(Material.Keys.FRPGRP, material.getFrpGrp());
        bundle.putString(Material.Keys.TUNNELKOD, material.getTunnelkod());
        bundle.putBoolean(Material.Keys.MILJO, material.getMiljo());

        MaterialsInfoFragment fragment = new MaterialsInfoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_materials_info, container, false);

        return view;
    }
}

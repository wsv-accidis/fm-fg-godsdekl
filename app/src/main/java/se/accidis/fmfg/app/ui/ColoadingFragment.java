package se.accidis.fmfg.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.utils.AndroidUtils;

/**
 * Fragment which displays co-loading rules.
 */
public final class ColoadingFragment extends Fragment implements MainActivity.HasTitle, MainActivity.HasNavigationItem {
    @Override
    public NavigationItem getItem() {
        return NavigationItem.COLOADING_ITEM;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.coloading_nav_title);
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidUtils.hideSoftKeyboard(getContext(), getView());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_coloading, container, false);
    }
}

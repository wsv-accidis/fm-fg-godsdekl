package se.accidis.fmfg.app.ui.instructions;

import android.content.Context;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.ui.MainActivity;
import se.accidis.fmfg.app.ui.NavigationItem;

/**
 * Fragment which displays co-loading rules.
 */
public final class ColoadingFragment extends WebViewFragmentBase implements MainActivity.HasTitle, MainActivity.HasNavigationItem {
    @Override
    public NavigationItem getItem() {
        return NavigationItem.COLOADING_ITEM;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.coloading_nav_title);
    }

    @Override
    protected String getUrl() {
        return "file:///android_asset/www/instr_samlast.html";
    }
}

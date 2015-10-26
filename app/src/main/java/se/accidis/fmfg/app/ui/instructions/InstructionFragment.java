package se.accidis.fmfg.app.ui.instructions;

import android.content.Context;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.ui.MainActivity;
import se.accidis.fmfg.app.ui.NavigationItem;

/**
 * Fragment which displays instructions for ADR.
 */
public final class InstructionFragment extends WebViewFragmentBase implements MainActivity.HasTitle, MainActivity.HasNavigationItem {
    @Override
    public NavigationItem getItem() {
        return NavigationItem.INSTRUCTIONS_ITEM;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.instructions_nav_title);
    }

    @Override
    protected String getUrl() {
        return "file:///android_asset/www/instr_adr.html";
    }
}

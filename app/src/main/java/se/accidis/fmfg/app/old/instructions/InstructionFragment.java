package se.accidis.fmfg.app.old.instructions;

import android.content.Context;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.old.MainActivity;

/**
 * Fragment which displays instructions for ADR.
 */
public final class InstructionFragment extends WebViewFragmentBase implements MainActivity.HasTitle, MainActivity.HasNavigationItem {
    @Override
    public int getItemId() {
        return R.id.nav_instructions;
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

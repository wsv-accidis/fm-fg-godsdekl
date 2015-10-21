package se.accidis.fmfg.app.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * General system utils.
 */
public final class AndroidUtils {
    public static final String LINE_SEPARATOR = "\n";

    public static void hideSoftKeyboard(Context context, View view) {
        if (null != context && null != view) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private AndroidUtils() {
    }
}

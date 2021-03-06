package se.accidis.fmfg.app.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 * General system utils.
 */
public final class AndroidUtils {
	public static final String LINE_SEPARATOR = "\n";

	private AndroidUtils() {
	}

	public static void assertIsTrue(boolean condition, String reason) {
		if (!condition) {
			throw new RuntimeException("Assertion failed: " + reason);
		}
	}

	public static String getAppVersionName(Context context) {
		return getPackageInfo(context).versionName;
	}

	public static void hideSoftKeyboard(Context context, View view) {
		if (null != context && null != view) {
			final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	private static PackageInfo getPackageInfo(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	public static void showSoftKeyboardForDialog(Dialog dialog) {
		final Window window = dialog.getWindow();
		if (null != window) {
			window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}
	}
}

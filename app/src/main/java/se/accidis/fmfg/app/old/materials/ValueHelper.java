package se.accidis.fmfg.app.old.materials;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Simple helper class for calculating and formatting values.
 */
public final class ValueHelper {
	private static final DecimalFormat mValueFormat = new DecimalFormat();

	private ValueHelper() {
	}

	public static String formatValue(BigDecimal bigDecimal) {
		return mValueFormat.format(bigDecimal);
	}

	public static int getMultiplierByTpKat(int tpKat) {
		switch (tpKat) {
			case 1:
				return 50;
			case 2:
				return 3;
			case 3:
				return 1;
			default:
				return 0;
		}
	}

	public static void initializeLocale(Locale locale) {
		Locale.setDefault(locale);
		mValueFormat.setMaximumFractionDigits(5);
		mValueFormat.setMinimumFractionDigits(0);
		mValueFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(locale));
	}

	public static BigDecimal parseValue(String text) {
		if (TextUtils.isEmpty(text)) {
			return BigDecimal.ZERO;
		}

		// BigDecimal constructor accepts only period as separator
		text = text.replace(',', '.');

		try {
			return new BigDecimal(text);
		} catch (NumberFormatException ignored) {
			return BigDecimal.ZERO;
		}
	}
}

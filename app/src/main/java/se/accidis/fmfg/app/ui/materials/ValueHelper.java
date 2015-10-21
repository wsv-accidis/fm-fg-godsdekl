package se.accidis.fmfg.app.ui.materials;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Simple helper class for calculating the value of a shipment.
 */
public final class ValueHelper {
    public static final int TPKAT_MIN = 1;
    public static final int TPKAT_MAX = 3;

    private ValueHelper() {
    }

    public static String formatValue(BigDecimal bigDecimal) {
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(5);
        format.setMinimumFractionDigits(0);
        return format.format(bigDecimal);
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

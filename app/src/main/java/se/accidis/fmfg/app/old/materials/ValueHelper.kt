package se.accidis.fmfg.app.old.materials

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Simple helper class for calculating and formatting values.
 */
object ValueHelper {
    private val valueFormat = DecimalFormat()

    @JvmStatic
    fun formatValue(bigDecimal: BigDecimal): String {
        return valueFormat.format(bigDecimal)
    }

    @JvmStatic
    fun getMultiplierByTpKat(tpKat: Int): Int {
        return when (tpKat) {
            1 -> 50
            2 -> 3
            3 -> 1
            else -> 0
        }
    }

    fun initializeLocale(locale: Locale) {
        Locale.setDefault(locale)
        valueFormat.maximumFractionDigits = 5
        valueFormat.minimumFractionDigits = 0
        valueFormat.decimalFormatSymbols = DecimalFormatSymbols.getInstance(locale)
    }

    @JvmStatic
    fun parseValue(text: String?): BigDecimal {
        if (text.isNullOrBlank()) {
            return BigDecimal.ZERO
        }

        // BigDecimal constructor accepts only period as separator
        val parseText = text.replace(',', '.')

        return try {
            BigDecimal(parseText)
        } catch (_: NumberFormatException) {
            BigDecimal.ZERO
        }
    }
}

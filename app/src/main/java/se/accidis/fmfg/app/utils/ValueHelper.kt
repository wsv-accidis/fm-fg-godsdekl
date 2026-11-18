package se.accidis.fmfg.app.utils

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.text.iterator

/**
 * Simple helper class for calculating and formatting values.
 */
object ValueHelper {
    @Volatile
    private var formatSymbols: DecimalFormatSymbols =
        DecimalFormatSymbols.getInstance(Locale.getDefault())

    private val valueFormat: ThreadLocal<DecimalFormat> = ThreadLocal.withInitial {
        DecimalFormat().apply {
            maximumFractionDigits = 5
            minimumFractionDigits = 0
            decimalFormatSymbols = formatSymbols
        }
    }

    @JvmStatic
    fun formatValue(bigDecimal: BigDecimal): String {
        return requireNotNull(valueFormat.get()).format(bigDecimal)
    }

    @JvmStatic
    fun formatPlainValue(bigDecimal: BigDecimal): String {
        return if (bigDecimal.compareTo(BigDecimal.ZERO) == 0) {
            "0"
        } else {
            bigDecimal.stripTrailingZeros().toPlainString().replace('.', ',')
        }
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
        formatSymbols = DecimalFormatSymbols.getInstance(locale)
    }

    @JvmStatic
    fun isValidDecimalInput(value: String): Boolean {
        if (value.isEmpty()) return true
        var hasSeparator = false
        for (ch in value) {
            if (ch == '.' || ch == ',') {
                if (hasSeparator) return false
                hasSeparator = true
            } else if (!ch.isDigit()) {
                return false
            }
        }
        return true
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

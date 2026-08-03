package se.accidis.fmfg.app.ui.materials

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.model.Material
import se.accidis.fmfg.app.old.materials.ValueHelper
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Units for NEM input.
 */
enum class NemUnit(val labelResId: Int, val factor: Long) {
    NONE(R.string.unit_none, 0L),
    GRAM(R.string.unit_g, 1_000L),
    KILOGRAM(R.string.unit_kg, 1_000_000L)
}

/**
 * ViewModel for the Material load screen.
 */
class MaterialLoadViewModel(initialMaterial: Material) : ViewModel() {
    var fbet by mutableStateOf(initialMaterial.fbet)
    var fben by mutableStateOf(initialMaterial.fben)
    var unNr by mutableStateOf(initialMaterial.UNnr)
    var namn by mutableStateOf(initialMaterial.namn)

    var klassKodList by mutableStateOf(initialMaterial.klassKod)
    var klassKodListVisible by mutableStateOf(false)

    // Internal value always in mg
    private var nemMgValue by mutableLongStateOf(initialMaterial.NEMmg.toLong())
    var isNemEnabled by mutableStateOf(initialMaterial.NEMmg != 0)

    val nemUnitSelected: NemUnit
        get() = when {
            !isNemEnabled -> NemUnit.NONE
            nemMgValue < 100_000L -> NemUnit.GRAM
            else -> NemUnit.KILOGRAM
        }

    var nemInputText: String by mutableStateOf(
        if (!isNemEnabled) ""
        else {
            val unit = if (nemMgValue < 100_000L) NemUnit.GRAM else NemUnit.KILOGRAM
            ValueHelper.formatValue(
                BigDecimal(nemMgValue).divide(
                    BigDecimal(unit.factor),
                    6,
                    RoundingMode.FLOOR,
                )
            )
        }
    )

    var nemUnitExpanded by mutableStateOf(false)

    var tpKat by mutableIntStateOf(initialMaterial.tpKat)
    var tpKatExpanded by mutableStateOf(false)

    var frpGrp by mutableStateOf(initialMaterial.frpGrp)
    var frpGrpExpanded by mutableStateOf(false)

    var tunnelKod by mutableStateOf(initialMaterial.tunnelKod)
    var tunnelKodExpanded by mutableStateOf(false)

    var miljo by mutableStateOf(initialMaterial.miljo)

    fun onNemInputChanged(newValue: String) {
        if (newValue.isEmpty() || newValue.all { (it.isDigit() || it == '.' || it == ',') }) {
            nemInputText = newValue
            if (isNemEnabled) {
                val parsed = ValueHelper.parseValue(newValue)
                nemMgValue = parsed.multiply(BigDecimal(nemUnitSelected.factor)).toLong()
            }
        }
    }

    fun onNemUnitSelected(unit: NemUnit) {
        if (unit == NemUnit.NONE) {
            isNemEnabled = false
            nemInputText = ""
        } else {
            isNemEnabled = true
            // If we're enabling it for the first time and value is 0, maybe set a default?
            // But the requirement says "restore previous value", so if it was 0, it stays 0.
            
            // Recalculate text based on the new unit
            val value = BigDecimal(nemMgValue).divide(BigDecimal(unit.factor), 6, RoundingMode.FLOOR)
            nemInputText = ValueHelper.formatValue(value)
        }
        nemUnitExpanded = false
    }

    /**
     * Returns the final NEM in mg, or 0 if disabled.
     */
    fun getFinalNemMg(): Int = if (isNemEnabled) nemMgValue.toInt() else 0
}

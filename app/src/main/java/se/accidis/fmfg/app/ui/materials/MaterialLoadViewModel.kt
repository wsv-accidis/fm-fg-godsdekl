package se.accidis.fmfg.app.ui.materials

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.model.DocumentRow
import se.accidis.fmfg.app.model.Material
import se.accidis.fmfg.app.model.MaterialSource
import se.accidis.fmfg.app.model.mutate
import se.accidis.fmfg.app.old.materials.ValueHelper
import se.accidis.fmfg.app.services.DocumentsRepository
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
 * Units for weight/volume input.
 */
enum class WeightVolumeUnit(val labelResId: Int) {
    KILOGRAM(R.string.unit_kg),
    LITER(R.string.unit_liter)
}

/**
 * ViewModel for the Material load screen.
 */
class MaterialLoadViewModel(
    private val initialMaterial: Material,
    private val initialRow: DocumentRow? = null,
    private val rowIndex: Int? = null
) : ViewModel() {

    val isEditing: Boolean
        get() = rowIndex != null

    val canLoadMaterial: Boolean
        get() {
            val numPkgs = numberOfPkgs.toIntOrNull() ?: 0
            val wtVol = ValueHelper.parseValue(weightVolume)
            val hasIdentity =
                UNnr.isNotBlank() || namn.isNotBlank() || fbet.isNotBlank() || fben.isNotBlank()
            return numPkgs > 0 && wtVol.signum() > 0 && hasIdentity
        }

    fun loadIntoDocument(repository: DocumentsRepository) {
        if (!canLoadMaterial) {
            return
        }

        val material = Material(
            fbet = fbet.trim(),
            fben = fben.trim(),
            UNnr = UNnr.trim(),
            namn = namn.trim(),
            klassKod = klassKodList,
            NEMmg = NEMmgValue.toInt(),
            tpKat = tpKat,
            frpGrp = frpGrp,
            tunnelKod = tunnelKod,
            miljo = miljo,
            source = MaterialSource.NONE
        )

        val row = DocumentRow(
            material = material,
            amount = ValueHelper.parseValue(amount),
            numberOfPackages = numberOfPkgs.toIntOrNull() ?: 0,
            typeOfPackages = typeOfPkgs.trim(),
            isVolume = weightVolumeUnit == WeightVolumeUnit.LITER,
            weightVolume = ValueHelper.parseValue(weightVolume)
        )

        val mutatedDoc = repository.currentDocument.mutate {
            if (rowIndex != null && rowIndex in rows.indices) {
                rows[rowIndex] = row
            } else {
                rows.add(row)
            }
        }
        repository.updateCurrentDocument(mutatedDoc)
    }

    // --------------------------
    // Fields for the DocumentRow
    // --------------------------

    var numberOfPkgs by mutableStateOf(
        if (initialRow != null && initialRow.numberOfPackages > 0) initialRow.numberOfPackages.toString() else ""
    )
    var typeOfPkgs by mutableStateOf(
        initialRow?.typeOfPackages ?: ""
    )
    var isTypeOfPkgsExpanded by mutableStateOf(false)
    var weightVolume by mutableStateOf(
        if (initialRow != null && initialRow.weightVolume.signum() != 0) ValueHelper.formatValue(initialRow.weightVolume) else ""
    )
    var weightVolumeUnit by mutableStateOf(
        if (initialRow?.isVolume == true) WeightVolumeUnit.LITER else WeightVolumeUnit.KILOGRAM
    )
    var isWeightVolumeUnitExpanded by mutableStateOf(false)
    var amount by mutableStateOf(
        if (initialRow != null && initialRow.amount.signum() != 0) ValueHelper.formatValue(initialRow.amount) else ""
    )

    var isNemPanelExpanded by mutableStateOf(initialMaterial.NEMmg > 0)

    val totalNEMmg: BigDecimal
        get() = ValueHelper.parseValue(amount).multiply(BigDecimal(NEMmgValue))

    val weightVolumePointsBasis: BigDecimal
        get() = if (NEMmgValue > 0) {
            totalNEMmg.divide(BigDecimal(NemUnit.KILOGRAM.factor), 6, RoundingMode.FLOOR)
        } else {
            ValueHelper.parseValue(weightVolume)
        }

    val totalPoints: BigDecimal
        get() = weightVolumePointsBasis.multiply(BigDecimal(ValueHelper.getMultiplierByTpKat(tpKat)))

    fun onNumberOfPkgsChanged(newValue: String) {
        if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.toIntOrNull() != null)) {
            numberOfPkgs = newValue
        }
    }

    fun onWeightVolumeChanged(newValue: String) {
        if (isValidDecimalInput(newValue)) {
            weightVolume = newValue
        }
    }

    fun onAmountChanged(newValue: String) {
        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
            amount = newValue
        }
    }

    // -----------------------
    // Fields for the Material
    // -----------------------

    var fbet by mutableStateOf(initialMaterial.fbet)
    var fben by mutableStateOf(initialMaterial.fben)
    var UNnr by mutableStateOf(initialMaterial.UNnr)
    var namn by mutableStateOf(initialMaterial.namn)

    var klassKodList by mutableStateOf(initialMaterial.klassKod)
    var isKlassKodListVisible by mutableStateOf(false)

    // Internal value always in mg
    var NEMmgValue by mutableLongStateOf(initialMaterial.NEMmg.toLong())
        private set

    var isNEMEnabled by mutableStateOf(initialMaterial.NEMmg != 0)

    var NEMUnitSelected by mutableStateOf(
        when {
            !isNEMEnabled -> NemUnit.NONE
            NEMmgValue < 100_000L -> NemUnit.GRAM
            else -> NemUnit.KILOGRAM
        }
    )
        private set

    var NEMInputText: String by mutableStateOf(
        if (!isNEMEnabled) {
            ""
        } else {
            ValueHelper.formatValue(
                BigDecimal(NEMmgValue).divide(
                    BigDecimal(NEMUnitSelected.factor),
                    6,
                    RoundingMode.FLOOR,
                )
            )
        }
    )

    var NEMUnitExpanded by mutableStateOf(false)

    var tpKat by mutableIntStateOf(initialMaterial.tpKat)
    var isTpKatExpanded by mutableStateOf(false)

    var frpGrp by mutableStateOf(initialMaterial.frpGrp)
    var isFrpGrpExpanded by mutableStateOf(false)

    var tunnelKod by mutableStateOf(initialMaterial.tunnelKod)
    var isTunnelKodExpanded by mutableStateOf(false)

    var miljo by mutableStateOf(initialMaterial.miljo)

    var isEditPanelExpanded by mutableStateOf(false)

    fun onUNnrChanged(newValue: String) {
        if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
            UNnr = newValue
        }
    }

    fun onNEMInputChanged(newValue: String) {
        if (isValidDecimalInput(newValue)) {
            if (isNEMEnabled) {
                val parsed = ValueHelper.parseValue(newValue)
                val mg = parsed.multiply(BigDecimal(NEMUnitSelected.factor))
                if (mg > BigDecimal(Int.MAX_VALUE)) {
                    return
                }
                NEMInputText = newValue
                NEMmgValue = mg.toLong()
            } else {
                NEMInputText = newValue
            }
        }
    }

    fun onNemUnitSelected(unit: NemUnit) {
        NEMUnitSelected = unit
        if (unit == NemUnit.NONE) {
            isNEMEnabled = false
            NEMmgValue = 0L
            NEMInputText = ""
        } else {
            isNEMEnabled = true
            NEMInputText = if (NEMmgValue == 0L) {
                ""
            } else {
                // Recalculate text based on the new unit
                val value =
                    BigDecimal(NEMmgValue).divide(BigDecimal(unit.factor), 6, RoundingMode.FLOOR)
                ValueHelper.formatValue(value)
            }
        }
        NEMUnitExpanded = false
    }

    private fun isValidDecimalInput(value: String): Boolean {
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
}

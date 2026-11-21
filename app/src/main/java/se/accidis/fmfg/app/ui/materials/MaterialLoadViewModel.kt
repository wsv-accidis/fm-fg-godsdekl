package se.accidis.fmfg.app.ui.materials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.model.DocumentRow
import se.accidis.fmfg.app.model.Material
import se.accidis.fmfg.app.model.MaterialSource
import se.accidis.fmfg.app.utils.ValueHelper
import se.accidis.fmfg.app.services.DocumentsRepository
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Units for NEM input.
 */
enum class NemUnit(val labelResId: Int, val factor: Long) {
    NONE(R.string.unit_none, 0L),
    GRAM(R.string.unit_g, 1_000L),
    KILOGRAM(R.string.unit_kg, 1_000_000L);

    private val factorDecimal: BigDecimal = BigDecimal.valueOf(factor)

    fun fromMg(mg: Long): BigDecimal = fromMg(BigDecimal.valueOf(mg))

    fun fromMg(mg: BigDecimal): BigDecimal {
        if (factor == 0L) return BigDecimal.ZERO
        return mg.divide(factorDecimal, 6, RoundingMode.FLOOR)
    }

    fun toMg(value: BigDecimal): BigDecimal = value.multiply(factorDecimal)

    companion object {
        private const val GRAM_THRESHOLD_MG = 100_000L
        private val GRAM_THRESHOLD_DECIMAL = BigDecimal.valueOf(GRAM_THRESHOLD_MG)

        fun forMg(mg: Long): NemUnit = when {
            mg == 0L -> NONE
            mg < GRAM_THRESHOLD_MG -> GRAM
            else -> KILOGRAM
        }

        fun displayUnitForMg(mg: BigDecimal): NemUnit =
            if (mg < GRAM_THRESHOLD_DECIMAL) GRAM else KILOGRAM
    }
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
    private val repository: DocumentsRepository,
    material: Material,
    private val rowIndex: Int? = null
) : ViewModel() {

    private val initialRow: DocumentRow? =
        rowIndex?.let { repository.currentDocumentOrNull?.rows?.getOrNull(it) }

    private val _uiState = MutableStateFlow(
        createInitialState(initialRow?.material ?: material, initialRow)
    )
    val uiState: StateFlow<MaterialLoadUiState> = _uiState.asStateFlow()

    init {
        if (rowIndex != null && initialRow == null) {
            viewModelScope.launch {
                val row = repository.awaitCurrentDocument().rows.getOrNull(rowIndex)
                if (row != null) {
                    _uiState.value = createInitialState(row.material, row)
                }
            }
        }
    }

    private fun createInitialState(
        initialMaterial: Material,
        initialRow: DocumentRow?
    ): MaterialLoadUiState {
        val nemMg = initialMaterial.NEMmg.toLong()
        val nemUnit = NemUnit.forMg(nemMg)
        val isNemEnabled = nemUnit != NemUnit.NONE
        val nemText = if (isNemEnabled) {
            ValueHelper.formatPlainValue(nemUnit.fromMg(nemMg))
        } else {
            ""
        }

        return MaterialLoadUiState(
            isEditing = rowIndex != null,
            numberOfPkgs = if (initialRow != null && initialRow.numberOfPackages > 0) {
                initialRow.numberOfPackages.toString()
            } else {
                ""
            },
            typeOfPkgs = initialRow?.typeOfPackages ?: "",
            weightVolume = if (initialRow != null && initialRow.weightVolume.signum() != 0) {
                ValueHelper.formatPlainValue(initialRow.weightVolume)
            } else {
                ""
            },
            weightVolumeUnit = if (initialRow?.isVolume == true) {
                WeightVolumeUnit.LITER
            } else {
                WeightVolumeUnit.KILOGRAM
            },
            amount = if (initialRow != null && initialRow.amount.signum() != 0) {
                ValueHelper.formatPlainValue(initialRow.amount)
            } else {
                ""
            },
            fbet = initialMaterial.fbet,
            fben = initialMaterial.fben,
            unNr = initialMaterial.UNnr,
            namn = initialMaterial.namn,
            klassKodList = initialMaterial.klassKod,
            nemMgValue = nemMg,
            isNemEnabled = isNemEnabled,
            nemUnitSelected = nemUnit,
            nemInputText = nemText,
            tpKat = initialMaterial.tpKat,
            frpGrp = initialMaterial.frpGrp,
            tunnelKod = initialMaterial.tunnelKod,
            miljo = initialMaterial.miljo
        )
    }

    fun loadIntoDocument() {
        val state = _uiState.value
        if (!state.canLoadMaterial) {
            return
        }

        val material = Material(
            fbet = state.fbet.trim(),
            fben = state.fben.trim(),
            UNnr = state.unNr.trim(),
            namn = state.namn.trim(),
            klassKod = state.klassKodList,
            NEMmg = state.nemMgValue.toInt(),
            tpKat = state.tpKat,
            frpGrp = state.frpGrp,
            tunnelKod = state.tunnelKod,
            miljo = state.miljo,
            source = MaterialSource.NONE
        )

        val row = DocumentRow(
            material = material,
            amount = ValueHelper.parseValue(state.amount),
            numberOfPackages = state.numberOfPkgs.toIntOrNull() ?: 0,
            typeOfPackages = state.typeOfPkgs.trim(),
            isVolume = state.weightVolumeUnit == WeightVolumeUnit.LITER,
            weightVolume = ValueHelper.parseValue(state.weightVolume)
        )

        repository.mutateCurrentDocument {
            if (rowIndex != null && rowIndex in rows.indices) {
                rows[rowIndex] = row
            } else {
                rows.add(row)
            }
        }
    }

    fun onNumberOfPkgsChanged(newValue: String) {
        if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.toIntOrNull() != null)) {
            _uiState.update { it.copy(numberOfPkgs = newValue) }
        }
    }

    fun onTypeOfPkgsChanged(newValue: String) {
        _uiState.update { it.copy(typeOfPkgs = newValue) }
    }

    fun onWeightVolumeChanged(newValue: String) {
        if (ValueHelper.isValidDecimalInput(newValue)) {
            _uiState.update { it.copy(weightVolume = newValue) }
        }
    }

    fun onWeightVolumeUnitSelected(unit: WeightVolumeUnit) {
        _uiState.update { it.copy(weightVolumeUnit = unit) }
    }

    fun onAmountChanged(newValue: String) {
        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
            _uiState.update { it.copy(amount = newValue) }
        }
    }

    fun onFbetChanged(newValue: String) {
        _uiState.update { it.copy(fbet = newValue) }
    }

    fun onFbenChanged(newValue: String) {
        _uiState.update { it.copy(fben = newValue) }
    }

    fun onUnNrChanged(newValue: String) {
        if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
            _uiState.update { it.copy(unNr = newValue) }
        }
    }

    fun onNamnChanged(newValue: String) {
        _uiState.update { it.copy(namn = newValue) }
    }

    fun onRemoveKlassKod(kod: String) {
        _uiState.update { it.copy(klassKodList = it.klassKodList - kod) }
    }

    fun onToggleKlassKod(kod: String) {
        _uiState.update { state ->
            val updated = if (state.klassKodList.contains(kod)) {
                state.klassKodList - kod
            } else {
                state.klassKodList + kod
            }
            state.copy(klassKodList = updated)
        }
    }

    fun onNemInputChanged(newValue: String) {
        if (ValueHelper.isValidDecimalInput(newValue)) {
            _uiState.update { state ->
                if (state.isNemEnabled) {
                    val parsed = ValueHelper.parseValue(newValue)
                    val mg = state.nemUnitSelected.toMg(parsed)
                    if (mg > MAX_NEM_MG) {
                        return@update state
                    }
                    state.copy(nemInputText = newValue, nemMgValue = mg.toLong())
                } else {
                    state.copy(nemInputText = newValue)
                }
            }
        }
    }

    fun onNemUnitSelected(unit: NemUnit) {
        _uiState.update { state ->
            if (unit == NemUnit.NONE) {
                state.copy(
                    nemUnitSelected = unit,
                    isNemEnabled = false,
                    nemMgValue = 0L,
                    nemInputText = ""
                )
            } else {
                val inputText = if (state.nemMgValue == 0L) {
                    ""
                } else {
                    ValueHelper.formatPlainValue(unit.fromMg(state.nemMgValue))
                }
                state.copy(
                    nemUnitSelected = unit,
                    isNemEnabled = true,
                    nemInputText = inputText
                )
            }
        }
    }

    fun onTpKatSelected(tpKat: Int) {
        _uiState.update { it.copy(tpKat = tpKat) }
    }

    fun onFrpGrpSelected(frpGrp: String) {
        _uiState.update { it.copy(frpGrp = frpGrp) }
    }

    fun onTunnelKodSelected(tunnelKod: String) {
        _uiState.update { it.copy(tunnelKod = tunnelKod) }
    }

    fun onMiljoChanged(miljo: Boolean) {
        _uiState.update { it.copy(miljo = miljo) }
    }

    companion object {
        private val MAX_NEM_MG: BigDecimal = BigDecimal.valueOf(Int.MAX_VALUE.toLong())
    }
}

package se.accidis.fmfg.app.ui.materials

import se.accidis.fmfg.app.utils.ValueHelper
import java.math.BigDecimal

/**
 * UI state for the Material load screen.
 */
data class MaterialLoadUiState(
    val isEditing: Boolean = false,
    val numberOfPkgs: String = "",
    val typeOfPkgs: String = "",
    val weightVolume: String = "",
    val weightVolumeUnit: WeightVolumeUnit = WeightVolumeUnit.KILOGRAM,
    val amount: String = "",
    val fbet: String = "",
    val fben: String = "",
    val unNr: String = "",
    val namn: String = "",
    val klassKodList: List<String> = emptyList(),
    val nemMgValue: Long = 0L,
    val isNemEnabled: Boolean = false,
    val nemUnitSelected: NemUnit = NemUnit.NONE,
    val nemInputText: String = "",
    val tpKat: Int = 0,
    val frpGrp: String = "",
    val tunnelKod: String = "",
    val miljo: Boolean = false
) {
    val canLoadMaterial: Boolean = run {
        val numPkgs = numberOfPkgs.toIntOrNull() ?: 0
        val wtVol = ValueHelper.parseValue(weightVolume)
        val hasIdentity =
            unNr.isNotBlank() || namn.isNotBlank() || fbet.isNotBlank() || fben.isNotBlank()
        numPkgs > 0 && wtVol.signum() > 0 && hasIdentity
    }

    val totalNemMg: BigDecimal =
        ValueHelper.parseValue(amount).multiply(BigDecimal.valueOf(nemMgValue))

    val weightVolumePointsBasis: BigDecimal = if (nemMgValue > 0) {
        NemUnit.KILOGRAM.fromMg(totalNemMg)
    } else {
        ValueHelper.parseValue(weightVolume)
    }

    val totalPoints: BigDecimal =
        weightVolumePointsBasis.multiply(
            BigDecimal.valueOf(ValueHelper.getMultiplierByTpKat(tpKat).toLong())
        )
}

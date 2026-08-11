package se.accidis.fmfg.app.model

import android.content.Context
import org.json.JSONObject
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.old.materials.ValueHelper
import java.math.BigDecimal

/**
 * Model object for rows in a transport declaration.
 */
data class DocumentRow(
    val material: Material,
    val amount: BigDecimal,
    val numberOfPackages: Int,
    val typeOfPackages: String,
    val isVolume: Boolean,
    val weightVolume: BigDecimal
) {
    private val multiplier: BigDecimal =
        BigDecimal(ValueHelper.getMultiplierByTpKat(material.tpKat))

    val calculatedValue: BigDecimal
        get() {
            val baseValue = if (material.NEMmg != 0) NEMkg else weightVolume
            return baseValue.multiply(multiplier)
        }

    fun hasNEM(): Boolean = material.hasNEM()

    val NEMkg: BigDecimal
        get() = material.NEMkg.multiply(amount)

    fun packagesText(context: Context): String = when {
        numberOfPackages == 0 && typeOfPackages.isBlank() -> ""
        typeOfPackages.isBlank() -> {
            val resId =
                if (numberOfPackages == 1) R.string.document_unspecified_package else R.string.document_unspecified_packages
            "$numberOfPackages ${context.getString(resId)}"
        }

        else -> "$numberOfPackages ${typeOfPackages.trim()}"
    }

    fun weightVolumeText(context: Context): String {
        val resId = if (isVolume) R.string.unit_liter_format else R.string.unit_kg_format
        return context.getString(resId, ValueHelper.formatValue(weightVolume))
    }

    val isFreeText: Boolean = true

    fun toJson(): JSONObject = material.toJson().apply {
        put(Keys.AMOUNT, amount.toString())
        put(Keys.IS_VOLUME, isVolume)
        put(Keys.NUMBER_OF_PKGS, numberOfPackages)
        put(Keys.TYPE_OF_PKGS, typeOfPackages)
        put(Keys.WEIGHT_VOLUME, weightVolume.toString())
    }

    object Keys {
        const val AMOUNT: String = "Amount"
        const val IS_VOLUME: String = "IsVolume"
        const val NUMBER_OF_PKGS: String = "NumberOfPkgs"
        const val TYPE_OF_PKGS: String = "TypeOfPkgs"
        const val WEIGHT_VOLUME: String = "WeightVolume"
    }

    companion object {
        @JvmStatic
        fun fromJson(json: JSONObject): DocumentRow = DocumentRow(
            material = Material.fromJSON(json, MaterialSource.NONE),
            amount = BigDecimal(json.getString(Keys.AMOUNT)),
            isVolume = json.getBoolean(Keys.IS_VOLUME),
            numberOfPackages = json.getInt(Keys.NUMBER_OF_PKGS),
            typeOfPackages = json.optString(Keys.TYPE_OF_PKGS, ""),
            weightVolume = BigDecimal(json.getString(Keys.WEIGHT_VOLUME))
        )
    }
}

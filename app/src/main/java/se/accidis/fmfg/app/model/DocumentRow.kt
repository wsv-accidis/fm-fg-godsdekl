package se.accidis.fmfg.app.model

import android.content.Context
import android.text.TextUtils
import org.json.JSONException
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
            val value: BigDecimal =
                if (0 != material.NEMmg) {
                    NEMkg
                } else {
                    weightVolume
                }
            return value.multiply(multiplier)
        }

    fun hasNEM(): Boolean {
        return material.hasNEM()
    }

    val NEMkg: BigDecimal
        get() = material.NEMkg.multiply(amount)

    fun packagesText(context: Context): String {
        return if (0 == numberOfPackages && typeOfPackages.isBlank()) {
            ""
        } else if (typeOfPackages.isBlank()) {
            numberOfPackages.toString() + ' ' + context.getString(if (1 == numberOfPackages) R.string.document_unspecified_package else R.string.document_unspecified_packages)
        } else {
            numberOfPackages.toString() + ' ' + typeOfPackages.trim { it <= ' ' }
        }
    }

    fun weightVolumeText(context: Context): String {
        return String.format(
            context.getString(if (isVolume) R.string.unit_liter_format else R.string.unit_kg_format),
            ValueHelper.formatValue(weightVolume)
        )
    }

    // TODO: No longer needed, delete when safe
    fun copyTo(other: DocumentRow) {
    }

    // TODO: No longer needed, delete when safe
    val isFreeText: Boolean
        get() = true

    @Throws(JSONException::class)
    fun toJson(): JSONObject {
        val json = material.toJson()
        json.put(Keys.AMOUNT, amount.toString())
        json.put(Keys.IS_VOLUME, isVolume)
        json.put(Keys.NUMBER_OF_PKGS, numberOfPackages)
        json.put(Keys.TYPE_OF_PKGS, typeOfPackages)
        json.put(Keys.WEIGHT_VOLUME, weightVolume.toString())
        return json
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
        @Throws(JSONException::class)
        fun fromJson(json: JSONObject): DocumentRow {
            val material = Material.fromJSON(json, MaterialSource.NONE)
            val row = DocumentRow(
                material = material,
                amount = BigDecimal(json.getString(Keys.AMOUNT)),
                isVolume = json.getBoolean(Keys.IS_VOLUME),
                numberOfPackages = json.getInt(Keys.NUMBER_OF_PKGS),
                typeOfPackages = json.optString(Keys.TYPE_OF_PKGS, ""),
                weightVolume = BigDecimal(json.getString(Keys.WEIGHT_VOLUME))
            )
            return row
        }
    }
}

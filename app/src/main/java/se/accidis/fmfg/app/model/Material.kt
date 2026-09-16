package se.accidis.fmfg.app.model

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import org.json.JSONArray
import org.json.JSONObject
import se.accidis.fmfg.app.utils.JSONUtils
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Model object for materials.
 */
@Serializable
@Parcelize
data class Material(
    val fbet: String,
    val fben: String,
    val UNnr: String,
    val namn: String,
    val klassKod: List<String>,
    val NEMmg: Int,
    val tpKat: Int,
    val frpGrp: String,
    val tunnelKod: String,
    val miljo: Boolean,
    val source: MaterialSource = MaterialSource.NONE
) : Parcelable {
    @IgnoredOnParcel
    val klassKodAsString: String = createLabels()

    @IgnoredOnParcel
    val fullText: String = createFullText()

    @IgnoredOnParcel
    private val mSearchText: String = createSearchText()

    val NEMkg: BigDecimal
        get() = BigDecimal(NEMmg).divide(BigDecimal(1_000_000), 6, RoundingMode.FLOOR)

    val uniqueKey: String
        get() = "$UNnr|$namn|$fben|$fbet|${klassKod.joinToString(",")}|$tpKat|$frpGrp|$tunnelKod"

    fun hasNEM(): Boolean = NEMmg != 0

    fun matches(search: CharSequence): Boolean = mSearchText.contains(search, ignoreCase = true)

    fun toBundle(): Bundle = Bundle().apply {
        putString(Keys.FBET, fbet)
        putString(Keys.FBEN, fben)
        putString(Keys.UNNR, UNnr)
        putString(Keys.NAMN, namn)
        putStringArray(Keys.KLASSKOD, klassKod.toTypedArray())
        putInt(Keys.NEMMG, NEMmg)
        putInt(Keys.TPKAT, tpKat)
        putString(Keys.FRPGRP, frpGrp)
        putString(Keys.TUNNELKOD, tunnelKod)
        putBoolean(Keys.MILJO, miljo)
        putString(Keys.SOURCE, source.name)
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put(Keys.FBET, fbet)
        put(Keys.FBEN, fben)
        put(Keys.UNNR, UNnr)
        put(Keys.NAMN, namn)
        put(Keys.KLASSKOD, JSONArray(klassKod))
        put(Keys.NEMMG, NEMmg)
        put(Keys.TPKAT, tpKat)
        put(Keys.FRPGRP, frpGrp)
        put(Keys.TUNNELKOD, tunnelKod)
        put(Keys.MILJO, miljo)
    }

    override fun toString(): String = fben.ifBlank { namn }

    private fun createFullText(): String = buildString {
        if (UNnr.isNotBlank()) {
            append("UN $UNnr")
        }
        if (source != MaterialSource.ADR_S) {
            if (isNotEmpty()) append(' ')
            append(namn)
        }
        if (klassKodAsString.isNotBlank()) {
            if (isNotEmpty()) append(", ")
            append(klassKodAsString)
        }
        if (frpGrp.isNotBlank()) {
            if (isNotEmpty()) append(", ")
            append(frpGrp)
        }
        if (tunnelKod.isNotBlank()) {
            if (isNotEmpty()) append(' ')
            append("($tunnelKod)")
        }
    }

    private fun createLabels(): String {
        if (klassKod.isEmpty()) return ""
        if (klassKod.size == 1) return klassKod[0]
        val rest = klassKod.drop(1).joinToString(", ")
        return "${klassKod[0]} ($rest)"
    }

    private fun createSearchText(): String = buildString {
        append(namn.lowercase())
        if (fbet.isNotBlank()) {
            append(' ')
            append(fbet.lowercase())
        }
        if (fben.isNotBlank()) {
            append(' ')
            append(fben.lowercase())
        }
        if (UNnr.isNotBlank()) {
            append(' ')
            append(UNnr)
        }
    }

    /**
     * Keys used for persistence.
     */
    object Keys {
        const val FBEN: String = "Fben"
        const val FBET: String = "Fbet"
        const val FRPGRP: String = "FrpGrp"
        const val KLASSKOD: String = "KlassKod"
        const val MILJO: String = "Miljo"
        const val NAMN: String = "Namn"
        const val NEMMG: String = "NEMmg"
        const val SOURCE: String = "Source"
        const val TPKAT: String = "TpKat"
        const val TUNNELKOD: String = "TunnelKod"
        const val UNNR: String = "UNnr"
    }

    companion object {
        const val TPKAT_MAX: Int = 3
        const val TPKAT_MIN: Int = 1

        @JvmStatic
        fun fromBundle(bundle: Bundle): Material = Material(
            fbet = bundle.getString(Keys.FBET).orEmpty(),
            fben = bundle.getString(Keys.FBEN).orEmpty(),
            UNnr = bundle.getString(Keys.UNNR).orEmpty(),
            namn = bundle.getString(Keys.NAMN).orEmpty(),
            klassKod = bundle.getStringArray(Keys.KLASSKOD)?.toList().orEmpty(),
            NEMmg = bundle.getInt(Keys.NEMMG),
            tpKat = bundle.getInt(Keys.TPKAT),
            frpGrp = bundle.getString(Keys.FRPGRP).orEmpty(),
            tunnelKod = bundle.getString(Keys.TUNNELKOD).orEmpty(),
            miljo = bundle.getBoolean(Keys.MILJO),
            source = bundle.getString(Keys.SOURCE)?.let { MaterialSource.valueOf(it) }
                ?: MaterialSource.NONE
        )

        @JvmStatic
        fun fromJSON(json: JSONObject, source: MaterialSource): Material {
            val klassKodJson = json.optJSONArray(Keys.KLASSKOD)
            val klassKod =
                if (null == klassKodJson) emptyList() else (0 until klassKodJson.length())
                    .map { i -> klassKodJson.getString(i) }

            return Material(
                fbet = JSONUtils.getStringOrNull(json, Keys.FBET).orEmpty(),
                fben = JSONUtils.getStringOrNull(json, Keys.FBEN).orEmpty(),
                UNnr = JSONUtils.getStringOrNull(json, Keys.UNNR).orEmpty(),
                namn = json.getString(Keys.NAMN),
                klassKod = klassKod,
                NEMmg = json.optInt(Keys.NEMMG),
                tpKat = json.getInt(Keys.TPKAT),
                frpGrp = JSONUtils.getStringOrNull(json, Keys.FRPGRP).orEmpty(),
                tunnelKod = JSONUtils.getStringOrNull(json, Keys.TUNNELKOD).orEmpty(),
                miljo = json.optBoolean(Keys.MILJO),
                source = source
            )
        }
    }
}

package se.accidis.fmfg.app.model

import android.os.Bundle
import android.text.TextUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import se.accidis.fmfg.app.utils.JSONUtils
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

/**
 * Model object for materials.
 */
data class Material(
    val fbet: String,
    val fben: String,
    val UNnr: String,
    val namn: String,
    val klassKod: List<String>,
    val NEMmg: Int,
    val tpKat: Int,
    val frpGrp: String,
    val tunnelkod: String,
    val miljo: Boolean
) {
    val klassKodAsString: String = createLabels()
    val fullText: String = createFullText()
    private val mSearchText: String = createSearchText()

    val NEMkg: BigDecimal?
        get() {
            val value = BigDecimal(this.NEMmg)
            return value.divide(BigDecimal(1000000), 6, RoundingMode.FLOOR)
        }

    fun hasNEM(): Boolean = (0 != this.NEMmg)

    fun matches(search: CharSequence): Boolean = mSearchText.contains(search, ignoreCase = true)

    fun toBundle(): Bundle {
        val bundle = Bundle()
        bundle.putString(Keys.FBET, this.fbet)
        bundle.putString(Keys.FBEN, this.fben)
        bundle.putString(Keys.UNNR, this.UNnr)
        bundle.putString(Keys.NAMN, this.namn)
        bundle.putStringArray(Keys.KLASSKOD, klassKod.toTypedArray<String>())
        bundle.putInt(Keys.NEMMG, this.NEMmg)
        bundle.putInt(Keys.TPKAT, this.tpKat)
        bundle.putString(Keys.FRPGRP, this.frpGrp)
        bundle.putString(Keys.TUNNELKOD, this.tunnelkod)
        bundle.putBoolean(Keys.MILJO, this.miljo)
        return bundle
    }

    @Throws(JSONException::class)
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put(Keys.FBET, this.fbet)
        json.put(Keys.FBEN, this.fben)
        json.put(Keys.UNNR, this.UNnr)
        json.put(Keys.NAMN, this.namn)
        json.put(Keys.KLASSKOD, JSONArray(this.klassKod))
        json.put(Keys.NEMMG, this.NEMmg)
        json.put(Keys.TPKAT, this.tpKat)
        json.put(Keys.FRPGRP, this.frpGrp)
        json.put(Keys.TUNNELKOD, this.tunnelkod)
        json.put(Keys.MILJO, this.miljo)
        return json
    }

    override fun toString(): String = (if (TextUtils.isEmpty(this.fben)) this.namn else this.fben)

    fun toUniqueKey(): String = this.namn + '|' + this.fben + '|' + this.fbet

    private fun createFullText(): String {
        val builder = StringBuilder()
        if (!TextUtils.isEmpty(this.UNnr)) {
            builder.append("UN ")
            builder.append(this.UNnr)
            builder.append(' ')
        }
        builder.append(this.namn)
        if (!TextUtils.isEmpty(this.klassKodAsString)) {
            builder.append(", ")
            builder.append(this.klassKodAsString)
        }
        if (!TextUtils.isEmpty(this.frpGrp)) {
            builder.append(", ")
            builder.append(this.frpGrp)
        }
        if (!TextUtils.isEmpty(this.tunnelkod)) {
            builder.append(" (")
            builder.append(this.tunnelkod)
            builder.append(')')
        }
        return builder.toString()
    }

    private fun createLabels(): String {
        if (klassKod.isEmpty()) return ""
        if (1 == klassKod.size) return klassKod[0]
        val builder = StringBuilder()
        for (i in 1 until klassKod.size) {
            if (builder.isNotEmpty()) builder.append(", ")
            builder.append(klassKod[i])
        }
        return String.format("%s (%s)", klassKod[0], builder.toString())
    }

    private fun createSearchText(): String {
        val builder = StringBuilder()
        builder.append(namn.lowercase(Locale.getDefault()))
        if (!TextUtils.isEmpty(this.fbet)) {
            builder.append(' ')
            builder.append(fbet.lowercase(Locale.getDefault()))
        }
        if (!TextUtils.isEmpty(this.fben)) {
            builder.append(' ')
            builder.append(fben.lowercase(Locale.getDefault()))
        }
        if (!TextUtils.isEmpty(this.UNnr)) {
            builder.append(' ')
            builder.append(this.UNnr)
        }
        return builder.toString()
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
        const val TPKAT: String = "TpKat"
        const val TUNNELKOD: String = "TunnelKod"
        const val UNNR: String = "UNnr"
    }

    companion object {
        const val TPKAT_MAX: Int = 3
        const val TPKAT_MIN: Int = 1

        @JvmStatic
        fun fromBundle(bundle: Bundle): Material {
            val fbet = bundle.getString(Keys.FBET)
            val fben = bundle.getString(Keys.FBEN)
            val unNr = bundle.getString(Keys.UNNR)
            val namn = bundle.getString(Keys.NAMN)
            val nEMmg = bundle.getInt(Keys.NEMMG)
            val tpKat = bundle.getInt(Keys.TPKAT)
            val frpGrp = bundle.getString(Keys.FRPGRP)
            val tunnelkod = bundle.getString(Keys.TUNNELKOD)
            val miljo = bundle.getBoolean(Keys.MILJO)

            val klassKodArray = bundle.getStringArray(Keys.KLASSKOD)
            val klassKod =
                if (null != klassKodArray) listOf(*klassKodArray) else emptyList<String>()

            return Material(
                fbet ?: "",
                fben ?: "",
                unNr ?: "",
                namn ?: "",
                klassKod,
                nEMmg,
                tpKat,
                frpGrp ?: "",
                tunnelkod ?: "",
                miljo
            )
        }

        @JvmStatic
        @Throws(JSONException::class)
        fun fromJSON(json: JSONObject): Material {
            val fbet = JSONUtils.getStringOrNull(json, Keys.FBET)
            val fben = JSONUtils.getStringOrNull(json, Keys.FBEN)
            val unNr = JSONUtils.getStringOrNull(json, Keys.UNNR)
            val namn = json.getString(Keys.NAMN)
            val nEMmg = json.optInt(Keys.NEMMG)
            val tpKat = json.getInt(Keys.TPKAT)
            val frpGrp = JSONUtils.getStringOrNull(json, Keys.FRPGRP)
            val tunnelkod = JSONUtils.getStringOrNull(json, Keys.TUNNELKOD)
            val miljo = json.optBoolean(Keys.MILJO)

            val klassKodJson = json.optJSONArray(Keys.KLASSKOD)
            val klassKod = mutableListOf<String>()
            if (null != klassKodJson) {
                for (i in 0 until klassKodJson.length()) {
                    klassKod.add(klassKodJson.getString(i))
                }
            }

            return Material(
                fbet,
                fben ?: "",
                unNr ?: "",
                namn ?: "",
                klassKod,
                nEMmg,
                tpKat,
                frpGrp ?: "",
                tunnelkod ?: "",
                miljo
            )
        }
    }
}

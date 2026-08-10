package se.accidis.fmfg.app.model

import android.content.Context
import org.joda.time.DateTime
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.old.materials.ValueHelper.formatValue
import se.accidis.fmfg.app.utils.JSONUtils
import java.math.BigDecimal
import java.util.UUID

/**
 * Model object for transport documents.
 */
data class Document(
    val id: UUID,
    val rows: List<DocumentRow>,
    val hasUnsavedChanges: Boolean,
    val timestamp: DateTime?,
    val author: String,
    val name: String,
    val sender: String,
    val recipient: String,
    val isProtectedTransport: Boolean?,
    val vehicleReg: String,
    val vehicleType: String
) {
    val calculatedTotalValue: BigDecimal
        get() = rows.fold(BigDecimal.ZERO) { acc, row -> acc.add(row.calculatedValue) }

    fun calculatedValueByTpKat(tpKat: Int): BigDecimal = rows
        .filter { it.material.tpKat == tpKat }
        .fold(BigDecimal.ZERO) { acc, row -> acc.add(row.calculatedValue) }

    val hasOptionalFields: Boolean
        get() = isProtectedTransport != null || vehicleReg.isNotBlank() || vehicleType.isNotBlank()

    val isSaved: Boolean
        get() = timestamp != null

    val materialsSet: Set<Material>
        get() = rows.map { it.material }.toSet()

    val totalNEMkg: BigDecimal
        get() = rows
            .filter { it.hasNEM() }
            .fold(BigDecimal.ZERO) { acc, row -> acc.add(row.NEMkg) }

    fun weightVolumeStringByTpKat(tpKat: Int, context: Context): String {
        var totalWeight = BigDecimal.ZERO
        var totalVolume = BigDecimal.ZERO

        rows.filter { it.material.tpKat == tpKat }.forEach { row ->
            when {
                row.hasNEM() -> totalWeight = totalWeight.add(row.NEMkg)
                row.isVolume -> totalVolume = totalVolume.add(row.weightVolume)
                else -> totalWeight = totalWeight.add(row.weightVolume)
            }
        }

        val hasWeight = totalWeight.signum() != 0
        val hasVolume = totalVolume.signum() != 0
        if (!hasWeight && !hasVolume) {
            return ""
        }

        return buildString {
            if (hasWeight) {
                append(context.getString(R.string.unit_kg_format, formatValue(totalWeight)))
                if (hasVolume) {
                    append(", ")
                }
            }
            if (hasVolume) {
                append(context.getString(R.string.unit_liter_format, formatValue(totalVolume)))
            }
        }
    }

    @Throws(JSONException::class)
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put(Keys.ID, id.toString())
        JSONUtils.putDateTime(json, Keys.TIMESTAMP, timestamp)
        json.put(Keys.NAME, name)
        json.put(Keys.SENDER, sender)
        json.put(Keys.RECIPIENT, recipient)
        json.put(Keys.AUTHOR, author)
        JSONUtils.putIfTrue(json, Keys.UNSAVED_CHANGES, hasUnsavedChanges)
        JSONUtils.putBooleanOrNull(json, Keys.PROTECTED_TRANSPORT, isProtectedTransport)
        JSONUtils.putIfNotEmpty(json, Keys.VEHICLE_REG, vehicleReg)
        JSONUtils.putIfNotEmpty(json, Keys.VEHICLE_TYPE, vehicleType)
        json.put(Keys.ROWS, JSONArray().apply { rows.forEach { put(it.toJson()) } })
        return json
    }

    object Keys {
        const val AUTHOR: String = "Author"
        const val ID: String = "Id"
        const val NAME: String = "Name"
        const val PROTECTED_TRANSPORT: String = "ProtectedTransport"
        const val RECIPIENT: String = "Recipient"
        const val ROWS: String = "Rows"
        const val SENDER: String = "Sender"
        const val TIMESTAMP: String = "Timestamp"
        const val UNSAVED_CHANGES: String = "UnsavedChanges"
        const val VEHICLE_REG: String = "VehicleReg"
        const val VEHICLE_TYPE: String = "VehicleType"
    }

    companion object {
        @Throws(JSONException::class)
        fun fromJson(json: JSONObject): Document {
            val rowsArray = json.getJSONArray(Keys.ROWS)
            val rows = (0 until rowsArray.length()).map { i ->
                DocumentRow.fromJson(rowsArray.getJSONObject(i))
            }

            return Document(
                id = UUID.fromString(json.getString(Keys.ID)),
                author = JSONUtils.getStringOrNull(json, Keys.AUTHOR),
                hasUnsavedChanges = JSONUtils.optBooleanOrNull(json, Keys.UNSAVED_CHANGES),
                timestamp = JSONUtils.getDateTimeOrNull(json, Keys.TIMESTAMP),
                isProtectedTransport = JSONUtils.optBooleanOrNull(json, Keys.PROTECTED_TRANSPORT),
                name = JSONUtils.getStringOrNull(json, Keys.NAME),
                sender = JSONUtils.getStringOrNull(json, Keys.SENDER),
                recipient = JSONUtils.getStringOrNull(json, Keys.RECIPIENT),
                rows = rows,
                vehicleReg = json.optString(Keys.VEHICLE_REG),
                vehicleType = json.optString(Keys.VEHICLE_TYPE)
            )
        }
    }
}

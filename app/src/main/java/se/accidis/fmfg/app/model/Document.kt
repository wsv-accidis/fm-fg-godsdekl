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
        get() {
            var result = BigDecimal.ZERO
            for (row in rows) {
                result = result.add(row.calculatedValue)
            }
            return result
        }

    fun calculatedValueByTpKat(tpKat: Int): BigDecimal {
        var result = BigDecimal.ZERO
        for (row in rows) {
            if (tpKat == row.material.tpKat) {
                result = result.add(row.calculatedValue)
            }
        }
        return result
    }

    val hasOptionalFields: Boolean =
        null != isProtectedTransport || vehicleReg.isNotBlank() || vehicleType.isNotBlank()

    val isSaved: Boolean
        get() = (null != this.timestamp)

    val materialsSet: Set<Material>
        get() {
            val materials = HashSet<Material>()
            for (row in rows) {
                materials.add(row.material)
            }
            return materials
        }

    val totalNEMkg: BigDecimal
        get() {
            var totalNEM = BigDecimal.ZERO
            for (row in rows) {
                if (row.hasNEM()) {
                    totalNEM = totalNEM.add(row.NEMkg)
                }
            }
            return totalNEM
        }

    fun weightVolumeStringByTpKat(tpKat: Int, context: Context): String {
        var totalWeight = BigDecimal.ZERO
        var totalVolume = BigDecimal.ZERO
        for (row in rows) {
            if (tpKat == row.material.tpKat) {
                if (row.hasNEM()) {
                    totalWeight = totalWeight.add(row.NEMkg)
                } else if (row.isVolume) {
                    totalVolume = totalVolume.add(row.weightVolume)
                } else {
                    totalWeight = totalWeight.add(row.weightVolume)
                }
            }
        }

        val hasWeight = (0.0 != totalWeight.toDouble())
        val hasVolume = (0.0 != totalVolume.toDouble())
        if (!hasWeight && !hasVolume) {
            return ""
        }

        // String will be of the format "1 kg, 2 liter" or either of the two if the other is zero
        val builder = StringBuilder()
        if (hasWeight) {
            builder.append(
                String.format(
                    context.getString(R.string.unit_kg_format),
                    formatValue(totalWeight)
                )
            )
            if (hasVolume) {
                builder.append(", ")
            }
        }
        if (hasVolume) {
            builder.append(
                String.format(
                    context.getString(R.string.unit_liter_format),
                    formatValue(totalVolume)
                )
            )
        }
        return builder.toString()
    }

    @Throws(JSONException::class)
    fun toJson(): JSONObject {
        val rowsArray = JSONArray()
        for (row in rows) {
            rowsArray.put(row.toJson())
        }

        val json = JSONObject()
        json.put(Keys.ID, id.toString())
        JSONUtils.putDateTime(
            json, Keys.TIMESTAMP,
            timestamp
        )
        json.put(Keys.NAME, name)
        json.put(Keys.SENDER, sender)
        json.put(Keys.RECIPIENT, recipient)
        json.put(Keys.AUTHOR, author)
        JSONUtils.putIfTrue(json, Keys.UNSAVED_CHANGES, hasUnsavedChanges)
        JSONUtils.putBooleanOrNull(json, Keys.PROTECTED_TRANSPORT, isProtectedTransport)
        JSONUtils.putIfNotEmpty(
            json, Keys.VEHICLE_REG,
            this.vehicleReg
        )
        JSONUtils.putIfNotEmpty(
            json, Keys.VEHICLE_TYPE,
            this.vehicleType
        )
        json.put(Keys.ROWS, rowsArray)
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
            val rows = ArrayList<DocumentRow>()
            val rowsArray = json.getJSONArray(Keys.ROWS)
            for (i in 0..<rowsArray.length()) {
                val row = DocumentRow.fromJson(rowsArray.getJSONObject(i))
                rows.add(row)
            }

            val document = Document(
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

            return document
        }
    }
}

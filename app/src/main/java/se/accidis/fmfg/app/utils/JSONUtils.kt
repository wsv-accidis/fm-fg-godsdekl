package se.accidis.fmfg.app.utils

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.json.JSONObject

/**
 * Utility methods for working with JSON.
 */
object JSONUtils {
    private const val BOOLEAN_NULL = 0
    private const val BOOLEAN_TRUE = 1

    private val isoDateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis()

    fun optDateTime(json: JSONObject, key: String): DateTime? =
        parseDateTime(json.optString(key))

    fun parseDateTime(str: String?): DateTime? =
        if (!str.isNullOrEmpty()) isoDateTimeFormatter.parseDateTime(str) else null

    fun putDateTime(json: JSONObject, key: String, dateTime: DateTime?) {
        if (null != dateTime) {
            json.put(key, dateTime.toString(isoDateTimeFormatter))
        } else {
            json.put(key, null)
        }
    }

    fun optBoolean(json: JSONObject, key: String?): Boolean {
        val value = json.optInt(key, BOOLEAN_NULL)
        return value == BOOLEAN_TRUE
    }

    fun putBooleanIfTrue(json: JSONObject, key: String, value: Boolean) {
        if (value) {
            json.put(key, BOOLEAN_TRUE)
        }
    }

    fun putOptString(json: JSONObject, key: String, value: String) {
        if (value.isNotEmpty()) {
            json.put(key, value)
        }
    }
}

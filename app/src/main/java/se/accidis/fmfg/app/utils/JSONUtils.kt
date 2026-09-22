package se.accidis.fmfg.app.utils

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.json.JSONObject

/**
 * Utility methods for working with JSON.
 */
object JSONUtils {
    private const val BOOLEAN_FALSE = 2
    private const val BOOLEAN_NULL = 0
    private const val BOOLEAN_TRUE = 1

    fun optDateTime(json: JSONObject, key: String): DateTime? {
        val str = json.optString(key)
        if (!str.isEmpty()) {
            val parser = ISODateTimeFormat.dateTimeNoMillis()
            return parser.parseDateTime(str)
        } else {
            return null
        }
    }

    fun putDateTime(json: JSONObject, key: String, dateTime: DateTime?) {
        if (null != dateTime) {
            val formatter = ISODateTimeFormat.dateTimeNoMillis()
            json.put(key, dateTime.toString(formatter))
        } else {
            json.put(key, null)
        }
    }

    fun optBoolean(json: JSONObject, key: String?): Boolean {
        val value = json.optInt(key, BOOLEAN_NULL)
        return value == BOOLEAN_TRUE
    }

    fun putBoolean(json: JSONObject, key: String, value: Boolean?) {
        if (null != value) {
            val intVal = (if (value) BOOLEAN_TRUE else BOOLEAN_FALSE)
            json.put(key, intVal)
        }
    }

    fun putBooleanIfTrue(json: JSONObject, key: String, value: Boolean) {
        if (value) {
            json.put(key, true)
        }
    }

    fun putOptString(json: JSONObject, key: String, value: String) {
        if (value.isNotEmpty()) {
            json.put(key, value)
        }
    }
}

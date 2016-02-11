package se.accidis.fmfg.app.utils;

import android.text.TextUtils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * Utility methods for working with JSON.
 */
public final class JSONUtils {
	private static final int BOOLEAN_FALSE = 2;
	private static final int BOOLEAN_NULL = 0;
	private static final int BOOLEAN_TRUE = 1;

	private JSONUtils() {
	}

	public static DateTime getDateTimeOrNull(JSONObject json, String key) throws JSONException {
		String str = getStringOrNull(json, key);
		if (!TextUtils.isEmpty(str)) {
			DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();
			return parser.parseDateTime(str);
		} else {
			return null;
		}
	}

	public static String getStringOrNull(JSONObject json, String key) throws JSONException {
		return (json.isNull(key) ? null : json.getString(key));
	}

	public static Boolean optBooleanOrNull(JSONObject json, String key) {
		int value = json.optInt(key, BOOLEAN_NULL);
		switch (value) {
			case BOOLEAN_NULL:
				return null;
			case BOOLEAN_TRUE:
				return true;
			default:
				return false;
		}
	}

	public static void putBooleanOrNull(JSONObject json, String key, Boolean value) throws JSONException {
		if (null != value) {
			int intVal = (value ? BOOLEAN_TRUE : BOOLEAN_FALSE);
			json.put(key, intVal);
		}
	}

	public static void putDateTime(JSONObject json, String key, DateTime dateTime) throws JSONException {
		if (null != dateTime) {
			DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
			json.put(key, dateTime.toString(formatter));
		} else {
			json.put(key, null);
		}
	}

	public static void putIfTrue(JSONObject json, String key, boolean value) throws JSONException {
		if (value) {
			json.put(key, true);
		}
	}

	public static void putIfNotEmpty(JSONObject json, String key, String value) throws JSONException {
		if(!TextUtils.isEmpty(value)) {
			json.put(key, value);
		}
	}
}

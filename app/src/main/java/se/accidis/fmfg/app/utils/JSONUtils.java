package se.accidis.fmfg.app.utils;

import android.text.TextUtils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility methods for working with JSON.
 */
public final class JSONUtils {
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
}

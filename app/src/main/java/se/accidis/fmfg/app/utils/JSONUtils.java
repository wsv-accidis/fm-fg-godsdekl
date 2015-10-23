package se.accidis.fmfg.app.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility methods for working with JSON.
 */
public final class JSONUtils {
    private JSONUtils() {
    }

    public static String getStringOrNull(JSONObject json, String key) throws JSONException {
        return (json.isNull(key) ? null : json.getString(key));
    }
}

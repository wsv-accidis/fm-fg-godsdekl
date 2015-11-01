package se.accidis.fmfg.app.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Model object for document settings.
 */
public final class DocumentSettings {
    private final JSONObject mJson;

    public DocumentSettings(JSONObject json) {
        mJson = (null != json ? json : new JSONObject());
    }

    public boolean getBoolean(String key) {
        return mJson.optBoolean(key);
    }

    public void put(String key, boolean value) {
        try {
            mJson.put(key, value);
        } catch (JSONException ignored) {
        }
    }

    public void remove(String key) {
        mJson.remove(key);
    }

    public JSONObject toJson() {
        return mJson;
    }

    public static class Keys {
        public static final String UNSAVED_CHANGES = "UnsavedChanges";
    }
}

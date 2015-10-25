package se.accidis.fmfg.app.model;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import se.accidis.fmfg.app.utils.JSONUtils;

/**
 * Model object for a document link.
 */
public final class DocumentLink implements Comparable<DocumentLink> {
    private final UUID mId;
    private final String mName;
    private final DateTime mTimestamp;

    public DocumentLink(UUID id, String name, DateTime timestamp) {
        mId = id;
        mName = (null != name ? name : "");
        mTimestamp = timestamp;
    }

    public static DocumentLink fromJson(JSONObject json) throws JSONException {
        UUID id = UUID.fromString(json.getString(Document.Keys.ID));
        String name = JSONUtils.getStringOrNull(json, Document.Keys.NAME);
        DateTime timestamp = JSONUtils.getDateTimeOrNull(json, Document.Keys.TIMESTAMP);
        return new DocumentLink(id, name, timestamp);
    }

    @Override
    public int compareTo(@NonNull DocumentLink another) {
        return mName.compareTo(another.mName);
    }

    public UUID getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public DateTime getTimestamp() {
        return mTimestamp;
    }
}

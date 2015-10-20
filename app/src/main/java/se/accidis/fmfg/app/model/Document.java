package se.accidis.fmfg.app.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import se.accidis.fmfg.app.utils.JSONUtils;

/**
 * Model object for transport documents.
 */
public final class Document {
    private final UUID mId;
    private final List<DocumentRow> mRows = new ArrayList<DocumentRow>();
    private String mRecipient;
    private String mSender;

    public Document() {
        this(UUID.randomUUID());
    }

    private Document(UUID id) {
        mId = id;
    }

    public static Document fromJson(JSONObject json) throws JSONException {
        UUID id = UUID.fromString(json.getString(Keys.ID));
        Document document = new Document(id);
        document.mRecipient = JSONUtils.getStringOrNull(json, Keys.RECIPIENT);
        document.mSender = JSONUtils.getStringOrNull(json, Keys.SENDER);

        JSONArray rowsArray = json.getJSONArray(Keys.ROWS);
        for (int i = 0; i < rowsArray.length(); i++) {
            DocumentRow row = DocumentRow.fromJson(rowsArray.getJSONObject(i));
            document.mRows.add(row);
        }

        return document;
    }

    public void addOrUpdateRow(DocumentRow row) {
        DocumentRow existing = getRowByMaterial(row.getMaterial());
        if (null != existing) {
            row.copyTo(existing);
        } else {
            mRows.add(row);
        }
    }

    public BigDecimal getCalculatedTotalValue() {
        BigDecimal result = BigDecimal.ZERO;
        for (DocumentRow row : mRows) {
            result = result.add(row.getCalculatedValue());
        }
        return result;
    }

    public UUID getId() {
        return mId;
    }

    public String getRecipient() {
        return mRecipient;
    }

    public void setRecipient(String recipient) {
        mRecipient = recipient;
    }

    public DocumentRow getRowByMaterial(Material material) {
        for (DocumentRow row : mRows) {
            if (row.getMaterial().equals(material)) {
                return row;
            }
        }
        return null;
    }

    public List<DocumentRow> getRows() {
        return Collections.unmodifiableList(mRows);
    }

    public String getSender() {
        return mSender;
    }

    public void setSender(String sender) {
        mSender = sender;
    }

    public void removeRowByMaterial(Material material) {
        DocumentRow row = getRowByMaterial(material);
        if (null != row) {
            mRows.remove(row);
        }
    }

    public JSONObject toJson() throws JSONException {
        JSONArray rowsArray = new JSONArray();
        for (DocumentRow row : mRows) {
            rowsArray.put(row.toJson());
        }

        JSONObject json = new JSONObject();
        json.put(Keys.ID, mId.toString());
        json.put(Keys.ROWS, rowsArray);
        return json;
    }

    public static class Keys {
        public static final String ID = "Id";
        public static final String RECIPIENT = "Recipient";
        public static final String ROWS = "Rows";
        public static final String SENDER = "Sender";

        private Keys() {
        }
    }
}

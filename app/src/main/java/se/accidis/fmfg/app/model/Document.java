package se.accidis.fmfg.app.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model object for transport documents.
 */
public final class Document {
    private final List<DocumentRow> mRows = new ArrayList<DocumentRow>();

    public static Document fromJson(JSONObject json) throws JSONException {
        Document document = new Document();

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

    public JSONObject toJson() throws JSONException {
        JSONArray rowsArray = new JSONArray();
        for (DocumentRow row : mRows) {
            rowsArray.put(row.toJson());
        }

        JSONObject json = new JSONObject();
        json.put(Keys.ROWS, rowsArray);
        return json;
    }

    public static class Keys {
        public static final String ROWS = "Rows";

        private Keys() {
        }
    }
}

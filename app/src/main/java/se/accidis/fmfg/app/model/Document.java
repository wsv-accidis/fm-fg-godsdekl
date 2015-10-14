package se.accidis.fmfg.app.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model object for transport documents.
 */
public final class Document {
    private final List<DocumentRow> mRows = new ArrayList<DocumentRow>();

    public List<DocumentRow> getRows() {
        return Collections.unmodifiableList(mRows);
    }

    public void addRow(DocumentRow row) {
        mRows.add(row);
    }

    // TOOD Sorting
}

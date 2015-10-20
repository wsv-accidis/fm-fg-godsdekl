package se.accidis.fmfg.app.ui.documents;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.List;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Document;
import se.accidis.fmfg.app.model.DocumentRow;
import se.accidis.fmfg.app.ui.materials.ValueHelper;

/**
 * Adapter for the document list view. The list contains both the document rows and additional information.
 */
public final class DocumentAdapter extends BaseAdapter {
    private static final int VIEW_TYPE_ROW = 0;
    private final LayoutInflater mInflater;
    private final Context mContext;
    private final Document mDocument;
    private final List<DocumentRow> mRows;

    public DocumentAdapter(Context context, Document document) {
        mContext = context;
        mDocument = document;
        mRows = document.getRows();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mRows.size();
    }

    @Override
    public int getViewTypeCount() {
        return 1; // TODO
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_ROW; // TODO
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_ROW:
                return getRowView(position, convertView, parent);
        }

        return null;
    }

    private View getRowView(int rowIndex, View convertView, ViewGroup parent) {
        View view;
        if (null == convertView) {
            view = mInflater.inflate(R.layout.list_item_document_row, parent, false);
        } else {
            view = convertView;
        }

        DocumentRow row = mRows.get(rowIndex);

        TextView fullTextText = (TextView) view.findViewById(R.id.document_row_text);
        fullTextText.setText(row.getMaterial().getFullText());

        TextView fbenText = (TextView) view.findViewById(R.id.document_row_fben);
        if (!TextUtils.isEmpty(row.getMaterial().getFben())) {
            fbenText.setText(row.getMaterial().getFben());
            fbenText.setVisibility(View.VISIBLE);
        } else {
            fbenText.setVisibility(View.GONE);
        }

        TextView nemText = (TextView) view.findViewById(R.id.document_row_nem);
        if (0 != row.getMaterial().getNEMmg()) {
            BigDecimal nemValue = row.getAmount().multiply(row.getMaterial().getNEMkg());
            nemText.setText(String.format(mContext.getString(R.string.document_nem_format), ValueHelper.formatValue(nemValue)));
            nemText.setVisibility(View.VISIBLE);
        } else {
            nemText.setVisibility(View.GONE);
        }

        TextView packagesText = (TextView) view.findViewById(R.id.document_row_packages);
        packagesText.setText(row.getPackagesText(mContext));

        TextView weightVolumeText = (TextView) view.findViewById(R.id.document_row_weightvolume);
        weightVolumeText.setText(row.getWeightVolumeText(mContext));

        return view;
    }
}

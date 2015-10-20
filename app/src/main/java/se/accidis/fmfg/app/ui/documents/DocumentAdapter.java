package se.accidis.fmfg.app.ui.documents;

import android.content.Context;
import android.support.v4.content.ContextCompat;
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
    private static final int ROW_BOTTOM_OFFSET = 2;
    private static final int ROW_TOP_OFFSET = 3;
    private static final int VIEW_TYPE_ADDRESS = 1;
    private static final int VIEW_TYPE_INFO = 2;
    private static final int VIEW_TYPE_ROW = 0;
    private static final int VIEW_TYPE_SEPARATOR = 3;
    private final Context mContext;
    private final Document mDocument;
    private final LayoutInflater mInflater;
    private final List<DocumentRow> mRows;

    public DocumentAdapter(Context context, Document document) {
        mContext = context;
        mDocument = document;
        mRows = document.getRows();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public int getCount() {
        return ROW_TOP_OFFSET + mRows.size() + ROW_BOTTOM_OFFSET;
    }

    @Override
    public Object getItem(int position) {
        if (VIEW_TYPE_ROW == getItemViewType(position)) {
            return mRows.get(position - ROW_TOP_OFFSET);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == ROW_TOP_OFFSET - 1 || position == ROW_TOP_OFFSET + mRows.size()) {
            // There is a separator between the addresses and the list, and another between the list and the summary
            return VIEW_TYPE_SEPARATOR;
        } else if (position < ROW_TOP_OFFSET) {
            // Above the list are addresses
            return VIEW_TYPE_ADDRESS;
        } else if (position >= ROW_TOP_OFFSET + mRows.size()) {
            // Below the list is the summary
            return VIEW_TYPE_INFO;
        } else {
            // Everything else is the list
            return VIEW_TYPE_ROW;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_ADDRESS:
                return getAddressView(position, convertView, parent);
            case VIEW_TYPE_INFO:
                return getInfoView(convertView, parent);
            case VIEW_TYPE_ROW:
                return getRowView(position - ROW_TOP_OFFSET, convertView, parent);
            case VIEW_TYPE_SEPARATOR:
                return getSeparatorView(convertView, parent);
        }

        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public boolean isEnabled(int position) {
        int type = getItemViewType(position);
        return (VIEW_TYPE_INFO != type && VIEW_TYPE_SEPARATOR != type);
    }

    private View getAddressView(int position, View convertView, ViewGroup parent) {
        View view;
        if (null == convertView) {
            view = mInflater.inflate(R.layout.list_item_document_address, parent, false);
        } else {
            view = convertView;
        }

        boolean isSender = (0 == position);

        TextView headingText = (TextView) view.findViewById(R.id.document_address_heading);
        headingText.setText(isSender ? R.string.document_sender : R.string.document_recipient);

        TextView addressText = (TextView) view.findViewById(R.id.document_address_text);
        String text = (isSender ? mDocument.getSender() : mDocument.getRecipient());

        if (TextUtils.isEmpty(text)) {
            addressText.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.ic_create), null, null, null);
            addressText.setText(R.string.document_tap_to_edit);
        } else {
            addressText.setCompoundDrawables(null, null, null, null);
            addressText.setText(text);
        }

        return view;
    }

    private View getInfoView(View convertView, ViewGroup parent) {
        View view;
        if (null == convertView) {
            view = mInflater.inflate(R.layout.list_item_document_info, parent, false);
        } else {
            view = convertView;
        }

        return view;
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
            // TODO Toggle showing Fbet here as well
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

    private View getSeparatorView(View convertView, ViewGroup parent) {
        return (null != convertView ? convertView : mInflater.inflate(R.layout.list_item_document_separator, parent, false));
    }
}
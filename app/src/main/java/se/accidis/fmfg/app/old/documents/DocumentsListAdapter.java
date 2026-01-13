package se.accidis.fmfg.app.old.documents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.DocumentLink;

/**
 * List adapter for the documents list.
 */
public final class DocumentsListAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final List<DocumentLink> mList;
    private static final DateTimeFormatter mFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    public DocumentsListAdapter(Context context, List<DocumentLink> list) {
        mList = list;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (null == convertView) {
            view = mInflater.inflate(R.layout.list_item_document_link, parent, false);
        } else {
            view = convertView;
        }

        DocumentLink doc = mList.get(position);

        TextView nameText = (TextView) view.findViewById(R.id.document_name);
        nameText.setText(doc.getName());

        TextView timeText = (TextView) view.findViewById(R.id.document_timestamp);
        if (null != doc.getTimestamp()) {
            timeText.setText(doc.getTimestamp().toString(mFormatter));
        } else {
            timeText.setText(R.string.document_no_data);
        }

        return view;
    }
}

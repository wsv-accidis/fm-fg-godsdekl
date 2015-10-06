package se.accidis.fmfg.app.ui.materials;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import se.accidis.fmfg.app.model.Material;

/**
 * List adapter for the materials list.
 */
public final class MaterialsListAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final List<Material> mList;

    public MaterialsListAdapter(Context context, List<Material> list) {
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
        if (convertView == null) {
            view = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        } else {
            view = convertView;
        }

        Material material = mList.get(position);

        TextView text = (TextView) view.findViewById(android.R.id.text1);
        text.setText(material.getNamn());

        return view;
    }
}

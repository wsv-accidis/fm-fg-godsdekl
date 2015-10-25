package se.accidis.fmfg.app.ui.materials;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Material;

/**
 * List adapter for the materials list.
 */
public final class MaterialsListAdapter extends BaseAdapter implements Filterable {
    private final LayoutInflater mInflater;
    private final List<Material> mList;
    private Filter mFilter = new MaterialsListFilter();
    private List<Material> mFilteredList;
    private Set<Material> mLoadedMaterials;

    public MaterialsListAdapter(Context context, List<Material> list, Set<Material> loadedMaterials) {
        mList = list;
        mFilteredList = list;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLoadedMaterials = loadedMaterials;
    }

    @Override
    public int getCount() {
        return mFilteredList.size();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public Object getItem(int position) {
        return mFilteredList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (null == convertView) {
            view = mInflater.inflate(R.layout.list_item_material, parent, false);
        } else {
            view = convertView;
        }

        Material material = mFilteredList.get(position);

        TextView fbenText = (TextView) view.findViewById(R.id.material_fben);
        fbenText.setText(material.getFben());

        if (mLoadedMaterials.contains(material)) {
            fbenText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle_small, 0, 0, 0);
        } else {
            fbenText.setCompoundDrawables(null, null, null, null);
        }

        TextView textText = (TextView) view.findViewById(R.id.material_text);
        textText.setText(material.getFullText());

        return view;
    }

    private final class MaterialsListFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Material> filteredList;

            if (TextUtils.isEmpty(constraint)) {
                filteredList = mList;
            } else {
                filteredList = new ArrayList<>();
                for (Material m : mList) {
                    if (m.matches(constraint)) {
                        filteredList.add(m);
                    }
                }
            }

            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            mFilteredList = (List<Material>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}

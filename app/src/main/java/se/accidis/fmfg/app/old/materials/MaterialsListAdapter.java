package se.accidis.fmfg.app.old.materials;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Material;
import se.accidis.fmfg.app.services.MaterialsRepository;

/**
 * List adapter for the materials list.
 */
public final class MaterialsListAdapter extends BaseAdapter implements Filterable {
	private final MaterialsComparator mComparator;
	private final LayoutInflater mInflater;
	private final List<Material> mList;
	private final MaterialsRepository mRepository;
	private Filter mFilter = new MaterialsListFilter();
	private List<Material> mFilteredList;
	private Set<Material> mLoadedMaterials;

	public MaterialsListAdapter(Context context, List<Material> list, Set<Material> loadedMaterials) {
		mList = list;
		mFilteredList = list;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLoadedMaterials = loadedMaterials;
		mRepository = MaterialsRepository.getInstance(context);
		mComparator = new MaterialsComparator(mRepository);
	}

	@Override
	public int getCount() {
		return null != mFilteredList ? mFilteredList.size() : 0;
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

		boolean isFavorite = mRepository.isFavoriteMaterial(material);
		boolean isLoaded = mLoadedMaterials.contains(material);

		if (isFavorite && isLoaded) {
			fbenText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_check_circle_small, 0, 0, 0);
		} else if (isFavorite) {
			fbenText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_circle_small, 0, 0, 0);
		} else if (isLoaded) {
			fbenText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle_small, 0, 0, 0);
		} else {
			fbenText.setCompoundDrawables(null, null, null, null);
		}

		TextView textText = (TextView) view.findViewById(R.id.material_text);
		textText.setText(material.getFullText());

		return view;
	}

	public int indexOf(Material material) {
		return mFilteredList.indexOf(material);
	}

	public void setLoadedMaterials(Set<Material> loadedMaterials) {
		mLoadedMaterials = loadedMaterials;
		notifyDataSetChanged();
	}

	private static class MaterialsComparator implements Comparator<Material> {
		private final MaterialsRepository mRepository;

		public MaterialsComparator(MaterialsRepository repository) {
			mRepository = repository;
		}

		@Override
		public int compare(Material lhs, Material rhs) {
			boolean leftFav = mRepository.isFavoriteMaterial(lhs);
			boolean rightFav = mRepository.isFavoriteMaterial(rhs);

			if (leftFav && !rightFav) {
				return -1;
			} else if (rightFav && !leftFav) {
				return 1;
			}

			return lhs.getFben().compareTo(rhs.getFben());
		}
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

			Collections.sort(filteredList, mComparator);
			results.values = filteredList;
			results.count = filteredList.size();
			return results;
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void publishResults(CharSequence constraint, FilterResults results) {
			mFilteredList = (List<Material>) results.values;
			if(null == mFilteredList) {
				// For some reason it seems we can get a null result
				mFilteredList = new ArrayList<>();
			}

			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
}

package se.accidis.fmfg.app.ui.materials;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import java.util.List;
import java.util.Set;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Document;
import se.accidis.fmfg.app.model.Material;
import se.accidis.fmfg.app.services.DocumentsRepository;
import se.accidis.fmfg.app.services.MaterialsRepository;
import se.accidis.fmfg.app.ui.MainActivity;
import se.accidis.fmfg.app.utils.AndroidUtils;

/**
 * Fragment showing the list of materials.
 */
public final class MaterialsListFragment extends ListFragment implements MainActivity.HasNavigationItem {
	private static final int INTERNAL_LIST_CONTAINER_ID = 0x00ff0003; // from android.support.v4.app.ListFragment
	private static final String STATE_LIST_VIEW = "materialsListViewState";
	private static final String STATE_SEARCH_QUERY = "materialsSearchQueryState";
	private static final String TAG = MaterialsListFragment.class.getSimpleName();
	private final Handler mHandler = new Handler();
	private ImageButton mClearSearchButton;
	private boolean mIsLoaded;
	private MaterialsListAdapter mListAdapter;
	private Parcelable mListState;
	private List<Material> mMaterialsList;
	private MaterialsRepository mRepository;
	private String mSearchQuery;
	private EditText mSearchText;

	@Override
	public int getItemId() {
		return R.id.nav_materials;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
		if (savedInstanceState != null) {
			mListState = savedInstanceState.getParcelable(STATE_LIST_VIEW);
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (null == mRepository) {
			mRepository = MaterialsRepository.getInstance(context);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		if (info.position < mListAdapter.getCount()) {
			Material material = (Material) mListAdapter.getItem(info.position);

			switch (item.getItemId()) {
				case R.id.material_menu_favorite:
					toggleFavorite(material);
					return true;
				case R.id.material_menu_load:
					MaterialsLoadDialogFragment dialog = new MaterialsLoadDialogFragment();
					dialog.setArguments(material.toBundle());
					dialog.setDialogListener(new MaterialsLoadDialogListener());
					dialog.show(getFragmentManager(), MaterialsLoadDialogFragment.class.getSimpleName());
					return true;
			}
		}
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.materials_list, menu);

		MenuItem favoriteItem = menu.findItem(R.id.material_menu_favorite);
		if (null != favoriteItem) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			Material material = (Material) mListAdapter.getItem(info.position);
			if (mRepository.isFavoriteMaterial(material)) {
				favoriteItem.setTitle(R.string.material_favorite_remove);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup root = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
		AndroidUtils.assertIsTrue(null != root, "Root view is null.");

		// Extract the internal list container from the root view
		@SuppressWarnings("ResourceType")
		View listContainer = root.findViewById(INTERNAL_LIST_CONTAINER_ID);
		root.removeView(listContainer);

		// Put the internal list container inside our custom container
		View outerContainer = inflater.inflate(R.layout.fragment_materials_list, root, false);
		FrameLayout innerContainer = (FrameLayout) outerContainer.findViewById(R.id.materials_list_container);
		innerContainer.addView(listContainer);

		// Put the custom container inside the root
		root.addView(outerContainer, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

		mSearchText = (EditText) outerContainer.findViewById(R.id.materials_search);
		mSearchText.addTextChangedListener(new SearchChangedListener());
		mClearSearchButton = (ImageButton) outerContainer.findViewById(R.id.materials_search_clear);
		mClearSearchButton.setOnClickListener(new ClearSearchClickedListener());

		if (null != savedInstanceState) {
			mSearchQuery = savedInstanceState.getString(STATE_SEARCH_QUERY);
		}

		return root;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (null == mListAdapter || position < 0 || position >= mListAdapter.getCount()) {
			return;
		}

		Material material = (Material) mListAdapter.getItem(position);
		MaterialsInfoFragment fragment = MaterialsInfoFragment.createInstance(material);

		Activity activity = getActivity();
		if (activity instanceof MainActivity) {
			AndroidUtils.hideSoftKeyboard(getContext(), getView());
			saveInstanceState();
			((MainActivity) activity).openFragment(fragment);
		} else {
			Log.e(TAG, "Activity holding fragment is not MainActivity!");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		setEmptyText(getString(R.string.materials_list_empty));

		mSearchText.setEnabled(false);
		mClearSearchButton.setEnabled(false);

		if (!mIsLoaded) {
			mRepository.setOnLoadedListener(new MaterialsLoadedListener());
			mRepository.beginLoad();
		} else {
			initializeList();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (null != getView()) {
			mListState = getListView().onSaveInstanceState();
			outState.putParcelable(STATE_LIST_VIEW, mListState);
		}
		if (null != mSearchQuery) {
			outState.putString(STATE_SEARCH_QUERY, mSearchQuery);
		}
	}

	private Set<Material> getLoadedMaterials() {
		Document currentDocument = DocumentsRepository.getInstance(getContext()).getCurrentDocument();
		return currentDocument.getMaterialsSet();
	}

	private void initializeList() {
		mSearchText.setEnabled(true);
		mClearSearchButton.setEnabled(true);

		mListAdapter = new MaterialsListAdapter(getContext(), mMaterialsList, getLoadedMaterials());
		setListAdapter(mListAdapter);

		if (null != mListState) {
			getListView().onRestoreInstanceState(mListState);
			mListState = null;
		}

		mListAdapter.getFilter().filter(mSearchQuery);
	}

	private void saveInstanceState() {
		mListState = getListView().onSaveInstanceState();
	}

	private void toggleFavorite(final Material material) {
		Filter.FilterListener filterListener = null;
		if (mRepository.isFavoriteMaterial(material)) {
			mRepository.removeFavoriteMaterial(material);
		} else {
			mRepository.addFavoriteMaterial(material);

			// This listener will scroll to the new position of the favorite, so it doesn't disappear from the user
			filterListener = new Filter.FilterListener() {
				@Override
				public void onFilterComplete(int count) {
					int position = mListAdapter.indexOf(material);
					ListView listView = getListView();
					if (null != listView && -1 != position) {
						getListView().smoothScrollToPosition(position);
					}
				}
			};
		}

		mListAdapter.getFilter().filter(mSearchQuery, filterListener);
	}

	private final class ClearSearchClickedListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			mSearchText.setText("");
		}
	}

	private final class MaterialsLoadDialogListener implements MaterialsLoadDialogFragment.MaterialsLoadDialogListener {
		@Override
		public void onDismiss() {
			mListAdapter.setLoadedMaterials(getLoadedMaterials());
		}
	}

	private final class MaterialsLoadedListener implements MaterialsRepository.OnLoadedListener {
		@Override
		public void onException(Exception ex) {
			Toast toast = Toast.makeText(getContext(), R.string.generic_unexpected_error, Toast.LENGTH_LONG);
			toast.show();
		}

		@Override
		public void onLoaded(final List<Material> list) {
			mMaterialsList = list;
			mIsLoaded = true;

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (null != getActivity()) {
						initializeList();
					}
				}
			});
		}
	}

	private final class SearchChangedListener implements TextWatcher {
		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			mSearchQuery = s.toString().toLowerCase();
			if (null != mListAdapter) {
				mListAdapter.getFilter().filter(mSearchQuery);
			}
		}
	}
}

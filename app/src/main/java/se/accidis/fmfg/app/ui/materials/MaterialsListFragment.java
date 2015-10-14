package se.accidis.fmfg.app.ui.materials;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Material;
import se.accidis.fmfg.app.services.MaterialsRepository;
import se.accidis.fmfg.app.ui.MainActivity;

/**
 * Fragment showing the list of materials.
 */
public final class MaterialsListFragment extends ListFragment {
    private static final String TAG = MaterialsListFragment.class.getSimpleName();
    private static final int INTERNAL_LIST_CONTAINER_ID = 0x00ff0003; // from android.support.v4.app.ListFragment
    private static final String STATE_SEARCH_QUERY = "materialsSearchQueryState";
    private static final String STATE_LIST_VIEW = "materialsListViewState";
    private final Handler mHandler = new Handler();
    private MaterialsRepository mMaterialsRepository;
    private EditText mSearchText;
    private ImageButton mClearSearchButton;
    private MaterialsListAdapter mListAdapter;
    private String mSearchQuery;
    private Parcelable mListState;
    private boolean mIsLoaded;
    private List<Material> mMaterialsList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(STATE_LIST_VIEW);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        assert (root != null);

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
            saveInstanceState();
            MainActivity mainActivity = (MainActivity) activity;
            mainActivity.openFragment(fragment);
        } else {
            Log.e(TAG, "Activity holding fragment is not MainActivity!");
        }
    }

    private void saveInstanceState() {
        mListState = getListView().onSaveInstanceState();
    }

    @Override
    public void onResume() {
        super.onResume();
        setEmptyText(getString(R.string.materials_list_empty));

        if (null == mMaterialsRepository) {
            mMaterialsRepository = MaterialsRepository.getInstance(getContext());
        }

        mSearchText.setEnabled(false);
        mClearSearchButton.setEnabled(false);

        if (!mIsLoaded) {
            mMaterialsRepository.setOnLoadedListener(new MaterialsLoadedListener());
            mMaterialsRepository.beginLoad();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != getView()) {
            outState.putParcelable(STATE_LIST_VIEW, getListView().onSaveInstanceState());
        }
        if (null != mSearchQuery) {
            outState.putString(STATE_SEARCH_QUERY, mSearchQuery);
        }
    }

    private final class SearchChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mSearchQuery = s.toString();
            if (null != mListAdapter) {
                mListAdapter.getFilter().filter(mSearchQuery.toLowerCase());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private final class ClearSearchClickedListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mSearchText.setText("");
        }
    }

    private final class MaterialsLoadedListener implements MaterialsRepository.OnLoadedListener {
        @Override
        public void onException(Exception ex) {
            Toast toast = Toast.makeText(getContext(), R.string.generic_load_error, Toast.LENGTH_LONG);
            toast.show();
        }

        @Override
        public void onLoaded(final List<Material> list) {
            mMaterialsList = list;
            mIsLoaded = true;

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (null != getActivity() && isVisible()) {
                        initializeList();
                    }
                }
            });
        }
    }

    private void initializeList() {
        mSearchText.setEnabled(true);
        mClearSearchButton.setEnabled(true);

        mIsLoaded = true;
        mListAdapter = new MaterialsListAdapter(getContext(), mMaterialsList);
        setListAdapter(mListAdapter);

        if (null != mListState) {
            getListView().onRestoreInstanceState(mListState);
            mListState = null;
        }

        if (!TextUtils.isEmpty(mSearchQuery)) {
            mListAdapter.getFilter().filter(mSearchQuery);
        }
    }
}

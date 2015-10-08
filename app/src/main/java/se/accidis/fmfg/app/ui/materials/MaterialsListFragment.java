package se.accidis.fmfg.app.ui.materials;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.List;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Material;
import se.accidis.fmfg.app.services.MaterialsRepository;

/**
 * Fragment showing the list of materials.
 */
public class MaterialsListFragment extends ListFragment {
    private static final int INTERNAL_LIST_CONTAINER_ID = 0x00ff0003; // from android.support.v4.app.ListFragment
    private final Handler mHandler = new Handler();
    private MaterialsRepository mMaterialsRepository;
    private EditText mSearchText;
    private ImageButton mClearSearchButton;
    private MaterialsListAdapter mListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        return root;
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

        mMaterialsRepository.setOnLoadedListener(new MaterialsLoadedListener());
        mMaterialsRepository.beginLoad();
    }

    private final class SearchChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (null != mListAdapter) {
                mListAdapter.getFilter().filter(s.toString().toLowerCase());
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
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSearchText.setEnabled(true);
                    mClearSearchButton.setEnabled(true);

                    mListAdapter = new MaterialsListAdapter(getContext(), list);
                    setListAdapter(mListAdapter);
                }
            });
        }
    }
}

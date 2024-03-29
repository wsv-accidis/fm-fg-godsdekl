package se.accidis.fmfg.app.ui.documents;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import java.util.List;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.DocumentLink;
import se.accidis.fmfg.app.services.DocumentsRepository;
import se.accidis.fmfg.app.ui.MainActivity;
import se.accidis.fmfg.app.utils.AndroidUtils;

/**
 * Fragment showing the list of saved documents.
 */
public final class DocumentsListFragment extends ListFragment implements MainActivity.HasNavigationItem {
    private static final String STATE_LIST_VIEW = "documentListViewState";
    private static final String TAG = DocumentsListFragment.class.getSimpleName();
    private final Handler mHandler = new Handler();
    private List<DocumentLink> mDocumentsList;
    private DocumentsListAdapter mListAdapter;
    private boolean mIsLoaded;
    private Parcelable mListState;

    @Override
    public int getItemId() {
        return R.id.nav_documents_list;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(STATE_LIST_VIEW);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setEmptyText(getString(R.string.documents_list_empty));
        AndroidUtils.hideSoftKeyboard(getContext(), getView());

        DocumentsRepository repository = DocumentsRepository.getInstance(getContext());
        if (!mIsLoaded || !repository.isLoaded()) {
            repository.setOnLoadedListener(new DocumentsLoadedListener());
            repository.beginLoad();
        } else {
            initializeList();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != getView()) {
            outState.putParcelable(STATE_LIST_VIEW, getListView().onSaveInstanceState());
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (null == mListAdapter || position < 0 || position >= mListAdapter.getCount()) {
            return;
        }

        DocumentLink docLink = (DocumentLink) mListAdapter.getItem(position);
        DocumentFragment fragment = DocumentFragment.createInstance(docLink);

        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            saveInstanceState();
            ((MainActivity) activity).openFragment(fragment);
        } else {
            Log.e(TAG, "Activity holding fragment is not MainActivity!");
        }
    }

    private void initializeList() {
        mListAdapter = new DocumentsListAdapter(getContext(), mDocumentsList);
        setListAdapter(mListAdapter);

        if (null != mListState) {
            getListView().onRestoreInstanceState(mListState);
            mListState = null;
        }
    }

    private void saveInstanceState() {
        mListState = getListView().onSaveInstanceState();
    }

    private final class DocumentsLoadedListener implements DocumentsRepository.OnLoadedListener {
        @Override
        public void onException(Exception ex) {
            Toast toast = Toast.makeText(getContext(), R.string.generic_unexpected_error, Toast.LENGTH_LONG);
            toast.show();
        }

        @Override
        public void onLoaded(List<DocumentLink> list) {
            mDocumentsList = list;
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
}

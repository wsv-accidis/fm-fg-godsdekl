package se.accidis.fmfg.app.ui.documents;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.DocumentLink;
import se.accidis.fmfg.app.services.DocumentsRepository;
import se.accidis.fmfg.app.ui.MainActivity;
import se.accidis.fmfg.app.ui.NavigationItem;
import se.accidis.fmfg.app.utils.AndroidUtils;

/**
 * Fragment showing the list of saved documents.
 */
public final class DocumentsListFragment extends ListFragment implements MainActivity.HasNavigationItem {
    private static final String STATE_LIST_VIEW = "documentListViewState";
    private final Handler mHandler = new Handler();
    private List<DocumentLink> mDocumentsList;
    private DocumentsListAdapter mListAdapter;
    private boolean mIsLoaded;
    private Parcelable mListState;

    @Override
    public NavigationItem getItem() {
        return NavigationItem.DOCUMENTS_ITEM;
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

        if (!mIsLoaded) {
            DocumentsRepository repository = DocumentsRepository.getInstance(getContext());
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
        // TODO Open document
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
            Toast toast = Toast.makeText(getContext(), R.string.generic_load_error, Toast.LENGTH_LONG);
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

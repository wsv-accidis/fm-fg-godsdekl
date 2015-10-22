package se.accidis.fmfg.app.ui.documents;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Document;
import se.accidis.fmfg.app.services.DocumentsRepository;
import se.accidis.fmfg.app.ui.MainActivity;
import se.accidis.fmfg.app.utils.AndroidUtils;

/**
 * Fragment for viewing/editing a document.
 */
public final class DocumentFragment extends ListFragment implements MainActivity.HasMenu {
    private static final String STATE_LIST_VIEW = "documentListViewState";
    private DocumentAdapter mAdapter;
    private View mButtonBar;
    private Document mDocument;
    private Parcelable mListState;
    private DocumentsRepository mRepository;

    @Override
    public int getMenu() {
        return R.menu.document;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(STATE_LIST_VIEW);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_document, container, false);

        mButtonBar = view.findViewById(R.id.document_button_bar);

        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (null == mAdapter || position < 0 || position >= mAdapter.getCount() || !isCurrentDocument()) {
            return;
        }

        if (DocumentAdapter.SENDER_POSITION == position || DocumentAdapter.RECIPIENT_POSITION == position) {
            Bundle args = new Bundle();
            boolean isSender = (DocumentAdapter.SENDER_POSITION == position);
            args.putBoolean(AddressDialogFragment.ARG_IS_SENDER, isSender);
            args.putString(AddressDialogFragment.ARG_CURRENT_ADDRESS, (isSender ? mDocument.getSender() : mDocument.getRecipient()));

            AddressDialogFragment dialog = new AddressDialogFragment();
            dialog.setArguments(args);
            dialog.setOnDismissListener(new AddressDialogDismissListener(isSender));
            dialog.show(getFragmentManager(), AddressDialogFragment.class.getSimpleName());
        }
    }

    @Override
    public boolean onMenuItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (null == mRepository) {
            mRepository = DocumentsRepository.getInstance(getContext());
        }

        mDocument = mRepository.getCurrentDocument();
        // TODO Or open saved document depending on args

        mButtonBar.setVisibility(isCurrentDocument() ? View.VISIBLE : View.GONE);

        AndroidUtils.hideSoftKeyboard(getContext(), getView());
        initializeList();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != getView()) {
            outState.putParcelable(STATE_LIST_VIEW, getListView().onSaveInstanceState());
        }
    }

    private void initializeList() {
        mAdapter = new DocumentAdapter(getContext(), mDocument, isCurrentDocument());
        setListAdapter(mAdapter);

        if (null != mListState) {
            getListView().onRestoreInstanceState(mListState);
            mListState = null;
        }
    }

    private boolean isCurrentDocument() {
        return mDocument.getId().equals(mRepository.getCurrentDocument().getId());
    }

    private void saveInstanceState() {
        mListState = getListView().onSaveInstanceState();
    }

    private final class AddressDialogDismissListener implements AddressDialogFragment.OnDismissListener {
        private final boolean mIsSender;

        public AddressDialogDismissListener(boolean isSender) {
            mIsSender = isSender;
        }

        @Override
        public void onDismiss(DialogInterface dialog, String address) {
            if (null == address) {
                return;
            }

            if (mIsSender) {
                mDocument.setSender(address);
            } else {
                mDocument.setRecipient(address);
            }

            mRepository.commit();
            mAdapter.notifyDataSetChanged();
        }
    }
}

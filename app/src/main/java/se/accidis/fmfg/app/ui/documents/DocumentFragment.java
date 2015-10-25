package se.accidis.fmfg.app.ui.documents;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Document;
import se.accidis.fmfg.app.model.DocumentRow;
import se.accidis.fmfg.app.services.DocumentsRepository;
import se.accidis.fmfg.app.ui.MainActivity;
import se.accidis.fmfg.app.ui.NavigationItem;
import se.accidis.fmfg.app.ui.materials.MaterialsInfoFragment;
import se.accidis.fmfg.app.utils.AndroidUtils;

/**
 * Fragment for viewing/editing a document.
 */
public final class DocumentFragment extends ListFragment implements MainActivity.HasNavigationItem, MainActivity.HasMenu, MainActivity.HasTitle {
    private static final String STATE_LIST_VIEW = "documentListViewState";
    private static final String TAG = DocumentFragment.class.getSimpleName();
    private DocumentAdapter mAdapter;
    private View mButtonBar;
    private Document mDocument;
    private Parcelable mListState;
    private DocumentsRepository mRepository;

    @Override
    public NavigationItem getItem() {
        return NavigationItem.CURRENT_DOCUMENT_ITEM;
    }

    @Override
    public int getMenu() {
        return R.menu.document;
    }

    @Override
    public String getTitle(Context context) {
        return (null != mDocument && mDocument.isSaved()) ? mDocument.getName() : context.getString(R.string.app_name);
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

        Button saveButton = (Button) view.findViewById(R.id.document_button_save);
        saveButton.setOnClickListener(new SaveDialogListener());

        Button clearButton = (Button) view.findViewById(R.id.document_button_clear);
        clearButton.setOnClickListener(new ClearDialogListener());

        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (null == mAdapter || position < 0 || position >= mAdapter.getCount() || !isCurrentDocument()) {
            return;
        }

        int type = mAdapter.getItemViewType(position);

        if (DocumentAdapter.VIEW_TYPE_ADDRESS == type) {
            Bundle args = new Bundle();
            boolean isSender = (DocumentAdapter.SENDER_POSITION == position);
            args.putBoolean(AddressDialogFragment.ARG_IS_SENDER, isSender);
            args.putString(AddressDialogFragment.ARG_CURRENT_ADDRESS, (isSender ? mDocument.getSender() : mDocument.getRecipient()));

            AddressDialogFragment dialog = new AddressDialogFragment();
            dialog.setArguments(args);
            dialog.setDialogListener(new AddressDialogListener(isSender));
            dialog.show(getFragmentManager(), AddressDialogFragment.class.getSimpleName());

        } else if (DocumentAdapter.VIEW_TYPE_ROW == type) {
            DocumentRow row = (DocumentRow) mAdapter.getItem(position);
            if (null != row) {
                MaterialsInfoFragment fragment = MaterialsInfoFragment.createInstance(row.getMaterial());
                Activity activity = getActivity();
                if (activity instanceof MainActivity) {
                    AndroidUtils.hideSoftKeyboard(getContext(), getView());
                    saveInstanceState();
                    MainActivity mainActivity = (MainActivity) activity;
                    mainActivity.openFragment(fragment);
                } else {
                    Log.e(TAG, "Activity holding fragment is not MainActivity!");
                }
            }
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
        updateMainView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != getView()) {
            outState.putParcelable(STATE_LIST_VIEW, getListView().onSaveInstanceState());
        }
    }

    void commit() {
        mRepository.commitCurrentDocument();
        mAdapter.notifyDataSetChanged();
        updateMainView();
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

    private void updateMainView() {
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).updateFragment();
        } else {
            Log.e(TAG, "Activity holding fragment is not MainActivity!");
        }
    }

    private final class AddressDialogListener implements AddressDialogFragment.AddressDialogListener {
        private final boolean mIsSender;

        public AddressDialogListener(boolean isSender) {
            mIsSender = isSender;
        }

        @Override
        public void onDismiss(String address) {
            if (mIsSender) {
                mDocument.setSender(address);
            } else {
                mDocument.setRecipient(address);
            }

            commit();
        }
    }

    private final class ClearDialogListener implements View.OnClickListener, ClearDialogFragment.ClearDialogListener {
        @Override
        public void onClick(View v) {
            ClearDialogFragment dialog = new ClearDialogFragment();
            dialog.setDialogListener(this);
            dialog.show(getFragmentManager(), ClearDialogFragment.class.getSimpleName());
        }

        @Override
        public void onDismiss(boolean keepAddresses) {
            mDocument.removeAllRows();
            if (!keepAddresses) {
                mDocument.setSender("");
                mDocument.setRecipient("");
            }

            // Ensure saving now does not overwrite an existing document
            mDocument.assignNewId();
            mDocument.setName("");
            mDocument.setTimestamp(null);

            commit();
        }
    }

    private final class SaveDialogListener implements View.OnClickListener, SaveDialogFragment.SaveDialogListener {
        @Override
        public void onClick(View v) {
            Bundle args = new Bundle();
            args.putBoolean(SaveDialogFragment.ARG_IS_SAVED, mDocument.isSaved());
            args.putString(SaveDialogFragment.ARG_NAME, mDocument.getName());

            SaveDialogFragment dialog = new SaveDialogFragment();
            dialog.setArguments(args);
            dialog.setDialogListener(this);
            dialog.show(getFragmentManager(), SaveDialogFragment.class.getSimpleName());
        }

        @Override
        public void onDismiss(String name, boolean asCopy) {
            if (asCopy) {
                mDocument.assignNewId();
            }
            try {
                name = name.trim();
                if (TextUtils.isEmpty(name)) {
                    name = getString(R.string.document_no_name);
                }
                mRepository.saveCurrentDocument(name);
            } catch (Exception ex) {
                Log.e(TAG, "Exception while saving document.", ex);
            }

            commit();
        }
    }
}

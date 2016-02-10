package se.accidis.fmfg.app.ui.documents;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.ShareCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.export.ExportFile;
import se.accidis.fmfg.app.export.PdfGenerator;
import se.accidis.fmfg.app.model.Document;
import se.accidis.fmfg.app.model.DocumentLink;
import se.accidis.fmfg.app.model.DocumentRow;
import se.accidis.fmfg.app.model.DocumentSettings;
import se.accidis.fmfg.app.services.DocumentsRepository;
import se.accidis.fmfg.app.services.Preferences;
import se.accidis.fmfg.app.ui.MainActivity;
import se.accidis.fmfg.app.ui.materials.MaterialsInfoFragment;
import se.accidis.fmfg.app.utils.AndroidUtils;

/**
 * Fragment for viewing/editing a document.
 */
public final class DocumentFragment extends ListFragment implements MainActivity.HasNavigationItem, MainActivity.HasMenu, MainActivity.HasTitle {
	public static final String ARG_ID = "id";
	private static final String STATE_LIST_VIEW = "documentListViewState";
	private static final String TAG = DocumentFragment.class.getSimpleName();
	private final Handler mHandler = new Handler();
	private DocumentAdapter mAdapter;
	private View mButtonBar;
	private Document mDocument;
	private boolean mIsCurrentDocument;
	private Parcelable mListState;
	private Preferences mPrefs;
	private DocumentsRepository mRepository;

	public static DocumentFragment createInstance(DocumentLink docLink) {
		Bundle bundle = new Bundle();
		bundle.putString(ARG_ID, docLink.getId().toString());
		DocumentFragment fragment = new DocumentFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public int getItemId() {
		return R.id.nav_document;
	}

	@Override
	public int getMenu() {
		return R.menu.document;
	}

	@Override
	public String getTitle(Context context) {
		String displayName = (null != mDocument && mDocument.isSaved()) ? mDocument.getName() : context.getString(R.string.document_no_name);
		return (mIsCurrentDocument ? (displayName + ' ' + context.getString(R.string.document_editing)) : displayName);
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

		mPrefs = new Preferences(getContext());
		mButtonBar = view.findViewById(R.id.document_button_bar);

		Button saveButton = (Button) view.findViewById(R.id.document_button_save);
		saveButton.setOnClickListener(new SaveDialogListener());

		Button clearButton = (Button) view.findViewById(R.id.document_button_clear);
		clearButton.setOnClickListener(new ClearDialogListener());

		return view;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (null == mAdapter || position < 0 || position >= mAdapter.getCount() || !mIsCurrentDocument) {
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
					saveInstanceState();
					((MainActivity) activity).openFragment(fragment);
				} else {
					Log.e(TAG, "Activity holding fragment is not MainActivity!");
				}
			}
		}
	}

	@Override
	public boolean onMenuItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.document_menu_delete:
				DeleteDialogFragment deleteDialog = new DeleteDialogFragment();
				deleteDialog.setDialogListener(new DeleteDialogListener());
				deleteDialog.show(getFragmentManager(), DeleteDialogFragment.class.getSimpleName());
				return true;

			case R.id.document_menu_edit:
				if (mRepository.getCurrentDocument().getSettings().getBoolean(DocumentSettings.Keys.UNSAVED_CHANGES)) {
					EditUnsavedDialogFragment editDialog = new EditUnsavedDialogFragment();
					editDialog.setDialogListener(new EditUnsavedDialogListener());
					editDialog.show(getFragmentManager(), EditUnsavedDialogFragment.class.getSimpleName());
				} else {
					makeCurrentDocument();
				}
				return true;

			case R.id.document_menu_export:
				ProgressDialog progressDialog = ProgressDialog.show(getContext(), getString(R.string.document_export), getString(R.string.document_export_please_wait), true, false);
				ExportPdfAsyncTask exportTask = new ExportPdfAsyncTask(getActivity(), progressDialog);
				exportTask.execute();
				return true;
		}

		return false;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		MenuItem deleteItem = menu.findItem(R.id.document_menu_delete);
		if (null != deleteItem) {
			boolean canDelete = (null != mDocument && !mIsCurrentDocument);
			deleteItem.setEnabled(canDelete);
		}
		MenuItem editItem = menu.findItem(R.id.document_menu_edit);
		if (null != editItem) {
			editItem.setEnabled(!mIsCurrentDocument);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (null == mRepository) {
			mRepository = DocumentsRepository.getInstance(getContext());
		}

		mDocument = null;
		Bundle args = getArguments();
		if (null != args && args.containsKey(ARG_ID)) {
			try {
				UUID id = UUID.fromString(args.getString(ARG_ID, ""));
				mDocument = mRepository.loadDocument(id);
				mIsCurrentDocument = false;
			} catch (Exception ex) {
				Log.e(TAG, "Exception while loading document.", ex);
				Toast toast = Toast.makeText(getContext(), R.string.generic_unexpected_error, Toast.LENGTH_LONG);
				toast.show();
			}
		}

		if (null == mDocument) {
			mDocument = mRepository.getCurrentDocument();
			mIsCurrentDocument = true;
		}

		mButtonBar.setVisibility(mIsCurrentDocument ? View.VISIBLE : View.GONE);
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
		mAdapter = new DocumentAdapter(getContext(), mDocument, mIsCurrentDocument);
		mAdapter.setShowFbet(mPrefs.shouldShowFbetInDocument());
		setListAdapter(mAdapter);

		if (null != mListState) {
			getListView().onRestoreInstanceState(mListState);
			mListState = null;
		}
	}

	private void makeCurrentDocument() {
		if (!mIsCurrentDocument) {
			mRepository.changeCurrentDocument(mDocument);
			mIsCurrentDocument = true;
			mAdapter.setIsCurrentDocument(true);
			mButtonBar.setVisibility(mIsCurrentDocument ? View.VISIBLE : View.GONE);
		}
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

			mDocument.getSettings().put(DocumentSettings.Keys.UNSAVED_CHANGES, true);
			commit();
		}
	}

	private final class ClearDialogListener implements View.OnClickListener, ClearDialogFragment.ClearDialogListener {
		@Override
		public void onClick(View v) {
			if (!mIsCurrentDocument) {
				return;
			}

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
			mDocument.getSettings().remove(DocumentSettings.Keys.UNSAVED_CHANGES);

			commit();
		}
	}

	private final class DeleteDialogListener implements DeleteDialogFragment.DeleteDialogListener {
		@Override
		public void onDismiss() {
			if (mIsCurrentDocument) {
				return;
			}

			mRepository.deleteDocument(mDocument.getId());

			Activity activity = getActivity();
			if (activity instanceof MainActivity) {
				((MainActivity) activity).popFragmentFromBackStack();
			} else {
				Log.e(TAG, "Activity holding fragment is not MainActivity!");
			}
		}
	}

	private final class EditUnsavedDialogListener implements EditUnsavedDialogFragment.EditUnsavedDialogListener {
		@Override
		public void onDismiss() {
			makeCurrentDocument();
		}
	}

	private class ExportPdfAsyncTask extends AsyncTask<Void, Void, Void> {
		private final Activity mContext;
		private final ProgressDialog mProgressDialog;

		public ExportPdfAsyncTask(Activity context, ProgressDialog progressDialog) {
			mContext = context;
			mProgressDialog = progressDialog;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				ExportFile.cleanUpOldExports(mContext);
				final ExportFile exportFile = ExportFile.fromDocument(mDocument, PdfGenerator.PDF_EXTENSION, mContext);

				Log.i(TAG, "Exporting current document to \"" + exportFile.getFile().getPath() + "\".");
				PdfGenerator.exportToPdf(mDocument, exportFile, mContext);

				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mProgressDialog.dismiss();

						@SuppressWarnings("deprecation")
						Intent intent = ShareCompat.IntentBuilder.from(mContext)
							.setChooserTitle(R.string.document_export_chooser_title)
							.setSubject(TextUtils.isEmpty(mDocument.getName()) ? mContext.getString(R.string.document_export_default_name) : mDocument.getName().trim())
							.setStream(exportFile.getUri())
							.setType(PdfGenerator.PDF_CONTENT_TYPE)
							.createChooserIntent()
							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET) // replaced with FLAG_ACTIVITY_NEW_DOCUMENT when API >= 21
							.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

						// This is required on older Android or certain devices for some reason
						List<ResolveInfo> resInfoList = mContext.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
						for (ResolveInfo resolveInfo : resInfoList) {
							String packageName = resolveInfo.activityInfo.packageName;
							mContext.grantUriPermission(packageName, exportFile.getUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION);
						}

						mContext.startActivity(intent);
					}
				});
			} catch (Exception ex) {
				Log.e(TAG, "Exception while trying export PDF.", ex);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mProgressDialog.dismiss();
						Toast toast = Toast.makeText(getContext(), R.string.document_export_error, Toast.LENGTH_LONG);
						toast.show();
					}
				});
			}

			return null;
		}
	}

	private final class SaveDialogListener implements View.OnClickListener, SaveDialogFragment.SaveDialogListener {
		@Override
		public void onClick(View v) {
			if (!mIsCurrentDocument) {
				return;
			}

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
				Toast toast = Toast.makeText(getContext(), R.string.generic_unexpected_error, Toast.LENGTH_LONG);
				toast.show();
			}

			commit();
		}
	}
}

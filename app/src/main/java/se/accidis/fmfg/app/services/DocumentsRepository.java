package se.accidis.fmfg.app.services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import se.accidis.fmfg.app.model.Document;
import se.accidis.fmfg.app.model.DocumentLink;
import se.accidis.fmfg.app.model.DocumentSettings;
import se.accidis.fmfg.app.utils.IOUtils;

/**
 * Repository for documents.
 */
public final class DocumentsRepository {
    private static final String CURRENT_DOCUMENT = "CurrentDocument.json";
    private static final String SAVED_DOCUMENT_FORMAT = "Saved_%1$s.json";
    private static final String SAVED_DOCUMENT_PREFIX = "Saved_";
    private static final String TAG = DocumentsRepository.class.getSimpleName();
    private final Context mContext;
    private Document mCurrentDocument;
    private static DocumentsRepository mInstance;
    private List<DocumentLink> mList;
    private OnLoadedListener mOnLoadedListener;

    private DocumentsRepository(Context context) {
        mContext = context.getApplicationContext();
    }

    public static DocumentsRepository getInstance(Context context) {
        return (null == mInstance ? (mInstance = new DocumentsRepository(context)) : mInstance);
    }

    public void beginLoad() {
        if (isLoaded()) {
            Log.d(TAG, "Documents already loaded, nothing to do.");
            if (null != mOnLoadedListener) {
                mOnLoadedListener.onLoaded(mList);
            }

            return;
        }

        Log.d(TAG, "Loading documents.");
        LoadTask loadTask = new LoadTask();
        loadTask.execute();
    }

    public void changeCurrentDocument(Document document) {
        Log.d(TAG, "Replacing current document with ID: " + document.getId());
        mCurrentDocument = document;
        commitCurrentDocument();
    }

    public void commitCurrentDocument() {
        try {
            String json = mCurrentDocument.toJson().toString();
            IOUtils.writeToStream(mContext.openFileOutput(CURRENT_DOCUMENT, Context.MODE_PRIVATE), json);
        } catch (Exception ex) {
            Log.e(TAG, "Exception while writing current document.", ex);
        }
    }

    public void deleteDocument(UUID id) {
        Log.d(TAG, "Deleting document with ID: " + id);
        String filename = getFilenameByDocumentId(id);
        mContext.deleteFile(filename);
        invalidate();
    }

    public void ensureCurrentDocumentLoaded() {
        if (null == mCurrentDocument) {
            try {
                mCurrentDocument = readDocument(CURRENT_DOCUMENT);
            } catch (FileNotFoundException ignored) {
                Log.w(TAG, "Current document not found, might be first startup.");
            } catch (Exception ex) {
                Log.e(TAG, "Exception while reading current document.", ex);
            }

            if (null == mCurrentDocument) {
                mCurrentDocument = new Document();
                Log.d(TAG, "Created a document with ID: " + mCurrentDocument.getId());
            } else {
                Log.d(TAG, "Loaded current document with ID: " + mCurrentDocument.getId());
            }
        }
    }

    public Document getCurrentDocument() {
        ensureCurrentDocumentLoaded();
        return mCurrentDocument;
    }

    public boolean isLoaded() {
        return (null != mList);
    }

    public Document loadDocument(UUID id) throws IOException, JSONException {
        Log.d(TAG, "Loading document with ID: " + id);
        String filename = getFilenameByDocumentId(id);
        return readDocument(filename);
    }

    public void saveCurrentDocument(String name) throws IOException, JSONException {
        Log.d(TAG, "Saving current document with ID: " + mCurrentDocument.getId() + ", name = " + mCurrentDocument.getName());
        ensureCurrentDocumentLoaded();
        mCurrentDocument.setName(name);
        mCurrentDocument.setTimestamp(DateTime.now());
        mCurrentDocument.getSettings().remove(DocumentSettings.Keys.UNSAVED_CHANGES);
        writeDocument(mCurrentDocument);
        invalidate();
    }

    public void setOnLoadedListener(OnLoadedListener listener) {
        mOnLoadedListener = listener;
    }

    private static String getFilenameByDocumentId(UUID documentId) {
        return String.format(SAVED_DOCUMENT_FORMAT, documentId.toString());
    }

    private void invalidate() {
        mList = null;
    }

    private Document readDocument(String fileName) throws IOException, JSONException {
        String str = IOUtils.readToEnd(mContext.openFileInput(fileName));
        JSONObject json = new JSONObject(str);
        return Document.fromJson(json);
    }

    private void writeDocument(Document document) throws IOException, JSONException {
        String fileName = getFilenameByDocumentId(document.getId());
        String json = document.toJson().toString();
        IOUtils.writeToStream(mContext.openFileOutput(fileName, Context.MODE_PRIVATE), json);
    }

    public interface OnLoadedListener {
        void onException(Exception ex);

        void onLoaded(List<DocumentLink> list);
    }

    private final class LoadTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                List<String> fileList = filterDocuments(mContext.fileList());

                ArrayList<DocumentLink> list = new ArrayList<>();
                for (String file : fileList) {
                    DocumentLink docLink = readDocumentLink(file);
                    list.add(docLink);
                }

                Collections.sort(list);
                mList = list;

                Log.i(TAG, "Finished loading documents (" + mList.size() + " documents loaded).");

            } catch (Exception ex) {
                Log.e(TAG, "Failed to load documents.", ex);

                if (null == mList && null != mOnLoadedListener) {
                    mOnLoadedListener.onException(ex);
                }
            }

            if (null != mList && null != mOnLoadedListener) {
                mOnLoadedListener.onLoaded(mList);
            }

            return null;
        }

        private List<String> filterDocuments(String[] files) {
            List<String> filtered = new ArrayList<>();
            for (String fileName : files) {
                if (fileName.startsWith(SAVED_DOCUMENT_PREFIX)) {
                    filtered.add(fileName);
                }
            }
            return filtered;
        }

        private DocumentLink readDocumentLink(String fileName) throws IOException, JSONException {
            String str = IOUtils.readToEnd(mContext.openFileInput(fileName));
            JSONObject json = new JSONObject(str);
            return DocumentLink.fromJson(json);
        }
    }
}

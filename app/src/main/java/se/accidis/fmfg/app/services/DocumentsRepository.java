package se.accidis.fmfg.app.services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import se.accidis.fmfg.app.model.Document;
import se.accidis.fmfg.app.model.DocumentLink;
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
        if (null != mList) {
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

    public void commitCurrentDocument() {
        OutputStream outputStream = null;
        OutputStreamWriter writer = null;

        try {
            JSONObject json = mCurrentDocument.toJson();
            outputStream = mContext.openFileOutput(CURRENT_DOCUMENT, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(outputStream);
            writer.write(json.toString());
        } catch (Exception ex) {
            Log.e(TAG, "Exception while writing current document.", ex);
        } finally {
            try {
                if (null != writer) {
                    writer.close();
                }
                if (null != outputStream) {
                    outputStream.close();
                }
            } catch (IOException ignored) {
            }
        }
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
            }
        }
    }

    public Document getCurrentDocument() {
        ensureCurrentDocumentLoaded();
        return mCurrentDocument;
    }

    public void saveCurrentDocument(String name) throws IOException, JSONException {
        ensureCurrentDocumentLoaded();
        mCurrentDocument.setName(name);
        mCurrentDocument.setTimestamp(DateTime.now());
        writeDocument(mCurrentDocument);
        // TODO Once we can open the document we should clear current
        //mCurrentDocument = new Document();
        invalidate();
    }

    public void setOnLoadedListener(OnLoadedListener listener) {
        mOnLoadedListener = listener;
    }

    private static String getFilenameByDocument(Document document) {
        return String.format(SAVED_DOCUMENT_FORMAT, document.getId().toString());
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
        String fileName = getFilenameByDocument(document);
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

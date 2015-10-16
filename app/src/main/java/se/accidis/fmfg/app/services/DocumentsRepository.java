package se.accidis.fmfg.app.services;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import se.accidis.fmfg.app.model.Document;
import se.accidis.fmfg.app.utils.IOUtils;

/**
 * Repository for documents.
 */
public final class DocumentsRepository {
    private static final String CURRENT_DOCUMENT = "CurrentDocument.json";
    private static final String TAG = DocumentsRepository.class.getSimpleName();
    private final Context mContext;
    private Document mCurrentDocument;
    private static DocumentsRepository mInstance;

    private DocumentsRepository(Context context) {
        mContext = context.getApplicationContext();
    }

    public static DocumentsRepository getInstance(Context context) {
        return (null == mInstance ? (mInstance = new DocumentsRepository(context)) : mInstance);
    }

    public void commit() {
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

    public void ensureInitialized() {
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
        ensureInitialized();
        return mCurrentDocument;
    }

    private Document readDocument(String fileName) throws IOException, JSONException {
        String str = IOUtils.readToEnd(mContext.openFileInput(fileName));
        JSONObject json = new JSONObject(str);
        return Document.fromJson(json);
    }
}

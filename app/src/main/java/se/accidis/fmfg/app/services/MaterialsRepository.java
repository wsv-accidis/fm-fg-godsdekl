package se.accidis.fmfg.app.services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import se.accidis.fmfg.app.model.Material;

/**
 * Repository for the materials list.
 */
public final class MaterialsRepository {
    private static final String ADR_JSON_ASSET = "ADR.json";
    private static final int BUFFER_SIZE = 8192;
    private static final String TAG = MaterialsRepository.class.getSimpleName();
    private final Context mContext;
    private static MaterialsRepository mInstance;
    private List<Material> mList;
    private OnLoadedListener mOnLoadedListener;

    private MaterialsRepository(Context context) {
        mContext = context.getApplicationContext();
    }

    public static MaterialsRepository getInstance(Context context) {
        return (null == mInstance ? (mInstance = new MaterialsRepository(context)) : mInstance);
    }

    public void beginLoad() {
        if (null != mList) {
            if (null != mOnLoadedListener) {
                mOnLoadedListener.onLoaded(mList);
            }

            return;
        }

        LoadTask loadTask = new LoadTask();
        loadTask.execute();
    }

    public void setOnLoadedListener(OnLoadedListener listener) {
        mOnLoadedListener = listener;
    }

    public interface OnLoadedListener {
        void onException(Exception ex);

        void onLoaded(List<Material> list);
    }

    private final class LoadTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String str = readAssetsFile();
                JSONArray jsonArray = new JSONArray(str);

                ArrayList<Material> list = new ArrayList<>(jsonArray.length());
                for (int i = 0; i < jsonArray.length(); i++) {
                    list.add(Material.fromJSON(jsonArray.getJSONObject(i)));
                }

                mList = list;
                Log.i(TAG, "Finished loading assets (" + mList.size() + " items loaded).");

            } catch (Exception ex) {
                Log.e(TAG, "Failed to load assets.", ex);

                if (null == mList && null != mOnLoadedListener) {
                    mOnLoadedListener.onException(ex);
                }
            }

            if (null != mList && null != mOnLoadedListener) {
                mOnLoadedListener.onLoaded(mList);
            }

            return null;
        }

        private String readAssetsFile() throws IOException {
            InputStream stream = null;
            InputStreamReader reader = null;
            try {
                stream = mContext.getAssets().open(ADR_JSON_ASSET);
                reader = new InputStreamReader(stream);

                StringBuilder string = new StringBuilder();
                char[] buffer = new char[BUFFER_SIZE];
                int numRead;
                while (0 <= (numRead = reader.read(buffer))) {
                    string.append(buffer, 0, numRead);
                }

                return string.toString();
            } finally {
                try {
                    if (null != reader) {
                        reader.close();
                    }
                    if (null != stream) {
                        stream.close();
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }
}

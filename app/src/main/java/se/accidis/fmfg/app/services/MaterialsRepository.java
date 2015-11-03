package se.accidis.fmfg.app.services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import se.accidis.fmfg.app.model.Material;
import se.accidis.fmfg.app.utils.IOUtils;

/**
 * Repository for the materials list.
 */
public final class MaterialsRepository {
    private static final String ADR_JSON_ASSET = "ADR.json";
    private static final String TAG = MaterialsRepository.class.getSimpleName();
    private final Context mContext;
    private final Set<String> mFavoriteMaterials;
    private final Preferences mPrefs;
    private static MaterialsRepository mInstance;
    private List<Material> mList;
    private OnLoadedListener mOnLoadedListener;

    private MaterialsRepository(Context context) {
        mContext = context.getApplicationContext();
        mPrefs = new Preferences(mContext);
        mFavoriteMaterials = mPrefs.getFavoriteMaterials();
    }

    public static MaterialsRepository getInstance(Context context) {
        return (null == mInstance ? (mInstance = new MaterialsRepository(context)) : mInstance);
    }

    public void addFavoriteMaterial(Material material) {
        String key = material.toUniqueKey();
        mFavoriteMaterials.add(key);
        mPrefs.setFavoriteMaterials(mFavoriteMaterials);
    }

    public void beginLoad() {
        if (null != mList) {
            Log.d(TAG, "Assets already loaded, nothing to do.");
            if (null != mOnLoadedListener) {
                mOnLoadedListener.onLoaded(mList);
            }

            return;
        }

        Log.d(TAG, "Loading assets.");
        LoadTask loadTask = new LoadTask();
        loadTask.execute();
    }

    public boolean isFavoriteMaterial(Material material) {
        String key = material.toUniqueKey();
        return mFavoriteMaterials.contains(key);
    }

    public void removeFavoriteMaterial(Material material) {
        String key = material.toUniqueKey();
        mFavoriteMaterials.remove(key);
        mPrefs.setFavoriteMaterials(mFavoriteMaterials);
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
            return IOUtils.readToEnd(mContext.getAssets().open(ADR_JSON_ASSET));
        }
    }
}

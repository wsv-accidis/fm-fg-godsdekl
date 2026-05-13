package se.accidis.fmfg.app.services

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import se.accidis.fmfg.app.model.Material
import se.accidis.fmfg.app.utils.IOUtils
import se.accidis.fmfg.app.utils.TAG

/**
 * Repository for the materials list.
 */
class MaterialsRepository private constructor(context: Context) {
    private val context: Context = context.applicationContext
    private val favoriteMaterials: MutableSet<String>
    private val prefs: Preferences = Preferences(this@MaterialsRepository.context)
    private var materials: MutableList<Material>? = null
    private var onLoadedListener: OnLoadedListener? = null
    private val repositoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        favoriteMaterials = prefs.favoriteMaterials
    }

    fun addFavoriteMaterial(material: Material) {
        val key = material.toUniqueKey()
        favoriteMaterials.add(key)
        prefs.favoriteMaterials = favoriteMaterials
    }

    fun beginLoad() {
        if (null != materials) {
            Log.d(TAG, "Assets already loaded, nothing to do.")
            onLoadedListener?.onLoaded(materials!!)
            return
        }

        Log.d(TAG, "Loading assets.")
        repositoryScope.launch {
            try {
                val loadedMaterials = withContext(Dispatchers.IO) {
                    val str = IOUtils.readToEnd(context.assets.open(ADR_JSON_ASSET))
                    val jsonArray = JSONArray(str)
                    val list = ArrayList<Material>(jsonArray.length())
                    for (i in 0 until jsonArray.length()) {
                        list.add(Material.fromJSON(jsonArray.getJSONObject(i)))
                    }
                    list
                }

                materials = loadedMaterials
                Log.i(TAG, "Finished loading assets (${materials!!.size} items loaded).")
                onLoadedListener?.onLoaded(materials!!)
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to load assets.", ex)
                if (null == materials) {
                    onLoadedListener?.onException(ex)
                }
            }
        }
    }

    fun isFavoriteMaterial(material: Material): Boolean {
        val key = material.toUniqueKey()
        return favoriteMaterials.contains(key)
    }

    fun removeFavoriteMaterial(material: Material) {
        val key = material.toUniqueKey()
        favoriteMaterials.remove(key)
        prefs.favoriteMaterials = favoriteMaterials
    }

    fun setOnLoadedListener(listener: OnLoadedListener?) {
        onLoadedListener = listener
    }

    interface OnLoadedListener {
        fun onException(ex: Exception)

        fun onLoaded(list: MutableList<Material>)
    }

    companion object {
        private const val ADR_JSON_ASSET = "ADR.json"
        private var singleton: MaterialsRepository? = null

        @JvmStatic
        fun getInstance(context: Context): MaterialsRepository {
            return (
                    if (null == singleton)
                        (MaterialsRepository(context).also { singleton = it })
                    else
                        singleton
                    )!!
        }
    }
}

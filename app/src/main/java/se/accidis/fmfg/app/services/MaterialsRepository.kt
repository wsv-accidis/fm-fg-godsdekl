package se.accidis.fmfg.app.services

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import se.accidis.fmfg.app.model.Material
import se.accidis.fmfg.app.utils.IOUtils
import se.accidis.fmfg.app.utils.Resource
import se.accidis.fmfg.app.utils.TAG

/**
 * Repository for the materials list.
 */
class MaterialsRepository private constructor(context: Context) {
    private val context: Context = context.applicationContext
    private val favoriteMaterials: MutableSet<String>
    private val prefs: Preferences = Preferences(this@MaterialsRepository.context)

    private val _materialsResource = MutableStateFlow<Resource<List<Material>>>(Resource.Loading)
    val materialsResource: StateFlow<Resource<List<Material>>> = _materialsResource.asStateFlow()

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
        val current = _materialsResource.value
        if (current is Resource.Success && current.data.isNotEmpty()) {
            Log.d(TAG, "Assets already loaded, nothing to do.")
            return
        }

        Log.d(TAG, "Loading assets.")
        _materialsResource.value = Resource.Loading
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

                _materialsResource.value = Resource.Success(loadedMaterials)
                Log.i(TAG, "Finished loading assets (${loadedMaterials.size} items loaded).")
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to load assets.", ex)
                _materialsResource.value = Resource.Error(ex)
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

    companion object {
        private const val ADR_JSON_ASSET = "amkat.mini.json"
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

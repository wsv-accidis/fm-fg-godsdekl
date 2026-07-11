package se.accidis.fmfg.app.services

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import se.accidis.fmfg.app.model.Material
import se.accidis.fmfg.app.model.MaterialSource
import se.accidis.fmfg.app.utils.IOUtils
import se.accidis.fmfg.app.utils.Resource
import se.accidis.fmfg.app.utils.TAG

/**
 * Repository for the materials list.
 */
class MaterialsRepository private constructor(context: Context) {
    private val context: Context = context.applicationContext
    private val _materialsResource = MutableStateFlow<Resource<List<Material>>>(Resource.Loading)
    val materialsResource: StateFlow<Resource<List<Material>>> = _materialsResource.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

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
                    val adrJob = async { loadFromAsset(ADR_S_JSON_ASSET, MaterialSource.ADR_S) }
                    val amkatJob = async { loadFromAsset(AMKAT_JSON_ASSET, MaterialSource.AMKAT) }
                    adrJob.await() + amkatJob.await()
                }

                _materialsResource.value = Resource.Success(loadedMaterials)
                Log.i(TAG, "Finished loading assets (${loadedMaterials.size} items loaded).")
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to load assets.", ex)
                _materialsResource.value = Resource.Error(ex)
            }
        }
    }

    private fun loadFromAsset(assetName: String, source: MaterialSource): List<Material> {
        val str = IOUtils.readToEnd(context.assets.open(assetName))
        val jsonArray = JSONArray(str)
        val list = ArrayList<Material>(jsonArray.length())
        for (i in 0 until jsonArray.length()) {
            list.add(Material.fromJSON(jsonArray.getJSONObject(i), source))
        }
        return list
    }

    companion object {
        private const val ADR_S_JSON_ASSET = "adr-s.mini.json"
        private const val AMKAT_JSON_ASSET = "amkat.mini.json"
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

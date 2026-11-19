package se.accidis.fmfg.app.services

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.DecodeSequenceMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeToSequence
import se.accidis.fmfg.app.model.Material
import se.accidis.fmfg.app.model.MaterialSource
import se.accidis.fmfg.app.utils.Resource
import timber.log.Timber

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
            Timber.d("Assets already loaded, nothing to do.")
            return
        }

        Timber.d("Loading assets...")
        _materialsResource.value = Resource.Loading
        repositoryScope.launch {
            try {
                val loadedMaterials = withContext(Dispatchers.IO) {
                    val adrJob = async { loadFromAsset(ADR_S_JSON_ASSET, MaterialSource.ADR_S) }
                    val amkatJob = async { loadFromAsset(AMKAT_JSON_ASSET, MaterialSource.AMKAT) }
                    adrJob.await() + amkatJob.await()
                }

                _materialsResource.value = Resource.Success(loadedMaterials)
                Timber.i("Finished loading assets (%d items loaded).", loadedMaterials.size)
            } catch (ex: Exception) {
                Timber.e(ex, "Failed to load assets.")
                _materialsResource.value = Resource.Error(ex)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadFromAsset(assetName: String, source: MaterialSource): List<Material> =
        context.assets.open(assetName).use { stream ->
            json.decodeToSequence<MaterialAsset>(stream, DecodeSequenceMode.ARRAY_WRAPPED)
                .map { it.toMaterial(source) }
                .toList()
        }

    @Serializable
    private class MaterialAsset(
        @SerialName(Material.Keys.FBET) val fbet: String = "",
        @SerialName(Material.Keys.FBEN) val fben: String = "",
        @SerialName(Material.Keys.UNNR) val unNr: String = "",
        @SerialName(Material.Keys.NAMN) val namn: String,
        @SerialName(Material.Keys.KLASSKOD) val klassKod: List<String> = emptyList(),
        @SerialName(Material.Keys.NEMMG) val nemMg: Int = 0,
        @SerialName(Material.Keys.TPKAT) val tpKat: Int,
        @SerialName(Material.Keys.FRPGRP) val frpGrp: String = "",
        @SerialName(Material.Keys.TUNNELKOD) val tunnelKod: String = "",
        @SerialName(Material.Keys.MILJO) val miljo: Boolean = false
    ) {
        fun toMaterial(source: MaterialSource): Material = Material(
            fbet = fbet,
            fben = fben,
            UNnr = unNr,
            namn = namn,
            klassKod = klassKod,
            NEMmg = nemMg,
            tpKat = tpKat,
            frpGrp = frpGrp,
            tunnelKod = tunnelKod,
            miljo = miljo,
            source = source
        )
    }

    companion object {
        private const val ADR_S_JSON_ASSET = "adr-s.mini.json"
        private const val AMKAT_JSON_ASSET = "amkat.mini.json"

        private val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        @Volatile
        private var singleton: MaterialsRepository? = null

        @JvmStatic
        fun getInstance(context: Context): MaterialsRepository =
            singleton ?: synchronized(this) {
                singleton ?: MaterialsRepository(context).also { singleton = it }
            }
    }
}

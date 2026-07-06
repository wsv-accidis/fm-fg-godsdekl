package se.accidis.fmfg.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import se.accidis.fmfg.app.model.Material
import se.accidis.fmfg.app.services.MaterialsRepository
import se.accidis.fmfg.app.utils.Resource

class MaterialsViewModel(
    private val repository: MaterialsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val uiState: StateFlow<MaterialsUiState> = combine(
        repository.materialsResource,
        _searchQuery
    ) { resource, query ->
        when (resource) {
            is Resource.Loading -> MaterialsUiState.Loading
            is Resource.Error -> MaterialsUiState.Error(resource.throwable)
            is Resource.Success -> {
                val filtered = if (query.isBlank()) {
                    resource.data
                } else {
                    resource.data.filter { material ->
                        material.fben?.contains(query, ignoreCase = true) == true
                    }
                }
                MaterialsUiState.Success(filtered)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MaterialsUiState.Loading
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    init {
        repository.beginLoad()
    }
}

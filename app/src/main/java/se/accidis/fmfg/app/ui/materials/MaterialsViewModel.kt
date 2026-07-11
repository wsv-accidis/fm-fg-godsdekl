package se.accidis.fmfg.app.ui.materials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import se.accidis.fmfg.app.services.MaterialsRepository
import se.accidis.fmfg.app.utils.Resource

/**
 * View model for the Materials screen.
 */
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
                        material.matches(query)
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

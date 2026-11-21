package se.accidis.fmfg.app.ui.materials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import se.accidis.fmfg.app.model.MaterialSource
import se.accidis.fmfg.app.services.MaterialsRepository
import se.accidis.fmfg.app.utils.Resource

/**
 * View model for the Materials screen.
 */
class MaterialsViewModel(
    repository: MaterialsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSource = MutableStateFlow(MaterialSource.AMKAT)
    val selectedSource: StateFlow<MaterialSource> = _selectedSource.asStateFlow()

    val uiState: StateFlow<MaterialsUiState> = combine(
        repository.materialsResource,
        _searchQuery,
        _selectedSource
    ) { resource, query, source ->
        when (resource) {
            is Resource.Loading -> MaterialsUiState.Loading
            is Resource.Error -> MaterialsUiState.Error(resource.throwable)
            is Resource.Success -> {
                val sourceItems = resource.data[source].orEmpty()
                val normalizedQuery = query.trim()
                val filtered = if (normalizedQuery.isEmpty()) {
                    sourceItems
                } else {
                    sourceItems.filter { it.matches(normalizedQuery) }
                }
                MaterialsUiState.Success(filtered)
            }
        }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MaterialsUiState.Loading
        )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun toggleSource() {
        _selectedSource.value = if (_selectedSource.value == MaterialSource.AMKAT) {
            MaterialSource.ADR_S
        } else {
            MaterialSource.AMKAT
        }
    }

    init {
        repository.beginLoad()
    }
}

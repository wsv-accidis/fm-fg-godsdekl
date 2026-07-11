package se.accidis.fmfg.app.ui.materials

import se.accidis.fmfg.app.model.Material

/**
 * Represents the different UI states for the Materials screen.
 */
sealed class MaterialsUiState {
    object Loading : MaterialsUiState()
    data class Success(val items: List<Material>) : MaterialsUiState()
    data class Error(val throwable: Throwable) : MaterialsUiState()
}

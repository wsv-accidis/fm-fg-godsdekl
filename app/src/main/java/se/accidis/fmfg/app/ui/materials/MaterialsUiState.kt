package se.accidis.fmfg.app.ui.materials

import se.accidis.fmfg.app.model.Material

/**
 * Represents the different UI states for the Materials screen.
 */
sealed class MaterialsUiState {
    /** Represents the loading state. */
    object Loading : MaterialsUiState()
    /** Represents the success state with loaded items. */
    data class Success(val items: List<Material>) : MaterialsUiState()
    /** Represents the error state with the corresponding throwable. */
    data class Error(val throwable: Throwable) : MaterialsUiState()
}

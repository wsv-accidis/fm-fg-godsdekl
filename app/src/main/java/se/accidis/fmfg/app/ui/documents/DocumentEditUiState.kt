package se.accidis.fmfg.app.ui.documents

import se.accidis.fmfg.app.model.DocumentRow

/**
 * UI state for the Document Edit screen.
 */
sealed interface DocumentEditUiState {
    data object Loading : DocumentEditUiState
    data class Success(val rows: List<DocumentRow>) : DocumentEditUiState
}

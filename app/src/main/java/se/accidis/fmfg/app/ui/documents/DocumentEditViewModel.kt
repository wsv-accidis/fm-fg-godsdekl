package se.accidis.fmfg.app.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import se.accidis.fmfg.app.services.DocumentsRepository

/**
 * ViewModel for editing the current document.
 */
class DocumentEditViewModel(
    private val repository: DocumentsRepository
) : ViewModel() {

    val uiState: StateFlow<DocumentEditUiState> = repository.currentDocumentFlow
        .map { document -> DocumentEditUiState.Success(document.rows) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DocumentEditUiState.Loading
        )
}

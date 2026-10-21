package se.accidis.fmfg.app.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import se.accidis.fmfg.app.services.DocumentsRepository
import se.accidis.fmfg.app.services.Preferences

/**
 * ViewModel for editing the current document.
 */
class DocumentEditViewModel(
    private val repository: DocumentsRepository,
    private val preferences: Preferences
) : ViewModel() {

    val uiState: StateFlow<DocumentEditUiState> = repository.currentDocumentFlow
        .map { document ->
            DocumentEditUiState.Success(
                rows = document.rows,
                showFbet = preferences.shouldShowFbetInDocument(),
                documentName = document.name
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DocumentEditUiState.Loading
        )

    fun createNewDocument() {
        repository.createNewDocument()
    }
}

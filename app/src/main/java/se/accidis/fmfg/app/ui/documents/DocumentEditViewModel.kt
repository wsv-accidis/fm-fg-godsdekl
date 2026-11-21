package se.accidis.fmfg.app.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import se.accidis.fmfg.app.model.DocumentLink
import se.accidis.fmfg.app.services.DocumentsRepository
import se.accidis.fmfg.app.services.Preferences
import se.accidis.fmfg.app.utils.Resource
import java.util.UUID

/**
 * ViewModel for editing the current document.
 */
class DocumentEditViewModel(
    private val repository: DocumentsRepository,
    preferences: Preferences
) : ViewModel() {

    val uiState: StateFlow<DocumentEditUiState> = combine(
        repository.currentDocumentFlow,
        preferences.showFbetInDocumentFlow
    ) { document, showFbet ->
        DocumentEditUiState.Success(
            rows = document.rows,
            showFbet = showFbet,
            documentName = document.name
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DocumentEditUiState.Loading
    )

    val savedDocuments: StateFlow<Resource<List<DocumentLink>>> = repository.documents

    fun loadSavedDocuments() {
        repository.beginLoad()
    }

    fun createNewDocument() {
        repository.createNewDocument()
    }

    fun updateName(name: String) {
        repository.mutateCurrentDocument {
            this.name = name.trim()
        }
    }

    fun openDocument(id: UUID) {
        repository.openDocument(id)
    }

    fun deleteDocument(id: UUID) {
        repository.deleteDocument(id)
    }
}

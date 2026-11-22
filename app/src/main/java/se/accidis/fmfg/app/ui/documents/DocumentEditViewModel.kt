package se.accidis.fmfg.app.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import se.accidis.fmfg.app.model.DocumentLink
import se.accidis.fmfg.app.model.Material
import se.accidis.fmfg.app.services.DocumentsRepository
import se.accidis.fmfg.app.services.Preferences
import se.accidis.fmfg.app.utils.ColoadingHelper
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
        val tpKatTotals = (Material.TPKAT_MIN..Material.TPKAT_MAX).mapNotNull { tpKat ->
            val (weightKg, volumeLiter) = document.weightAndVolumeByTpKat(tpKat)
            if (weightKg.signum() != 0 || volumeLiter.signum() != 0) {
                TpKatTotal(
                    tpKat = tpKat,
                    weightKg = weightKg,
                    volumeLiter = volumeLiter,
                    value = document.calculatedValueByTpKat(tpKat)
                )
            } else {
                null
            }
        }

        DocumentEditUiState.Success(
            rows = document.rows,
            showFbet = showFbet,
            documentName = document.name,
            sender = document.sender,
            recipient = document.recipient,
            vehicleReg = document.vehicleReg,
            vehicleType = document.vehicleType,
            isProtectedTransport = document.isProtectedTransport,
            totalNemKg = document.totalNEMkg,
            tpKatTotals = tpKatTotals,
            totalValue = document.calculatedTotalValue,
            isViolatingColoadingRules = ColoadingHelper.isViolationOfColoadingRules(document)
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

    fun updateAddresses(sender: String, recipient: String) {
        repository.mutateCurrentDocument {
            this.sender = sender.trim()
            this.recipient = recipient.trim()
        }
    }

    fun updateOptionalFields(
        vehicleReg: String,
        vehicleType: String,
        isProtectedTransport: Boolean
    ) {
        repository.mutateCurrentDocument {
            this.vehicleReg = vehicleReg.trim()
            this.vehicleType = vehicleType.trim()
            this.isProtectedTransport = isProtectedTransport
        }
    }

    fun openDocument(id: UUID) {
        repository.openDocument(id)
    }

    fun deleteDocument(id: UUID) {
        repository.deleteDocument(id)
    }
}

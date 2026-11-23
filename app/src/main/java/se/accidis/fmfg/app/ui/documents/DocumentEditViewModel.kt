package se.accidis.fmfg.app.ui.documents

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.export.ExportFile
import se.accidis.fmfg.app.export.PdfGenerator
import se.accidis.fmfg.app.model.DocumentLink
import se.accidis.fmfg.app.model.Material
import se.accidis.fmfg.app.services.DocumentsRepository
import se.accidis.fmfg.app.services.Preferences
import se.accidis.fmfg.app.utils.ColoadingHelper
import se.accidis.fmfg.app.utils.Resource
import timber.log.Timber
import java.util.UUID

/**
 * ViewModel for editing the current document.
 */
class DocumentEditViewModel(
    private val repository: DocumentsRepository,
    preferences: Preferences
) : ViewModel() {

    private val _isExporting = MutableStateFlow(false)

    private val _events = Channel<DocumentEditEvent>(Channel.BUFFERED)
    val events: Flow<DocumentEditEvent> = _events.receiveAsFlow()

    val uiState: StateFlow<DocumentEditUiState> = combine(
        repository.currentDocumentFlow,
        preferences.showFbetInDocumentFlow,
        _isExporting
    ) { document, showFbet, isExporting ->
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
            isViolatingColoadingRules = ColoadingHelper.isViolationOfColoadingRules(document),
            isExporting = isExporting
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

    fun exportToPdf(context: Context) {
        if (_isExporting.value) return
        val appContext = context.applicationContext

        viewModelScope.launch {
            _isExporting.value = true
            try {
                val document = repository.awaitCurrentDocument()
                val exportFile = withContext(Dispatchers.IO) {
                    ExportFile.cleanUpOldExports(appContext)
                    val file = ExportFile.fromDocument(document, PdfGenerator.PDF_EXTENSION, appContext)
                    Timber.i("Exporting current document to \"%s\".", file.file.path)
                    PdfGenerator.exportToPdf(document, file, appContext)
                    file
                }
                val subject = document.name.trim().ifBlank {
                    appContext.getString(R.string.document_export_default_name)
                }
                _events.send(
                    DocumentEditEvent.SharePdf(
                        uri = exportFile.uri,
                        subject = subject,
                        filename = exportFile.filename
                    )
                )
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                Timber.e(ex, "Exception while trying to export PDF.")
                _events.send(DocumentEditEvent.ExportError)
            } finally {
                _isExporting.value = false
            }
        }
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

package se.accidis.fmfg.app.ui.documents

import android.net.Uri
import se.accidis.fmfg.app.model.DocumentRow
import java.math.BigDecimal

data class TpKatTotal(
    val tpKat: Int,
    val weightKg: BigDecimal,
    val volumeLiter: BigDecimal,
    val value: BigDecimal
)

/**
 * UI state for the Document Edit screen.
 */
sealed interface DocumentEditUiState {
    data object Loading : DocumentEditUiState
    data class Success(
        val rows: List<DocumentRow>,
        val showFbet: Boolean,
        val documentName: String,
        val sender: String,
        val recipient: String,
        val vehicleReg: String,
        val vehicleType: String,
        val isProtectedTransport: Boolean,
        val totalNemKg: BigDecimal,
        val tpKatTotals: List<TpKatTotal>,
        val totalValue: BigDecimal,
        val isViolatingColoadingRules: Boolean,
        val isExporting: Boolean
    ) : DocumentEditUiState
}

/**
 * One-shot UI events emitted by [DocumentEditViewModel].
 */
sealed interface DocumentEditEvent {
    data class SharePdf(val uri: Uri, val subject: String, val filename: String) : DocumentEditEvent
    data object ExportError : DocumentEditEvent
}

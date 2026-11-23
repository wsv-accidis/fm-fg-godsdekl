package se.accidis.fmfg.app.ui.documents

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.service.chooser.ChooserAction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.export.PdfGenerator
import se.accidis.fmfg.app.model.DocumentRow
import se.accidis.fmfg.app.services.DocumentsRepository
import se.accidis.fmfg.app.services.Preferences
import timber.log.Timber

@Composable
fun DocumentEditScreen(
    onRowClick: (Int, DocumentRow) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel: DocumentEditViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                DocumentEditViewModel(
                    repository = DocumentsRepository.getInstance(appContext),
                    preferences = Preferences.getInstance(appContext)
                )
            }
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedDocuments by viewModel.savedDocuments.collectAsStateWithLifecycle()
    var isExportErrorVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is DocumentEditEvent.SharePdf -> {
                        if (!sharePdf(context, event.uri, event.subject, event.filename)) {
                            isExportErrorVisible = true
                        }
                    }

                    DocumentEditEvent.ExportError -> {
                        isExportErrorVisible = true
                    }
                }
            }
        }
    }

    when (val state = uiState) {
        is DocumentEditUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        is DocumentEditUiState.Success -> {
            var isNameBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
            var isOptionalFieldsBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
            var isOpenBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
            var activeAddressField by rememberSaveable { mutableStateOf<DocumentAddressField?>(null) }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = state.documentName.ifBlank { stringResource(R.string.document_nav_title) },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        actions = {
                            var showMenu by remember { mutableStateOf(false) }

                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = null
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.document_export)) },
                                    leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                                    enabled = !state.isExporting,
                                    onClick = {
                                        showMenu = false
                                        viewModel.exportToPdf(appContext)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.document_edit_opt_fields)) },
                                    leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        isOptionalFieldsBottomSheetVisible = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.document_set_name)) },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        isNameBottomSheetVisible = true
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.document_new)) },
                                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.createNewDocument()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.document_open)) },
                                    leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.loadSavedDocuments()
                                        isOpenBottomSheetVisible = true
                                    }
                                )
                            }
                        }
                    )
                }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    item(key = "addresses") {
                        DocumentAddressRows(
                            sender = state.sender,
                            recipient = state.recipient,
                            onAddressClick = { field -> activeAddressField = field }
                        )
                    }

                    if (state.rows.isEmpty()) {
                        item(key = "empty") {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.document_empty),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        itemsIndexed(items = state.rows) { index, row ->
                            DocumentRowItem(
                                row = row,
                                showFbet = state.showFbet,
                                showDivider = index < state.rows.lastIndex,
                                onClick = { onRowClick(index, row) }
                            )
                        }
                        item(key = "totals") {
                            DocumentTotalsRow(
                                totalNemKg = state.totalNemKg,
                                tpKatTotals = state.tpKatTotals,
                                totalValue = state.totalValue
                            )
                        }
                        if (state.isViolatingColoadingRules) {
                            item(key = "coloading_warning") {
                                DocumentColoadingWarningRow()
                            }
                        }
                    }
                }
            }

            activeAddressField?.let { field ->
                DocumentAddressBottomSheet(
                    initialFocusField = field,
                    initialSender = state.sender,
                    initialRecipient = state.recipient,
                    onDismiss = { newSender, newRecipient ->
                        viewModel.updateAddresses(
                            sender = newSender,
                            recipient = newRecipient
                        )
                        activeAddressField = null
                    }
                )
            }

            if (isNameBottomSheetVisible) {
                DocumentNameBottomSheet(
                    initialName = state.documentName,
                    onDismiss = { newName ->
                        viewModel.updateName(newName)
                        isNameBottomSheetVisible = false
                    }
                )
            }

            if (isOptionalFieldsBottomSheetVisible) {
                DocumentOptionalFieldsBottomSheet(
                    initialVehicleReg = state.vehicleReg,
                    initialVehicleType = state.vehicleType,
                    initialIsProtectedTransport = state.isProtectedTransport,
                    onDismiss = { vehicleReg, vehicleType, isProtectedTransport ->
                        viewModel.updateOptionalFields(
                            vehicleReg = vehicleReg,
                            vehicleType = vehicleType,
                            isProtectedTransport = isProtectedTransport
                        )
                        isOptionalFieldsBottomSheetVisible = false
                    }
                )
            }

            if (isOpenBottomSheetVisible) {
                DocumentOpenBottomSheet(
                    documentsResource = savedDocuments,
                    onDocumentClick = { id ->
                        viewModel.openDocument(id)
                        isOpenBottomSheetVisible = false
                    },
                    onDeleteDocument = { id ->
                        viewModel.deleteDocument(id)
                    },
                    onDismiss = { isOpenBottomSheetVisible = false }
                )
            }

            if (state.isExporting) {
                AlertDialog(
                    onDismissRequest = {},
                    properties = DialogProperties(
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false
                    ),
                    title = { Text(stringResource(R.string.document_export)) },
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator()
                            Text(stringResource(R.string.document_export_please_wait))
                        }
                    },
                    confirmButton = {}
                )
            }
        }
    }

    if (isExportErrorVisible) {
        AlertDialog(
            onDismissRequest = { isExportErrorVisible = false },
            title = { Text(stringResource(R.string.document_export)) },
            text = { Text(stringResource(R.string.document_export_error)) },
            confirmButton = {
                TextButton(onClick = { isExportErrorVisible = false }) {
                    Text(stringResource(R.string.generic_close))
                }
            }
        )
    }
}

private const val SAVE_PDF_REQUEST_CODE = 1001

private fun sharePdf(context: Context, uri: Uri, subject: String, filename: String): Boolean {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = PdfGenerator.PDF_CONTENT_TYPE
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        clipData = ClipData.newRawUri(subject, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooserIntent = Intent.createChooser(
        sendIntent,
        context.getString(R.string.document_export_chooser_title)
    ).apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val saveIntent = SavePdfActivity.createIntent(context, uri, filename)
            val options = ActivityOptions.makeBasic().apply {
                val startMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_ALWAYS
                } else {
                    @Suppress("DEPRECATION")
                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                }
                setPendingIntentCreatorBackgroundActivityStartMode(startMode)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                SAVE_PDF_REQUEST_CODE,
                saveIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                options.toBundle()
            )
            val saveAction = ChooserAction.Builder(
                Icon.createWithResource(context, R.drawable.ic_save),
                context.getString(R.string.document_export_save_as_file),
                pendingIntent
            ).build()
            putExtra(Intent.EXTRA_CHOOSER_CUSTOM_ACTIONS, arrayOf(saveAction))
        }
    }
    return try {
        context.startActivity(chooserIntent)
        true
    } catch (ex: ActivityNotFoundException) {
        Timber.e(ex, "No activity found to handle PDF share intent.")
        false
    }
}

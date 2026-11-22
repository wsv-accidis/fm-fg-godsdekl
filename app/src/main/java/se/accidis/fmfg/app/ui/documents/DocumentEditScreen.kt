package se.accidis.fmfg.app.ui.documents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.model.DocumentRow
import se.accidis.fmfg.app.services.DocumentsRepository
import se.accidis.fmfg.app.services.Preferences

@Composable
fun DocumentEditScreen(
    onRowClick: (Int, DocumentRow) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current.applicationContext
    val viewModel: DocumentEditViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                DocumentEditViewModel(
                    repository = DocumentsRepository.getInstance(context),
                    preferences = Preferences.getInstance(context)
                )
            }
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedDocuments by viewModel.savedDocuments.collectAsStateWithLifecycle()

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
                                    onClick = { showMenu = false }
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
        }
    }
}

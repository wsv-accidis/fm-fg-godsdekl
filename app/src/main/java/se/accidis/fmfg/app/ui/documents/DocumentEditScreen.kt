package se.accidis.fmfg.app.ui.documents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.model.DocumentRow
import se.accidis.fmfg.app.old.materials.ValueHelper
import se.accidis.fmfg.app.services.DocumentsRepository
import se.accidis.fmfg.app.services.Preferences

@OptIn(ExperimentalMaterial3Api::class)
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
                    preferences = Preferences(context)
                )
            }
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is DocumentEditUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        is DocumentEditUiState.Success -> {
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
                                    text = { Text(stringResource(R.string.document_set_name)) },
                                    leadingIcon = { Icon(Icons.Default.Save, contentDescription = null) },
                                    onClick = { showMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.document_edit_opt_fields)) },
                                    leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null) },
                                    onClick = { showMenu = false }
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
                                    onClick = { showMenu = false }
                                )
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                        .fillMaxSize()
                ) {
                    if (state.rows.isEmpty()) {
                        Text(
                            text = stringResource(R.string.document_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            itemsIndexed(items = state.rows) { index, row ->
                                DocumentRowItem(
                                    row = row,
                                    showFbet = state.showFbet,
                                    onClick = { onRowClick(index, row) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentRowItem(
    row: DocumentRow,
    showFbet: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val formattedAmount = remember(row.amount) { ValueHelper.formatValue(row.amount) }
    val amountText = if (row.hasNEM()) stringResource(R.string.document_amount_format, formattedAmount) else ""

    val middleLeftText = remember(row.material, showFbet, amountText) {
        buildString {
            val material = row.material
            if (material.fben.isNotBlank()) {
                if (showFbet && material.fbet.isNotBlank()) {
                    append(material.fbet)
                    append(' ')
                }
                append(material.fben)
            }
            if (amountText.isNotBlank()) {
                if (isNotEmpty()) append(' ')
                append(amountText)
            }
        }
    }

    val packagesText = remember(row, context) { row.packagesText(context) }
    val weightVolumeText = remember(row, context) { row.weightVolumeText(context) }

    Column(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left Column: Description, NEM summary
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Top Left: Material
                Text(
                    text = row.material.fullText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Middle Left: Fben/Fbet + amount
                if (middleLeftText.isNotBlank()) {
                    Text(
                        text = middleLeftText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Bottom Left: NEM
                if (row.hasNEM()) {
                    Text(
                        text = stringResource(
                            R.string.document_nem_format,
                            ValueHelper.formatValue(row.NEMkg)
                        ),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right Column: Packages, Weight/Volume
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Top Right: Packages
                if (packagesText.isNotBlank()) {
                    Text(
                        text = packagesText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End
                    )
                }

                // Middle Right: Weight/Volume
                if (weightVolumeText.isNotBlank()) {
                    Text(
                        text = weightVolumeText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

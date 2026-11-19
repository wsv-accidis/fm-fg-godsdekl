package se.accidis.fmfg.app.ui.documents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.model.DocumentLink
import se.accidis.fmfg.app.utils.Resource
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DocumentOpenBottomSheet(
    documentsResource: Resource<List<DocumentLink>>,
    onDocumentClick: (UUID) -> Unit,
    onDeleteDocument: (UUID) -> Unit,
    onDismiss: () -> Unit
) {
    var documentToDeleteId by rememberSaveable { mutableStateOf<UUID?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.document_saved_documents),
                style = MaterialTheme.typography.titleMedium
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (documentsResource) {
                    is Resource.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is Resource.Error -> {
                        Text(
                            text = stringResource(R.string.generic_unexpected_error),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.Center)
                        )
                    }

                    is Resource.Success -> {
                        if (documentsResource.data.isEmpty()) {
                            Text(
                                text = stringResource(R.string.documents_list_empty),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(
                                    items = documentsResource.data,
                                    key = { it.id ?: it.hashCode() }
                                ) { docLink ->
                                    DocumentLinkItem(
                                        docLink = docLink,
                                        onClick = {
                                            docLink.id?.let { id -> onDocumentClick(id) }
                                        },
                                        onDeleteClick = {
                                            documentToDeleteId = docLink.id
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.generic_close))
            }
        }
    }

    documentToDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { documentToDeleteId = null },
            text = {
                Text(stringResource(R.string.document_delete_heading))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteDocument(id)
                        documentToDeleteId = null
                    }
                ) {
                    Text(stringResource(R.string.generic_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { documentToDeleteId = null }) {
                    Text(stringResource(R.string.generic_no))
                }
            }
        )
    }
}

@Composable
private fun DocumentLinkItem(
    docLink: DocumentLink,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val formattedTimestamp = remember(docLink.timestamp) {
        docLink.timestamp?.toString("yyyy-MM-dd HH:mm") ?: ""
    }

    Column(modifier = Modifier.clickable(onClick = onClick)) {
        ListItem(
            supportingContent = {
                if (formattedTimestamp.isNotBlank()) {
                    Text(
                        text = formattedTimestamp,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            trailingContent = {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.document_delete)
                    )
                }
            }
        ) {
            Text(
                text = docLink.name.ifBlank { stringResource(R.string.document_no_name) },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

package se.accidis.fmfg.app.ui.documents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.model.DocumentRow
import se.accidis.fmfg.app.services.DocumentsRepository

@Composable
fun DocumentEditScreen() {
    val context = LocalContext.current.applicationContext
    val viewModel: DocumentEditViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                DocumentEditViewModel(DocumentsRepository.getInstance(context))
            }
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is DocumentEditUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is DocumentEditUiState.Success -> {
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
                        items(items = state.rows) { row ->
                            DocumentRowItem(row = row)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentRowItem(row: DocumentRow) {
    Column {
        ListItem(
            headlineContent = {
                Text(
                    text = row.material.namn,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                if (row.material.UNnr.isNotBlank()) {
                    Text(
                        text = "UN ${row.material.UNnr}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

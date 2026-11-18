package se.accidis.fmfg.app.ui.materials

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.services.LabelsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MaterialEditKlassKodBottomSheet(
    selectedKlassKods: List<String>,
    onToggleKlassKod: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sortedLabels = remember { LabelsRepository.allLabels.sortedBy { it.klassKod } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxHeight(0.6f)
        ) {
            Text(
                text = stringResource(R.string.material_klasskod_select),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(
                    items = sortedLabels,
                    key = { it.klassKod }
                ) { label ->
                    val isSelected = selectedKlassKods.contains(label.klassKod)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onToggleKlassKod(label.klassKod) },
                        label = { Text(label.klassKod) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(label.smallDrawable),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = Color.Unspecified
                            )
                        },
                        modifier = Modifier.height(48.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.generic_close))
            }
        }
    }
}

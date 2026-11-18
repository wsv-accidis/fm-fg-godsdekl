package se.accidis.fmfg.app.ui.materials

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.services.LabelsRepository
import se.accidis.fmfg.app.ui.components.ExpandableCard
import se.accidis.fmfg.app.ui.components.FieldLabel
import se.accidis.fmfg.app.ui.components.InputRow
import se.accidis.fmfg.app.ui.components.ReadOnlyDropdown

@Composable
internal fun MaterialEditPanel(
    uiState: MaterialLoadUiState,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAddKlassKodClick: () -> Unit,
    viewModel: MaterialLoadViewModel
) {
    ExpandableCard(
        titleResId = R.string.material_edit_material,
        isExpanded = isExpanded,
        onExpandedChange = onExpandedChange
    ) {
        MaterialEditFields(
            uiState = uiState,
            onAddKlassKodClick = onAddKlassKodClick,
            viewModel = viewModel
        )
    }
}

@Composable
private fun MaterialEditFields(
    uiState: MaterialLoadUiState,
    onAddKlassKodClick: () -> Unit,
    viewModel: MaterialLoadViewModel
) {
    val focusManager = LocalFocusManager.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InputRow {
            TextField(
                value = uiState.fbet,
                onValueChange = viewModel::onFbetChanged,
                label = { FieldLabel(R.string.material_fbet) },
                modifier = Modifier.width(150.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true
            )
            TextField(
                value = uiState.fben,
                onValueChange = viewModel::onFbenChanged,
                label = { FieldLabel(R.string.material_fben) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true
            )
        }

        InputRow {
            TextField(
                value = uiState.unNr,
                onValueChange = viewModel::onUnNrChanged,
                label = { FieldLabel(R.string.material_unnr) },
                modifier = Modifier.width(100.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )
            TextField(
                value = uiState.namn,
                onValueChange = viewModel::onNamnChanged,
                label = { FieldLabel(R.string.material_namn) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true
            )
        }

        KlassKodChips(
            klassKodList = uiState.klassKodList,
            onRemoveKlassKod = viewModel::onRemoveKlassKod,
            onAddClick = onAddKlassKodClick
        )

        InputRow {
            TextField(
                value = uiState.nemInputText,
                onValueChange = viewModel::onNemInputChanged,
                enabled = uiState.isNemEnabled,
                label = { FieldLabel(R.string.material_nem_per_piece) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                singleLine = true
            )

            ReadOnlyDropdown(
                labelResId = R.string.material_nem_unit,
                value = stringResource(uiState.nemUnitSelected.labelResId),
                options = NemUnit.entries,
                optionText = { stringResource(it.labelResId) },
                onOptionSelected = viewModel::onNemUnitSelected,
                modifier = Modifier.width(100.dp)
            )
        }

        val tpKatArray = stringArrayResource(R.array.material_tpkat_options)
        val tpKatOptions = remember(tpKatArray) { tpKatArray.toList() }
        val frpGrpArray = stringArrayResource(R.array.material_frpgrp_options)
        val frpGrpOptions = remember(frpGrpArray) { frpGrpArray.toList() }
        val tunnelKodArray = stringArrayResource(R.array.material_tunnelkod_options)
        val tunnelKodOptions = remember(tunnelKodArray) { tunnelKodArray.toList() }

        InputRow {
            ReadOnlyDropdown(
                labelResId = R.string.material_tpkat,
                value = uiState.tpKat.toString(),
                options = tpKatOptions,
                optionText = { it },
                onOptionSelected = { viewModel.onTpKatSelected(it.toInt()) },
                modifier = Modifier.weight(1f)
            )

            ReadOnlyDropdown(
                labelResId = R.string.material_frpgrp_short,
                value = uiState.frpGrp.ifEmpty { "-" },
                options = frpGrpOptions,
                optionText = { it.ifEmpty { "-" } },
                onOptionSelected = { viewModel.onFrpGrpSelected(if (it == "-") "" else it) },
                modifier = Modifier.weight(1f)
            )
        }

        InputRow {
            ReadOnlyDropdown(
                labelResId = R.string.material_tunnelkod,
                value = uiState.tunnelKod.ifEmpty { "-" },
                options = tunnelKodOptions,
                optionText = { it.ifEmpty { "-" } },
                onOptionSelected = { viewModel.onTunnelKodSelected(if (it == "-") "" else it) },
                modifier = Modifier.weight(1f)
            )

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.label_miljo_sm),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color.Unspecified
                    )
                    Text(
                        stringResource(R.string.material_miljofarligt),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Switch(checked = uiState.miljo, onCheckedChange = viewModel::onMiljoChanged)
            }
        }
    }
}

@Composable
private fun KlassKodChips(
    klassKodList: List<String>,
    onRemoveKlassKod: (String) -> Unit,
    onAddClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.material_klasskod),
            style = MaterialTheme.typography.labelMedium
        )
        val sortedKlassKods = remember(klassKodList) { klassKodList.sorted() }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sortedKlassKods.forEach { kod ->
                val label = LabelsRepository.getLabelByKlassKod(kod)
                InputChip(
                    selected = true,
                    onClick = { onRemoveKlassKod(kod) },
                    label = { Text(kod) },
                    modifier = Modifier.height(48.dp),
                    leadingIcon = {
                        label?.let {
                            Icon(
                                painter = painterResource(it.smallDrawable),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = Color.Unspecified
                            )
                        }
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
            AssistChip(
                onClick = onAddClick,
                label = { Text(stringResource(R.string.material_klasskod_add)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null
                    )
                }
            )
        }
    }
}

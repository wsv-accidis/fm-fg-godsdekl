package se.accidis.fmfg.app.ui.materials

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.model.Material
import se.accidis.fmfg.app.services.LabelsRepository

/**
 * Screen for editing material properties before loading it into a document.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MaterialLoadScreen(
    material: Material,
    onBack: () -> Unit
) {
    val viewModel: MaterialLoadViewModel = viewModel(
        key = material.uniqueKey,
        factory = viewModelFactory {
            initializer {
                MaterialLoadViewModel(material)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.material_material)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    Button(
                        onClick = { /* TODO: Implement load logic */ },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(stringResource(R.string.material_load))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = viewModel.fbet,
                    onValueChange = { viewModel.fbet = it },
                    label = { Text(stringResource(R.string.material_fbet)) },
                    modifier = Modifier.width(150.dp),
                    singleLine = true
                )
                TextField(
                    value = viewModel.fben,
                    onValueChange = { viewModel.fben = it },
                    label = { Text(stringResource(R.string.material_fben)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = viewModel.unNr,
                    onValueChange = { newValue ->
                        if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                            viewModel.unNr = newValue
                        }
                    },
                    label = { Text(stringResource(R.string.material_unnr)) },
                    modifier = Modifier.width(100.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                TextField(
                    value = viewModel.namn,
                    onValueChange = { viewModel.namn = it },
                    label = { Text(stringResource(R.string.material_namn)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.material_klasskod),
                    style = MaterialTheme.typography.labelMedium
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.klassKodList.sortedBy { it }.forEach { kod ->
                        val label = LabelsRepository.getLabelByKlassKod(kod)
                        InputChip(
                            selected = true,
                            onClick = { viewModel.klassKodList -= kod },
                            label = { Text(kod) },
                            modifier = Modifier.height(48.dp),
                            leadingIcon = {
                                if (label != null) {
                                    Icon(
                                        painter = painterResource(label.smallDrawable),
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
                        onClick = { viewModel.klassKodListVisible = true },
                        label = { Text(stringResource(R.string.material_klasskod_add)) },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = viewModel.nemInputText,
                    onValueChange = { viewModel.onNemInputChanged(it) },
                    enabled = viewModel.isNemEnabled,
                    label = { Text(stringResource(R.string.material_nem_per_piece)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = viewModel.nemUnitExpanded,
                    onExpandedChange = { viewModel.nemUnitExpanded = it },
                    modifier = Modifier.width(100.dp)
                ) {
                    TextField(
                        value = stringResource(viewModel.nemUnitSelected.labelResId),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.material_nem_unit)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = viewModel.nemUnitExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier.menuAnchor(
                            ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            true
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = viewModel.nemUnitExpanded,
                        onDismissRequest = { viewModel.nemUnitExpanded = false }
                    ) {
                        NemUnit.entries.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(stringResource(unit.labelResId)) },
                                onClick = { viewModel.onNemUnitSelected(unit) }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = viewModel.tpKatExpanded,
                    onExpandedChange = { viewModel.tpKatExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    TextField(
                        value = viewModel.tpKat.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.material_tpkat)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = viewModel.tpKatExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier.menuAnchor(
                            ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            true
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = viewModel.tpKatExpanded,
                        onDismissRequest = { viewModel.tpKatExpanded = false }
                    ) {
                        stringArrayResource(R.array.material_tpkat_options).forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.tpKat = option.toInt()
                                    viewModel.tpKatExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = viewModel.frpGrpExpanded,
                    onExpandedChange = { viewModel.frpGrpExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    TextField(
                        value = viewModel.frpGrp.ifEmpty { "-" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.material_frpgrp)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = viewModel.frpGrpExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier.menuAnchor(
                            ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            true
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = viewModel.frpGrpExpanded,
                        onDismissRequest = { viewModel.frpGrpExpanded = false }
                    ) {
                        stringArrayResource(R.array.material_frpgrp_options).forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.ifEmpty { "-" }) },
                                onClick = {
                                    viewModel.frpGrp = if (option == "-") "" else option
                                    viewModel.frpGrpExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExposedDropdownMenuBox(
                    expanded = viewModel.tunnelKodExpanded,
                    onExpandedChange = { viewModel.tunnelKodExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    TextField(
                        value = viewModel.tunnelKod.ifEmpty { "-" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.material_tunnelkod)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = viewModel.tunnelKodExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier.menuAnchor(
                            ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            true
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = viewModel.tunnelKodExpanded,
                        onDismissRequest = { viewModel.tunnelKodExpanded = false }
                    ) {
                        stringArrayResource(R.array.material_tunnelkod_options).forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.tunnelKod = if (option == "-") "" else option
                                    viewModel.tunnelKodExpanded = false
                                }
                            )
                        }
                    }
                }

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
                    Switch(checked = viewModel.miljo, onCheckedChange = { viewModel.miljo = it })
                }
            }
        }
    }

    if (viewModel.klassKodListVisible) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.klassKodListVisible = false },
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
                    items(LabelsRepository.allLabels.sortedBy { it.klassKod }) { label ->
                        val isSelected = viewModel.klassKodList.contains(label.klassKod)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    viewModel.klassKodList -= label.klassKod
                                } else {
                                    viewModel.klassKodList += label.klassKod
                                }
                            },
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
                    onClick = { viewModel.klassKodListVisible = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.generic_close))
                }
            }
        }
    }
}

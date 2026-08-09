package se.accidis.fmfg.app.ui.materials

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.model.Material
import se.accidis.fmfg.app.old.materials.ValueHelper
import se.accidis.fmfg.app.services.LabelsRepository
import java.math.BigDecimal
import java.math.RoundingMode

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
            MaterialSummaryLabels(viewModel)
            DocumentRowFields(viewModel)
            MaterialEditPanel(viewModel)
        }
    }

    if (viewModel.isKlassKodListVisible) {
        MaterialEditKlassKodBottomSheet(viewModel)
    }
}

@Composable
private fun MaterialSummaryLabels(viewModel: MaterialLoadViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val fbetFben = "${viewModel.fbet} ${viewModel.fben}".trim()
        if (fbetFben.isNotBlank()) {
            Text(
                text = fbetFben,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (viewModel.unNr.isNotBlank()) {
            Text(
                text = "UN ${viewModel.unNr} ${viewModel.namn}".trim(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentRowFields(viewModel: MaterialLoadViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = viewModel.numberOfPkgs,
                onValueChange = {
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) viewModel.numberOfPkgs =
                        it
                },
                label = { Text(stringResource(R.string.material_load_num_pkgs)) },
                modifier = Modifier.width(100.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            TextField(
                value = viewModel.typeOfPkgs,
                onValueChange = { viewModel.typeOfPkgs = it },
                label = { Text(stringResource(R.string.material_load_type_pkgs)) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = viewModel.weightVolume,
                onValueChange = {
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) viewModel.weightVolume =
                        it
                },
                label = { Text(stringResource(R.string.material_load_weight_volume)) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            val units = stringArrayResource(R.array.unit_weight_volume)
            if (viewModel.weightVolumeUnit.isEmpty() && units.isNotEmpty()) {
                viewModel.weightVolumeUnit = units[0]
            }

            ExposedDropdownMenuBox(
                expanded = viewModel.isWeightVolumeUnitExpanded,
                onExpandedChange = { viewModel.isWeightVolumeUnitExpanded = it },
                modifier = Modifier.width(120.dp)
            ) {
                TextField(
                    value = viewModel.weightVolumeUnit,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Enhet") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = viewModel.isWeightVolumeUnitExpanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.menuAnchor(
                        ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                        true
                    )
                )
                ExposedDropdownMenu(
                    expanded = viewModel.isWeightVolumeUnitExpanded,
                    onDismissRequest = { viewModel.isWeightVolumeUnitExpanded = false }
                ) {
                    units.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit) },
                            onClick = {
                                viewModel.weightVolumeUnit = unit
                                viewModel.isWeightVolumeUnitExpanded = false
                            }
                        )
                    }
                }
            }
        }

        NemCalculationPanel(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NemCalculationPanel(viewModel: MaterialLoadViewModel) {
    ElevatedCard(
        onClick = { viewModel.isNemPanelExpanded = !viewModel.isNemPanelExpanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.material_load_nem_calculation),
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = if (viewModel.isNemPanelExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = viewModel.isNemPanelExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (viewModel.nemMgValue == 0L) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = stringResource(R.string.material_load_nem_warning),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    TextField(
                        value = viewModel.amount,
                        onValueChange = {
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) viewModel.amount =
                                it
                        },
                        label = { Text(stringResource(R.string.material_load_amount)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    val amountBd = ValueHelper.parseValue(viewModel.amount)
                    val totalNemMgBd = amountBd.multiply(BigDecimal(viewModel.nemMgValue))
                    val displayUnit =
                        if (totalNemMgBd < BigDecimal(100_000L)) NemUnit.GRAM else NemUnit.KILOGRAM

                    val totalNemDisplayValue = ValueHelper.formatValue(
                        totalNemMgBd.divide(
                            BigDecimal(displayUnit.factor),
                            6,
                            RoundingMode.FLOOR
                        )
                    )

                    Text(
                        text = stringResource(
                            R.string.material_load_nem_total,
                            totalNemDisplayValue,
                            stringResource(displayUnit.labelResId)
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialEditPanel(viewModel: MaterialLoadViewModel) {
    ElevatedCard(
        onClick = { viewModel.isEditPanelExpanded = !viewModel.isEditPanelExpanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.material_edit_material),
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = if (viewModel.isEditPanelExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = viewModel.isEditPanelExpanded) {
                MaterialEditFields(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun MaterialEditFields(viewModel: MaterialLoadViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    onClick = { viewModel.isKlassKodListVisible = true },
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
                expanded = viewModel.itTpKatExpanded,
                onExpandedChange = { viewModel.itTpKatExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                TextField(
                    value = viewModel.tpKat.toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.material_tpkat)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = viewModel.itTpKatExpanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.menuAnchor(
                        ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                        true
                    )
                )
                ExposedDropdownMenu(
                    expanded = viewModel.itTpKatExpanded,
                    onDismissRequest = { viewModel.itTpKatExpanded = false }
                ) {
                    stringArrayResource(R.array.material_tpkat_options).forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.tpKat = option.toInt()
                                viewModel.itTpKatExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = viewModel.isFrpGrpExpanded,
                onExpandedChange = { viewModel.isFrpGrpExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                TextField(
                    value = viewModel.frpGrp.ifEmpty { "-" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.material_frpgrp_short)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = viewModel.isFrpGrpExpanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.menuAnchor(
                        ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                        true
                    )
                )
                ExposedDropdownMenu(
                    expanded = viewModel.isFrpGrpExpanded,
                    onDismissRequest = { viewModel.isFrpGrpExpanded = false }
                ) {
                    stringArrayResource(R.array.material_frpgrp_options).forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.ifEmpty { "-" }) },
                            onClick = {
                                viewModel.frpGrp = if (option == "-") "" else option
                                viewModel.isFrpGrpExpanded = false
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
                expanded = viewModel.isTunnelKodExpanded,
                onExpandedChange = { viewModel.isTunnelKodExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                TextField(
                    value = viewModel.tunnelKod.ifEmpty { "-" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.material_tunnelkod)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = viewModel.isTunnelKodExpanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.menuAnchor(
                        ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                        true
                    )
                )
                ExposedDropdownMenu(
                    expanded = viewModel.isTunnelKodExpanded,
                    onDismissRequest = { viewModel.isTunnelKodExpanded = false }
                ) {
                    stringArrayResource(R.array.material_tunnelkod_options).forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.tunnelKod = if (option == "-") "" else option
                                viewModel.isTunnelKodExpanded = false
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MaterialEditKlassKodBottomSheet(viewModel: MaterialLoadViewModel) {
    ModalBottomSheet(
        onDismissRequest = { viewModel.isKlassKodListVisible = false },
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
                onClick = { viewModel.isKlassKodListVisible = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.generic_close))
            }
        }
    }
}

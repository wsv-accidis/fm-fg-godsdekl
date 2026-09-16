package se.accidis.fmfg.app.ui.materials

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.model.Material
import se.accidis.fmfg.app.old.materials.ValueHelper
import se.accidis.fmfg.app.services.DocumentsRepository
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
    sessionId: String,
    onBack: () -> Unit
) {
    val viewModel: MaterialLoadViewModel = viewModel(
        key = "${material.uniqueKey}_$sessionId",
        factory = viewModelFactory {
            initializer {
                MaterialLoadViewModel(material)
            }
        }
    )

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val onLoad = {
        if (viewModel.canLoadMaterial) {
            focusManager.clearFocus()
            viewModel.loadIntoDocument(DocumentsRepository.getInstance(context))
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.material_material)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = onLoad,
                    enabled = viewModel.canLoadMaterial,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(stringResource(R.string.material_load))
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
            DocumentRowFields(viewModel, onLoad)
            MaterialEditPanel(viewModel, onLoad)
        }
    }

    if (viewModel.isKlassKodListVisible) {
        MaterialEditKlassKodBottomSheet(viewModel)
    }
}

@Composable
private fun MaterialSummaryLabels(viewModel: MaterialLoadViewModel) {
    val labels = listOfNotNull(
        "${viewModel.fbet} ${viewModel.fben}".trim().takeIf { it.isNotBlank() },
        listOfNotNull(
            viewModel.UNnr.takeIf { it.isNotBlank() }?.let { "UN $it" },
            viewModel.namn.takeIf { it.isNotBlank() }
        ).joinToString(" ").takeIf { it.isNotBlank() }
    )

    if (labels.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentRowFields(
    viewModel: MaterialLoadViewModel,
    onLoad: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InputRow {
            TextField(
                value = viewModel.numberOfPkgs,
                onValueChange = { viewModel.onNumberOfPkgsChanged(it) },
                label = { FieldLabel(R.string.material_load_num_pkgs) },
                modifier = Modifier.width(100.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = viewModel.isTypeOfPkgsExpanded,
                onExpandedChange = { viewModel.isTypeOfPkgsExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                val suggestions = stringArrayResource(R.array.material_pkg_types)
                val filteredSuggestions = suggestions.filter {
                    it.contains(viewModel.typeOfPkgs, ignoreCase = true)
                }

                TextField(
                    value = viewModel.typeOfPkgs,
                    onValueChange = {
                        viewModel.typeOfPkgs = it
                        viewModel.isTypeOfPkgsExpanded = true
                    },
                    label = { FieldLabel(R.string.material_load_type_pkgs) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true
                )

                if (filteredSuggestions.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = viewModel.isTypeOfPkgsExpanded,
                        onDismissRequest = { viewModel.isTypeOfPkgsExpanded = false }
                    ) {
                        filteredSuggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = {
                                    viewModel.typeOfPkgs = suggestion
                                    viewModel.isTypeOfPkgsExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        InputRow {
            TextField(
                value = viewModel.weightVolume,
                onValueChange = { viewModel.onWeightVolumeChanged(it) },
                label = { FieldLabel(R.string.material_load_weight_volume) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = if (viewModel.NEMmgValue > 0) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onLoad() }),
                singleLine = true
            )

            val unitOptions = WeightVolumeUnit.entries.toList()
            val unitLabels = unitOptions.map { stringResource(it.labelResId) }

            ReadOnlyDropdown(
                labelResId = R.string.material_load_weight_volume_unit,
                value = stringResource(viewModel.weightVolumeUnit.labelResId),
                options = unitLabels.toTypedArray(),
                expanded = viewModel.isWeightVolumeUnitExpanded,
                onExpandedChange = { viewModel.isWeightVolumeUnitExpanded = it },
                onOptionSelected = { label ->
                    val index = unitLabels.indexOf(label)
                    if (index >= 0) {
                        viewModel.weightVolumeUnit = unitOptions[index]
                    }
                },
                modifier = Modifier.width(120.dp)
            )
        }

        NemCalculationPanel(viewModel, onLoad)
        CalculationSummary(viewModel)
    }
}

@Composable
private fun CalculationSummary(viewModel: MaterialLoadViewModel) {
    val multiplier = ValueHelper.getMultiplierByTpKat(viewModel.tpKat)
    val calculatedMassBd = viewModel.weightVolumePointsBasis
    val pointsBd = viewModel.totalPoints

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.material_load_multiplier),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.material_load_value_basis),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.material_load_value),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = multiplier.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = ValueHelper.formatValue(calculatedMassBd),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(
                    R.string.unit_points_format,
                    ValueHelper.formatValue(pointsBd)
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun NemCalculationPanel(
    viewModel: MaterialLoadViewModel,
    onLoad: () -> Unit
) {
    ExpandableCard(
        titleResId = R.string.material_load_nem_calculation,
        isExpanded = viewModel.isNemPanelExpanded,
        onExpandedChange = { viewModel.isNemPanelExpanded = it }
    ) {
        if (viewModel.NEMmgValue == 0L) {
            InputRow {
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
            onValueChange = { viewModel.onAmountChanged(it) },
            label = { FieldLabel(R.string.material_load_amount) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onLoad() }),
            singleLine = true
        )

        val totalNemMgBd = viewModel.totalNEMmg
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

@Composable
private fun MaterialEditPanel(
    viewModel: MaterialLoadViewModel,
    onLoad: () -> Unit
) {
    ExpandableCard(
        titleResId = R.string.material_edit_material,
        isExpanded = viewModel.isEditPanelExpanded,
        onExpandedChange = { viewModel.isEditPanelExpanded = it }
    ) {
        MaterialEditFields(viewModel, onLoad)
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun MaterialEditFields(
    viewModel: MaterialLoadViewModel,
    onLoad: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InputRow {
            TextField(
                value = viewModel.fbet,
                onValueChange = { viewModel.fbet = it },
                label = { FieldLabel(R.string.material_fbet) },
                modifier = Modifier.width(150.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true
            )
            TextField(
                value = viewModel.fben,
                onValueChange = { viewModel.fben = it },
                label = { FieldLabel(R.string.material_fben) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true
            )
        }

        InputRow {
            TextField(
                value = viewModel.UNnr,
                onValueChange = { viewModel.onUNnrChanged(it) },
                label = { FieldLabel(R.string.material_unnr) },
                modifier = Modifier.width(100.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )
            TextField(
                value = viewModel.namn,
                onValueChange = { viewModel.namn = it },
                label = { FieldLabel(R.string.material_namn) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true
            )
        }

        KlassKodChips(viewModel)

        InputRow {
            TextField(
                value = viewModel.NEMInputText,
                onValueChange = { viewModel.onNEMInputChanged(it) },
                enabled = viewModel.isNEMEnabled,
                label = { FieldLabel(R.string.material_nem_per_piece) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onLoad() }),
                singleLine = true
            )

            val nemOptions = NemUnit.entries.toList()
            val nemLabels = nemOptions.map { stringResource(it.labelResId) }

            ReadOnlyDropdown(
                labelResId = R.string.material_nem_unit,
                value = stringResource(viewModel.NEMUnitSelected.labelResId),
                options = nemLabels.toTypedArray(),
                expanded = viewModel.NEMUnitExpanded,
                onExpandedChange = { viewModel.NEMUnitExpanded = it },
                onOptionSelected = { label ->
                    val index = nemLabels.indexOf(label)
                    if (index >= 0) {
                        viewModel.onNemUnitSelected(nemOptions[index])
                    }
                },
                modifier = Modifier.width(100.dp)
            )
        }

        InputRow {
            ReadOnlyDropdown(
                labelResId = R.string.material_tpkat,
                value = viewModel.tpKat.toString(),
                options = stringArrayResource(R.array.material_tpkat_options),
                expanded = viewModel.isTpKatExpanded,
                onExpandedChange = { viewModel.isTpKatExpanded = it },
                onOptionSelected = { viewModel.tpKat = it.toInt() },
                modifier = Modifier.weight(1f)
            )

            ReadOnlyDropdown(
                labelResId = R.string.material_frpgrp_short,
                value = viewModel.frpGrp.ifEmpty { "-" },
                options = stringArrayResource(R.array.material_frpgrp_options),
                expanded = viewModel.isFrpGrpExpanded,
                onExpandedChange = { viewModel.isFrpGrpExpanded = it },
                onOptionSelected = { viewModel.frpGrp = if (it == "-") "" else it },
                modifier = Modifier.weight(1f)
            )
        }

        InputRow {
            ReadOnlyDropdown(
                labelResId = R.string.material_tunnelkod,
                value = viewModel.tunnelKod.ifEmpty { "-" },
                options = stringArrayResource(R.array.material_tunnelkod_options),
                expanded = viewModel.isTunnelKodExpanded,
                onExpandedChange = { viewModel.isTunnelKodExpanded = it },
                onOptionSelected = { viewModel.tunnelKod = if (it == "-") "" else it },
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
                Switch(checked = viewModel.miljo, onCheckedChange = { viewModel.miljo = it })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun KlassKodChips(viewModel: MaterialLoadViewModel) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandableCard(
    @StringRes titleResId: Int,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        onClick = { onExpandedChange(!isExpanded) },
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
                    text = stringResource(titleResId),
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
private fun FieldLabel(@StringRes stringResId: Int) {
    Text(
        text = stringResource(stringResId),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun InputRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadOnlyDropdown(
    @StringRes labelResId: Int,
    value: String,
    options: Array<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier
    ) {
        TextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { FieldLabel(labelResId) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.ifEmpty { "-" }) },
                    onClick = {
                        onOptionSelected(option)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

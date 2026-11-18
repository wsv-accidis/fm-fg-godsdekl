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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialLoadScreen(
    material: Material,
    rowIndex: Int? = null,
    sessionId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val sessionKey = "${material.uniqueKey}_${rowIndex}_$sessionId"
    val viewModel: MaterialLoadViewModel = viewModel(
        key = sessionKey,
        factory = viewModelFactory {
            initializer {
                MaterialLoadViewModel(
                    repository = DocumentsRepository.getInstance(context),
                    material = material,
                    rowIndex = rowIndex
                )
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isNemPanelExpanded by rememberSaveable(sessionKey) {
        mutableStateOf(uiState.nemMgValue > 0)
    }
    var isEditPanelExpanded by rememberSaveable(sessionKey) { mutableStateOf(false) }
    var isKlassKodListVisible by rememberSaveable(sessionKey) { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val onLoad = {
        if (uiState.canLoadMaterial) {
            focusManager.clearFocus()
            viewModel.loadIntoDocument()
            onBack()
        }
    }

    val buttonTextResId =
        if (uiState.isEditing) R.string.material_edit_load else R.string.material_load

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.material_material)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = onLoad,
                    enabled = uiState.canLoadMaterial,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(stringResource(buttonTextResId))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MaterialSummaryLabels(uiState)
            DocumentRowFields(
                uiState = uiState,
                isNemPanelExpanded = isNemPanelExpanded,
                onNemPanelExpandedChange = { isNemPanelExpanded = it },
                viewModel = viewModel,
                onLoad = onLoad
            )
            MaterialEditPanel(
                uiState = uiState,
                isExpanded = isEditPanelExpanded,
                onExpandedChange = { isEditPanelExpanded = it },
                onAddKlassKodClick = { isKlassKodListVisible = true },
                viewModel = viewModel
            )
        }
    }

    if (isKlassKodListVisible) {
        MaterialEditKlassKodBottomSheet(
            selectedKlassKods = uiState.klassKodList,
            onToggleKlassKod = viewModel::onToggleKlassKod,
            onDismiss = { isKlassKodListVisible = false }
        )
    }
}

@Composable
private fun MaterialSummaryLabels(uiState: MaterialLoadUiState) {
    val labels = listOfNotNull(
        "${uiState.fbet} ${uiState.fben}".trim().takeIf { it.isNotBlank() },
        listOfNotNull(
            uiState.unNr.takeIf { it.isNotBlank() }?.let { "UN $it" },
            uiState.namn.takeIf { it.isNotBlank() }
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
    uiState: MaterialLoadUiState,
    isNemPanelExpanded: Boolean,
    onNemPanelExpandedChange: (Boolean) -> Unit,
    viewModel: MaterialLoadViewModel,
    onLoad: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InputRow {
            TextField(
                value = uiState.numberOfPkgs,
                onValueChange = viewModel::onNumberOfPkgsChanged,
                label = { FieldLabel(R.string.material_load_num_pkgs) },
                modifier = Modifier.width(100.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )

            var isTypeOfPkgsExpanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = isTypeOfPkgsExpanded,
                onExpandedChange = { isTypeOfPkgsExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                val suggestions = stringArrayResource(R.array.material_pkg_types)
                val filteredSuggestions = remember(suggestions, uiState.typeOfPkgs) {
                    suggestions.filter {
                        it.contains(uiState.typeOfPkgs, ignoreCase = true)
                    }
                }

                TextField(
                    value = uiState.typeOfPkgs,
                    onValueChange = {
                        viewModel.onTypeOfPkgsChanged(it)
                        isTypeOfPkgsExpanded = true
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
                        expanded = isTypeOfPkgsExpanded,
                        onDismissRequest = { isTypeOfPkgsExpanded = false }
                    ) {
                        filteredSuggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = {
                                    viewModel.onTypeOfPkgsChanged(suggestion)
                                    isTypeOfPkgsExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        InputRow {
            TextField(
                value = uiState.weightVolume,
                onValueChange = viewModel::onWeightVolumeChanged,
                label = { FieldLabel(R.string.material_load_weight_volume) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = if (uiState.nemMgValue > 0 && isNemPanelExpanded) {
                        ImeAction.Next
                    } else {
                        ImeAction.Done
                    }
                ),
                keyboardActions = KeyboardActions(onDone = { onLoad() }),
                singleLine = true
            )

            ReadOnlyDropdown(
                labelResId = R.string.material_load_weight_volume_unit,
                value = stringResource(uiState.weightVolumeUnit.labelResId),
                options = WeightVolumeUnit.entries,
                optionText = { stringResource(it.labelResId) },
                onOptionSelected = viewModel::onWeightVolumeUnitSelected,
                modifier = Modifier.width(120.dp)
            )
        }

        NemCalculationPanel(
            uiState = uiState,
            isExpanded = isNemPanelExpanded,
            onExpandedChange = onNemPanelExpandedChange,
            onAmountChanged = viewModel::onAmountChanged,
            onLoad = onLoad
        )
        CalculationSummary(uiState)
    }
}

@Composable
private fun CalculationSummary(uiState: MaterialLoadUiState) {
    val multiplier = ValueHelper.getMultiplierByTpKat(uiState.tpKat)
    val calculatedMassBd = uiState.weightVolumePointsBasis
    val pointsBd = uiState.totalPoints

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
    uiState: MaterialLoadUiState,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAmountChanged: (String) -> Unit,
    onLoad: () -> Unit
) {
    ExpandableCard(
        titleResId = R.string.material_load_nem_calculation,
        isExpanded = isExpanded,
        onExpandedChange = onExpandedChange
    ) {
        if (uiState.nemMgValue == 0L) {
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
            value = uiState.amount,
            onValueChange = onAmountChanged,
            label = { FieldLabel(R.string.material_load_amount) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onLoad() }),
            singleLine = true
        )

        val totalNemMgBd = uiState.totalNemMg
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MaterialEditKlassKodBottomSheet(
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
private fun <T> ReadOnlyDropdown(
    @StringRes labelResId: Int,
    value: String,
    options: List<T>,
    optionText: @Composable (T) -> String,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        TextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { FieldLabel(labelResId) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionText(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

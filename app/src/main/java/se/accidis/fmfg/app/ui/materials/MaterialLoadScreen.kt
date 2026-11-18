package se.accidis.fmfg.app.ui.materials

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.model.Material
import se.accidis.fmfg.app.ui.ValueHelper
import se.accidis.fmfg.app.services.DocumentsRepository
import se.accidis.fmfg.app.ui.components.ExpandableCard
import se.accidis.fmfg.app.ui.components.FieldLabel
import se.accidis.fmfg.app.ui.components.InputRow
import se.accidis.fmfg.app.ui.components.ReadOnlyDropdown
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
                        .navigationBarsPadding()
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

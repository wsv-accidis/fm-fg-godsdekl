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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.model.Material
import se.accidis.fmfg.app.old.materials.ValueHelper
import se.accidis.fmfg.app.services.LabelsRepository
import java.math.BigDecimal
import java.math.RoundingMode

enum class NemUnit(val labelResId: Int, val factor: Long) {
    NONE(R.string.unit_none, 0L),
    GRAM(R.string.unit_g, 1_000L),
    KILOGRAM(R.string.unit_kg, 1_000_000L)
}

/**
 * Screen for editing material properties before loading it into a document.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MaterialLoadScreen(
    material: Material,
    onBack: () -> Unit
) {
    var fbet by remember { mutableStateOf(material.fbet) }
    var fben by remember { mutableStateOf(material.fben) }
    var unNr by remember { mutableStateOf(material.UNnr) }
    var namn by remember { mutableStateOf(material.namn) }

    var klassKodList by remember { mutableStateOf(material.klassKod) }
    var klassKodListVisible by remember { mutableStateOf(false) }

    var nemMgActual by remember { mutableLongStateOf(material.NEMmg.toLong()) }
    var nemUnitExpanded by remember { mutableStateOf(false) }
    val nemUnitInitial = when {
        nemMgActual == 0L -> NemUnit.NONE
        nemMgActual < 100_000L -> NemUnit.GRAM
        else -> NemUnit.KILOGRAM
    }
    var nemUnitSelected by remember { mutableStateOf(nemUnitInitial) }
    var nemInputText by remember {
        mutableStateOf(
            if (nemUnitInitial == NemUnit.NONE) ""
            else ValueHelper.formatValue(
                BigDecimal(nemMgActual).divide(
                    BigDecimal(nemUnitInitial.factor),
                    6,
                    RoundingMode.FLOOR
                )
            )
        )
    }

    var tpKat by remember { mutableIntStateOf(material.tpKat) }
    var tpKatExpanded by remember { mutableStateOf(false) }

    var frpGrp by remember { mutableStateOf(material.frpGrp) }
    var frpGrpExpanded by remember { mutableStateOf(false) }

    var tunnelKod by remember { mutableStateOf(material.tunnelKod) }
    var tunnelKodExpanded by remember { mutableStateOf(false) }

    var miljo by remember { mutableStateOf(material.miljo) }

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
                    value = fbet,
                    onValueChange = { fbet = it },
                    label = { Text(stringResource(R.string.material_fbet)) },
                    modifier = Modifier.width(150.dp),
                    singleLine = true
                )
                TextField(
                    value = fben,
                    onValueChange = { fben = it },
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
                    value = unNr,
                    onValueChange = { newValue ->
                        if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                            unNr = newValue
                        }
                    },
                    label = { Text(stringResource(R.string.material_unnr)) },
                    modifier = Modifier.width(100.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                TextField(
                    value = namn,
                    onValueChange = { namn = it },
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
                    klassKodList.sortedBy { it }.forEach { kod ->
                        val label = LabelsRepository.getLabelByKlassKod(kod)
                        InputChip(
                            selected = true,
                            onClick = { klassKodList = klassKodList - kod },
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
                        onClick = { klassKodListVisible = true },
                        label = { Text(stringResource(R.string.generic_add)) },
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
                    value = nemInputText,
                    onValueChange = { newValue ->
                        // Allow digits, dots, and commas
                        if (newValue.isEmpty() || newValue.all { it.isDigit() || it == '.' || it == ',' }) {
                            nemInputText = newValue
                            if (nemUnitSelected != NemUnit.NONE) {
                                val parsed = ValueHelper.parseValue(newValue)
                                nemMgActual =
                                    parsed.multiply(BigDecimal(nemUnitSelected.factor)).toLong()
                            }
                        }
                    },
                    enabled = nemUnitSelected != NemUnit.NONE,
                    label = { Text(stringResource(R.string.material_nem_per_piece)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = nemUnitExpanded,
                    onExpandedChange = { nemUnitExpanded = it },
                    modifier = Modifier.width(100.dp)
                ) {
                    TextField(
                        value = stringResource(nemUnitSelected.labelResId),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.material_nem_unit)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = nemUnitExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier.menuAnchor(
                            ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            true
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = nemUnitExpanded,
                        onDismissRequest = { nemUnitExpanded = false }
                    ) {
                        NemUnit.entries.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(stringResource(unit.labelResId)) },
                                onClick = {
                                    nemUnitSelected = unit
                                    nemUnitExpanded = false

                                    if (unit == NemUnit.NONE) {
                                        nemInputText = ""
                                    } else {
                                        // Recalculate based on existing internal mg to preserve precision
                                        val value = BigDecimal(nemMgActual).divide(
                                            BigDecimal(unit.factor),
                                            6,
                                            RoundingMode.FLOOR
                                        )
                                        nemInputText = ValueHelper.formatValue(value)
                                    }
                                }
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
                    expanded = tpKatExpanded,
                    onExpandedChange = { tpKatExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    TextField(
                        value = tpKat.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.material_tpkat)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tpKatExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier.menuAnchor(
                            ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            true
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = tpKatExpanded,
                        onDismissRequest = { tpKatExpanded = false }
                    ) {
                        stringArrayResource(R.array.material_tpkat_options).forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    tpKat = option.toInt()
                                    tpKatExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = frpGrpExpanded,
                    onExpandedChange = { frpGrpExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    TextField(
                        value = frpGrp.ifEmpty { "-" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.material_frpgrp)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frpGrpExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier.menuAnchor(
                            ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            true
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = frpGrpExpanded,
                        onDismissRequest = { frpGrpExpanded = false }
                    ) {
                        stringArrayResource(R.array.material_frpgrp_options).forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.ifEmpty { "-" }) },
                                onClick = {
                                    frpGrp = if (option == "-") "" else option
                                    frpGrpExpanded = false
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
                    expanded = tunnelKodExpanded,
                    onExpandedChange = { tunnelKodExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    TextField(
                        value = tunnelKod.ifEmpty { "-" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.material_tunnelkod)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tunnelKodExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier.menuAnchor(
                            ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            true
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = tunnelKodExpanded,
                        onDismissRequest = { tunnelKodExpanded = false }
                    ) {
                        stringArrayResource(R.array.material_tunnelkod_options).forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    tunnelKod = if (option == "-") "" else option
                                    tunnelKodExpanded = false
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
                    Switch(checked = miljo, onCheckedChange = { miljo = it })
                }
            }
        }
    }

    if (klassKodListVisible) {
        ModalBottomSheet(
            onDismissRequest = { klassKodListVisible = false },
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
                        val isSelected = klassKodList.contains(label.klassKod)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                klassKodList = if (isSelected) {
                                    klassKodList - label.klassKod
                                } else {
                                    klassKodList + label.klassKod
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
                    onClick = { klassKodListVisible = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.generic_close))
                }
            }
        }
    }
}

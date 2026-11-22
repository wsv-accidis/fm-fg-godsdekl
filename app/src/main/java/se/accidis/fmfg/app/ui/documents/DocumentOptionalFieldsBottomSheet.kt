package se.accidis.fmfg.app.ui.documents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.ui.components.FieldLabel
import se.accidis.fmfg.app.ui.components.InputRow
import se.accidis.fmfg.app.ui.components.rememberSheetDismissHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DocumentOptionalFieldsBottomSheet(
    initialVehicleReg: String,
    initialVehicleType: String,
    initialIsProtectedTransport: Boolean,
    onDismiss: (vehicleReg: String, vehicleType: String, isProtectedTransport: Boolean) -> Unit
) {
    var vehicleReg by rememberSaveable(initialVehicleReg) { mutableStateOf(initialVehicleReg) }
    var vehicleType by rememberSaveable(initialVehicleType) { mutableStateOf(initialVehicleType) }
    var isProtectedTransport by rememberSaveable(initialIsProtectedTransport) {
        mutableStateOf(initialIsProtectedTransport)
    }

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )
    val dismissWithAnimation = rememberSheetDismissHandler(sheetState)
    val focusManager = LocalFocusManager.current

    val notifyDismiss = {
        onDismiss(vehicleReg, vehicleType, isProtectedTransport)
    }

    ModalBottomSheet(
        onDismissRequest = notifyDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.document_edit_opt_fields),
                style = MaterialTheme.typography.titleMedium
            )

            InputRow {
                TextField(
                    value = vehicleReg,
                    onValueChange = { vehicleReg = it },
                    label = { FieldLabel(R.string.document_vehicle_reg) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true
                )

                var isVehicleTypeExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = isVehicleTypeExpanded,
                    onExpandedChange = { isVehicleTypeExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    val suggestions = stringArrayResource(R.array.document_vehicle_types)
                    val filteredSuggestions = remember(suggestions, vehicleType) {
                        suggestions.filter {
                            it.contains(vehicleType, ignoreCase = true)
                        }
                    }

                    TextField(
                        value = vehicleType,
                        onValueChange = {
                            vehicleType = it
                            isVehicleTypeExpanded = true
                        },
                        label = { FieldLabel(R.string.document_vehicle_type) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                isVehicleTypeExpanded = false
                                focusManager.clearFocus()
                            }
                        ),
                        singleLine = true
                    )

                    if (filteredSuggestions.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = isVehicleTypeExpanded,
                            onDismissRequest = { isVehicleTypeExpanded = false }
                        ) {
                            filteredSuggestions.forEach { suggestion ->
                                DropdownMenuItem(
                                    text = { Text(suggestion) },
                                    onClick = {
                                        vehicleType = suggestion
                                        isVehicleTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.document_protected_transport),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = isProtectedTransport,
                    onCheckedChange = { isProtectedTransport = it }
                )
            }

            Button(
                onClick = { dismissWithAnimation(notifyDismiss) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.generic_close))
            }
        }
    }
}

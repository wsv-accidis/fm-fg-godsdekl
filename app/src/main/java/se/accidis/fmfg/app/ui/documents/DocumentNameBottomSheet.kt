package se.accidis.fmfg.app.ui.documents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.ui.components.rememberSheetDismissHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DocumentNameBottomSheet(
    initialName: String,
    onDismiss: (String) -> Unit
) {
    var textValue by rememberSaveable(initialName) { mutableStateOf(initialName) }
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )
    val dismissWithAnimation = rememberSheetDismissHandler(sheetState)

    ModalBottomSheet(
        onDismissRequest = { onDismiss(textValue) },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.document_set_name_heading),
                style = MaterialTheme.typography.titleMedium
            )

            TextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = { Text(stringResource(R.string.document_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { dismissWithAnimation { onDismiss(textValue) } }
                ),
                singleLine = true
            )

            Button(
                onClick = { dismissWithAnimation { onDismiss(textValue) } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.generic_close))
            }
        }
    }
}

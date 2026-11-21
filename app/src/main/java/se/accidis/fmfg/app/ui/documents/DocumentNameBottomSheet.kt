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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import se.accidis.fmfg.app.R

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
    val scope = rememberCoroutineScope()
    var isDismissing by remember { mutableStateOf(false) }

    fun dismissWithAnimation() {
        if (isDismissing) return
        isDismissing = true
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismiss(textValue)
            } else {
                isDismissing = false
            }
        }
    }

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
                keyboardActions = KeyboardActions(onDone = { dismissWithAnimation() }),
                singleLine = true
            )

            Button(
                onClick = { dismissWithAnimation() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.generic_close))
            }
        }
    }
}

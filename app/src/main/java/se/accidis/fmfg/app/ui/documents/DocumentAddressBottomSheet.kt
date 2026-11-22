package se.accidis.fmfg.app.ui.documents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.ui.components.FieldLabel
import se.accidis.fmfg.app.ui.components.rememberSheetDismissHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DocumentAddressBottomSheet(
    initialFocusField: DocumentAddressField,
    initialSender: String,
    initialRecipient: String,
    onDismiss: (sender: String, recipient: String) -> Unit
) {
    var sender by rememberSaveable(initialSender) { mutableStateOf(initialSender) }
    var recipient by rememberSaveable(initialRecipient) { mutableStateOf(initialRecipient) }

    val senderFocusRequester = remember { FocusRequester() }
    val recipientFocusRequester = remember { FocusRequester() }

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )
    val dismissWithAnimation = rememberSheetDismissHandler(sheetState)

    val notifyDismiss = {
        onDismiss(sender, recipient)
    }

    LaunchedEffect(initialFocusField) {
        when (initialFocusField) {
            DocumentAddressField.SENDER -> senderFocusRequester.requestFocus()
            DocumentAddressField.RECIPIENT -> recipientFocusRequester.requestFocus()
        }
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
            TextField(
                value = sender,
                onValueChange = { sender = it },
                label = { FieldLabel(R.string.document_sender) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(senderFocusRequester),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                minLines = 3,
                maxLines = 5
            )

            TextField(
                value = recipient,
                onValueChange = { recipient = it },
                label = { FieldLabel(R.string.document_recipient) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(recipientFocusRequester),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                minLines = 3,
                maxLines = 5
            )

            Button(
                onClick = { dismissWithAnimation(notifyDismiss) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.generic_close))
            }
        }
    }
}

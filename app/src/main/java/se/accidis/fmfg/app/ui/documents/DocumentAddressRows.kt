package se.accidis.fmfg.app.ui.documents

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.accidis.fmfg.app.R

internal enum class DocumentAddressField(
    @StringRes val labelResId: Int,
    @StringRes val emptyTextResId: Int
) {
    SENDER(
        labelResId = R.string.document_sender,
        emptyTextResId = R.string.document_tap_to_edit_sender
    ),
    RECIPIENT(
        labelResId = R.string.document_recipient,
        emptyTextResId = R.string.document_tap_to_edit_recipient
    )
}

@Composable
internal fun DocumentAddressRows(
    sender: String,
    recipient: String,
    onAddressClick: (DocumentAddressField) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        DocumentAddressItem(
            field = DocumentAddressField.SENDER,
            value = sender,
            onClick = { onAddressClick(DocumentAddressField.SENDER) }
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        DocumentAddressItem(
            field = DocumentAddressField.RECIPIENT,
            value = recipient,
            onClick = { onAddressClick(DocumentAddressField.RECIPIENT) }
        )

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun DocumentAddressItem(
    field: DocumentAddressField,
    value: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (value.isBlank()) {
            Text(
                text = stringResource(field.emptyTextResId),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = stringResource(field.labelResId),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

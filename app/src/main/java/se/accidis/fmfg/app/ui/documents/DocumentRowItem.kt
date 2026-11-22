package se.accidis.fmfg.app.ui.documents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.model.DocumentRow
import se.accidis.fmfg.app.utils.ValueHelper

@Composable
internal fun DocumentRowItem(
    row: DocumentRow,
    showFbet: Boolean,
    showDivider: Boolean = true,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val formattedAmount = remember(row.amount) { ValueHelper.formatValue(row.amount) }
    val amountText =
        if (row.hasNEM()) stringResource(R.string.document_amount_format, formattedAmount) else ""

    val middleLeftText = remember(row.material, showFbet, amountText) {
        buildString {
            val material = row.material
            if (material.fben.isNotBlank()) {
                if (showFbet && material.fbet.isNotBlank()) {
                    append(material.fbet)
                    append(' ')
                }
                append(material.fben)
            }
            if (amountText.isNotBlank()) {
                if (isNotEmpty()) append(' ')
                append(amountText)
            }
        }
    }

    val packagesText = remember(row, context) { row.packagesText(context) }
    val weightVolumeText = remember(row, context) { row.weightVolumeText(context) }

    Column(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left Column: Description, NEM summary
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Top Left: Material
                Text(
                    text = row.material.fullText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Middle Left: Fben/Fbet + amount
                if (middleLeftText.isNotBlank()) {
                    Text(
                        text = middleLeftText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Bottom Left: NEM
                if (row.hasNEM()) {
                    val formattedNem = remember(row.NEMkg) { ValueHelper.formatValue(row.NEMkg) }
                    Text(
                        text = stringResource(
                            R.string.document_nem_format,
                            formattedNem
                        ),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right Column: Packages, Weight/Volume
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Top Right: Packages
                if (packagesText.isNotBlank()) {
                    Text(
                        text = packagesText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End
                    )
                }

                // Middle Right: Weight/Volume
                if (weightVolumeText.isNotBlank()) {
                    Text(
                        text = weightVolumeText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

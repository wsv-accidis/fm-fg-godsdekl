package se.accidis.fmfg.app.ui.documents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.utils.ValueHelper
import java.math.BigDecimal

@Composable
internal fun DocumentTotalsRow(
    totalNemKg: BigDecimal,
    tpKatTotals: List<TpKatTotal>,
    totalValue: BigDecimal,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (totalNemKg.signum() != 0) {
                val formattedNem = remember(totalNemKg) { ValueHelper.formatValue(totalNemKg) }
                Text(
                    text = stringResource(R.string.document_summary_total_nem_format, formattedNem),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            tpKatTotals.forEach { total ->
                val hasWeight = total.weightKg.signum() != 0
                val hasVolume = total.volumeLiter.signum() != 0
                val weightText = if (hasWeight) {
                    val formattedWeight = remember(total.weightKg) { ValueHelper.formatValue(total.weightKg) }
                    stringResource(R.string.unit_kg_format, formattedWeight)
                } else {
                    null
                }
                val volumeText = if (hasVolume) {
                    val formattedVolume = remember(total.volumeLiter) { ValueHelper.formatValue(total.volumeLiter) }
                    stringResource(R.string.unit_liter_format, formattedVolume)
                } else {
                    null
                }
                val weightVolumeText = listOfNotNull(weightText, volumeText).joinToString(", ")
                val formattedValue = remember(total.value) { ValueHelper.formatValue(total.value) }

                Text(
                    text = stringResource(
                        R.string.document_summary_tpkat_format,
                        total.tpKat,
                        weightVolumeText,
                        formattedValue
                    ),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val formattedTotalValue = remember(totalValue) { ValueHelper.formatValue(totalValue) }
            Text(
                text = stringResource(R.string.document_summary_total_format, formattedTotalValue),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

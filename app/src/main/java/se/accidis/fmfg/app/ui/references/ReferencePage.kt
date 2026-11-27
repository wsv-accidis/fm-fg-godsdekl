package se.accidis.fmfg.app.ui.references

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import se.accidis.fmfg.app.R

/**
 * Documentation and reference pages displayed in the References tab.
 */
@Serializable
enum class ReferencePage(
    @StringRes val titleResId: Int,
    @StringRes val descriptionResId: Int,
    val icon: ImageVector,
    val assetUrl: String,
) {
    ADR_INSTRUCTIONS(
        titleResId = R.string.reference_adr_title,
        descriptionResId = R.string.reference_adr_description,
        icon = Icons.Default.HealthAndSafety,
        assetUrl = "file:///android_asset/www/instr_adr.html",
    ),
    COLOADING(
        titleResId = R.string.reference_coloading_title,
        descriptionResId = R.string.reference_coloading_description,
        icon = Icons.Default.TableChart,
        assetUrl = "file:///android_asset/www/instr_samlast.html",
    ),
    HANDBOOK(
        titleResId = R.string.reference_handbook_title,
        descriptionResId = R.string.reference_handbook_description,
        icon = Icons.AutoMirrored.Filled.MenuBook,
        assetUrl = "file:///android_asset/www/instr_hbok.html",
    ),
    ABOUT(
        titleResId = R.string.reference_about_title,
        descriptionResId = R.string.reference_about_description,
        icon = Icons.Default.Info,
        assetUrl = "file:///android_asset/www/instr_om.html",
    ),
}

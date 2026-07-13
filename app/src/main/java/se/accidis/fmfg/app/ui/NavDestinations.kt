package se.accidis.fmfg.app.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import se.accidis.fmfg.app.R

/**
 * Base interface for all navigation destinations in the app.
 */
@Serializable
sealed interface NavDestination

/**
 * Destination for the Materials list screen.
 */
@Serializable
data object Materials : NavDestination

/**
 * Destination for the Documents list screen.
 */
@Serializable
data object Documents : NavDestination

/**
 * Destination for the References screen.
 */
@Serializable
data object References : NavDestination

/**
 * Destination for the Settings screen.
 */
@Serializable
data object Settings : NavDestination

/**
 * Destination for the Material load screen, taking a material as a parameter.
 */
@Serializable
data class MaterialLoad(val material: se.accidis.fmfg.app.model.Material) : NavDestination

/**
 * Enum representing top-level destinations in the app's navigation bar.
 */
enum class TopLevelDestination(
    val route: NavDestination,
    val icon: ImageVector,
    @StringRes val labelTextId: Int
) {
    MATERIALS(Materials, Icons.Default.List, R.string.materials_nav_title),
    DOCUMENTS(Documents, Icons.Default.Description, R.string.documents_list_nav_title),
    REFERENCES(References, Icons.Default.Info, R.string.instructions_nav_title),
    SETTINGS(Settings, Icons.Default.Settings, R.string.preferences_nav_title)
}

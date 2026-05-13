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

@Serializable
sealed interface NavDestination

@Serializable
data object Materials : NavDestination

@Serializable
data object Documents : NavDestination

@Serializable
data object References : NavDestination

@Serializable
data object Settings : NavDestination

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

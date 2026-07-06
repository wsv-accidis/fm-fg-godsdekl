package se.accidis.fmfg.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import se.accidis.fmfg.app.ui.theme.AppTheme

/**
 * Main activity. Container for the Compose UI and top-level navigation.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                MainView()
            }
        }
    }
}

@Composable
fun MainView() {
    var currentDestination: NavDestination by remember { mutableStateOf(Materials) }

    Scaffold(
        bottomBar = {
            FmFgNavigationBar(
                currentDestination = currentDestination,
                onNavigate = { currentDestination = it }
            )
        }
    ) { innerPadding ->
        BoxWithPadding(modifier = Modifier.padding(innerPadding)) {
            when (currentDestination) {
                is Materials -> MaterialsScreen()
                is Documents -> DocumentsScreen()
                is References -> ReferencesScreen()
                is Settings -> SettingsScreen()
            }
        }
    }
}

@Composable
fun BoxWithPadding(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier) {
        content()
    }
}

@Composable
fun FmFgNavigationBar(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit
) {
    NavigationBar {
        TopLevelDestination.entries.forEach { item ->
            NavigationBarItem(
                selected = currentDestination == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(stringResource(item.labelTextId)) }
            )
        }
    }
}

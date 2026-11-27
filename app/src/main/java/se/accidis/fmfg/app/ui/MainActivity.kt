package se.accidis.fmfg.app.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import se.accidis.fmfg.app.ui.documents.DocumentEditScreen
import se.accidis.fmfg.app.ui.materials.MaterialLoadScreen
import se.accidis.fmfg.app.ui.materials.MaterialsScreen
import se.accidis.fmfg.app.ui.references.ReferenceDetailScreen
import se.accidis.fmfg.app.ui.references.ReferencesScreen
import se.accidis.fmfg.app.ui.theme.AppTheme

/**
 * Main activity. Container for the Compose UI and top-level navigation.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            AppTheme {
                MainView()
            }
        }
    }
}

@Composable
fun MainView() {
    val backStack = rememberNavBackStack(Materials)
    val currentDestination = (backStack.lastOrNull() as? NavDestination) ?: Materials

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (currentDestination !is MaterialLoad && currentDestination !is ReferenceDetail) {
                FmFgNavigationBar(
                    currentDestination = currentDestination,
                    onNavigate = { route ->
                        if (route != currentDestination) {
                            backStack.removeAll { it !is Materials }
                            if (route !is Materials) {
                                backStack.add(route)
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            entryProvider = entryProvider {
                entry<Materials> {
                    MaterialsScreen(
                        onMaterialClick = { material ->
                            backStack.add(MaterialLoad(material))
                        }
                    )
                }

                entry<DocumentEdit> {
                    DocumentEditScreen(
                        onRowClick = { index, row ->
                            backStack.add(
                                MaterialLoad(
                                    material = row.material,
                                    rowIndex = index
                                )
                            )
                        }
                    )
                }

                entry<References> {
                    ReferencesScreen(
                        onPageClick = { page ->
                            backStack.add(ReferenceDetail(page))
                        }
                    )
                }

                entry<ReferenceDetail> { dest ->
                    ReferenceDetailScreen(
                        page = dest.page,
                        onBack = {
                            if (backStack.size > 1) {
                                backStack.removeLastOrNull()
                            }
                        }
                    )
                }

                entry<Settings> {
                    SettingsScreen()
                }

                entry<MaterialLoad> { dest ->
                    MaterialLoadScreen(
                        material = dest.material,
                        rowIndex = dest.rowIndex,
                        sessionId = dest.sessionId,
                        onBack = {
                            if (backStack.size > 1) {
                                backStack.removeLastOrNull()
                            }
                        }
                    )
                }
            }
        )
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

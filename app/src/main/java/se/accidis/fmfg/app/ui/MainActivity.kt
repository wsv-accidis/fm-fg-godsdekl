package se.accidis.fmfg.app.ui

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import se.accidis.fmfg.app.ui.theme.AppTheme

/**
 * Main activity. Only functions as a container for the Compose UI.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContent {
            AppTheme {

            }
        }
    }
}

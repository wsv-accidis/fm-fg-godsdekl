package se.accidis.fmfg.app.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberSheetDismissHandler(sheetState: SheetState): (() -> Unit) -> Unit {
    val scope = rememberCoroutineScope()
    val isDismissing = remember { mutableStateOf(false) }

    return remember(sheetState, scope) {
        { afterHide ->
            if (!isDismissing.value) {
                isDismissing.value = true
                scope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        afterHide()
                    } else {
                        isDismissing.value = false
                    }
                }
            }
        }
    }
}

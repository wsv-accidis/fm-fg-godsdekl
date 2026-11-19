package se.accidis.fmfg.app.services

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.UUID

/**
 * Wrapper for Android shared preferences. Used to store application data between launches.
 */
class Preferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

    var currentDocumentId: UUID?
        get() = prefs.getString(Keys.CURRENT_DOCUMENT_ID, null)?.let {
            try {
                UUID.fromString(it)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
        set(value) {
            prefs.edit {
                if (value != null) {
                    putString(Keys.CURRENT_DOCUMENT_ID, value.toString())
                } else {
                    remove(Keys.CURRENT_DOCUMENT_ID)
                }
            }
        }

    val defaultAuthor: String
        get() = prefs.getString(Keys.DEFAULT_AUTHOR, "").orEmpty()

    var favoriteMaterials: MutableSet<String>
        get() {
            val set = prefs.getStringSet(
                Keys.FAVORITE_MATERIALS,
                null
            )
            return if (set != null) HashSet(set) else HashSet()
        }
        set(materialsKeys) {
            prefs.edit {
                putStringSet(
                    Keys.FAVORITE_MATERIALS,
                    materialsKeys
                )
            }
        }

    fun shouldShowAuthorInDocument(): Boolean {
        return prefs.getBoolean(Keys.SHOW_AUTHOR_IN_DOCUMENT, false)
    }

    fun shouldShowFbetInDocument(): Boolean {
        return prefs.getBoolean(Keys.SHOW_FBET_IN_DOCUMENT, false)
    }

    val showFbetInDocumentFlow: Flow<Boolean> = callbackFlow {
        trySend(shouldShowFbetInDocument())
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == Keys.SHOW_FBET_IN_DOCUMENT || key == null) {
                trySend(shouldShowFbetInDocument())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged()

    object Keys {
        const val CURRENT_DOCUMENT_ID: String = "CurrentDocumentId"
        const val DEFAULT_AUTHOR: String = "DefaultAuthor"
        const val FAVORITE_MATERIALS: String = "FavoriteMaterials"
        const val SHOW_AUTHOR_IN_DOCUMENT: String = "ShowAuthorInDocument"
        const val SHOW_FBET_IN_DOCUMENT: String = "ShowFbetInDocument"
    }

    companion object {
        const val PREFERENCES_FILE: String = "ThePreferences"
    }
}

package se.accidis.fmfg.app.services

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Wrapper for Android shared preferences. Used to store application data between launches.
 */
class Preferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

    val defaultAuthor: String
        get() = prefs.getString(Keys.DEFAULT_AUTHOR, "")!!

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

    object Keys {
        const val DEFAULT_AUTHOR: String = "DefaultAuthor"
        const val FAVORITE_MATERIALS: String = "FavoriteMaterials"
        const val SHOW_AUTHOR_IN_DOCUMENT: String = "ShowAuthorInDocument"
        const val SHOW_FBET_IN_DOCUMENT: String = "ShowFbetInDocument"
    }

    companion object {
        const val PREFERENCES_FILE: String = "ThePreferences"
    }
}

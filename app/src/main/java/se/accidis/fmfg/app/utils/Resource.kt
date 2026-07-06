package se.accidis.fmfg.app.utils

/**
 * A generic class that holds a value with its loading status.
 */
sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val throwable: Throwable) : Resource<Nothing>()
}

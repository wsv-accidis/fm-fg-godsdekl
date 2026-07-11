package se.accidis.fmfg.app.utils

/**
 * A generic class that holds a value with its loading status.
 */
sealed class Resource<out T> {
    /** Represents the loading state of a resource. */
    object Loading : Resource<Nothing>()
    /** Represents the successful state of a resource with data. */
    data class Success<out T>(val data: T) : Resource<T>()
    /** Represents the error state of a resource with a throwable. */
    data class Error(val throwable: Throwable) : Resource<Nothing>()
}

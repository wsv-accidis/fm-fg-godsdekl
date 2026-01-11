package se.accidis.fmfg.app.utils

/**
 * Extends every class with a TAG for logging.
 */
val Any.TAG: String
    get() = this::class.java.simpleName

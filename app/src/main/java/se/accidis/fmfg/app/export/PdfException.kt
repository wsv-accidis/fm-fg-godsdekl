package se.accidis.fmfg.app.export

/**
 * Exception class for exceptions thrown during export to PDF.
 */
class PdfException(cause: Throwable) : Exception(cause.message, cause)

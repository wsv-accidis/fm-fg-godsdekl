package se.accidis.fmfg.app.export;

/**
 * Exception class for exceptions thrown during export to PDF.
 */
public final class PdfException extends Exception {
	public PdfException(Exception ex) {
		super(ex.getMessage(), ex);
	}
}

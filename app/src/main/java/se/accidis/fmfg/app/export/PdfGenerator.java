package se.accidis.fmfg.app.export;

import android.util.Log;

import com.pdfjet.A4;
import com.pdfjet.Box;
import com.pdfjet.Color;
import com.pdfjet.Line;
import com.pdfjet.PDF;
import com.pdfjet.Page;
import com.pdfjet.Point;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

import se.accidis.fmfg.app.model.Document;

/**
 * Generates a PDF from a Document.
 */
public final class PdfGenerator {
	public static final String PDF_CONTENT_TYPE = "application/pdf";
	public static final String PDF_EXTENSION = ".pdf";
	private final static String TAG = PdfGenerator.class.getSimpleName();
	private final Document mDocument;
	private final PDF mPdf;

	private PdfGenerator(Document document, PDF pdf) {
		mDocument = document;
		mPdf = pdf;
	}

	public static void exportToPdf(Document document, ExportFile exportFile) throws PdfException {
		BufferedOutputStream outputStream;
		try {
			outputStream = new BufferedOutputStream(new FileOutputStream(exportFile.getFile()));
		} catch (Exception ex) {
			Log.e(TAG, "Exception while trying to open file \"" + exportFile.getFilename() + "\".");
			throw new PdfException(ex);
		}

		PDF pdf = null;
		try {
			pdf = new PDF(outputStream);
			PdfGenerator generator = new PdfGenerator(document, pdf);
			generator.writeContent();
		} catch (Exception ex) {
			Log.e(TAG, "Exception while trying to write PDF to \"" + exportFile.getFilename() + "\".");
			throw new PdfException(ex);
		} finally {
			try {
				if (null != pdf) {
					pdf.close();
				}
			} catch (Exception ignored) {
			}
			try {
				outputStream.close();
			} catch (Exception ignored) {
			}
		}
	}

	private void writeContent() throws Exception {
		Page page = new Page(mPdf, A4.PORTRAIT);

		Box flag = new Box();
		flag.setLocation(100f, 100f);
		flag.setSize(190f, 100f);
		flag.setColor(Color.white);
		flag.drawOn(page);

		float sw = 7.69f;   // stripe width
		Line stripe = new Line(0.0f, sw / 2, 190.0f, sw / 2);
		stripe.setWidth(sw);
		stripe.setColor(Color.oldgloryred);
		for (int row = 0; row < 7; row++) {
			stripe.placeIn(flag, 0.0f, row * 2 * sw);
			stripe.drawOn(page);
		}

		Box union = new Box();
		union.setSize(76.0f, 53.85f);
		union.setColor(Color.oldgloryblue);
		union.setFillShape(true);
		union.placeIn(flag, 0f, 0f);
		union.drawOn(page);

		float h_si = 12.6f; // horizontal star interval
		float v_si = 10.8f; // vertical star interval
		Point star = new Point(h_si / 2, v_si / 2);
		star.setShape(Point.STAR);
		star.setRadius(3.0f);
		star.setColor(Color.white);
		star.setFillShape(true);

		for (int row = 0; row < 6; row++) {
			for (int col = 0; col < 5; col++) {
				star.placeIn(union, row * h_si, col * v_si);
				star.drawOn(page);
			}
		}

		star.setLocation(h_si, v_si);
		for (int row = 0; row < 5; row++) {
			for (int col = 0; col < 4; col++) {
				star.placeIn(union, row * h_si, col * v_si);
				star.drawOn(page);
			}
		}
	}
}

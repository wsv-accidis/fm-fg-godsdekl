package se.accidis.fmfg.app.export;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.pdfjet.A4;
import com.pdfjet.Cell;
import com.pdfjet.CoreFont;
import com.pdfjet.Font;
import com.pdfjet.Image;
import com.pdfjet.ImageType;
import com.pdfjet.Line;
import com.pdfjet.PDF;
import com.pdfjet.Page;
import com.pdfjet.Point;
import com.pdfjet.Table;
import com.pdfjet.TextBlock;
import com.pdfjet.TextLine;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Document;
import se.accidis.fmfg.app.model.DocumentRow;
import se.accidis.fmfg.app.ui.materials.LabelsHelper;
import se.accidis.fmfg.app.utils.AndroidUtils;

/**
 * Generates a PDF from a Document.
 */
public final class PdfGenerator {
	public static final String PDF_CONTENT_TYPE = "application/pdf";
	public static final String PDF_EXTENSION = ".pdf";
	private static final float ADDRESS_BLOCK_MIN_HEIGHT = 60.0f;
	private static final float FOOTER_BOTTOM_MARGIN = 35.0f;
	private static final float HORIZONTAL_MARGIN = 40.0f;
	private static final float INNER_MARGIN = 5.0f;
	private static final int LABELS_PER_ROW = 8;
	private static final float PAGE_HEIGHT = A4.PORTRAIT[1];
	private static final float PAGE_WIDTH = A4.PORTRAIT[0];
	private static final float CONTENT_WIDTH = PAGE_WIDTH - HORIZONTAL_MARGIN * 2.0f;
	private static final float LABEL_SIZE = CONTENT_WIDTH / LABELS_PER_ROW - INNER_MARGIN;
	private static final float CENTER_OF_PAGE = HORIZONTAL_MARGIN + CONTENT_WIDTH / 2.0f;
	private final static String TAG = PdfGenerator.class.getSimpleName();
	private static final float VERTICAL_MARGIN = 50.0f;
	private final Context mContext;
	private final Document mDocument;
	private final Font mLabelFont;
	private final PDF mPdf;
	private final Font mTextFont;
	private final Font mVanityFont;

	private PdfGenerator(Document document, PDF pdf, Context context) throws Exception {
		mDocument = document;
		mPdf = pdf;
		mContext = context;

		mLabelFont = new Font(mPdf, CoreFont.TIMES_ROMAN);
		mLabelFont.setSize(6.0f);

		mTextFont = new Font(mPdf, CoreFont.TIMES_ROMAN);
		mLabelFont.setSize(10.0f);

		mVanityFont = new Font(mPdf, CoreFont.TIMES_ROMAN);
		mVanityFont.setSize(5.0f);
		mVanityFont.setItalic(true);
	}

	public static void exportToPdf(Document document, ExportFile exportFile, Context context) throws FileNotFoundException, PdfException {
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(exportFile.getFile()));

		PDF pdf = null;
		try {
			pdf = new PDF(outputStream);
			PdfGenerator generator = new PdfGenerator(document, pdf, context);
			generator.writeDocument();
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

	@NonNull
	private Table createTable(float headerBottom, float footerHeight) throws Exception {
		Table table = new Table();
		table.setData(createTableData(), Table.DATA_HAS_1_HEADER_ROWS);
		table.setLocation(HORIZONTAL_MARGIN, headerBottom + INNER_MARGIN);
		table.setBottomMargin(VERTICAL_MARGIN + footerHeight);
		table.setNoCellBorders();

		table.setColumnWidth(0, CONTENT_WIDTH * 0.7f);
		table.setColumnWidth(1, CONTENT_WIDTH * 0.15f);
		table.setColumnWidth(2, CONTENT_WIDTH * 0.15f);
		table.wrapAroundCellText();

		return table;
	}

	private List<List<Cell>> createTableData() throws Exception {
		List<List<Cell>> rows = new ArrayList<>();
		createTableHeader(rows);

		for (int i = 0; i < 30; i++) { // TODO Just for test
			for (DocumentRow documentRow : mDocument.getRows()) {
				List<Cell> row = new ArrayList<>(3);

				Cell materialCell = new Cell(mTextFont, documentRow.getMaterial().getFullText());
				materialCell.setLeftPadding(0);
				row.add(materialCell);
				Cell packagesCell = new Cell(mTextFont, documentRow.getPackagesText(mContext));
				row.add(packagesCell);
				Cell weightVolumeCell = new Cell(mTextFont, documentRow.getWeightVolumeText(mContext));
				row.add(weightVolumeCell);

				rows.add(row);
			}
		}

		return rows;
	}

	private void createTableHeader(List<List<Cell>> rows) {
		List<Cell> row = new ArrayList<>(3);

		Cell materialHeaderCell = new Cell(mLabelFont, mContext.getString(R.string.document_export_material_header));
		materialHeaderCell.setLeftPadding(0);
		row.add(materialHeaderCell);

		Cell packagesHeaderCell = new Cell(mLabelFont, mContext.getString(R.string.document_export_pkgs_header));
		row.add(packagesHeaderCell);

		Cell weightVolumeHeaderCell = new Cell(mLabelFont, mContext.getString(R.string.document_export_weight_volume_header));
		row.add(weightVolumeHeaderCell);

		rows.add(row);
	}

	private void writeDocument() throws Exception {
		Page page = new Page(mPdf, A4.PORTRAIT);

		final float headerBottom = writeDocumentHeader(page, CENTER_OF_PAGE);
		float footerHeight = writeDocumentFooter(page);

		Table table = createTable(headerBottom, footerHeight);
		final int pageTotal = table.getNumberOfPages(page);
		int pageNo = 1;

		TextLine vanityLabel = new TextLine(mVanityFont, String.format(mContext.getString(R.string.document_export_vanity_format), mContext.getString(R.string.app_name), AndroidUtils.getAppVersionName(mContext)));
		vanityLabel.setLocation(HORIZONTAL_MARGIN, PAGE_HEIGHT - FOOTER_BOTTOM_MARGIN);
		vanityLabel.drawOn(page);

		TextLine pageLabel = new TextLine(mTextFont);
		writePageLabel(page, pageLabel, pageNo, pageTotal);

		Point tableBottom = table.drawOn(page);
		while (table.hasMoreData()) {
			page = new Page(mPdf, A4.PORTRAIT);
			table.setLocation(HORIZONTAL_MARGIN, VERTICAL_MARGIN);
			tableBottom = table.drawOn(page);

			vanityLabel.drawOn(page);
			writePageLabel(page, pageLabel, ++pageNo, pageTotal);
		}

		TextLine endLabel = new TextLine(mLabelFont, "SLUT");
		endLabel.setLocation(HORIZONTAL_MARGIN, tableBottom.getY() + endLabel.getHeight() + INNER_MARGIN);
		endLabel.drawOn(page);
	}

	private float writeDocumentFooter(Page page) throws Exception {
		List<Integer> labels = LabelsHelper.getLabelsByDocument(mDocument, true);
		if (labels.isEmpty()) {
			return 0.0f;
		}

		final int numRows = 1 + labels.size() / LABELS_PER_ROW;
		final float labelBoxSize = LABEL_SIZE + INNER_MARGIN;
		final float verticalLoc = PAGE_HEIGHT - VERTICAL_MARGIN - numRows * labelBoxSize;
		int column = 0, row = 0;

		for (Integer labelResId : labels) {
			InputStream labelStream = mContext.getResources().openRawResource(labelResId);
			Image image = new Image(mPdf, labelStream, ImageType.PNG);
			image.setLocation(HORIZONTAL_MARGIN + column * labelBoxSize, verticalLoc + row * labelBoxSize);
			image.scaleBy(LABEL_SIZE / Math.max(image.getWidth(), image.getHeight()));
			image.drawOn(page);

			if (LABELS_PER_ROW == ++column) {
				column = 0;
				row++;
			}
		}

		TextLine labelsLabel = new TextLine(mLabelFont, mContext.getString(R.string.document_export_labels));
		labelsLabel.setLocation(HORIZONTAL_MARGIN, verticalLoc - 2 * INNER_MARGIN);
		labelsLabel.drawOn(page);

		final float separatorTop = verticalLoc - labelsLabel.getHeight() - 2 * INNER_MARGIN;
		Line separatorLine = new Line(HORIZONTAL_MARGIN, separatorTop, CONTENT_WIDTH + HORIZONTAL_MARGIN, separatorTop);
		separatorLine.drawOn(page);

		return numRows * labelBoxSize + labelsLabel.getHeight() + 2 * INNER_MARGIN;
	}

	private float writeDocumentHeader(Page page, float centerOfPage) throws Exception {
		TextLine senderLabel = new TextLine(mLabelFont, mContext.getString(R.string.document_sender));
		senderLabel.setLocation(HORIZONTAL_MARGIN, VERTICAL_MARGIN + mLabelFont.getHeight());
		senderLabel.drawOn(page);
		TextLine recipientLabel = new TextLine(mLabelFont, mContext.getString(R.string.document_recipient));
		recipientLabel.setLocation(centerOfPage + INNER_MARGIN, VERTICAL_MARGIN + mLabelFont.getHeight());
		recipientLabel.drawOn(page);

		final float addressBlockWidth = CONTENT_WIDTH / 2.0f - INNER_MARGIN;
		final float labelHeight = senderLabel.getHeight();

		TextBlock senderBlock = new TextBlock(mTextFont);
		senderBlock.setText(TextUtils.isEmpty(mDocument.getSender()) ? "" : mDocument.getSender());
		senderBlock.setLocation(HORIZONTAL_MARGIN, VERTICAL_MARGIN + labelHeight + INNER_MARGIN);
		senderBlock.setWidth(addressBlockWidth);
		senderBlock.drawOn(page);
		TextBlock recipientBlock = new TextBlock(mTextFont);
		recipientBlock.setText(TextUtils.isEmpty(mDocument.getRecipient()) ? "" : mDocument.getRecipient());
		recipientBlock.setLocation(centerOfPage + INNER_MARGIN, VERTICAL_MARGIN + labelHeight + INNER_MARGIN);
		recipientBlock.setWidth(addressBlockWidth);
		recipientBlock.drawOn(page);

		final float addressBlockHeight = Math.max(ADDRESS_BLOCK_MIN_HEIGHT, Math.max(recipientBlock.getHeight(), senderBlock.getHeight()));
		final float headerBottom = VERTICAL_MARGIN + labelHeight + addressBlockHeight + 2 * INNER_MARGIN;

		Line line = new Line(centerOfPage, VERTICAL_MARGIN, centerOfPage, headerBottom);
		line.drawOn(page);
		line = new Line(HORIZONTAL_MARGIN, headerBottom, HORIZONTAL_MARGIN + CONTENT_WIDTH, headerBottom);
		line.drawOn(page);

		return headerBottom;
	}

	private void writePageLabel(Page page, TextLine pageLabel, int pageNo, int pageTotal) throws Exception {
		pageLabel.setText(String.format(mContext.getString(R.string.document_export_page_format), pageNo, pageTotal));
		pageLabel.setLocation(PAGE_WIDTH - HORIZONTAL_MARGIN - pageLabel.getWidth(), PAGE_HEIGHT - FOOTER_BOTTOM_MARGIN);
		pageLabel.drawOn(page);
	}
}

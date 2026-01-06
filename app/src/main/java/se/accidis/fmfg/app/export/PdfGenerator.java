package se.accidis.fmfg.app.export;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.pdfjet.A4;
import com.pdfjet.Cell;
import com.pdfjet.CoreFont;
import com.pdfjet.Font;
import com.pdfjet.Image;
import com.pdfjet.ImageType;
import com.pdfjet.Line;
import com.pdfjet.PDF;
import com.pdfjet.Page;
import com.pdfjet.Table;
import com.pdfjet.TextBlock;
import com.pdfjet.TextLine;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Document;
import se.accidis.fmfg.app.model.DocumentRow;
import se.accidis.fmfg.app.model.Material;
import se.accidis.fmfg.app.services.LabelsRepository;
import se.accidis.fmfg.app.services.Preferences;
import se.accidis.fmfg.app.ui.materials.ValueHelper;
import se.accidis.fmfg.app.utils.AndroidUtils;

/**
 * Generates a PDF from a Document.
 */
public final class PdfGenerator {
	public static final String PDF_CONTENT_TYPE = "application/pdf";
	public static final String PDF_EXTENSION = ".pdf";
	private static final float ADDRESS_BLOCK_MIN_HEIGHT = 40.0f;
	private static final String EMPTY_STR = "";
	private static final float FOOTER_BOTTOM_MARGIN = 35.0f;
	private static final float HORIZONTAL_MARGIN = 40.0f;
	private static final float INNER_MARGIN = 5.0f;
	private static final int LABELS_PER_ROW = 8;
	private static final float PAGE_HEIGHT = A4.PORTRAIT[1];
	private static final float PAGE_WIDTH = A4.PORTRAIT[0];
	private static final float CONTENT_WIDTH = PAGE_WIDTH - HORIZONTAL_MARGIN * 2.0f;
	private static final float CENTER_OF_PAGE = HORIZONTAL_MARGIN + CONTENT_WIDTH / 2.0f;
	private static final float ADDRESS_BLOCK_WIDTH = CONTENT_WIDTH / 2.0f - INNER_MARGIN;
	private static final float LABEL_SIZE = CONTENT_WIDTH / LABELS_PER_ROW - INNER_MARGIN;
	private static final float ROW_BOTTOM_PADDING = 8.0f;
	private static final float SIGNATURE_BLOCK_HEIGHT = 32.0f;
	private static final float TABLE_TOP_MARGIN = 8.0f;
	private final static String TAG = PdfGenerator.class.getSimpleName();
	private static final float VERTICAL_MARGIN = 50.0f;
	private final Context mContext;
	private final Document mDocument;
	private final Font mLabelFont;
	private final PDF mPdf;
	private final Preferences mPrefs;
	private final Font mTextFont;
	private final Font mVanityFont;

	private PdfGenerator(Document document, PDF pdf, Context context) throws Exception {
		mDocument = document;
		mPdf = pdf;
		mContext = context;
		mPrefs = new Preferences(context);

		mLabelFont = new Font(mPdf, CoreFont.TIMES_ROMAN);
		mLabelFont.setSize(6.0f);

		mTextFont = new Font(mPdf, CoreFont.TIMES_ROMAN);
		mTextFont.setSize(10.0f);

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
	private List<Cell> createDocumentRow1(DocumentRow row) {
		Cell materialCell = new Cell(mTextFont, row.getMaterial().getFullText());
		materialCell.setLeftPadding(0);
		if (row.isFreeText()) {
			materialCell.setBottomPadding(ROW_BOTTOM_PADDING);
		}

		Cell packagesCell = new Cell(mTextFont, row.getPackagesText(mContext));
		Cell weightVolumeCell = new Cell(mTextFont, row.getWeightVolumeText(mContext));

		return Arrays.asList(materialCell, packagesCell, weightVolumeCell);
	}

	@NonNull
	private List<Cell> createDocumentRow2(DocumentRow row, boolean withFbet) {
		StringBuilder materialBuilder = new StringBuilder();

		Material material = row.getMaterial();
		if (!TextUtils.isEmpty(material.getFben())) {
			materialBuilder.append(material.getFben());
			if (withFbet && !TextUtils.isEmpty(material.getFbet())) {
				materialBuilder.insert(0, material.getFbet() + ' ');
			}
		}

		if (row.hasNEM()) {
			if (materialBuilder.length() > 0) {
				materialBuilder.append(' ');
			}
			materialBuilder.append(String.format(mContext.getString(R.string.document_amount_format), ValueHelper.formatValue(row.getAmount())));
		}

		Cell materialCell = new Cell(mTextFont, materialBuilder.toString());
		materialCell.setLeftPadding(0);
		materialCell.setBottomPadding(ROW_BOTTOM_PADDING);

		Cell emptyCell = new Cell(mTextFont, EMPTY_STR);
		Cell nemCell;

		if (row.hasNEM()) {
			nemCell = new Cell(mTextFont, String.format(mContext.getString(R.string.document_nem_format), ValueHelper.formatValue(row.getNEMkg())));
		} else {
			nemCell = emptyCell;
		}

		return Arrays.asList(materialCell, emptyCell, nemCell);
	}

	private void createDocumentRows(List<List<Cell>> rows) {
		boolean withFbet = mPrefs.shouldShowFbetInDocument();

		for (DocumentRow documentRow : mDocument.getRows()) {
			rows.add(createDocumentRow1(documentRow));
			if (!documentRow.isFreeText()) {
				rows.add(createDocumentRow2(documentRow, withFbet));
			}
		}
	}

	private void createSummaryRows(List<List<Cell>> rows) {
		Cell emptyCell = new Cell(mTextFont, EMPTY_STR);
		rows.add(Arrays.asList(emptyCell, emptyCell, emptyCell));

		BigDecimal documentNEM = mDocument.getTotalNEMkg();
		if (0.0 != documentNEM.doubleValue()) {
			Cell nemCell = new Cell(mTextFont, String.format(mContext.getString(R.string.document_summary_total_nem_format), ValueHelper.formatValue(documentNEM)));
			nemCell.setLeftPadding(0);
			rows.add(Arrays.asList(nemCell, emptyCell, emptyCell));
		}

		for (int tpKat = Material.TPKAT_MIN; tpKat <= Material.TPKAT_MAX; tpKat++) {
			BigDecimal valueByTpKat = mDocument.getCalculatedValueByTpKat(tpKat);
			if (0.0 != valueByTpKat.doubleValue()) {
				String weightVolumeByTpKat = mDocument.getWeightVolumeStringByTpKat(tpKat, mContext);
				Cell valueCell = new Cell(mTextFont, String.format(mContext.getString(R.string.document_summary_tpkat_format), tpKat, weightVolumeByTpKat, ValueHelper.formatValue(valueByTpKat)));
				valueCell.setLeftPadding(0);
				rows.add(Arrays.asList(valueCell, emptyCell, emptyCell));
			}
		}

		BigDecimal totalValue = mDocument.getCalculatedTotalValue();
		Cell totalCell = new Cell(mTextFont, String.format(mContext.getString(R.string.document_summary_total_format), ValueHelper.formatValue(totalValue)));
		totalCell.setLeftPadding(0);
		rows.add(Arrays.asList(totalCell, emptyCell, emptyCell));
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
		List<List<Cell>> rows = new ArrayList<>(10 + mDocument.getRows().size() * 2); // too large, avoids reallocs
		rows.add(createTableHeader());
		createDocumentRows(rows);
		createSummaryRows(rows);
		return rows;
	}

	private List<Cell> createTableHeader() {
		Cell materialHeaderCell = new Cell(mLabelFont, mContext.getString(R.string.document_export_material_header));
		materialHeaderCell.setLeftPadding(0);

		Cell packagesHeaderCell = new Cell(mLabelFont, mContext.getString(R.string.document_export_pkgs_header));
		Cell weightVolumeHeaderCell = new Cell(mLabelFont, mContext.getString(R.string.document_export_weight_volume_header));

		return Arrays.asList(materialHeaderCell, packagesHeaderCell, weightVolumeHeaderCell);
	}

	private List<Pair<String, String>> formatOptionalFields() {
		List<Pair<String, String>> result = new ArrayList<>();

		if (!TextUtils.isEmpty(mDocument.getVehicleType())) {
			result.add(Pair.create(mContext.getString(R.string.document_vehicle_type), mDocument.getVehicleType()));
		}
		if (!TextUtils.isEmpty(mDocument.getVehicleReg())) {
			result.add(Pair.create(mContext.getString(R.string.document_vehicle_reg), mDocument.getVehicleReg()));
		}
		if (mDocument.isProtectedTransportSpecified()) {
			result.add(Pair.create(mContext.getString(R.string.document_protected_transport), mContext.getString(mDocument.isProtectedTransport() ? R.string.generic_yes : R.string.generic_no)));
		}

		return result;
	}

	private float writeAddressBlocks(Page page) throws Exception {
		final float labelHeight = mLabelFont.getHeight();

		TextLine senderLabel = new TextLine(mLabelFont, mContext.getString(R.string.document_sender));
		senderLabel.setLocation(HORIZONTAL_MARGIN, VERTICAL_MARGIN + labelHeight);
		senderLabel.drawOn(page);
		TextLine recipientLabel = new TextLine(mLabelFont, mContext.getString(R.string.document_recipient));
		recipientLabel.setLocation(CENTER_OF_PAGE + INNER_MARGIN, VERTICAL_MARGIN + labelHeight);
		recipientLabel.drawOn(page);

		TextBlock senderBlock = new TextBlock(mTextFont);
		senderBlock.setText(TextUtils.isEmpty(mDocument.getSender()) ? EMPTY_STR : mDocument.getSender());
		senderBlock.setLocation(HORIZONTAL_MARGIN, VERTICAL_MARGIN + labelHeight + INNER_MARGIN);
		senderBlock.setWidth(ADDRESS_BLOCK_WIDTH);
		senderBlock.drawOn(page);
		TextBlock recipientBlock = new TextBlock(mTextFont);
		recipientBlock.setText(TextUtils.isEmpty(mDocument.getRecipient()) ? EMPTY_STR : mDocument.getRecipient());
		recipientBlock.setLocation(CENTER_OF_PAGE + INNER_MARGIN, VERTICAL_MARGIN + labelHeight + INNER_MARGIN);
		recipientBlock.setWidth(ADDRESS_BLOCK_WIDTH);
		recipientBlock.drawOn(page);

		final float blockHeight = Math.max(ADDRESS_BLOCK_MIN_HEIGHT, labelHeight + Math.max(recipientBlock.getHeight(), senderBlock.getHeight()));
		return VERTICAL_MARGIN + blockHeight + 2 * INNER_MARGIN;
	}

	private float writeAuthorBlock(Page page, float verticalLocation) throws Exception {
		final float labelHeight = mLabelFont.getHeight();

		TextLine authorLabel = new TextLine(mLabelFont, mContext.getString(R.string.document_author));
		authorLabel.setLocation(HORIZONTAL_MARGIN, verticalLocation + labelHeight);
		authorLabel.drawOn(page);

		TextBlock authorBlock = new TextBlock(mTextFont);
		authorBlock.setText(TextUtils.isEmpty(mDocument.getAuthor()) ? EMPTY_STR : mDocument.getAuthor());
		authorBlock.setLocation(HORIZONTAL_MARGIN, verticalLocation + labelHeight + INNER_MARGIN);
		authorBlock.setWidth(ADDRESS_BLOCK_WIDTH);
		authorBlock.drawOn(page);

		return labelHeight + authorBlock.getHeight() + INNER_MARGIN;
	}

	private void writeDocument() throws Exception {
		Page page = new Page(mPdf, A4.PORTRAIT);

		final float headerBottom = writeDocumentHeader(page) + TABLE_TOP_MARGIN;
		final float footerHeight = writeDocumentFooter(page);

		Table table = createTable(headerBottom, footerHeight);
		final int pageTotal = table.getNumberOfPages(page);
		int pageNo = 1;

		TextLine vanityLabel = new TextLine(mVanityFont, String.format(mContext.getString(R.string.document_export_vanity_format), mContext.getString(R.string.app_name), AndroidUtils.getAppVersionName(mContext)));
		vanityLabel.setLocation(HORIZONTAL_MARGIN, PAGE_HEIGHT - FOOTER_BOTTOM_MARGIN);
		vanityLabel.drawOn(page);

		TextLine pageLabel = new TextLine(mTextFont);
		writePageLabel(page, pageLabel, pageNo, pageTotal);

		table.drawOn(page);
		while (table.hasMoreData()) {
			// Keep drawing new pages until we run out of table...
			page = new Page(mPdf, A4.PORTRAIT);
			table.setLocation(HORIZONTAL_MARGIN, VERTICAL_MARGIN);
			table.setBottomMargin(VERTICAL_MARGIN);
			table.drawOn(page);

			vanityLabel.drawOn(page);
			writePageLabel(page, pageLabel, ++pageNo, pageTotal);
		}
	}

	private float writeDocumentFooter(Page page) throws Exception {
		float labelsHeight = writeLabels(page);
		return writeSignatureLine(page, labelsHeight);
	}

	private float writeDocumentHeader(Page page) throws Exception {
		final float addressBlocksBottom = writeAddressBlocks(page);
		final float allBlocksBottom = writeOptionalBlocks(page, addressBlocksBottom);

		Line line = new Line(CENTER_OF_PAGE, VERTICAL_MARGIN, CENTER_OF_PAGE, allBlocksBottom);
		line.drawOn(page);
		line = new Line(HORIZONTAL_MARGIN, addressBlocksBottom, HORIZONTAL_MARGIN + CONTENT_WIDTH, addressBlocksBottom);
		line.drawOn(page);
		line = new Line(HORIZONTAL_MARGIN, allBlocksBottom, HORIZONTAL_MARGIN + CONTENT_WIDTH, allBlocksBottom);
		line.drawOn(page);

		return allBlocksBottom;
	}

	private float writeLabels(Page page) throws Exception {
		List<Integer> labels = LabelsRepository.getLabelsByDocument(mDocument, true);
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

	private float writeOptionalBlocks(Page page, float addressBlocksBottom) throws Exception {
		if (TextUtils.isEmpty(mDocument.getAuthor()) && !mDocument.hasOptionalFields()) {
			// Do not draw these sections if there is no data at all
			return addressBlocksBottom;
		}

		float authorBlockHeight = writeAuthorBlock(page, addressBlocksBottom);
		float optionalFieldsHeight = 0;

		if (mDocument.hasOptionalFields()) {
			List<Pair<String, String>> optionalFields = formatOptionalFields();
			optionalFieldsHeight = writeOptionalFields(page, optionalFields, addressBlocksBottom);
		}

		return addressBlocksBottom + Math.max(authorBlockHeight, optionalFieldsHeight);
	}

	private float writeOptionalFields(Page page, List<Pair<String, String>> optionalFields, float verticalLocation) throws Exception {
		final float centerLine = CENTER_OF_PAGE + CONTENT_WIDTH / 4;
		final float leftOffset = CENTER_OF_PAGE + INNER_MARGIN;
		final float centerOffset = centerLine + INNER_MARGIN;
		final float blockWidth = CONTENT_WIDTH / 4 - (2 * INNER_MARGIN);
		final float labelHeight = mLabelFont.getHeight();

		boolean rightColumn = false;
		float verticalOffset = verticalLocation;
		float currentRowHeight = 0;
		float totalHeight = 0;
		int idx = 0;

		for (Pair<String, String> pair : optionalFields) {
			float horizontalLoc = (rightColumn ? centerOffset : leftOffset);

			TextLine labelLine = new TextLine(mLabelFont, pair.first);
			labelLine.setLocation(horizontalLoc, verticalOffset + labelHeight);
			labelLine.drawOn(page);

			TextBlock valueBlock = new TextBlock(mTextFont);
			valueBlock.setText(TextUtils.isEmpty(pair.second) ? EMPTY_STR : pair.second);
			valueBlock.setLocation(horizontalLoc, verticalOffset + labelHeight + INNER_MARGIN);
			valueBlock.setWidth(blockWidth);
			valueBlock.drawOn(page);

			// Switch columns on every other row, track which of the two is the tallest so we can adjust accordingly
			currentRowHeight = Math.max(currentRowHeight, labelHeight + valueBlock.getHeight() + 2 * INNER_MARGIN);
			if (rightColumn) {
				rightColumn = false;
				verticalOffset += currentRowHeight;
				totalHeight += currentRowHeight;
				currentRowHeight = 0;

				// Draw a separator line unless this was the last field
				if (idx != optionalFields.size() - 1) {
					Line separatorLine = new Line(CENTER_OF_PAGE, verticalOffset, CENTER_OF_PAGE + CONTENT_WIDTH / 2, verticalOffset);
					separatorLine.drawOn(page);
				}
			} else {
				rightColumn = true;
			}

			idx++;
		}

		// In case we ended on a left column we need to update totalHeight
		if (rightColumn) {
			totalHeight += currentRowHeight;
		}

		Line centerSeparatorLine = new Line(centerLine, verticalLocation, centerLine, verticalLocation + totalHeight);
		centerSeparatorLine.drawOn(page);

		return totalHeight;
	}

	private void writePageLabel(Page page, TextLine pageLabel, int pageNo, int pageTotal) throws Exception {
		pageLabel.setText(String.format(mContext.getString(R.string.document_export_page_format), pageNo, pageTotal));
		pageLabel.setLocation(PAGE_WIDTH - HORIZONTAL_MARGIN - pageLabel.getWidth(), PAGE_HEIGHT - FOOTER_BOTTOM_MARGIN);
		pageLabel.drawOn(page);
	}

	private float writeSignatureLine(Page page, float labelsHeight) throws Exception {
		final float verticalLoc = PAGE_HEIGHT - VERTICAL_MARGIN - labelsHeight - SIGNATURE_BLOCK_HEIGHT;
		final float columnWidth = CONTENT_WIDTH / 4.0f;
		final float left1 = HORIZONTAL_MARGIN, left2 = HORIZONTAL_MARGIN + columnWidth, left3 = CENTER_OF_PAGE, left4 = CENTER_OF_PAGE + columnWidth;

		TextLine senderLabel = new TextLine(mLabelFont, mContext.getString(R.string.document_export_signature_sender));
		final float labelTop = verticalLoc + senderLabel.getHeight();
		senderLabel.setLocation(left1, labelTop);
		senderLabel.drawOn(page);

		TextLine dateSenderLabel = new TextLine(mLabelFont, mContext.getString(R.string.document_export_signature_date));
		dateSenderLabel.setLocation(left2 + INNER_MARGIN, labelTop);
		dateSenderLabel.drawOn(page);

		TextLine driverLabel = new TextLine(mLabelFont, mContext.getString(R.string.document_export_signature_driver));
		driverLabel.setLocation(left3 + INNER_MARGIN, labelTop);
		driverLabel.drawOn(page);

		TextLine dateDriverLabel = new TextLine(mLabelFont, mContext.getString(R.string.document_export_signature_date));
		dateDriverLabel.setLocation(left4 + INNER_MARGIN, labelTop);
		dateDriverLabel.drawOn(page);

		final float separatorHeight = verticalLoc + SIGNATURE_BLOCK_HEIGHT;
		Line line = new Line(HORIZONTAL_MARGIN, verticalLoc, HORIZONTAL_MARGIN + CONTENT_WIDTH, verticalLoc);
		line.drawOn(page);
		line = new Line(left2, verticalLoc, left2, separatorHeight);
		line.drawOn(page);
		line = new Line(left3, verticalLoc, left3, separatorHeight);
		line.drawOn(page);
		line = new Line(left4, verticalLoc, left4, separatorHeight);
		line.drawOn(page);

		return labelsHeight + SIGNATURE_BLOCK_HEIGHT + 2 * INNER_MARGIN;
	}
}

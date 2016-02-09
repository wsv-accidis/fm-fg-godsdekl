package se.accidis.fmfg.app.export;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.pdfjet.A4;
import com.pdfjet.Cell;
import com.pdfjet.CoreFont;
import com.pdfjet.Font;
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
import java.util.ArrayList;
import java.util.List;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Document;
import se.accidis.fmfg.app.model.DocumentRow;
import se.accidis.fmfg.app.utils.AndroidUtils;

/**
 * Generates a PDF from a Document.
 */
public final class PdfGenerator {
	public static final String PDF_CONTENT_TYPE = "application/pdf";
	public static final String PDF_EXTENSION = ".pdf";
	private final static String TAG = PdfGenerator.class.getSimpleName();
	private static final float mAddressBlockMinHeight = 60.0f;
	private static final float mHorizontalMargin = 40.0f;
	private static final float mInnerMargin = 5.0f;
	private static final float mPageHeight = A4.PORTRAIT[1];
	private static final float mPageWidth = A4.PORTRAIT[0];
	private static final float mContentWidth = mPageWidth - mHorizontalMargin * 2.0f;
	private static final float mFooterBottomMargin = 30.0f;
	private static final float mVerticalMargin = 50.0f;
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

	private List<List<Cell>> createTableData() throws Exception {
		List<List<Cell>> rows = new ArrayList<>();
		createTableHeader(rows);

		for (int i = 0; i < 30; i++) {
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
		float centerOfPage = mHorizontalMargin + mContentWidth / 2.0f;

		Page page = new Page(mPdf, A4.PORTRAIT);
		final float headerBottom = writeDocumentHeader(page, centerOfPage);

		Table table = new Table();
		table.setData(createTableData(), Table.DATA_HAS_1_HEADER_ROWS);
		table.setLocation(mHorizontalMargin, headerBottom + mInnerMargin);
		table.setBottomMargin(mVerticalMargin);
		table.setNoCellBorders();

		table.setColumnWidth(0, mContentWidth * 0.7f);
		table.setColumnWidth(1, mContentWidth * 0.15f);
		table.setColumnWidth(2, mContentWidth * 0.15f);
		table.wrapAroundCellText();

		int pageNo = 1;
		final int pageTotal = table.getNumberOfPages(page);
		Point tableBottom;

		TextLine vanityLabel = new TextLine(mVanityFont, String.format(mContext.getString(R.string.document_export_vanity_format), mContext.getString(R.string.app_name), AndroidUtils.getAppVersionName(mContext)));
		vanityLabel.setLocation(mHorizontalMargin, mPageHeight - mFooterBottomMargin);
		vanityLabel.drawOn(page);

		TextLine pageLabel = new TextLine(mTextFont);
		writePageLabel(page, pageLabel, pageNo, pageTotal);

		while (true) {
			tableBottom = table.drawOn(page);
			if (!table.hasMoreData()) {
				break;
			}

			pageNo++;
			page = new Page(mPdf, A4.PORTRAIT);
			table.setLocation(mHorizontalMargin, mVerticalMargin);

			vanityLabel.drawOn(page);
			writePageLabel(page, pageLabel, pageNo, pageTotal);
		}

		TextLine endLabel = new TextLine(mLabelFont, "SLUT");
		endLabel.setLocation(mHorizontalMargin, tableBottom.getY() + endLabel.getHeight() + mInnerMargin);
		endLabel.drawOn(page);
	}

	private void writePageLabel(Page page, TextLine pageLabel, int pageNo, int pageTotal) throws Exception {
		pageLabel.setText(String.format(mContext.getString(R.string.document_export_page_format), pageNo, pageTotal));
		pageLabel.setLocation(mPageWidth - mHorizontalMargin - pageLabel.getWidth(), mPageHeight - mFooterBottomMargin);
		pageLabel.drawOn(page);
	}

	private float writeDocumentHeader(Page page, float centerOfPage) throws Exception {
		TextLine senderLabel = new TextLine(mLabelFont, mContext.getString(R.string.document_sender));
		senderLabel.setLocation(mHorizontalMargin, mVerticalMargin + mLabelFont.getHeight());
		senderLabel.drawOn(page);
		TextLine recipientLabel = new TextLine(mLabelFont, mContext.getString(R.string.document_recipient));
		recipientLabel.setLocation(centerOfPage + mInnerMargin, mVerticalMargin + mLabelFont.getHeight());
		recipientLabel.drawOn(page);

		final float addressBlockWidth = mContentWidth / 2.0f - mInnerMargin;
		final float labelHeight = senderLabel.getHeight();

		TextBlock senderBlock = new TextBlock(mTextFont);
		senderBlock.setText(TextUtils.isEmpty(mDocument.getSender()) ? "" : mDocument.getSender());
		senderBlock.setLocation(mHorizontalMargin, mVerticalMargin + labelHeight + mInnerMargin);
		senderBlock.setWidth(addressBlockWidth);
		senderBlock.drawOn(page);
		TextBlock recipientBlock = new TextBlock(mTextFont);
		recipientBlock.setText(TextUtils.isEmpty(mDocument.getRecipient()) ? "" : mDocument.getRecipient());
		recipientBlock.setLocation(centerOfPage + mInnerMargin, mVerticalMargin + labelHeight + mInnerMargin);
		recipientBlock.setWidth(addressBlockWidth);
		recipientBlock.drawOn(page);

		final float addressBlockHeight = Math.max(mAddressBlockMinHeight, Math.max(recipientBlock.getHeight(), senderBlock.getHeight()));
		final float headerBottom = mVerticalMargin + labelHeight + addressBlockHeight + 2 * mInnerMargin;

		Line line = new Line(centerOfPage, mVerticalMargin, centerOfPage, headerBottom);
		line.drawOn(page);
		line = new Line(mHorizontalMargin, headerBottom, mHorizontalMargin + mContentWidth, headerBottom);
		line.drawOn(page);

		return headerBottom;
	}
}

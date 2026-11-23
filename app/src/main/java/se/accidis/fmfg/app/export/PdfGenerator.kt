package se.accidis.fmfg.app.export

import android.content.Context
import com.pdfjet.A4
import com.pdfjet.Cell
import com.pdfjet.CoreFont
import com.pdfjet.Font
import com.pdfjet.Image
import com.pdfjet.ImageType
import com.pdfjet.Line
import com.pdfjet.PDF
import com.pdfjet.Page
import com.pdfjet.Table
import com.pdfjet.TextBlock
import com.pdfjet.TextLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.model.Document
import se.accidis.fmfg.app.model.DocumentRow
import se.accidis.fmfg.app.model.Material
import se.accidis.fmfg.app.services.LabelsRepository
import se.accidis.fmfg.app.services.Preferences
import se.accidis.fmfg.app.utils.AndroidUtils
import se.accidis.fmfg.app.utils.ValueHelper
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.max

/**
 * Generates a PDF from a [Document].
 */
class PdfGenerator private constructor(
    private val document: Document,
    private val pdf: PDF,
    private val context: Context,
) {
    private val prefs: Preferences = Preferences.getInstance(context)

    private val labelFont: Font = Font(pdf, CoreFont.TIMES_ROMAN).apply {
        setSize(6.0f)
    }

    private val textFont: Font = Font(pdf, CoreFont.TIMES_ROMAN).apply {
        setSize(10.0f)
    }

    private val vanityFont: Font = Font(pdf, CoreFont.TIMES_ROMAN).apply {
        setSize(5.0f)
        setItalic(true)
    }

    private fun createDocumentRow1(row: DocumentRow): List<Cell> {
        val materialCell = Cell(textFont, row.material.fullText).apply {
            setLeftPadding(0f)
            if (row.isFreeText) {
                setBottomPadding(ROW_BOTTOM_PADDING)
            }
        }

        val packagesCell = Cell(textFont, row.packagesText(context))
        val weightVolumeCell = Cell(textFont, row.weightVolumeText(context))

        return listOf(materialCell, packagesCell, weightVolumeCell)
    }

    private fun createDocumentRow2(row: DocumentRow, withFbet: Boolean): List<Cell> {
        val materialText = buildString {
            val material = row.material
            if (material.fben.isNotEmpty()) {
                if (withFbet) {
                    append(material.fbet)
                    append(' ')
                }
                append(material.fben)
            }

            if (row.hasNEM()) {
                if (isNotEmpty()) {
                    append(' ')
                }
                append(
                    context.getString(
                        R.string.document_amount_format,
                        ValueHelper.formatValue(row.amount)
                    )
                )
            }
        }

        val materialCell = Cell(textFont, materialText).apply {
            setLeftPadding(0f)
            setBottomPadding(ROW_BOTTOM_PADDING)
        }

        val emptyCell = Cell(textFont, EMPTY_STR)
        val nemCell = if (row.hasNEM()) {
            Cell(
                textFont,
                context.getString(
                    R.string.document_nem_format,
                    ValueHelper.formatValue(row.NEMkg)
                )
            )
        } else {
            emptyCell
        }

        return listOf(materialCell, emptyCell, nemCell)
    }

    private fun createDocumentRows(rows: MutableList<List<Cell>>) {
        val withFbet = prefs.shouldShowFbetInDocument()

        for (documentRow in document.rows) {
            rows.add(createDocumentRow1(documentRow))
            if (!documentRow.isFreeText) {
                rows.add(createDocumentRow2(documentRow, withFbet))
            }
        }
    }

    private fun createSummaryRows(rows: MutableList<List<Cell>>) {
        val emptyCell = Cell(textFont, EMPTY_STR)
        rows.add(listOf(emptyCell, emptyCell, emptyCell))

        val documentNEM = document.totalNEMkg
        if (documentNEM.signum() != 0) {
            val nemCell = Cell(
                textFont,
                context.getString(
                    R.string.document_summary_total_nem_format,
                    ValueHelper.formatValue(documentNEM)
                )
            ).apply {
                setLeftPadding(0f)
            }
            rows.add(listOf(nemCell, emptyCell, emptyCell))
        }

        for (tpKat in Material.TPKAT_MIN..Material.TPKAT_MAX) {
            val weightVolumeByTpKat = document.weightVolumeStringByTpKat(tpKat, context)
            if (weightVolumeByTpKat.isNotEmpty()) {
                val valueByTpKat = document.calculatedValueByTpKat(tpKat)
                val valueCell = Cell(
                    textFont,
                    context.getString(
                        R.string.document_summary_tpkat_format,
                        tpKat,
                        weightVolumeByTpKat,
                        ValueHelper.formatValue(valueByTpKat)
                    )
                ).apply {
                    setLeftPadding(0f)
                }
                rows.add(listOf(valueCell, emptyCell, emptyCell))
            }
        }

        val totalValue = document.calculatedTotalValue
        val totalCell = Cell(
            textFont,
            context.getString(
                R.string.document_summary_total_format,
                ValueHelper.formatValue(totalValue)
            )
        ).apply {
            setLeftPadding(0f)
        }
        rows.add(listOf(totalCell, emptyCell, emptyCell))
    }

    private fun createTable(headerBottom: Float, footerHeight: Float): Table =
        Table().apply {
            setData(createTableData(), Table.DATA_HAS_1_HEADER_ROWS)
            setLocation(HORIZONTAL_MARGIN, headerBottom + INNER_MARGIN)
            setBottomMargin(VERTICAL_MARGIN + footerHeight)
            setNoCellBorders()

            setColumnWidth(0, CONTENT_WIDTH * 0.7f)
            setColumnWidth(1, CONTENT_WIDTH * 0.15f)
            setColumnWidth(2, CONTENT_WIDTH * 0.15f)
            wrapAroundCellText()
        }

    private fun createTableData(): List<List<Cell>> {
        val rows = ArrayList<List<Cell>>(10 + (document.rows.size * 2))
        rows.add(createTableHeader())
        createDocumentRows(rows)
        createSummaryRows(rows)
        return rows
    }

    private fun createTableHeader(): List<Cell> {
        val materialHeaderCell = Cell(
            labelFont,
            context.getString(R.string.document_export_material_header)
        ).apply {
            setLeftPadding(0f)
        }

        val packagesHeaderCell = Cell(labelFont, context.getString(R.string.document_export_pkgs_header))
        val weightVolumeHeaderCell = Cell(
            labelFont,
            context.getString(R.string.document_export_weight_volume_header)
        )

        return listOf(materialHeaderCell, packagesHeaderCell, weightVolumeHeaderCell)
    }

    private fun formatOptionalFields(): List<Pair<String, String>> = buildList {
        if (document.vehicleType.isNotEmpty()) {
            add(context.getString(R.string.document_vehicle_type) to document.vehicleType)
        }
        if (document.vehicleReg.isNotEmpty()) {
            add(context.getString(R.string.document_vehicle_reg) to document.vehicleReg)
        }
        if (document.isProtectedTransport) {
            add(
                context.getString(R.string.document_protected_transport) to
                    context.getString(R.string.generic_yes)
            )
        }
    }

    private fun writeAddressBlocks(page: Page): Float {
        val labelHeight = labelFont.height

        TextLine(labelFont, context.getString(R.string.document_sender)).apply {
            setLocation(HORIZONTAL_MARGIN, VERTICAL_MARGIN + labelHeight)
            drawOn(page)
        }
        TextLine(labelFont, context.getString(R.string.document_recipient)).apply {
            setLocation(CENTER_OF_PAGE + INNER_MARGIN, VERTICAL_MARGIN + labelHeight)
            drawOn(page)
        }

        val senderBlock = TextBlock(textFont).apply {
            setText(document.sender.ifEmpty { EMPTY_STR })
            setLocation(HORIZONTAL_MARGIN, VERTICAL_MARGIN + labelHeight + INNER_MARGIN)
            setWidth(ADDRESS_BLOCK_WIDTH)
            drawOn(page)
        }
        val recipientBlock = TextBlock(textFont).apply {
            setText(document.recipient.ifEmpty { EMPTY_STR })
            setLocation(CENTER_OF_PAGE + INNER_MARGIN, VERTICAL_MARGIN + labelHeight + INNER_MARGIN)
            setWidth(ADDRESS_BLOCK_WIDTH)
            drawOn(page)
        }

        val blockHeight = max(
            ADDRESS_BLOCK_MIN_HEIGHT,
            labelHeight + max(recipientBlock.height, senderBlock.height)
        )
        return VERTICAL_MARGIN + blockHeight + 2 * INNER_MARGIN
    }

    private fun writeAuthorBlock(page: Page, verticalLocation: Float): Float {
        val labelHeight = labelFont.height

        TextLine(labelFont, context.getString(R.string.document_author)).apply {
            setLocation(HORIZONTAL_MARGIN, verticalLocation + labelHeight)
            drawOn(page)
        }

        val authorBlock = TextBlock(textFont).apply {
            setText(document.author.ifEmpty { EMPTY_STR })
            setLocation(HORIZONTAL_MARGIN, verticalLocation + labelHeight + INNER_MARGIN)
            setWidth(ADDRESS_BLOCK_WIDTH)
            drawOn(page)
        }

        return labelHeight + authorBlock.height + INNER_MARGIN
    }

    private fun writeDocument() {
        var page = Page(pdf, A4.PORTRAIT)

        val headerBottom = writeDocumentHeader(page) + TABLE_TOP_MARGIN
        val footerHeight = writeDocumentFooter(page)

        val table = createTable(headerBottom, footerHeight)
        val pageTotal = table.getNumberOfPages(page)
        var pageNo = 1

        val vanityLabel = TextLine(
            vanityFont,
            context.getString(
                R.string.document_export_vanity_format,
                context.getString(R.string.app_name),
                AndroidUtils.getAppVersionName(context)
            )
        ).apply {
            setLocation(HORIZONTAL_MARGIN, PAGE_HEIGHT - FOOTER_BOTTOM_MARGIN)
            drawOn(page)
        }

        val pageLabel = TextLine(textFont)
        writePageLabel(page, pageLabel, pageNo, pageTotal)

        table.drawOn(page)
        while (table.hasMoreData()) {
            // Keep drawing new pages until we run out of table...
            page = Page(pdf, A4.PORTRAIT)
            table.setLocation(HORIZONTAL_MARGIN, VERTICAL_MARGIN)
            table.setBottomMargin(VERTICAL_MARGIN)
            table.drawOn(page)

            vanityLabel.drawOn(page)
            writePageLabel(page, pageLabel, ++pageNo, pageTotal)
        }
    }

    private fun writeDocumentFooter(page: Page): Float {
        val labelsHeight = writeLabels(page)
        return writeSignatureLine(page, labelsHeight)
    }

    private fun writeDocumentHeader(page: Page): Float {
        val addressBlocksBottom = writeAddressBlocks(page)
        val allBlocksBottom = writeOptionalBlocks(page, addressBlocksBottom)

        Line(CENTER_OF_PAGE, VERTICAL_MARGIN, CENTER_OF_PAGE, allBlocksBottom).drawOn(page)
        Line(
            HORIZONTAL_MARGIN,
            addressBlocksBottom,
            HORIZONTAL_MARGIN + CONTENT_WIDTH,
            addressBlocksBottom
        ).drawOn(page)
        Line(
            HORIZONTAL_MARGIN,
            allBlocksBottom,
            HORIZONTAL_MARGIN + CONTENT_WIDTH,
            allBlocksBottom
        ).drawOn(page)

        return allBlocksBottom
    }

    private fun writeLabels(page: Page): Float {
        val labels = LabelsRepository.getLabelsByDocument(document, true)
        if (labels.isEmpty()) {
            return 0.0f
        }

        val numRows = 1 + labels.size / LABELS_PER_ROW
        val labelBoxSize = LABEL_SIZE + INNER_MARGIN
        val verticalLoc = PAGE_HEIGHT - VERTICAL_MARGIN - numRows * labelBoxSize
        var column = 0
        var row = 0

        for (labelResId in labels) {
            context.resources.openRawResource(labelResId).use { labelStream ->
                val image = Image(pdf, labelStream, ImageType.PNG)
                image.setLocation(
                    HORIZONTAL_MARGIN + column * labelBoxSize,
                    verticalLoc + row * labelBoxSize
                )
                image.scaleBy(LABEL_SIZE / max(image.width, image.height))
                image.drawOn(page)
            }

            if (++column == LABELS_PER_ROW) {
                column = 0
                row++
            }
        }

        val labelsLabel = TextLine(labelFont, context.getString(R.string.document_export_labels)).apply {
            setLocation(HORIZONTAL_MARGIN, verticalLoc - 2 * INNER_MARGIN)
            drawOn(page)
        }

        val separatorTop = verticalLoc - labelsLabel.height - 2 * INNER_MARGIN
        Line(
            HORIZONTAL_MARGIN,
            separatorTop,
            CONTENT_WIDTH + HORIZONTAL_MARGIN,
            separatorTop
        ).drawOn(page)

        return numRows * labelBoxSize + labelsLabel.height + 2 * INNER_MARGIN
    }

    private fun writeOptionalBlocks(page: Page, addressBlocksBottom: Float): Float {
        if (document.author.isEmpty() && !document.hasOptionalFields) {
            // Do not draw these sections if there is no data at all
            return addressBlocksBottom
        }

        val authorBlockHeight = writeAuthorBlock(page, addressBlocksBottom)
        var optionalFieldsHeight = 0f

        if (document.hasOptionalFields) {
            val optionalFields = formatOptionalFields()
            optionalFieldsHeight = writeOptionalFields(page, optionalFields, addressBlocksBottom)
        }

        return addressBlocksBottom + max(authorBlockHeight, optionalFieldsHeight)
    }

    private fun writeOptionalFields(
        page: Page,
        optionalFields: List<Pair<String, String>>,
        verticalLocation: Float
    ): Float {
        val centerLine = CENTER_OF_PAGE + CONTENT_WIDTH / 4
        val leftOffset = CENTER_OF_PAGE + INNER_MARGIN
        val centerOffset = centerLine + INNER_MARGIN
        val blockWidth = CONTENT_WIDTH / 4 - 2 * INNER_MARGIN
        val labelHeight = labelFont.height

        var rightColumn = false
        var verticalOffset = verticalLocation
        var currentRowHeight = 0f
        var totalHeight = 0f

        optionalFields.forEachIndexed { idx, (label, value) ->
            val horizontalLoc = if (rightColumn) centerOffset else leftOffset

            TextLine(labelFont, label).apply {
                setLocation(horizontalLoc, verticalOffset + labelHeight)
                drawOn(page)
            }

            val valueBlock = TextBlock(textFont).apply {
                setText(value.ifEmpty { EMPTY_STR })
                setLocation(horizontalLoc, verticalOffset + labelHeight + INNER_MARGIN)
                setWidth(blockWidth)
                drawOn(page)
            }

            // Switch columns on every other row, track which of the two is the tallest so we can adjust accordingly
            currentRowHeight = max(
                currentRowHeight,
                labelHeight + valueBlock.height + 2 * INNER_MARGIN
            )
            if (rightColumn) {
                rightColumn = false
                verticalOffset += currentRowHeight
                totalHeight += currentRowHeight
                currentRowHeight = 0f

                // Draw a separator line unless this was the last field
                if (idx != optionalFields.lastIndex) {
                    Line(
                        CENTER_OF_PAGE,
                        verticalOffset,
                        CENTER_OF_PAGE + CONTENT_WIDTH / 2,
                        verticalOffset
                    ).drawOn(page)
                }
            } else {
                rightColumn = true
            }
        }

        // In case we ended on a left column we need to update totalHeight
        if (rightColumn) {
            totalHeight += currentRowHeight
        }

        Line(centerLine, verticalLocation, centerLine, verticalLocation + totalHeight).drawOn(page)

        return totalHeight
    }

    private fun writePageLabel(page: Page, pageLabel: TextLine, pageNo: Int, pageTotal: Int) {
        pageLabel.setText(context.getString(R.string.document_export_page_format, pageNo, pageTotal))
        pageLabel.setLocation(
            PAGE_WIDTH - HORIZONTAL_MARGIN - pageLabel.width,
            PAGE_HEIGHT - FOOTER_BOTTOM_MARGIN
        )
        pageLabel.drawOn(page)
    }

    private fun writeSignatureLine(page: Page, labelsHeight: Float): Float {
        val verticalLoc = PAGE_HEIGHT - VERTICAL_MARGIN - labelsHeight - SIGNATURE_BLOCK_HEIGHT
        val columnWidth = CONTENT_WIDTH / 4.0f
        val left1 = HORIZONTAL_MARGIN
        val left2 = HORIZONTAL_MARGIN + columnWidth
        val left3 = CENTER_OF_PAGE
        val left4 = CENTER_OF_PAGE + columnWidth

        val senderLabel = TextLine(
            labelFont,
            context.getString(R.string.document_export_signature_sender)
        )
        val labelTop = verticalLoc + senderLabel.height
        senderLabel.setLocation(left1, labelTop)
        senderLabel.drawOn(page)

        TextLine(labelFont, context.getString(R.string.document_export_signature_date)).apply {
            setLocation(left2 + INNER_MARGIN, labelTop)
            drawOn(page)
        }

        TextLine(labelFont, context.getString(R.string.document_export_signature_driver)).apply {
            setLocation(left3 + INNER_MARGIN, labelTop)
            drawOn(page)
        }

        TextLine(labelFont, context.getString(R.string.document_export_signature_date)).apply {
            setLocation(left4 + INNER_MARGIN, labelTop)
            drawOn(page)
        }

        val separatorHeight = verticalLoc + SIGNATURE_BLOCK_HEIGHT
        Line(HORIZONTAL_MARGIN, verticalLoc, HORIZONTAL_MARGIN + CONTENT_WIDTH, verticalLoc).drawOn(page)
        Line(left2, verticalLoc, left2, separatorHeight).drawOn(page)
        Line(left3, verticalLoc, left3, separatorHeight).drawOn(page)
        Line(left4, verticalLoc, left4, separatorHeight).drawOn(page)

        return labelsHeight + SIGNATURE_BLOCK_HEIGHT + 2 * INNER_MARGIN
    }

    companion object {
        const val PDF_CONTENT_TYPE = "application/pdf"
        const val PDF_EXTENSION = ".pdf"

        private const val ADDRESS_BLOCK_MIN_HEIGHT = 40.0f
        private const val EMPTY_STR = ""
        private const val FOOTER_BOTTOM_MARGIN = 35.0f
        private const val HORIZONTAL_MARGIN = 40.0f
        private const val INNER_MARGIN = 5.0f
        private const val LABELS_PER_ROW = 8
        private val PAGE_HEIGHT = A4.PORTRAIT[1]
        private val PAGE_WIDTH = A4.PORTRAIT[0]
        private val CONTENT_WIDTH = PAGE_WIDTH - HORIZONTAL_MARGIN * 2.0f
        private val CENTER_OF_PAGE = HORIZONTAL_MARGIN + CONTENT_WIDTH / 2.0f
        private val ADDRESS_BLOCK_WIDTH = CONTENT_WIDTH / 2.0f - INNER_MARGIN
        private val LABEL_SIZE = CONTENT_WIDTH / LABELS_PER_ROW - INNER_MARGIN
        private const val ROW_BOTTOM_PADDING = 8.0f
        private const val SIGNATURE_BLOCK_HEIGHT = 32.0f
        private const val TABLE_TOP_MARGIN = 8.0f
        private const val VERTICAL_MARGIN = 50.0f

        /**
         * Cleans up expired exports, creates a unique [ExportFile] for [document],
         * and writes the generated PDF on [Dispatchers.IO].
         */
        @Throws(PdfException::class)
        suspend fun exportDocument(document: Document, context: Context): ExportFile =
            withContext(Dispatchers.IO) {
                val appContext = context.applicationContext
                ExportFile.cleanUpOldExports(appContext)
                val exportFile = try {
                    ExportFile.fromDocument(document, PDF_EXTENSION, appContext)
                } catch (ex: Exception) {
                    throw PdfException(ex)
                }
                Timber.i("Exporting current document to \"%s\".", exportFile.file.path)
                exportToPdf(document, exportFile, appContext)
                exportFile
            }

        @Throws(FileNotFoundException::class, PdfException::class)
        fun exportToPdf(document: Document, exportFile: ExportFile, context: Context) {
            BufferedOutputStream(FileOutputStream(exportFile.file)).use { outputStream ->
                writeToStream(document, outputStream, context, exportFile.filename)
            }
        }

        @Throws(PdfException::class)
        fun writeToStream(
            document: Document,
            outputStream: OutputStream,
            context: Context,
            debugName: String = "stream"
        ) {
            var pdf: PDF? = null
            try {
                pdf = PDF(outputStream)
                val generator = PdfGenerator(document, pdf, context)
                generator.writeDocument()
            } catch (ex: Exception) {
                Timber.e(ex, "Exception while trying to write PDF to \"%s\".", debugName)
                throw PdfException(ex)
            } finally {
                try {
                    pdf?.close()
                } catch (_: Exception) {
                }
            }
        }
    }
}

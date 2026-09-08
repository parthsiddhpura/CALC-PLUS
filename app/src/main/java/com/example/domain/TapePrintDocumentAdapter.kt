package com.example.domain

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import com.example.model.WorksheetDocument
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TapePrintDocumentAdapter(
    private val context: Context,
    private val doc: WorksheetDocument,
    private val indianGrouping: Boolean = true
) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback?.onLayoutCancelled()
            return
        }

        val info = PrintDocumentInfo.Builder("${doc.title}.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(1)
            .build()
        callback?.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (595 x 842 pt)
        val page = pdf.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val normalPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        val monoPaint = Paint().apply {
            color = Color.BLACK
            textSize = 13f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }

        val boldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var y = 50f
        val marginX = 40f
        val rightX = 555f

        // Top Header: Date left, Doc name right
        val dateFormat = SimpleDateFormat("d MMM yyyy h:mm:ss a", Locale.getDefault())
        normalPaint.color = Color.GRAY
        canvas.drawText(dateFormat.format(Date(doc.updatedAt)), marginX, y, normalPaint)
        val titleWidth = boldPaint.measureText(doc.title)
        canvas.drawText(doc.title, rightX - titleWidth, y, boldPaint)
        y += 20f

        canvas.drawLine(marginX, y, rightX, y, linePaint)
        y += 24f

        if (doc.headerNote.isNotBlank()) {
            boldPaint.color = Color.BLACK
            canvas.drawText(doc.headerNote, marginX, y, boldPaint)
            y += 22f
        }

        doc.lines.forEach { line ->
            if (line.hasDividerBefore) {
                canvas.drawLine(marginX + 20f, y - 6f, marginX + 180f, y - 6f, linePaint)
                y += 6f
            }

            // Operator
            monoPaint.color = Color.DKGRAY
            canvas.drawText(line.operator, marginX + 10f, y, monoPaint)

            // Number
            val formatted = WorksheetTapeEngine.formatNumber(line.evaluatedNumber, indianGrouping)
            val isNeg = line.operator == "-" || line.evaluatedNumber < 0
            monoPaint.color = if (isNeg) Color.parseColor("#E53935") else Color.BLACK
            canvas.drawText(formatted, marginX + 45f, y, monoPaint)

            // Note / Remark
            normalPaint.color = Color.DKGRAY
            if (line.note.isNotBlank()) {
                canvas.drawText(line.note, marginX + 180f, y, normalPaint)
            }

            y += 18f
        }

        y += 10f
        canvas.drawLine(marginX, y, rightX, y, linePaint)
        y += 4f
        canvas.drawLine(marginX, y, rightX, y, linePaint)
        y += 22f

        // Total Row
        boldPaint.color = Color.BLACK
        boldPaint.textSize = 15f
        canvas.drawText("TOTAL", marginX + 10f, y, boldPaint)
        val grandTotalStr = WorksheetTapeEngine.formatNumber(doc.grandTotal, indianGrouping)
        monoPaint.textSize = 15f
        monoPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        monoPaint.color = Color.BLACK
        canvas.drawText(grandTotalStr, marginX + 100f, y, monoPaint)

        pdf.finishPage(page)

        try {
            destination?.let {
                val out = FileOutputStream(it.fileDescriptor)
                pdf.writeTo(out)
                out.close()
            }
            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback?.onWriteFailed(e.message)
        } finally {
            pdf.close()
        }
    }
}

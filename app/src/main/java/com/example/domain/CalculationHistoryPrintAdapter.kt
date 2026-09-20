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
import android.print.PrintManager
import android.widget.Toast
import com.example.model.CalculationHistory
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Native Android Print & Save-as-PDF adapter for Calculation History.
 * Exports formatted calculations with timestamps, expressions, results, and custom notes.
 */
class CalculationHistoryPrintAdapter(
    private val context: Context,
    private val historyList: List<CalculationHistory>
) : PrintDocumentAdapter() {

    private val pageWidth = 595 // Standard A4 width in points (72 dpi)
    private val pageHeight = 842 // Standard A4 height in points (72 dpi)
    private var totalPages = 1
    private val itemsPerPage = 12

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

        totalPages = if (historyList.isEmpty()) 1 else ((historyList.size - 1) / itemsPerPage) + 1

        val info = PrintDocumentInfo.Builder("Calculation_History_${System.currentTimeMillis()}.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(totalPages)
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

        val marginX = 40f
        val rightX = 555f
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

        val titlePaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val subPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 10f
            isAntiAlias = true
        }

        val exprPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 12f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }

        val resultPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 13f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        val notePaint = Paint().apply {
            color = Color.parseColor("#2563EB")
            textSize = 9.5f
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }

        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
        }

        for (pageIdx in 0 until totalPages) {
            if (cancellationSignal?.isCanceled == true) {
                pdf.close()
                callback?.onWriteCancelled()
                return
            }

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIdx + 1).create()
            val page = pdf.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            var y = 45f

            // Document Header
            canvas.drawRect(marginX - 10f, y - 15f, rightX + 10f, y + 45f, headerBgPaint)
            canvas.drawText("CALCULATION HISTORY REPORT", marginX, y + 10f, titlePaint)
            canvas.drawText("Generated on ${dateFormat.format(Date())} • Total: ${historyList.size} calculations", marginX, y + 28f, subPaint)

            val pageLabel = "Page ${pageIdx + 1} of $totalPages"
            val pageLabelWidth = subPaint.measureText(pageLabel)
            canvas.drawText(pageLabel, rightX - pageLabelWidth, y + 28f, subPaint)

            y += 55f
            canvas.drawLine(marginX - 10f, y, rightX + 10f, y, linePaint)
            y += 18f

            val startIndex = pageIdx * itemsPerPage
            val endIndex = minOf(startIndex + itemsPerPage, historyList.size)

            for (i in startIndex until endIndex) {
                val item = historyList[i]

                // Index badge & timestamp
                val timeStr = dateFormat.format(Date(item.timestamp))
                val favTag = if (item.isFavorite) " ★ Favorite" else ""
                val modeTag = if (item.mode.isNotBlank()) " [${item.mode}]" else ""
                subPaint.color = Color.parseColor("#64748B")
                canvas.drawText("#${i + 1} • $timeStr$modeTag$favTag", marginX, y, subPaint)

                // Expression
                y += 16f
                val exprDisplay = if (item.expression.length > 55) item.expression.take(52) + "..." else item.expression
                canvas.drawText(exprDisplay, marginX + 10f, y, exprPaint)

                // Result (Right aligned)
                val resDisplay = "= ${item.result}"
                val resWidth = resultPaint.measureText(resDisplay)
                canvas.drawText(resDisplay, rightX - resWidth, y, resultPaint)

                // Note (if present)
                if (item.note.isNotBlank()) {
                    y += 14f
                    val noteDisplay = "Note: ${item.note}"
                    canvas.drawText(noteDisplay, marginX + 10f, y, notePaint)
                }

                y += 18f
                canvas.drawLine(marginX, y, rightX, y, linePaint)
                y += 14f
            }

            // Footer
            val footerY = pageHeight - 35f
            canvas.drawLine(marginX, footerY - 10f, rightX, footerY - 10f, linePaint)
            subPaint.textSize = 9f
            canvas.drawText("Exported from Calc + AI Smart Scientific Suite", marginX, footerY, subPaint)

            pdf.finishPage(page)
        }

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

    companion object {
        fun printHistory(context: Context, historyList: List<CalculationHistory>) {
            if (historyList.isEmpty()) {
                Toast.makeText(context, "No calculation history to export", Toast.LENGTH_SHORT).show()
                return
            }
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                Toast.makeText(context, "Print service not available on device", Toast.LENGTH_SHORT).show()
                return
            }
            val jobName = "Calculation_History_${System.currentTimeMillis()}"
            printManager.print(jobName, CalculationHistoryPrintAdapter(context, historyList), null)
        }
    }
}

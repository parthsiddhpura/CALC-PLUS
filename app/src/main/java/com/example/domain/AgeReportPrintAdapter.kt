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
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * Native Android Print & Save-as-PDF adapter for Age & AstroSage Vedic Horoscope Report.
 */
class AgeReportPrintAdapter(
    private val context: Context,
    private val personName: String,
    private val birthDateTime: LocalDateTime,
    private val ageResult: AgeResult,
    private val astrologyResult: ComprehensiveAstrologyResult
) : PrintDocumentAdapter() {

    private val pageWidth = 595 // Standard A4 (595 x 842 pt)
    private val pageHeight = 842

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

        val safeName = personName.ifBlank { "Profile" }.replace(" ", "_")
        val info = PrintDocumentInfo.Builder("Age_Horoscope_Report_${safeName}.pdf")
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
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val marginX = 40f
        val rightX = 555f
        val df = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))
        val birthDateFmt = DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a (EEEE)", Locale.ENGLISH)
        val generatedFmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

        val titlePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val sectionTitlePaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 11.5f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val bodyBoldPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 10f
            isAntiAlias = true
        }

        val subPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 9f
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }

        val accentBgPaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
        }

        var y = 45f

        // Document Banner
        canvas.drawRect(marginX - 10f, y - 15f, rightX + 10f, y + 42f, accentBgPaint)
        canvas.drawText("AGE & ASTROSAGE VEDIC HOROSCOPE REPORT", marginX, y + 10f, titlePaint)
        val nameLabel = if (personName.isNotBlank()) "Profile: $personName" else "Personal Chronometer"
        canvas.drawText("$nameLabel • Generated on ${generatedFmt.format(Date())}", marginX, y + 26f, subPaint)

        y += 55f
        canvas.drawLine(marginX - 10f, y, rightX + 10f, y, linePaint)
        y += 18f

        // Section 1: Birth Details & Chronological Age
        canvas.drawText("1. CHRONOLOGICAL AGE SUMMARY", marginX, y, sectionTitlePaint)
        y += 14f

        canvas.drawText("Date & Time of Birth:", marginX + 10f, y, subPaint)
        canvas.drawText(birthDateTime.format(birthDateFmt), marginX + 130f, y, bodyBoldPaint)
        y += 15f

        canvas.drawText("Exact Chronological Age:", marginX + 10f, y, subPaint)
        val exactAgeStr = "${ageResult.years} Years, ${ageResult.months} Months, ${ageResult.days} Days (${ageResult.hours}h ${ageResult.minutes}m ${ageResult.seconds}s)"
        canvas.drawText(exactAgeStr, marginX + 130f, y, bodyBoldPaint)
        y += 15f

        canvas.drawText("Day Born:", marginX + 10f, y, subPaint)
        canvas.drawText(ageResult.dayOfWeekBorn, marginX + 130f, y, bodyPaint)
        y += 15f

        canvas.drawText("Next Birthday:", marginX + 10f, y, subPaint)
        val bdayStr = "In ${ageResult.nextBirthdayMonths} months, ${ageResult.nextBirthdayDays} days (will fall on ${ageResult.nextBirthdayDayOfWeek})"
        canvas.drawText(bdayStr, marginX + 130f, y, bodyPaint)
        y += 20f

        canvas.drawLine(marginX, y, rightX, y, linePaint)
        y += 18f

        // Section 2: Lifetime Totals
        canvas.drawText("2. TOTAL LIFE CHRONOMETRICS", marginX, y, sectionTitlePaint)
        y += 14f

        val col1X = marginX + 10f
        val col2X = marginX + 180f
        val col3X = marginX + 350f

        canvas.drawText("• Total Days Lived: ${df.format(ageResult.totalDays)}", col1X, y, bodyPaint)
        canvas.drawText("• Total Weeks: ${df.format(ageResult.totalWeeks)}", col2X, y, bodyPaint)
        canvas.drawText("• Total Months: ${df.format(ageResult.totalMonths)}", col3X, y, bodyPaint)
        y += 14f

        canvas.drawText("• Total Hours: ${df.format(ageResult.totalHours)}", col1X, y, bodyPaint)
        canvas.drawText("• Total Minutes: ${df.format(ageResult.totalMinutes)}", col2X, y, bodyPaint)
        canvas.drawText("• Total Seconds: ${df.format(ageResult.totalSeconds)}", col3X, y, bodyPaint)
        y += 20f

        canvas.drawLine(marginX, y, rightX, y, linePaint)
        y += 18f

        // Section 3: AstroSage Vedic Jyotish & Horoscope
        canvas.drawText("3. ASTROSAGE VEDIC JYOTISH & HOROSCOPE", marginX, y, sectionTitlePaint)
        y += 15f

        val rashi = astrologyResult.vedicMoonRashi
        canvas.drawText("Chandra Rashi (Moon Sign):", marginX + 10f, y, subPaint)
        val rashiStr = "${rashi.sanskritName} (${rashi.hindiName} / ${rashi.englishName}) ${rashi.symbol}"
        canvas.drawText(rashiStr, marginX + 160f, y, bodyBoldPaint)
        y += 15f

        canvas.drawText("Moon Position & Degree:", marginX + 10f, y, subPaint)
        canvas.drawText(astrologyResult.rashiDegreeStr, marginX + 160f, y, bodyPaint)
        y += 15f

        canvas.drawText("Ruling Lord & Element:", marginX + 10f, y, subPaint)
        canvas.drawText("${rashi.rulingPlanet} • Element: ${rashi.element}", marginX + 160f, y, bodyPaint)
        y += 15f

        canvas.drawText("Vedic Nakshatra:", marginX + 10f, y, subPaint)
        canvas.drawText("${astrologyResult.nakshatraName} (Pada ${astrologyResult.nakshatraPada}, Lord: ${astrologyResult.nakshatraLord})", marginX + 160f, y, bodyPaint)
        y += 15f

        canvas.drawText("Lucky Gemstone:", marginX + 10f, y, subPaint)
        canvas.drawText("${rashi.luckyGemstone} (${rashi.luckyGemstoneHindi})", marginX + 160f, y, bodyPaint)
        y += 15f

        canvas.drawText("Lucky Day & Color:", marginX + 10f, y, subPaint)
        canvas.drawText("${rashi.luckyDay} • ${rashi.luckyColor}", marginX + 160f, y, bodyPaint)
        y += 15f

        canvas.drawText("Lucky Numbers:", marginX + 10f, y, subPaint)
        canvas.drawText(rashi.luckyNumbers.joinToString(", "), marginX + 160f, y, bodyPaint)
        y += 15f

        canvas.drawText("Western & Chinese Zodiac:", marginX + 10f, y, subPaint)
        val westernStr = "${astrologyResult.westernSign} ${astrologyResult.westernSymbol} • Year of the ${astrologyResult.chineseZodiac} (${astrologyResult.chineseElement})"
        canvas.drawText(westernStr, marginX + 160f, y, bodyPaint)
        y += 18f

        // Vedic Beej Mantra
        canvas.drawText("Vedic Beej Mantra:", marginX + 10f, y, subPaint)
        val mantraPaint = Paint(bodyBoldPaint).apply {
            typeface = Typeface.MONOSPACE
            color = Color.parseColor("#1D4ED8")
        }
        canvas.drawText(rashi.mantra, marginX + 160f, y, mantraPaint)
        y += 20f

        // Planetary Guidance
        canvas.drawText("Daily Planetary Guidance (AstroSage):", marginX + 10f, y, subPaint)
        y += 13f
        val reading = astrologyResult.dailyHoroscopeReading
        val wrappedLines = wrapText(reading, rightX - marginX - 20f, bodyPaint)
        wrappedLines.forEach { line ->
            canvas.drawText(line, marginX + 10f, y, bodyPaint)
            y += 13f
        }

        y += 12f
        canvas.drawLine(marginX, y, rightX, y, linePaint)
        y += 18f

        // Footer
        val footerY = pageHeight - 35f
        canvas.drawLine(marginX, footerY - 10f, rightX, footerY - 10f, linePaint)
        subPaint.textSize = 9f
        canvas.drawText("Calc + Chronometer & Vedic Astrology Report • Generated via AstroSage Ephemeris", marginX, footerY, subPaint)

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

    private fun wrapText(text: String, maxWidth: Float, paint: Paint): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)
        return lines
    }

    companion object {
        fun printAgeReport(
            context: Context,
            personName: String,
            birthDateTime: LocalDateTime,
            ageResult: AgeResult,
            astrologyResult: ComprehensiveAstrologyResult
        ) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                Toast.makeText(context, "Print service not available on device", Toast.LENGTH_SHORT).show()
                return
            }
            val jobName = "Age_Report_${personName.ifBlank { "Profile" }}_${System.currentTimeMillis()}"
            printManager.print(
                jobName,
                AgeReportPrintAdapter(context, personName, birthDateTime, ageResult, astrologyResult),
                null
            )
        }
    }
}

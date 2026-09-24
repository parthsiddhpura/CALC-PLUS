package com.example.domain

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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
 * Native Android Print & Save-as-PDF adapter for Age & Vedic Horoscope Report.
 * Displays app logo, CALC + branding, full-page comprehensive Vedic horoscopic report,
 * and developer attribution: "DEVELOPED BY PARTH SIDDHPURA".
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
        val info = PrintDocumentInfo.Builder("Age_Vedic_Horoscope_Report_${safeName}.pdf")
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

        val marginX = 36f
        val rightX = 559f
        val contentWidth = rightX - marginX
        val df = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))
        val birthDateFmt = DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a (EEEE)", Locale.ENGLISH)
        val generatedFmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

        val appTitlePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val reportTitlePaint = Paint().apply {
            color = Color.parseColor("#1E3A8A")
            textSize = 11.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.04f
        }

        val sectionTitlePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.03f
        }

        val bodyBoldPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 8.2f
            isAntiAlias = true
        }

        val subPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 7.8f
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 0.75f
        }

        val accentCardPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }

        val cardBorderPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }

        var y = 26f

        // ==========================================
        // 1. APP LOGO & HEADER BANNER
        // ==========================================
        val logoSize = 34f
        val logoRect = RectF(marginX, y, marginX + logoSize, y + logoSize)

        // 1. Official Logo Outer Black Squircle (Transparent outside)
        val logoBgPaint = Paint().apply {
            color = Color.parseColor("#000000")
            isAntiAlias = true
        }
        canvas.drawRoundRect(logoRect, 7.5f, 7.5f, logoBgPaint)

        // 2. Official Logo Inner White Rounded Card
        val innerCardSize = 16f
        val innerX = marginX + (logoSize - innerCardSize) / 2f
        val innerY = y + (logoSize - innerCardSize) / 2f
        val innerRect = RectF(innerX, innerY, innerX + innerCardSize, innerY + innerCardSize)
        val innerBgPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
        }
        canvas.drawRoundRect(innerRect, 3.5f, 3.5f, innerBgPaint)

        // 3. Official Logo Math Symbols in crisp Black
        val symbolPaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        // Top-Left: Minus Sign (−)
        canvas.drawRect(innerX + 2.4f, innerY + 4.2f, innerX + 7.0f, innerY + 5.5f, symbolPaint)

        // Top-Right: Multiply Sign (×)
        canvas.save()
        canvas.translate(innerX + 11.5f, innerY + 4.8f)
        canvas.rotate(45f)
        canvas.drawRect(-2.3f, -0.65f, 2.3f, 0.65f, symbolPaint)
        canvas.drawRect(-0.65f, -2.3f, 0.65f, 2.3f, symbolPaint)
        canvas.restore()

        // Bottom-Left: Plus Sign (+)
        canvas.drawRect(innerX + 2.4f, innerY + 11.2f, innerX + 7.0f, innerY + 12.5f, symbolPaint)
        canvas.drawRect(innerX + 4.05f, innerY + 9.55f, innerX + 5.35f, innerY + 14.15f, symbolPaint)

        // Bottom-Right: Equals Sign (=)
        canvas.drawRect(innerX + 9.2f, innerY + 10.1f, innerX + 13.8f, innerY + 11.3f, symbolPaint)
        canvas.drawRect(innerX + 9.2f, innerY + 12.3f, innerX + 13.8f, innerY + 13.5f, symbolPaint)

        // App Name & Document Titles next to Logo
        val textLeftX = marginX + logoSize + 10f
        canvas.drawText("CALC +", textLeftX, y + 13f, appTitlePaint)

        // App tag
        val tagPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 7.5f
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
            isAntiAlias = true
        }
        canvas.drawText("PRECISION CHRONOMETER & VEDIC ASTROLOGY", textLeftX + 54f, y + 13f, tagPaint)

        // "AGE AND VEDIC HOROSCOPE REPORT"
        canvas.drawText("AGE AND VEDIC HOROSCOPE REPORT", textLeftX, y + 26f, reportTitlePaint)

        // Subtitle line: Native name and generation time
        val nativeName = if (personName.isNotBlank()) personName else "Personal Chronometer"
        val subtitleStr = "Native: $nativeName  •  DOB: ${birthDateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH))}  •  Generated: ${generatedFmt.format(Date())}"
        canvas.drawText(subtitleStr, textLeftX, y + 36f, subPaint)

        y += 44f
        canvas.drawLine(marginX, y, rightX, y, linePaint)
        y += 12f

        // ==========================================
        // 2. CHRONOLOGICAL AGE & LIFE CHRONOMETRICS
        // ==========================================
        canvas.drawText("1. CHRONOLOGICAL AGE & LIFE CHRONOMETRICS", marginX, y, sectionTitlePaint)
        y += 12f

        // Exact Age Box
        val ageCardRect = RectF(marginX, y, rightX, y + 42f)
        canvas.drawRoundRect(ageCardRect, 6f, 6f, accentCardPaint)
        canvas.drawRoundRect(ageCardRect, 6f, 6f, cardBorderPaint)

        val exactAgeStr = "${ageResult.years} Years, ${ageResult.months} Months, ${ageResult.days} Days  (${ageResult.hours}h ${ageResult.minutes}m ${ageResult.seconds}s)"
        canvas.drawText("Exact Chronological Age:", marginX + 10f, y + 14f, subPaint)
        canvas.drawText(exactAgeStr, marginX + 125f, y + 14f, bodyBoldPaint)

        canvas.drawText("Day Born:", marginX + 10f, y + 26f, subPaint)
        canvas.drawText(ageResult.dayOfWeekBorn, marginX + 125f, y + 26f, bodyPaint)

        canvas.drawText("Next Birthday:", marginX + 220f, y + 26f, subPaint)
        val nextBdayStr = "In ${ageResult.nextBirthdayMonths} months, ${ageResult.nextBirthdayDays} days (will fall on ${ageResult.nextBirthdayDayOfWeek})"
        canvas.drawText(nextBdayStr, marginX + 285f, y + 26f, bodyPaint)

        // Lifetime stats summary line
        val totalsStr = "Lifetime Totals:  ${df.format(ageResult.totalDays)} Days  •  ${df.format(ageResult.totalWeeks)} Weeks  •  ${df.format(ageResult.totalMonths)} Months  •  ${df.format(ageResult.totalHours)} Hours  •  ${df.format(ageResult.totalMinutes)} Mins"
        canvas.drawText(totalsStr, marginX + 10f, y + 36f, subPaint)

        y += 50f
        canvas.drawLine(marginX, y, rightX, y, linePaint)
        y += 12f

        // ==========================================
        // 3. VEDIC KUNDLI & ASTRONOMICAL PLACEMENT
        // ==========================================
        canvas.drawText("2. VEDIC KUNDLI & ASTRONOMICAL PLACEMENT", marginX, y, sectionTitlePaint)
        y += 12f

        val rashi = astrologyResult.vedicMoonRashi

        // 2-Column layout for Vedic details
        val col1LabelX = marginX + 8f
        val col1ValX = marginX + 125f
        val col2LabelX = marginX + 270f
        val col2ValX = marginX + 380f

        // Row 1: Chandra Rashi & Nakshatra
        canvas.drawText("Chandra Rashi (Moon Sign):", col1LabelX, y, subPaint)
        val moonSignStr = "${rashi.sanskritName} (${rashi.hindiName} / ${rashi.englishName}) ${rashi.symbol}"
        canvas.drawText(moonSignStr, col1ValX, y, bodyBoldPaint)

        canvas.drawText("Vedic Nakshatra:", col2LabelX, y, subPaint)
        val nakshatraStr = "${astrologyResult.nakshatraName} (Pada ${astrologyResult.nakshatraPada}, Lord: ${astrologyResult.nakshatraLord})"
        canvas.drawText(nakshatraStr, col2ValX, y, bodyBoldPaint)
        y += 13f

        // Row 2: Moon Longitude & Ruling Planet
        canvas.drawText("Moon Longitude & Degree:", col1LabelX, y, subPaint)
        canvas.drawText(astrologyResult.rashiDegreeStr, col1ValX, y, bodyPaint)

        canvas.drawText("Ruling Graha (Lord):", col2LabelX, y, subPaint)
        canvas.drawText("${rashi.rulingPlanet} (${rashi.rulingPlanetHindi})", col2ValX, y, bodyPaint)
        y += 13f

        // Row 3: Element & Sign Quality
        canvas.drawText("Elemental Nature (Tattva):", col1LabelX, y, subPaint)
        canvas.drawText("${rashi.element} (${rashi.elementHindi})", col1ValX, y, bodyPaint)

        canvas.drawText("Sign Quality (Guna):", col2LabelX, y, subPaint)
        canvas.drawText(rashi.quality, col2ValX, y, bodyPaint)
        y += 13f

        // Row 4: Surya Rashi & Western Sign
        canvas.drawText("Surya Rashi (Sun Sign):", col1LabelX, y, subPaint)
        canvas.drawText("${astrologyResult.vedicSuryaRashi.sanskritName} (${astrologyResult.vedicSuryaRashi.hindiName})", col1ValX, y, bodyPaint)

        canvas.drawText("Western Zodiac Sign:", col2LabelX, y, subPaint)
        canvas.drawText("${astrologyResult.westernSign} ${astrologyResult.westernSymbol}", col2ValX, y, bodyPaint)
        y += 13f

        // Row 5: Chinese Zodiac & Birthstone
        canvas.drawText("Chinese Zodiac & Element:", col1LabelX, y, subPaint)
        canvas.drawText("Year of the ${astrologyResult.chineseZodiac} (${astrologyResult.chineseElement})", col1ValX, y, bodyPaint)

        canvas.drawText("Traditional Birthstone:", col2LabelX, y, subPaint)
        canvas.drawText(astrologyResult.birthStone, col2ValX, y, bodyPaint)
        y += 16f

        canvas.drawLine(marginX, y, rightX, y, linePaint)
        y += 12f

        // ==========================================
        // 4. AUSPICIOUS ASTROLOGICAL CORRESPONDENCES
        // ==========================================
        canvas.drawText("3. AUSPICIOUS ASTROLOGICAL CORRESPONDENCES", marginX, y, sectionTitlePaint)
        y += 12f

        // Row 1: Gemstone & Day
        canvas.drawText("Lucky Gemstone (Ratna):", col1LabelX, y, subPaint)
        canvas.drawText("${rashi.luckyGemstone} (${rashi.luckyGemstoneHindi})", col1ValX, y, bodyBoldPaint)

        canvas.drawText("Lucky Day of Week:", col2LabelX, y, subPaint)
        canvas.drawText(rashi.luckyDay, col2ValX, y, bodyPaint)
        y += 13f

        // Row 2: Lucky Colors & Numbers
        canvas.drawText("Auspicious Colors:", col1LabelX, y, subPaint)
        canvas.drawText(rashi.luckyColor, col1ValX, y, bodyPaint)

        canvas.drawText("Auspicious Numbers:", col2LabelX, y, subPaint)
        canvas.drawText("${rashi.luckyNumbers.joinToString(", ")}  (Daily Transit: ${astrologyResult.dailyLuckyNumber})", col2ValX, y, bodyPaint)
        y += 13f

        // Row 3: Friendly Signs & Sacred Name Letters
        canvas.drawText("Compatible Rashis:", col1LabelX, y, subPaint)
        canvas.drawText(rashi.friendlyRashis.joinToString(", "), col1ValX, y, bodyPaint)

        canvas.drawText("Auspicious Name Letters:", col2LabelX, y, subPaint)
        canvas.drawText(rashi.nameLetters, col2ValX, y, bodyPaint)
        y += 16f

        canvas.drawLine(marginX, y, rightX, y, linePaint)
        y += 11f

        // ==========================================
        // 5. SACRED VEDIC BEEJ MANTRA (GRAHA SHANTI)
        // ==========================================
        val mantraCardHeight = 36f
        val mantraCardRect = RectF(marginX, y, rightX, y + mantraCardHeight)

        val mantraBgPaint = Paint().apply {
            color = Color.parseColor("#EFF6FF")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val mantraBorderPaint = Paint().apply {
            color = Color.parseColor("#93C5FD")
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            isAntiAlias = true
        }
        canvas.drawRoundRect(mantraCardRect, 6f, 6f, mantraBgPaint)
        canvas.drawRoundRect(mantraCardRect, 6f, 6f, mantraBorderPaint)

        val mantraHeaderPaint = Paint().apply {
            color = Color.parseColor("#1E40AF")
            textSize = 7.5f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            letterSpacing = 0.04f
        }
        canvas.drawText("SACRED VEDIC BEEJ MANTRA FOR GRAHA SHANTI & PLANETARY BLESSINGS", marginX + 10f, y + 11f, mantraHeaderPaint)

        val mantraTextPaint = Paint().apply {
            color = Color.parseColor("#1D4ED8")
            textSize = 8.8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(rashi.mantra, marginX + 10f, y + 23f, mantraTextPaint)

        val mantraNotePaint = Paint().apply {
            color = Color.parseColor("#3B82F6")
            textSize = 7.2f
            isAntiAlias = true
        }
        canvas.drawText("Recite this auspicious Beej Mantra 108 times daily to neutralize planetary malefic influences and enhance vital energy.", marginX + 10f, y + 32f, mantraNotePaint)

        y += mantraCardHeight + 8f
        canvas.drawLine(marginX, y, rightX, y, linePaint)
        y += 12f

        // ==========================================
        // 6. VEDIC PERSONALITY & CHARACTER ARCHETYPE
        // ==========================================
        canvas.drawText("4. VEDIC PERSONALITY & CHARACTER ARCHETYPE", marginX, y, sectionTitlePaint)
        y += 12f

        canvas.drawText("Inherent Temperament:", col1LabelX, y, subPaint)
        canvas.drawText(rashi.nature, col1ValX, y, bodyPaint)
        y += 12f

        canvas.drawText("Core Key Strengths:", col1LabelX, y, subPaint)
        canvas.drawText(rashi.positiveTraits.joinToString("  •  "), col1ValX, y, bodyBoldPaint)
        y += 12f

        val cleanPersonality = rashi.personalityOverview
            .replace("AstroSage Analysis:", "Vedic Jyotish Analysis:")
            .replace("AstroSage", "Vedic")
            .trim()
        val personalityLines = wrapText(cleanPersonality, contentWidth - 10f, bodyPaint)
        personalityLines.forEach { line ->
            canvas.drawText(line, marginX + 6f, y, bodyPaint)
            y += 11f
        }
        y += 2f

        canvas.drawLine(marginX, y, rightX, y, linePaint)
        y += 11f

        // ==========================================
        // 7. VEDIC LIFE GUIDANCE — CAREER, WEALTH & HEALTH
        // ==========================================
        canvas.drawText("5. VEDIC LIFE GUIDANCE — CAREER, WEALTH & HEALTH", marginX, y, sectionTitlePaint)
        y += 12f

        // Career Guidance
        canvas.drawText("• Career & Vocational Pathways:", col1LabelX, y, bodyBoldPaint)
        y += 10f
        val careerLines = wrapText(rashi.careerAdvice, contentWidth - 20f, bodyPaint)
        careerLines.forEach { line ->
            canvas.drawText(line, marginX + 16f, y, bodyPaint)
            y += 10f
        }
        y += 3f

        // Wealth Guidance
        canvas.drawText("• Wealth & Financial Accumulation:", col1LabelX, y, bodyBoldPaint)
        y += 10f
        val wealthLines = wrapText(rashi.wealthGuidance, contentWidth - 20f, bodyPaint)
        wealthLines.forEach { line ->
            canvas.drawText(line, marginX + 16f, y, bodyPaint)
            y += 10f
        }
        y += 3f

        // Health Guidance
        canvas.drawText("• Health & Holistic Vitality:", col1LabelX, y, bodyBoldPaint)
        y += 10f
        val healthLines = wrapText(rashi.healthAdvice, contentWidth - 20f, bodyPaint)
        healthLines.forEach { line ->
            canvas.drawText(line, marginX + 16f, y, bodyPaint)
            y += 10f
        }
        y += 4f

        canvas.drawLine(marginX, y, rightX, y, linePaint)
        y += 11f

        // ==========================================
        // 8. PLANETARY TRANSIT & COSMIC FORECAST
        // ==========================================
        canvas.drawText("6. PLANETARY TRANSIT & COSMIC FORECAST", marginX, y, sectionTitlePaint)
        y += 11f

        canvas.drawText("Planetary Transit Alignment:", col1LabelX, y, subPaint)
        canvas.drawText(astrologyResult.planetaryTransitNote, col1ValX, y, bodyBoldPaint)
        y += 12f

        val cleanReading = astrologyResult.dailyHoroscopeReading
            .replace("AstroSage Transit:", "Vedic Transit Influence:")
            .replace("AstroSage", "Vedic")
            .trim()
        val readingLines = wrapText(cleanReading, contentWidth - 10f, bodyPaint)
        readingLines.forEach { line ->
            canvas.drawText(line, marginX + 6f, y, bodyPaint)
            y += 11f
        }

        // ==========================================
        // 9. FOOTER ATTRIBUTION (DEVELOPED BY PARTH SIDDHPURA)
        // ==========================================
        val footerY = pageHeight - 32f
        canvas.drawLine(marginX, footerY - 10f, rightX, footerY - 10f, linePaint)

        // Shaded footer highlight box
        val footerBoxRect = RectF(marginX, footerY - 7f, rightX, footerY + 15f)
        val footerBoxPaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(footerBoxRect, 5f, 5f, footerBoxPaint)

        // Prominent Developer Attribution: DEVELOPED BY PARTH SIDDHPURA
        val devPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.06f
            isAntiAlias = true
        }
        canvas.drawText("DEVELOPED BY PARTH SIDDHPURA", marginX + 10f, footerY + 7f, devPaint)

        // App Branding on Right Side of Footer
        val brandPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.04f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("CALC + ULTRA  •  PRECISION CHRONOMETER & VEDIC ASTROLOGY", rightX - 10f, footerY + 7f, brandPaint)

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
            val jobName = "Age_Vedic_Horoscope_Report_${personName.ifBlank { "Profile" }}_${System.currentTimeMillis()}"
            printManager.print(
                jobName,
                AgeReportPrintAdapter(context, personName, birthDateTime, ageResult, astrologyResult),
                null
            )
        }
    }
}

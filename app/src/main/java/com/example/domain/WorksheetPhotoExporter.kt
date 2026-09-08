package com.example.domain

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.WorksheetDocument
import com.example.model.WorksheetLine
import com.example.model.WorksheetLineType
import com.example.model.WorksheetSettings
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WorksheetPhotoExporter {

    /**
     * Renders an authentic paper tape ledger onto a high-resolution Bitmap matching the app aesthetic.
     */
    fun createWorksheetBitmap(
        context: Context,
        document: WorksheetDocument,
        settings: WorksheetSettings = WorksheetSettings()
    ): Bitmap {
        val width = 1080
        val paddingX = 64f
        val headerHeight = 220f
        val rowHeight = 72f
        val footerHeight = 180f

        val totalLines = maxOf(document.lines.size, 8)
        val height = (headerHeight + (totalLines * rowHeight) + footerHeight).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Colors matching app dark slate theme
        val bgColor = Color.parseColor("#2C3036")
        val ruledLineColor = Color.parseColor("#3A424C")
        val headerBgColor = Color.parseColor("#22272E")
        val textPrimaryColor = Color.parseColor("#F1F5F9")
        val textSecondaryColor = Color.parseColor("#94A3B8")
        val deductionColor = Color.parseColor("#E57373")
        val dividerColor = Color.parseColor("#64748B")
        val accentGreen = Color.parseColor("#4CAF50")
        val grandTotalBg = Color.parseColor("#1F242C")

        // Draw canvas background
        canvas.drawColor(bgColor)

        // Draw horizontal ruled lines across entire canvas
        val ruledPaint = Paint().apply {
            color = ruledLineColor
            strokeWidth = 2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        var curRuledY = headerHeight
        while (curRuledY < height - footerHeight + rowHeight) {
            canvas.drawLine(0f, curRuledY, width.toFloat(), curRuledY, ruledPaint)
            curRuledY += rowHeight
        }

        // Header Background
        val headerBgPaint = Paint().apply { color = headerBgColor }
        canvas.drawRect(0f, 0f, width.toFloat(), headerHeight, headerBgPaint)

        // App Branding & Status in Header
        val brandPaint = Paint().apply {
            color = accentGreen
            textSize = 28f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("[ ⏱ EVAL ]  CALC +", paddingX, 60f, brandPaint)

        // Document Title
        val titlePaint = Paint().apply {
            color = textPrimaryColor
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(document.title, paddingX, 126f, titlePaint)

        // Formatted Date
        val datePaint = Paint().apply {
            color = textSecondaryColor
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val dateFormat = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(document.updatedAt))
        canvas.drawText("Last updated: $dateStr", paddingX, 175f, datePaint)

        // Header bottom border
        val headerBorderPaint = Paint().apply {
            color = Color.parseColor("#38414D")
            strokeWidth = 3f
        }
        canvas.drawLine(0f, headerHeight, width.toFloat(), headerHeight, headerBorderPaint)

        // Paints for calculation rows
        val opPaint = Paint().apply {
            color = textPrimaryColor
            textSize = 36f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        val numPaint = Paint().apply {
            color = textPrimaryColor
            textSize = 36f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        val deductionPaint = Paint().apply {
            color = deductionColor
            textSize = 36f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        val notePaint = Paint().apply {
            color = textSecondaryColor
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val rowDividerPaint = Paint().apply {
            color = dividerColor
            strokeWidth = 2.5f
            isAntiAlias = true
        }

        // Render each line
        var yPos = headerHeight + 50f
        for (line in document.lines) {
            // Divider line before subtotal or grand total
            if (line.hasDividerBefore || line.lineType == WorksheetLineType.SUB_TOTAL || line.lineType == WorksheetLineType.GRAND_TOTAL) {
                canvas.drawLine(paddingX, yPos - 38f, width - paddingX, yPos - 38f, rowDividerPaint)
            }

            val isDeduction = line.operator == "-" || line.evaluatedNumber < 0
            val currentNumPaint = if (isDeduction) deductionPaint else numPaint

            // Operator
            canvas.drawText(line.operator, paddingX, yPos, opPaint)

            // Number with Indian digit grouping
            val formattedNum = WorksheetTapeEngine.formatNumber(line.evaluatedNumber, settings.indianDigitGrouping, settings.decimals)
            canvas.drawText(formattedNum, paddingX + 70f, yPos, currentNumPaint)

            // Note / remark
            if (line.note.isNotBlank()) {
                val numWidth = currentNumPaint.measureText(formattedNum)
                canvas.drawText(line.note, paddingX + 90f + numWidth, yPos - 2f, notePaint)
            }

            yPos += rowHeight
        }

        // Footer / Grand Total Summary Card
        val footerStartY = height - footerHeight.toFloat()
        val footerBgPaint = Paint().apply { color = grandTotalBg }
        canvas.drawRect(0f, footerStartY, width.toFloat(), height.toFloat(), footerBgPaint)

        val footerDividerPaint = Paint().apply {
            color = Color.parseColor("#475569")
            strokeWidth = 3f
        }
        canvas.drawLine(0f, footerStartY, width.toFloat(), footerStartY, footerDividerPaint)

        // Grand Total Label & Value
        val totalLabelPaint = Paint().apply {
            color = textSecondaryColor
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("GRAND TOTAL", paddingX, footerStartY + 64f, totalLabelPaint)

        val totalValuePaint = Paint().apply {
            color = if (document.grandTotal < 0) deductionColor else Color.parseColor("#38BDF8")
            textSize = 54f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        val formattedGrandTotal = WorksheetTapeEngine.formatNumber(document.grandTotal, settings.indianDigitGrouping, settings.decimals)
        canvas.drawText(formattedGrandTotal, paddingX, footerStartY + 128f, totalValuePaint)

        // Watermark on right
        val watermarkPaint = Paint().apply {
            color = textSecondaryColor
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("CALC + Worksheet & Paper Tape", width - paddingX, footerStartY + 120f, watermarkPaint)

        return bitmap
    }

    /**
     * Saves the worksheet bitmap to app cache and launches Android's standard photo sharing sheet.
     */
    fun shareAsPhoto(context: Context, document: WorksheetDocument, settings: WorksheetSettings = WorksheetSettings()) {
        try {
            val bitmap = createWorksheetBitmap(context, document, settings)
            val cacheImagesDir = File(context.cacheDir, "images").apply { mkdirs() }
            val cleanTitle = document.title.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val photoFile = File(cacheImagesDir, "worksheet_${cleanTitle}_${System.currentTimeMillis()}.png")

            FileOutputStream(photoFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val photoUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, photoUri)
                putExtra(Intent.EXTRA_SUBJECT, document.title)
                putExtra(Intent.EXTRA_TEXT, "Worksheet: ${document.title} (Total: ${WorksheetTapeEngine.formatNumber(document.grandTotal, settings.indianDigitGrouping, settings.decimals)})")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Worksheet as Photo"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not share as photo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Saves the worksheet photo directly to the device's Pictures / Gallery.
     */
    fun savePhotoToGallery(context: Context, document: WorksheetDocument, settings: WorksheetSettings = WorksheetSettings()): Boolean {
        return try {
            val bitmap = createWorksheetBitmap(context, document, settings)
            val filename = "Worksheet_${document.title.replace("[^a-zA-Z0-9]".toRegex(), "_")}_${System.currentTimeMillis()}.png"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Worksheets")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    Toast.makeText(context, "Saved to Pictures/Worksheets", Toast.LENGTH_SHORT).show()
                    true
                } else {
                    false
                }
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(picturesDir, "Worksheets").apply { mkdirs() }
                val file = File(appDir, filename)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT).show()
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving photo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}

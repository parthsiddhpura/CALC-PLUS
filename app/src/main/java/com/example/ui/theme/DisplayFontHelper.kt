package com.example.ui.theme

import android.graphics.Typeface
import android.os.Build
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.model.DisplayFontType

object DisplayFontHelper {

    val MonospaceFont: FontFamily = FontFamily.Monospace

    val DigitalLcdFont: FontFamily = try {
        FontFamily(Typeface.create("sans-serif-condensed", Typeface.BOLD))
    } catch (e: Throwable) {
        FontFamily.Monospace
    }

    val ModernSansFont: FontFamily = FontFamily.SansSerif

    val RoundedFont: FontFamily = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            FontFamily(Typeface.create("sans-serif-rounded", Typeface.NORMAL))
        } else {
            FontFamily(Typeface.create("casual", Typeface.NORMAL))
        }
    } catch (e: Throwable) {
        try {
            FontFamily(Typeface.create("casual", Typeface.NORMAL))
        } catch (e2: Throwable) {
            FontFamily.SansSerif
        }
    }

    val PixelFont: FontFamily = try {
        FontFamily(Typeface.create("monospace", Typeface.BOLD))
    } catch (e: Throwable) {
        FontFamily.Monospace
    }

    val KawaiiFont: FontFamily = try {
        FontFamily(Typeface.create("casual", Typeface.BOLD))
    } catch (e: Throwable) {
        FontFamily.Cursive
    }

    fun getFontFamily(fontType: DisplayFontType): FontFamily {
        return when (fontType) {
            DisplayFontType.MONOSPACE -> MonospaceFont
            DisplayFontType.DIGITAL_LCD -> DigitalLcdFont
            DisplayFontType.MODERN_SANS -> ModernSansFont
            DisplayFontType.ROUNDED -> RoundedFont
            DisplayFontType.PIXEL_8BIT -> PixelFont
            DisplayFontType.KAWAII_CANDY -> KawaiiFont
        }
    }

    fun getLetterSpacing(fontType: DisplayFontType): TextUnit {
        return when (fontType) {
            DisplayFontType.DIGITAL_LCD -> 2.0.sp
            DisplayFontType.MONOSPACE -> 0.6.sp
            DisplayFontType.PIXEL_8BIT -> 1.5.sp
            else -> 0.sp
        }
    }

    fun getFontWeight(fontType: DisplayFontType, isResult: Boolean = false): FontWeight {
        return when (fontType) {
            DisplayFontType.DIGITAL_LCD -> FontWeight.Bold
            DisplayFontType.PIXEL_8BIT -> FontWeight.ExtraBold
            DisplayFontType.ROUNDED -> if (isResult) FontWeight.Bold else FontWeight.Medium
            DisplayFontType.MONOSPACE -> if (isResult) FontWeight.Bold else FontWeight.Normal
            else -> if (isResult) FontWeight.Bold else FontWeight.Medium
        }
    }
}

package com.example.widget

import android.content.res.ColorStateList
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import com.example.R
import com.example.model.ThemeId
import com.example.model.ThemePalette
import com.example.model.onSurfaceSubtextColor
import com.example.model.onSurfaceTextColor
import com.example.ui.theme.CalculatorThemes

/**
 * Dynamic Widget Theme model that mirrors the exact ThemePalette selected by the user in the app.
 * Replaces the legacy translucent glass theme with 100% solid, high-contrast theme synchronization.
 */
data class WidgetDynamicTheme(
    val themeId: ThemeId,
    val title: String,
    val description: String,
    @DrawableRes val bgRes: Int = R.drawable.widget_bg_minimal,
    @DrawableRes val screenBgRes: Int = R.drawable.widget_screen_bg_minimal,
    @DrawableRes val btnBgRes: Int = R.drawable.widget_btn_bg_minimal,
    @DrawableRes val btnAccentBgRes: Int = R.drawable.widget_btn_accent_glass,
    val bgColorHex: Int,
    val screenBgColorHex: Int,
    val btnBgColorHex: Int,
    val btnAccentBgColorHex: Int,
    val textColorHex: Int,
    val subtextColorHex: Int,
    val accentColorHex: Int,
    val headerTextColorHex: Int,
    val btnTextColorHex: Int,
    val btnAccentTextColorHex: Int,
    val composeBackground: Color,
    val composeAccent: Color
) {
    fun applyRoot(views: RemoteViews, rootId: Int = R.id.widget_root) {
        views.setInt(rootId, "setBackgroundResource", bgRes)
        tintViewBackground(views, rootId, bgColorHex)
    }

    fun applyScreen(views: RemoteViews, screenId: Int) {
        views.setInt(screenId, "setBackgroundResource", screenBgRes)
        tintViewBackground(views, screenId, screenBgColorHex)
    }

    fun applyButton(views: RemoteViews, buttonId: Int, isAccent: Boolean = false) {
        views.setInt(buttonId, "setBackgroundResource", if (isAccent) btnAccentBgRes else btnBgRes)
        tintViewBackground(views, buttonId, if (isAccent) btnAccentBgColorHex else btnBgColorHex)
        views.setTextColor(buttonId, if (isAccent) btnAccentTextColorHex else btnTextColorHex)
    }

    fun applyThemeCycleButton(views: RemoteViews, buttonId: Int = R.id.btn_theme_cycle) {
        views.setInt(buttonId, "setBackgroundResource", btnBgRes)
        tintViewBackground(views, buttonId, btnBgColorHex)
        views.setTextColor(buttonId, accentColorHex)
        val shortTitle = if (title.length > 12) title.take(12) else title
        views.setTextViewText(buttonId, "🎨 $shortTitle")
    }

    companion object {
        private fun tintViewBackground(views: RemoteViews, viewId: Int, colorArgb: Int) {
            val opaqueColor = colorArgb or (0xFF shl 24)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                views.setColorStateList(
                    viewId,
                    "setBackgroundTintList",
                    ColorStateList.valueOf(opaqueColor)
                )
            } else {
                views.setInt(viewId, "setBackgroundColor", opaqueColor)
            }
        }

        fun fromThemePalette(palette: ThemePalette): WidgetDynamicTheme {
            val solidBg = palette.backgroundColor.copy(alpha = 1f)
            val solidScreen = palette.screenBackground.copy(alpha = 1f)
            val solidBtn = palette.numberButtonBg.copy(alpha = 1f)
            val solidOperatorBtn = palette.operatorButtonBg.copy(alpha = 1f)

            val readableAccent = if (
                palette.isDark && palette.accentColor.luminance() < 0.15f
            ) {
                Color(0xFFF5C518)
            } else if (
                !palette.isDark && palette.backgroundColor.luminance() > 0.65f && palette.accentColor.luminance() > 0.70f
            ) {
                palette.secondaryAccent.copy(alpha = 1f)
            } else {
                palette.accentColor.copy(alpha = 1f)
            }

            val headerColor = if (
                (palette.backgroundColor.luminance() > 0.55f && readableAccent.luminance() > 0.6f) ||
                (palette.backgroundColor.luminance() < 0.25f && readableAccent.luminance() < 0.15f)
            ) {
                palette.onSurfaceTextColor.copy(alpha = 1f)
            } else {
                readableAccent
            }

            val screenText = palette.screenTextColor.copy(alpha = 1f)
            val screenSubtext = palette.screenExpressionColor.copy(alpha = 1f)
            val numBtnText = palette.numberButtonText.copy(alpha = 1f)
            val opBtnText = palette.operatorButtonText.copy(alpha = 1f)

            return WidgetDynamicTheme(
                themeId = palette.id,
                title = palette.name,
                description = palette.subtitle,
                bgRes = R.drawable.widget_bg_minimal,
                screenBgRes = R.drawable.widget_screen_bg_minimal,
                btnBgRes = R.drawable.widget_btn_bg_minimal,
                btnAccentBgRes = R.drawable.widget_btn_accent_glass,
                bgColorHex = solidBg.toArgb(),
                screenBgColorHex = solidScreen.toArgb(),
                btnBgColorHex = solidBtn.toArgb(),
                btnAccentBgColorHex = solidOperatorBtn.toArgb(),
                textColorHex = screenText.toArgb(),
                subtextColorHex = screenSubtext.toArgb(),
                accentColorHex = readableAccent.toArgb(),
                headerTextColorHex = headerColor.toArgb(),
                btnTextColorHex = numBtnText.toArgb(),
                btnAccentTextColorHex = opBtnText.toArgb(),
                composeBackground = solidBg,
                composeAccent = readableAccent
            )
        }
    }
}

enum class WidgetThemeFamily(
    val title: String,
    val description: String,
    @DrawableRes val bgRes: Int,
    @DrawableRes val screenBgRes: Int,
    @DrawableRes val btnBgRes: Int,
    @DrawableRes val btnAccentBgRes: Int,
    val textColorHex: Int,
    val subtextColorHex: Int,
    val accentColorHex: Int,
    val headerTextColorHex: Int,
    val composeBackground: Color,
    val composeAccent: Color
) {
    DARK_KNIGHT(
        title = "The Dark Knight",
        description = "Matte obsidian armor with Gotham gold accents",
        bgRes = R.drawable.widget_bg_minimal,
        screenBgRes = R.drawable.widget_screen_bg_minimal,
        btnBgRes = R.drawable.widget_btn_bg_minimal,
        btnAccentBgRes = R.drawable.widget_btn_accent_glass,
        textColorHex = 0xFFF8FAFC.toInt(),
        subtextColorHex = 0xFF94A3B8.toInt(),
        accentColorHex = 0xFFF5C518.toInt(),
        headerTextColorHex = 0xFFF5C518.toInt(),
        composeBackground = Color(0xFF090B0E),
        composeAccent = Color(0xFFF5C518)
    ),
    MINIMAL(
        title = "Minimal",
        description = "Clean, dark monochrome with razor-sharp contrast",
        bgRes = R.drawable.widget_bg_minimal,
        screenBgRes = R.drawable.widget_screen_bg_minimal,
        btnBgRes = R.drawable.widget_btn_bg_minimal,
        btnAccentBgRes = R.drawable.widget_btn_bg_minimal,
        textColorHex = 0xFFFFFFFF.toInt(),
        subtextColorHex = 0xFF8A93A6.toInt(),
        accentColorHex = 0xFFFFFFFF.toInt(),
        headerTextColorHex = 0xFFFFFFFF.toInt(),
        composeBackground = Color(0xFF121418),
        composeAccent = Color(0xFFFFFFFF)
    ),
    RETRO(
        title = "Retro Mac 1984 / CRT",
        description = "Macintosh 1984 beige casing & glowing CRT green phosphor",
        bgRes = R.drawable.widget_bg_retro,
        screenBgRes = R.drawable.widget_screen_bg_retro,
        btnBgRes = R.drawable.widget_btn_bg_retro,
        btnAccentBgRes = R.drawable.widget_btn_accent_retro,
        textColorHex = 0xFF00FF66.toInt(),
        subtextColorHex = 0xFF00B347.toInt(),
        accentColorHex = 0xFF00FF66.toInt(),
        headerTextColorHex = 0xFF3D362A.toInt(),
        composeBackground = Color(0xFFD6CEBE),
        composeAccent = Color(0xFF00FF66)
    );

    fun toDynamicTheme(): WidgetDynamicTheme {
        return when (this) {
            DARK_KNIGHT -> WidgetDynamicTheme.fromThemePalette(CalculatorThemes.getThemeById(ThemeId.BATMAN_DARK_KNIGHT))
            MINIMAL -> WidgetDynamicTheme.fromThemePalette(CalculatorThemes.getThemeById(ThemeId.MONOCHROME_LUXURY))
            RETRO -> WidgetDynamicTheme.fromThemePalette(CalculatorThemes.getThemeById(ThemeId.MAC_CLASSIC_1984))
        }
    }
}

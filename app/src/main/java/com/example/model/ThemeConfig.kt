package com.example.model

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ThemeId {
    CYBERPUNK,
    SYNTHWAVE,
    NEO_BRUTALISM,
    CRT_TERMINAL,
    MATCHA_ZEN,
    SUNSET_GLOW,
    OLED_OBSIDIAN,
    PASTEL_BUBBLEGUM,
    MONOCHROME_LUXURY,
    NORDIC_ICE,
    GAMEBOY_8BIT,
    SOLARIZED_DARK,
    SAKURA_BLOOM,
    MATRIX_HACKER,
    AMETHYST_ROYAL,
    MAC_CLASSIC_1984,
    MAC_CLASSIC_1984_DARK,
    VAPORWAVE_95,
    ESPRESSO_ROAST,
    LAVA_MAGMA,
    COSMIC_AURORA,
    MIDNIGHT_TOKYO,
    EMERALD_FOREST,
    STEAMPUNK_BRASS,
    COTTON_CANDY,
    DRACULA_VAMPIRIC,
    DEEP_SPACE_SAPPHIRE,
    ABYSSAL_CRIMSON,
    CYBER_MATRIX_GOLD,
    NORD_POLAR_NIGHT,
    NEON_VOID,
    QUANTUM_SUPERCONDUCTOR,
    BATMAN_DARK_KNIGHT,
    IRON_MAN_MARK_85,
    IRON_MAN_STEALTH,
    IRON_MAN_SILVER_CENTURION,
    IRON_MAN_HULKBUSTER,
    BIOLUMINESCENT_ABYSS,
    ARCADE_PHOSPHOR_84,
    PHANTOM_STEALTH,
    NEUMORPHIC_ICE_LIGHT,
    NEUMORPHIC_MIDNIGHT_AZURE,
    MIDNIGHT_OCEAN_RADIAL,
    MINIMAL_POWDER_BLUE,
    OBSIDIAN_TANGERINE,
    OBSIDIAN_EMERALD,
    OBSIDIAN_COBALT,
    KAWAII_CLAY_PINK,
    KAWAII_MATCHA_CLAY,
    RETRO_MACARON_TYPEWRITER,
    RETRO_TYPEWRITER_SAGE,
    INDUSTRIAL_AMBER_BEZEL,
    INDUSTRIAL_CYAN_BEZEL,
    PICO_KAWAII_PIXEL,
    GIRL_MATH_PASTEL,
    Y2K_GLOSSY_POP,
    NEKO_MOCHI_CAT,
    NEKO_MOCHI_DARK,
    RETRO_CIRCUIT_RED,
    NOTHING_DOSSIER,
    BAUHAUS_DOSSIER,
    TERRACOTTA_STUDIO,
    ORTYL_MINIMAL_MATTE,
    OLED_STEALTH_VOID,
    STARRY_NIGHT_GOTHAM,
    COSMIC_SINGULARITY
}

enum class ButtonShapeType {
    PILL,
    SQUIRCLE,
    ROUNDED_SQUARE,
    BRUTALIST_RECT,
    CIRCLE,
    PIXEL_BLOCK,
    GLOSSY_JELLY,
    NEKO_EARS
}

enum class PressAnimationType {
    BOUNCE,
    DEEP_SINK,
    NEON_GLOW,
    BRUTAL_OFFSET,
    JELLY_SQUISH,
    PIXEL_STEP
}

enum class DisplayFontType {
    MONOSPACE,
    MODERN_SANS,
    DIGITAL_LCD,
    ROUNDED,
    PIXEL_8BIT,
    KAWAII_CANDY
}

data class ThemePalette(
    val id: ThemeId,
    val name: String,
    val subtitle: String,
    val category: String,
    val isDark: Boolean,
    
    // Background & Containers
    val backgroundBrush: Brush,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val cardBackground: Color,
    
    // Display Screen
    val screenBackground: Color,
    val screenBorderColor: Color,
    val screenTextColor: Color,
    val screenExpressionColor: Color,
    val screenPreviewColor: Color,
    val cursorColor: Color,
    val displayFont: DisplayFontType,
    val hasScanlines: Boolean = false,
    val hasBatSignal: Boolean = false,
    val hasArcReactor: Boolean = false,
    val ironManSuit: IronManSuitType? = null,
    
    // Keypad - Number Keys (0-9, .)
    val numberButtonBg: Color,
    val numberButtonText: Color,
    val numberButtonBorder: Color = Color.Transparent,
    
    // Keypad - Basic Operators (+, -, *, /)
    val operatorButtonBg: Color,
    val operatorButtonText: Color,
    val operatorButtonBorder: Color = Color.Transparent,
    
    // Keypad - Functions (C, AC, DEL, %, +/-, Trig, Log, (), etc.)
    val functionButtonBg: Color,
    val functionButtonText: Color,
    val functionButtonBorder: Color = Color.Transparent,
    
    // Equals Action Key (Special Accent)
    val equalsButtonBrush: Brush,
    val equalsButtonText: Color,
    val equalsButtonBorder: Color = Color.Transparent,
    
    // Accent Glow & Highlights
    val accentColor: Color,
    val secondaryAccent: Color,
    val glowColor: Color,
    
    // Physical Styling
    val shapeType: ButtonShapeType,
    val cornerRadiusDp: Dp = 16.dp,
    val borderWidthDp: Dp = 0.dp,
    val hasShadow: Boolean = false,
    val shadowElevationDp: Dp = 2.dp,
    val isBrutalistShadow: Boolean = false,
    val brutalistShadowColor: Color = Color.Black,
    val pressAnimation: PressAnimationType = PressAnimationType.BOUNCE,
    
    // Status Bar & Navigation
    val statusBarDarkIcons: Boolean = false,

    // Specialized Kawaii / Retro / Pop Styling Flags
    val isPixelArt: Boolean = false,
    val isGirlMath: Boolean = false,
    val isY2kGlossy: Boolean = false,
    val isNekoMochi: Boolean = false,
    val isRetroCircuit: Boolean = false,
    val isNothingDossier: Boolean = false,
    val isBauhausDossier: Boolean = false,
    val isTerracottaStudio: Boolean = false,
    val isOrtylMinimal: Boolean = false,
    val isOledStealthVoid: Boolean = false,
    val isStarryGotham: Boolean = false,
    val isCosmicSingularity: Boolean = false,
    val customKeyColors: Map<String, Color>? = null,
    val customKeyTextColors: Map<String, Color>? = null
) {
    fun getShape(): Shape {
        return when (shapeType) {
            ButtonShapeType.PILL -> RoundedCornerShape(percent = 50)
            ButtonShapeType.CIRCLE -> CircleShape
            ButtonShapeType.SQUIRCLE -> RoundedCornerShape(cornerRadiusDp)
            ButtonShapeType.ROUNDED_SQUARE -> RoundedCornerShape(cornerRadiusDp)
            ButtonShapeType.BRUTALIST_RECT -> RoundedCornerShape(cornerRadiusDp)
            ButtonShapeType.PIXEL_BLOCK -> RoundedCornerShape(2.dp)
            ButtonShapeType.GLOSSY_JELLY -> RoundedCornerShape(cornerRadiusDp)
            ButtonShapeType.NEKO_EARS -> RoundedCornerShape(
                topStart = 8.dp,
                topEnd = 8.dp,
                bottomStart = cornerRadiusDp,
                bottomEnd = cornerRadiusDp
            )
        }
    }
}

enum class IronManSuitType {
    MARK_85_CLASSIC,
    STEALTH_STRIKE,
    SILVER_CENTURION,
    HULKBUSTER
}

/**
 * Dynamic readable contrast colors for surfaces and cards outside the LCD/OLED screen display.
 * Resolves issues where themes with light casing (e.g. Neko Mochi Cat, Girl Math) have dark screen backgrounds,
 * ensuring text on dialogs, sheets, and cards is always crystal clear and high-contrast.
 */
val ThemePalette.onSurfaceTextColor: Color
    get() = when {
        isNekoMochi && !isDark -> Color(0xFF3B1A23)
        isGirlMath && !isDark -> Color(0xFF4A202D)
        surfaceColor.luminance() > 0.45f -> Color(0xFF1E293B)
        else -> screenTextColor
    }

val ThemePalette.onSurfaceSubtextColor: Color
    get() = when {
        isNekoMochi && !isDark -> Color(0xFF70404C)
        isGirlMath && !isDark -> Color(0xFF8A5563)
        surfaceColor.luminance() > 0.45f -> Color(0xFF64748B)
        else -> screenExpressionColor
    }

val ThemePalette.onCardColor: Color
    get() = when {
        isNekoMochi && !isDark -> Color(0xFF3B1A23)
        isGirlMath && !isDark -> Color(0xFF4A202D)
        cardBackground.luminance() > 0.45f -> Color(0xFF1E293B)
        else -> screenTextColor
    }

val ThemePalette.onCardSubtextColor: Color
    get() = when {
        isNekoMochi && !isDark -> Color(0xFF70404C)
        isGirlMath && !isDark -> Color(0xFF8A5563)
        cardBackground.luminance() > 0.45f -> Color(0xFF64748B)
        else -> screenExpressionColor
    }

/**
 * High-contrast palette generator for specialized tools (BMI, Currency, Age, EMI, Programmer, Engineering, etc.)
 * Ensures that for themes with light casing (e.g. Neko Mochi Cat, Girl Math), all tool cards, badges, steppers,
 * and text elements have optimal contrast, deep legible typography, and vibrant accents.
 */
fun ThemePalette.toToolTheme(): ThemePalette {
    if (isDark || isRetroCircuit || isNothingDossier || isBauhausDossier || isTerracottaStudio) {
        return this
    }
    val isLightCanvas = !isDark && backgroundColor.luminance() > 0.45f
    val isNekoOrLight = isLightCanvas && (id == ThemeId.NEKO_MOCHI_CAT || isNekoMochi || isGirlMath || screenTextColor.luminance() > 0.45f)
    return if (isNekoOrLight) {
        val primaryText = if (id == ThemeId.NEKO_MOCHI_CAT || isNekoMochi) Color(0xFF3B1A23)
        else if (isGirlMath) Color(0xFF4A202D)
        else Color(0xFF1E293B)

        val secondaryText = if (id == ThemeId.NEKO_MOCHI_CAT || isNekoMochi) Color(0xFF70404C)
        else if (isGirlMath) Color(0xFF8A5563)
        else Color(0xFF64748B)

        val cardBg = Color(0xFFFFFFFF)
        val screenBg = Color(0xFFFFFFFF)
        val surfaceBg = if (id == ThemeId.NEKO_MOCHI_CAT || isNekoMochi) Color(0xFFFFDDE6) else surfaceColor

        val vibrantAccent = if (id == ThemeId.NEKO_MOCHI_CAT || isNekoMochi) Color(0xFFC23A63)
        else if (isGirlMath) Color(0xFFD81B60)
        else if (accentColor.luminance() > 0.55f) Color(0xFF0284C7)
        else accentColor

        val borderColor = if (id == ThemeId.NEKO_MOCHI_CAT || isNekoMochi) Color(0xFFE5BCC7)
        else if (isGirlMath) Color(0xFFF3C2D0)
        else Color(0xFFCBD5E1)

        val numBtnText = if (id == ThemeId.NEKO_MOCHI_CAT || isNekoMochi) Color(0xFF3B1A23) else primaryText
        val fnBtnText = if (id == ThemeId.NEKO_MOCHI_CAT || isNekoMochi) Color(0xFF3B1A23) else primaryText

        this.copy(
            screenTextColor = primaryText,
            screenExpressionColor = secondaryText,
            screenPreviewColor = vibrantAccent,
            screenBackground = screenBg,
            screenBorderColor = borderColor,
            secondaryAccent = vibrantAccent,
            cardBackground = cardBg,
            surfaceColor = surfaceBg,
            numberButtonText = numBtnText,
            functionButtonText = fnBtnText
        )
    } else {
        this
    }
}



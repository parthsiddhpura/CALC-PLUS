package com.example.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.LandscapeDisplayPane
import com.example.ui.components.LandscapeDualPaneView
import com.example.ui.components.LandscapeScientificKeypad
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.LanguageStrings
import com.example.model.CalculatorMode
import com.example.model.ThemeId
import com.example.model.ThemePalette
import com.example.model.toToolTheme
import com.example.ui.components.AgeCalculatorView
import com.example.ui.components.AiMathCopilotView
import com.example.ui.components.BatmanLogoIcon
import com.example.ui.components.OrtylScreenBackground
import com.example.ui.components.OledStealthVoidScreenBackground
import com.example.ui.components.StarryGothamScreenBackground
import com.example.ui.components.CosmicSingularityScreenBackground
import com.example.ui.components.BatmanScreenBackground
import com.example.ui.components.ArcReactorIcon
import com.example.ui.components.IronManScreenBackground
import com.example.ui.components.KawaiiScreenBackground
import com.example.ui.components.StudioScreenBackground
import com.example.ui.components.BmiCalculatorView
import com.example.ui.components.CalculationChainsView
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.CustomCalculatorBuilderView
import com.example.ui.components.DecimalConverterSheet
import com.example.ui.components.EditNoteDialog
import com.example.ui.components.EmiCalculatorView
import com.example.ui.components.EngineeringCalculatorView
import com.example.ui.components.GstCalculatorView
import com.example.ui.components.HistorySheet
import com.example.ui.components.MoreModesSheet
import com.example.ui.components.ProgrammerKeypad
import com.example.ui.components.RealCurrencyConverterView
import com.example.ui.components.ScientificKeypad
import com.example.ui.components.SettingsSheet
import com.example.ui.components.StandardKeypad
import com.example.ui.components.TipSplitterView
import com.example.ui.components.UnitConverterView
import com.example.ui.components.WorksheetTapeView
import com.example.ui.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val ageProfiles by viewModel.ageProfilesList.collectAsStateWithLifecycle()
    val theme = remember(
        uiState.currentThemeId,
        uiState.customAccentColor,
        uiState.customShapeType,
        uiState.customDisplayFont,
        uiState.displayConfig.showScanlinesOverride
    ) {
        viewModel.getEffectiveTheme(uiState)
    }

    // High-contrast palette for tools in the "More" section when using light/pastel themes
    val toolTheme = remember(theme) { theme.toToolTheme() }
    val haptics = LocalHapticFeedback.current

    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val moreModesSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val decimalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Remembered stable callbacks to ensure Keypad composables skip recomposition when typing (butter-smooth typing)
    val onInputCallback = remember(viewModel, haptics) { { char: String -> viewModel.onInput(char, haptics) } }
    val onClearCallback = remember(viewModel, haptics) { { viewModel.onClear(haptics) } }
    val onBackspaceCallback = remember(viewModel, haptics) { { viewModel.onBackspace(haptics) } }
    val onNegateCallback = remember(viewModel, haptics) { { viewModel.onNegate(haptics) } }
    val onEqualsCallback = remember(viewModel, haptics) { { viewModel.onEquals(haptics) } }
    val onFunctionCallback = remember(viewModel, haptics) { { fn: String -> viewModel.onFunction(fn, haptics) } }
    val onConstantCallback = remember(viewModel, haptics) { { c: String -> viewModel.onConstant(c, haptics) } }
    val onMemoryAddCallback = remember(viewModel, haptics) { { viewModel.onMemoryAdd(haptics) } }
    val onMemorySubtractCallback = remember(viewModel, haptics) { { viewModel.onMemorySubtract(haptics) } }
    val onMemoryRecallCallback = remember(viewModel, haptics) { { viewModel.onMemoryRecall(haptics) } }
    val onMemoryClearCallback = remember(viewModel, haptics) { { viewModel.onMemoryClear(haptics) } }
    val onEngCallback = remember(viewModel, haptics) { { viewModel.onEngKey(haptics) } }
    val onToggleAngleCallback = remember(viewModel) { { viewModel.toggleAngleMode() } }
    val onToggle2ndCallback = remember(viewModel) { { viewModel.toggleSecondFunction() } }
    val onToggleNotationCallback = remember(viewModel, haptics) { { viewModel.toggleDisplayNotation(haptics) } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = theme.backgroundColor
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.backgroundBrush)
        ) {
            AnimatedContent(
                targetState = theme,
                transitionSpec = {
                    fadeIn(animationSpec = tween(350, easing = FastOutSlowInEasing)) togetherWith
                            fadeOut(animationSpec = tween(350, easing = FastOutSlowInEasing))
                },
                label = "theme_bg_transition",
                modifier = Modifier.fillMaxSize()
            ) { currentTheme ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(currentTheme.backgroundBrush)
                ) {
                    if (currentTheme.hasBatSignal) {
                        BatmanScreenBackground(modifier = Modifier.fillMaxSize())
                    } else if (currentTheme.hasArcReactor) {
                        IronManScreenBackground(
                            modifier = Modifier.fillMaxSize(),
                            suitType = currentTheme.ironManSuit ?: com.example.model.IronManSuitType.MARK_85_CLASSIC
                        )
                    } else if (currentTheme.isOrtylMinimal) {
                        OrtylScreenBackground(modifier = Modifier.fillMaxSize())
                    } else if (currentTheme.isOledStealthVoid) {
                        OledStealthVoidScreenBackground(modifier = Modifier.fillMaxSize())
                    } else if (currentTheme.isStarryGotham) {
                        StarryGothamScreenBackground(modifier = Modifier.fillMaxSize())
                    } else if (currentTheme.isCosmicSingularity) {
                        CosmicSingularityScreenBackground(modifier = Modifier.fillMaxSize())
                    } else if (currentTheme.isGirlMath || currentTheme.isNekoMochi || currentTheme.isY2kGlossy || currentTheme.isPixelArt) {
                        KawaiiScreenBackground(
                            theme = currentTheme,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (currentTheme.isRetroCircuit || currentTheme.isNothingDossier || currentTheme.isBauhausDossier || currentTheme.isTerracottaStudio) {
                        StudioScreenBackground(
                            theme = currentTheme,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(
                        start = if (isLandscape) 10.dp else 12.dp,
                        end = if (isLandscape) 10.dp else 12.dp,
                        top = 2.dp,
                        bottom = 4.dp
                    )
                    .then(if (!isLandscape) Modifier.widthIn(max = 650.dp) else Modifier)
                    .align(Alignment.Center),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Clean Minimal Top Header Bar (No clutter, No shuffle, Single Settings Access)
                val topBarTextColor = if (theme.isDark) {
                    theme.screenTextColor
                } else {
                    if (theme.isNekoMochi || theme.isGirlMath) Color(0xFF3E1E28) else Color(0xFF1E293B)
                }
                val topBarBadgeBg = if (theme.isDark) {
                    theme.surfaceColor
                } else {
                    if (theme.isNekoMochi) Color(0xFFFFD6E0) else theme.surfaceColor
                }
                val topBarBadgeText = if (theme.isDark) {
                    theme.screenExpressionColor
                } else {
                    if (theme.isNekoMochi || theme.isGirlMath) Color(0xFF5A2534) else Color(0xFF334155)
                }

                if (isLandscape) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Brand & Theme
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TopBarBrandIcon(theme = theme)

                            Text(
                                text = "CALC +",
                                color = topBarTextColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = topBarBadgeBg,
                                border = if (!theme.isDark && theme.isNekoMochi) androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFFFF85A1).copy(alpha = 0.5f)) else null
                            ) {
                                Text(
                                    text = theme.name,
                                    color = topBarBadgeText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Center: Mode tabs
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val landscapeModes = listOf(
                                Pair(CalculatorMode.STANDARD, LanguageStrings.modeStandard(uiState.currentLanguage)),
                                Pair(CalculatorMode.GST_CALCULATOR, LanguageStrings.modeGst(uiState.currentLanguage)),
                                Pair(CalculatorMode.SCIENTIFIC, LanguageStrings.modeScientific(uiState.currentLanguage)),
                                Pair(CalculatorMode.WORKSHEET_TAPE, "Tape")
                            )

                            landscapeModes.forEach { (modeItem, label) ->
                                val isSelected = uiState.mode == modeItem
                                val tabBg = when {
                                    isSelected -> theme.accentColor
                                    !theme.isDark -> if (theme.isNekoMochi) Color(0xFFFFF0F3) else theme.surfaceColor
                                    else -> theme.surfaceColor
                                }
                                val tabBorder = if (!isSelected && !theme.isDark) {
                                    androidx.compose.foundation.BorderStroke(1.dp, if (theme.isNekoMochi) Color(0xFFFFB5C5) else theme.accentColor.copy(alpha = 0.25f))
                                } else null
                                val tabTextColor = when {
                                    isSelected -> if (theme.accentColor.luminance() > 0.6f) Color(0xFF211118) else Color.White
                                    !theme.isDark -> if (theme.isNekoMochi || theme.isGirlMath) Color(0xFF4A202D) else Color(0xFF1E293B)
                                    else -> theme.screenTextColor.copy(alpha = 0.88f)
                                }

                                Surface(
                                    color = tabBg,
                                    shape = RoundedCornerShape(8.dp),
                                    border = tabBorder,
                                    modifier = Modifier
                                        .clickable { viewModel.setMode(modeItem) }
                                        .testTag("land_tab_mode_${modeItem.name.lowercase()}")
                                ) {
                                    Text(
                                        text = label,
                                        color = tabTextColor,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            val isMoreSelected = uiState.mode !in listOf(
                                CalculatorMode.STANDARD,
                                CalculatorMode.GST_CALCULATOR,
                                CalculatorMode.SCIENTIFIC,
                                CalculatorMode.WORKSHEET_TAPE
                            )
                            val moreTabBg = when {
                                isMoreSelected -> theme.accentColor
                                !theme.isDark -> if (theme.isNekoMochi) Color(0xFFFFF0F3) else theme.surfaceColor
                                else -> theme.surfaceColor
                            }
                            val moreTabBorder = if (!isMoreSelected && !theme.isDark) {
                                androidx.compose.foundation.BorderStroke(1.dp, if (theme.isNekoMochi) Color(0xFFFFB5C5) else theme.accentColor.copy(alpha = 0.25f))
                            } else null
                            val moreTabTextColor = when {
                                isMoreSelected -> if (theme.accentColor.luminance() > 0.6f) Color(0xFF211118) else Color.White
                                !theme.isDark -> if (theme.isNekoMochi || theme.isGirlMath) Color(0xFF4A202D) else Color(0xFF1E293B)
                                else -> theme.screenTextColor.copy(alpha = 0.88f)
                            }

                            Surface(
                                color = moreTabBg,
                                shape = RoundedCornerShape(8.dp),
                                border = moreTabBorder,
                                modifier = Modifier
                                    .clickable { viewModel.setShowMoreModesSheet(true) }
                                    .testTag("land_tab_mode_more")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = if (isMoreSelected) uiState.mode.shortName else LanguageStrings.modeMore(uiState.currentLanguage),
                                        color = moreTabTextColor,
                                        fontSize = 11.sp,
                                        fontWeight = if (isMoreSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Icon(
                                        imageVector = Icons.Default.MoreHoriz,
                                        contentDescription = "More modes",
                                        tint = moreTabTextColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }

                        // Right: Angle Mode Toggle (Scientific only) & Settings Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (uiState.mode == CalculatorMode.SCIENTIFIC) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = topBarBadgeBg,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.5f)),
                                    modifier = Modifier.clickable { viewModel.toggleAngleMode() }
                                ) {
                                    Text(
                                        text = uiState.angleMode.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.accentColor,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.setShowSettingsSheet(true) },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(topBarBadgeBg)
                                    .size(32.dp)
                                    .testTag("land_btn_settings")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings & Appearance",
                                    tint = topBarTextColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 1.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TopBarBrandIcon(theme = theme)

                                Text(
                                    text = "CALC +",
                                    color = topBarTextColor,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = topBarBadgeBg,
                                    border = if (!theme.isDark && theme.isNekoMochi) androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFFFF85A1).copy(alpha = 0.5f)) else null
                                ) {
                                    Text(
                                        text = theme.name,
                                        color = topBarBadgeText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Settings Icon Button
                            IconButton(
                                onClick = { viewModel.setShowSettingsSheet(true) },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(topBarBadgeBg)
                                    .size(36.dp)
                                    .testTag("btn_settings")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings & Appearance",
                                    tint = topBarTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Reorganized Navigation: 3 Main Buttons (Standard, GST Calc, Scientific) + "More" Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val mainModes: List<Pair<CalculatorMode, String>> = listOf(
                                Pair(CalculatorMode.STANDARD, LanguageStrings.modeStandard(uiState.currentLanguage)),
                                Pair(CalculatorMode.GST_CALCULATOR, LanguageStrings.modeGst(uiState.currentLanguage)),
                                Pair(CalculatorMode.SCIENTIFIC, LanguageStrings.modeScientific(uiState.currentLanguage))
                            )

                            mainModes.forEach { (modeItem, label) ->
                                val isSelected = uiState.mode == modeItem
                                val tabBg = when {
                                    isSelected -> theme.accentColor
                                    !theme.isDark -> if (theme.isNekoMochi) Color(0xFFFFF0F3) else theme.surfaceColor
                                    else -> theme.surfaceColor
                                }
                                val tabBorder = if (!isSelected && !theme.isDark) {
                                    androidx.compose.foundation.BorderStroke(1.dp, if (theme.isNekoMochi) Color(0xFFFFB5C5) else theme.accentColor.copy(alpha = 0.3f))
                                } else null
                                val tabTextColor = when {
                                    isSelected -> if (theme.accentColor.luminance() > 0.6f) Color(0xFF211118) else Color.White
                                    !theme.isDark -> if (theme.isNekoMochi || theme.isGirlMath) Color(0xFF4A202D) else Color(0xFF1E293B)
                                    else -> theme.screenTextColor.copy(alpha = 0.88f)
                                }

                                Surface(
                                    color = tabBg,
                                    shape = RoundedCornerShape(12.dp),
                                    border = tabBorder,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setMode(modeItem) }
                                        .testTag("tab_mode_${modeItem.name.lowercase()}")
                                ) {
                                    Text(
                                        text = label,
                                        color = tabTextColor,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }

                            // "More" Button (with active mode indicator if a secondary mode is selected)
                            val isMoreSelected = uiState.mode !in listOf(
                                CalculatorMode.STANDARD,
                                CalculatorMode.GST_CALCULATOR,
                                CalculatorMode.SCIENTIFIC
                            )
                            val moreTabBg = when {
                                isMoreSelected -> theme.accentColor
                                !theme.isDark -> if (theme.isNekoMochi) Color(0xFFFFF0F3) else theme.surfaceColor
                                else -> theme.surfaceColor
                            }
                            val moreTabBorder = if (!isMoreSelected && !theme.isDark) {
                                androidx.compose.foundation.BorderStroke(1.dp, if (theme.isNekoMochi) Color(0xFFFFB5C5) else theme.accentColor.copy(alpha = 0.3f))
                            } else null
                            val moreTabTextColor = when {
                                isMoreSelected -> if (theme.accentColor.luminance() > 0.6f) Color(0xFF211118) else Color.White
                                !theme.isDark -> if (theme.isNekoMochi || theme.isGirlMath) Color(0xFF4A202D) else Color(0xFF1E293B)
                                else -> theme.screenTextColor.copy(alpha = 0.88f)
                            }

                            Surface(
                                color = moreTabBg,
                                shape = RoundedCornerShape(12.dp),
                                border = moreTabBorder,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setShowMoreModesSheet(true) }
                                    .testTag("tab_mode_more")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isMoreSelected) uiState.mode.shortName else LanguageStrings.modeMore(uiState.currentLanguage),
                                        color = moreTabTextColor,
                                        fontSize = 13.sp,
                                        fontWeight = if (isMoreSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.Default.MoreHoriz,
                                        contentDescription = "More modes",
                                        tint = moreTabTextColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (isLandscape) 2.dp else 4.dp))

                // Middle Content & Screen Display (Expands to fill available space)
                AnimatedContent(
                    targetState = uiState.mode,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                         scaleIn(initialScale = 0.97f, animationSpec = tween(180, easing = FastOutSlowInEasing)))
                            .togetherWith(
                                fadeOut(animationSpec = tween(120)) +
                                scaleOut(targetScale = 0.99f, animationSpec = tween(120))
                            )
                    },
                    label = "mode_transition",
                    modifier = Modifier.weight(1f)
                ) { targetMode ->
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        when (targetMode) {
                            CalculatorMode.STANDARD -> {
                                if (isLandscape) {
                                    LandscapeDualPaneView(
                                        uiState = uiState,
                                        theme = theme,
                                        historyList = historyList,
                                        viewModel = viewModel,
                                        haptics = haptics
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        CalculatorDisplay(
                                            expression = uiState.expression,
                                            result = uiState.result,
                                            previewResult = uiState.previewResult,
                                            theme = theme,
                                            angleMode = uiState.angleMode,
                                            hasMemory = uiState.hasMemory,
                                            mode = uiState.mode,
                                            displayConfig = uiState.displayConfig,
                                            historyCount = historyList.size,
                                            cursorPosition = uiState.cursorPosition,
                                            onCursorChange = { viewModel.setCursorPosition(it) },
                                            isEvaluated = uiState.lastEvaluated,
                                            onToggleAngleMode = { viewModel.toggleAngleMode() },
                                            onOpenHistory = { viewModel.setShowHistorySheet(true) },
                                            onOpenDecimalConverter = { viewModel.setShowDecimalConverterSheet(true) },
                                            onToggleNotation = { viewModel.toggleDisplayNotation(haptics) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(bottom = 8.dp)
                                        )

                                        StandardKeypad(
                                            theme = theme,
                                            onInput = onInputCallback,
                                            onClear = onClearCallback,
                                            onBackspace = onBackspaceCallback,
                                            onNegate = onNegateCallback,
                                            onEquals = onEqualsCallback,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                }
                            }

                            CalculatorMode.GST_CALCULATOR -> {
                                GstCalculatorView(
                                    theme = toolTheme,
                                    amountInput = uiState.gstAmountInput,
                                    calculationType = uiState.gstCalculationType,
                                    selectedSlabId = uiState.gstSelectedSlabId,
                                    slabs = uiState.gstSlabs,
                                    currentResult = uiState.gstCurrentResult,
                                    grandTotalGross = uiState.gstGrandTotalGross,
                                    grandTotalGst = uiState.gstGrandTotalGst,
                                    calculationCount = uiState.gstCalculationCount,
                                    language = uiState.currentLanguage,
                                    onInputDigit = { viewModel.onGstInputDigit(it, haptics) },
                                    onEquals = { viewModel.onGstEquals(haptics) },
                                    onClear = { viewModel.onGstClear(haptics) },
                                    onBackspace = { viewModel.onGstBackspace(haptics) },
                                    onToggleType = { viewModel.onGstToggleType(haptics) },
                                    onSelectSlab = { viewModel.onGstSelectSlab(it, haptics) },
                                    onClearGrandTotal = { viewModel.onGstClearGrandTotal() },
                                    onUpdateSlabRate = { id, rate -> viewModel.onGstUpdateSlabRate(id, rate) },
                                    onApplyPreset = { viewModel.onGstApplyPreset(it) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            CalculatorMode.SCIENTIFIC -> {
                                if (isLandscape) {
                                    LandscapeDualPaneView(
                                        uiState = uiState,
                                        theme = theme,
                                        historyList = historyList,
                                        viewModel = viewModel,
                                        haptics = haptics
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        CalculatorDisplay(
                                            expression = uiState.expression,
                                            result = uiState.result,
                                            previewResult = uiState.previewResult,
                                            theme = theme,
                                            angleMode = uiState.angleMode,
                                            hasMemory = uiState.hasMemory,
                                            mode = uiState.mode,
                                            displayConfig = uiState.displayConfig,
                                            historyCount = historyList.size,
                                            cursorPosition = uiState.cursorPosition,
                                            onCursorChange = { viewModel.setCursorPosition(it) },
                                            isEvaluated = uiState.lastEvaluated,
                                            onToggleAngleMode = { viewModel.toggleAngleMode() },
                                            onOpenHistory = { viewModel.setShowHistorySheet(true) },
                                            onOpenDecimalConverter = { viewModel.setShowDecimalConverterSheet(true) },
                                            onToggleNotation = { viewModel.toggleDisplayNotation(haptics) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(bottom = 6.dp)
                                        )

                                        ScientificKeypad(
                                            theme = theme,
                                            angleMode = uiState.angleMode,
                                            isSecondFunction = uiState.isSecondFunction,
                                            onToggleAngleMode = onToggleAngleCallback,
                                            onToggleSecondFunction = onToggle2ndCallback,
                                            onInput = onInputCallback,
                                            onFunction = onFunctionCallback,
                                            onConstant = onConstantCallback,
                                            onClear = onClearCallback,
                                            onBackspace = onBackspaceCallback,
                                            onNegate = onNegateCallback,
                                            onEquals = onEqualsCallback,
                                            onMemoryAdd = onMemoryAddCallback,
                                            onMemorySubtract = onMemorySubtractCallback,
                                            onMemoryRecall = onMemoryRecallCallback,
                                            onMemoryClear = onMemoryClearCallback,
                                            onEng = onEngCallback,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                }
                            }

                            CalculatorMode.AGE_CALCULATOR -> {
                                AgeCalculatorView(
                                    theme = toolTheme,
                                    birthDateTime = uiState.ageBirthDateTime,
                                    targetDateTime = uiState.ageTargetDateTime,
                                    currentPersonName = uiState.ageCurrentPersonName,
                                    notes = uiState.ageProfileNotes,
                                    savedProfiles = ageProfiles,
                                    selectedProfile = uiState.ageSelectedProfile,
                                    onUpdateBirthDateTime = { viewModel.onAgeUpdateBirthDateTime(it) },
                                    onUpdateTargetDateTime = { viewModel.onAgeUpdateTargetDateTime(it) },
                                    onPersonNameChange = { viewModel.setAgePersonName(it) },
                                    onNotesChange = { viewModel.setAgeProfileNotes(it) },
                                    onSaveProfile = { name, rel, notes -> viewModel.saveCurrentAgeProfile(name, rel, notes) },
                                    onLoadProfile = { viewModel.loadAgeProfile(it) },
                                    onDeleteProfile = { viewModel.deleteAgeProfile(it) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            CalculatorMode.BMI_CALCULATOR -> {
                                BmiCalculatorView(
                                    theme = toolTheme,
                                    weightInput = uiState.bmiWeightInput,
                                    heightInput = uiState.bmiHeightInput,
                                    ageInput = uiState.bmiAgeInput,
                                    isMetric = uiState.bmiIsMetric,
                                    isMale = uiState.bmiIsMale,
                                    onWeightChange = { viewModel.onBmiWeightChange(it) },
                                    onHeightChange = { viewModel.onBmiHeightChange(it) },
                                    onAgeChange = { viewModel.onBmiAgeChange(it) },
                                    onToggleMetric = { viewModel.onBmiToggleMetric(it) },
                                    onToggleGender = { viewModel.onBmiToggleGender(it) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            CalculatorMode.CURRENCY_CONVERTER -> {
                                RealCurrencyConverterView(
                                    theme = toolTheme,
                                    fromCode = uiState.currencyFromCode,
                                    toCode = uiState.currencyToCode,
                                    inputAmount = uiState.currencyInput,
                                    outputAmount = uiState.currencyOutput,
                                    isLoading = uiState.isCurrencyLoading,
                                    statusText = uiState.currencyStatusText,
                                    isOnline = uiState.currencyIsOnline,
                                    ratesMap = uiState.currencyRatesMap,
                                    onAmountChange = { viewModel.onCurrencyInputChange(it) },
                                    onFromChange = { viewModel.setCurrencyFrom(it) },
                                    onToChange = { viewModel.setCurrencyTo(it) },
                                    onSwap = { viewModel.swapCurrencies() },
                                    onQuickInrAmount = { viewModel.setQuickInrAmount(it) },
                                    onRefreshRates = { viewModel.fetchLiveCurrencyRates() },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            CalculatorMode.EMI_LOAN -> {
                                EmiCalculatorView(
                                    theme = toolTheme,
                                    principalInput = uiState.emiPrincipalInput,
                                    interestRateInput = uiState.emiInterestRateInput,
                                    tenureYearsInput = uiState.emiTenureInput,
                                    isTenureInYears = uiState.emiIsTenureInYears,
                                    onPrincipalChange = { viewModel.onEmiPrincipalChange(it) },
                                    onRateChange = { viewModel.onEmiRateChange(it) },
                                    onTenureChange = { viewModel.onEmiTenureChange(it) },
                                    onToggleTenureUnit = { viewModel.onEmiToggleTenureUnit(it) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            CalculatorMode.PROGRAMMER -> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    CalculatorDisplay(
                                        expression = uiState.progPendingOp?.let { "${uiState.progStoredValue ?: 0} $it" } ?: "",
                                        result = uiState.progInput,
                                        previewResult = null,
                                        theme = theme,
                                        angleMode = uiState.angleMode,
                                        hasMemory = false,
                                        mode = uiState.mode,
                                        displayConfig = uiState.displayConfig,
                                        historyCount = historyList.size,
                                        onOpenHistory = { viewModel.setShowHistorySheet(true) },
                                        onOpenDecimalConverter = { viewModel.setShowDecimalConverterSheet(true) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(bottom = 6.dp)
                                    )

                                    ProgrammerKeypad(
                                        theme = toolTheme,
                                        value = uiState.progValue,
                                        activeBase = uiState.progBase,
                                        wordSize = uiState.progWordSize,
                                        onBaseSelect = { viewModel.setProgBase(it) },
                                        onWordSizeSelect = { viewModel.setProgWordSize(it) },
                                        onDigit = { viewModel.onProgDigit(it, haptics) },
                                        onBitwiseOp = { viewModel.onProgBitwiseOp(it, haptics) },
                                        onBitToggle = { viewModel.onProgBitToggle(it, haptics) },
                                        onClear = { viewModel.onProgClear(haptics) },
                                        onBackspace = { viewModel.onProgBackspace(haptics) },
                                        onEquals = { viewModel.onProgEquals(haptics) },
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }

                            CalculatorMode.UNIT_CONVERTER -> {
                                UnitConverterView(
                                    theme = toolTheme,
                                    category = uiState.convCategory,
                                    fromUnit = uiState.convFromUnit,
                                    toUnit = uiState.convToUnit,
                                    inputValue = uiState.convInput,
                                    outputValue = uiState.convOutput,
                                    onCategorySelect = { viewModel.setUnitCategory(it) },
                                    onFromUnitSelect = { viewModel.setFromUnit(it) },
                                    onToUnitSelect = { viewModel.setToUnit(it) },
                                    onSwapUnits = { viewModel.swapUnits() },
                                    onInputChange = { viewModel.onConverterInput(it) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            CalculatorMode.TIP_SPLIT -> {
                                TipSplitterView(
                                    theme = toolTheme,
                                    billInput = uiState.tipBillInput,
                                    tipPercent = uiState.tipPercent,
                                    peopleCount = uiState.tipPeopleCount,
                                    tipAmount = uiState.tipAmount,
                                    totalAmount = uiState.tipTotal,
                                    perPersonAmount = uiState.tipPerPerson,
                                    onBillChange = { viewModel.onTipBillChange(it) },
                                    onTipPercentChange = { viewModel.onTipPercentChange(it) },
                                    onPeopleChange = { viewModel.onTipPeopleChange(it) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            CalculatorMode.ENGINEERING -> {
                                EngineeringCalculatorView(
                                    theme = toolTheme,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            CalculatorMode.CUSTOM_BUILDER -> {
                                CustomCalculatorBuilderView(
                                    theme = toolTheme,
                                    calculatorsList = uiState.customCalculators,
                                    activeCalculator = uiState.activeCustomCalculator,
                                    onSelectCalculator = { viewModel.selectCustomCalculator(it) },
                                    onSaveCustomCalculator = { viewModel.saveCustomCalculator(it) },
                                    onDeleteCustomCalculator = { viewModel.deleteCustomCalculator(it) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            CalculatorMode.CALCULATION_CHAINS -> {
                                CalculationChainsView(
                                    theme = toolTheme,
                                    chainsList = uiState.calculationChains,
                                    activeChain = uiState.activeCalculationChain,
                                    onSelectChain = { viewModel.selectCalculationChain(it) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            CalculatorMode.AI_COPILOT -> {
                                AiMathCopilotView(
                                    theme = toolTheme,
                                    onNavigateMode = { viewModel.setMode(it) },
                                    onLoadToExpression = { viewModel.onLoadCopilotExpression(it) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            CalculatorMode.WORKSHEET_TAPE -> {
                                WorksheetTapeView(
                                    theme = toolTheme,
                                    documents = uiState.worksheetDocuments,
                                    activeDocument = uiState.activeWorksheetDocument,
                                    settings = uiState.worksheetSettings,
                                    onUpdateSettings = { viewModel.updateWorksheetSettings(it) },
                                    onSaveDocument = { viewModel.saveWorksheetDocument(it) },
                                    onSelectDocument = { viewModel.selectWorksheetDocument(it) },
                                    onDeleteDocument = { viewModel.deleteWorksheetDocument(it) },
                                    onNewDocument = { title -> viewModel.createNewWorksheetDocument(title) },
                                    onDuplicateDocument = { viewModel.duplicateWorksheetDocument(it) },
                                    onRestoreDocuments = { viewModel.restoreWorksheets(it) },
                                    onApplyTemplate = { viewModel.applyWorksheetTemplate(it) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Settings & Appearance Bottom Sheet
        if (uiState.showSettingsSheet) {
            SettingsSheet(
                activeTheme = toolTheme,
                onSelectTheme = { viewModel.setTheme(it) },
                customAccentColor = uiState.customAccentColor,
                onSelectAccentColor = { viewModel.setCustomAccentColor(it) },
                customShapeType = uiState.customShapeType,
                onSelectShapeType = { viewModel.setCustomShapeType(it) },
                customDisplayFont = uiState.customDisplayFont,
                onSelectDisplayFont = { viewModel.setCustomDisplayFont(it) },
                displayConfig = uiState.displayConfig,
                currentLanguage = uiState.currentLanguage,
                onSelectLanguage = { viewModel.setAppLanguage(it) },
                onSelectDisplaySeparator = { viewModel.setDisplaySeparator(it) },
                onSelectDisplayPrecision = { viewModel.setDisplayPrecision(it) },
                onSelectDisplayScale = { viewModel.setDisplayScale(it) },
                onSelectDisplayNotation = { viewModel.setDisplayNotation(it) },
                onToggleLivePreview = { viewModel.toggleLivePreview() },
                onToggleStatusBadges = { viewModel.toggleStatusBadges() },
                onToggleScanlinesOverride = { viewModel.toggleScanlinesOverride() },
                onToggleCopyOnTap = { viewModel.toggleCopyOnTap() },
                onResetDisplaySettings = { viewModel.resetDisplaySettings() },
                isSoundEnabled = uiState.isSoundEnabled,
                onToggleSound = { viewModel.toggleSound() },
                isHapticsEnabled = uiState.isHapticsEnabled,
                onToggleHaptics = { viewModel.toggleHaptics() },
                onResetAppearance = { viewModel.resetAppearanceCustomizations() },
                onResetAllSettings = { viewModel.resetAllSettingsToDefaults() },
                onDismiss = { viewModel.setShowSettingsSheet(false) },
                sheetState = settingsSheetState
            )
        }

        // More Calculators & Tools Sheet
        if (uiState.showMoreModesSheet) {
            MoreModesSheet(
                activeMode = uiState.mode,
                theme = toolTheme,
                onSelectMode = { viewModel.setMode(it) },
                onDismiss = { viewModel.setShowMoreModesSheet(false) },
                sheetState = moreModesSheetState
            )
        }

        // Calculation History Bottom Sheet
        if (uiState.showHistorySheet) {
            HistorySheet(
                historyList = historyList,
                theme = toolTheme,
                searchQuery = uiState.historySearchQuery,
                onSearchQueryChange = { viewModel.setHistorySearchQuery(it) },
                onlyFavorites = uiState.historyOnlyFavorites,
                onToggleOnlyFavorites = { viewModel.setHistoryOnlyFavorites(it) },
                onUseItem = { viewModel.useHistoryItem(it) },
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onEditNote = { viewModel.setEditingNoteFor(it) },
                onDeleteItem = { viewModel.deleteHistoryItem(it) },
                onClearAll = { viewModel.clearAllHistory() },
                onDismiss = { viewModel.setShowHistorySheet(false) },
                sheetState = historySheetState
            )
        }

        // Decimal Conversion Modal Bottom Sheet
        if (uiState.showDecimalConverterSheet) {
            DecimalConverterSheet(
                targetValue = uiState.decimalConverterTarget,
                theme = toolTheme,
                onUseValue = { selectedVal ->
                    viewModel.setShowDecimalConverterSheet(false)
                },
                onDismiss = { viewModel.setShowDecimalConverterSheet(false) },
                sheetState = decimalSheetState
            )
        }

        // Edit History Note Dialog
        if (uiState.editingNoteFor != null) {
            EditNoteDialog(
                history = uiState.editingNoteFor!!,
                theme = theme,
                onSave = { note -> viewModel.saveHistoryNote(uiState.editingNoteFor!!, note) },
                onDismiss = { viewModel.setEditingNoteFor(null) }
            )
        }
    }
}

@Composable
private fun TopBarBrandIcon(theme: ThemePalette) {
    if (theme.hasBatSignal) {
        BatmanLogoIcon(
            modifier = Modifier.size(16.dp),
            tint = theme.accentColor
        )
    } else if (theme.hasArcReactor) {
        ArcReactorIcon(
            modifier = Modifier.size(18.dp),
            glowColor = theme.accentColor,
            showOuterTabs = false
        )
    } else if (theme.isGirlMath) {
        Text(
            text = "♡",
            color = theme.accentColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
    } else if (theme.isNekoMochi) {
        Text(
            text = "🐾",
            fontSize = 15.sp
        )
    } else if (theme.isY2kGlossy) {
        Text(
            text = "✨",
            fontSize = 15.sp
        )
    } else if (theme.isPixelArt) {
        Text(
            text = "👾",
            fontSize = 15.sp
        )
    } else if (theme.isRetroCircuit) {
        Text(
            text = "⚡",
            fontSize = 15.sp
        )
    } else if (theme.isNothingDossier) {
        Text(
            text = "▫",
            color = theme.accentColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    } else if (theme.isBauhausDossier) {
        Text(
            text = "▣",
            color = theme.accentColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    } else if (theme.isTerracottaStudio) {
        Text(
            text = "⚪",
            fontSize = 14.sp
        )
    } else {
        Surface(
            shape = CircleShape,
            color = theme.accentColor,
            modifier = Modifier.size(10.dp)
        ) {}
    }
}

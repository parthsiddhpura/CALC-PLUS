package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CurrencyRepository
import com.example.data.local.AppDatabase
import com.example.data.local.HistoryRepository
import com.example.domain.AgeCalculatorEngine
import com.example.domain.BmiCalculatorEngine
import com.example.domain.CalculatorEngine
import com.example.domain.EmiCalculatorEngine
import com.example.domain.GstEngine
import com.example.domain.ProgrammerEngine
import com.example.domain.SoundHapticHelper
import com.example.domain.WordSize
import com.example.model.AgeProfile
import com.example.model.AngleMode
import com.example.model.AppLanguage
import com.example.model.CalculationHistory
import com.example.model.CalculatorMode
import com.example.model.ConversionUnit
import com.example.model.CurrencyInfo
import com.example.model.GstCalculationType
import com.example.model.GstResult
import com.example.model.GstSlab
import com.example.model.NumberBase
import com.example.model.ButtonShapeType
import com.example.model.DisplayConfig
import com.example.model.DisplayFontType
import com.example.model.DisplayNotation
import com.example.model.DisplayPrecisionMode
import com.example.model.DisplayScaleSize
import com.example.model.DisplaySeparatorStyle
import com.example.model.ThemeId
import com.example.model.ThemePalette
import com.example.model.UnitCategory
import com.example.model.UnitConverterData
import com.example.domain.CalculationChainEngine
import com.example.domain.CustomCalculatorEngine
import com.example.domain.TaxPreset
import com.example.model.CalculationChain
import com.example.model.CustomCalculator
import com.example.model.WorksheetDocument
import com.example.model.WorksheetLine
import com.example.model.WorksheetTemplate
import com.example.domain.WorksheetTapeEngine
import com.example.data.local.WorksheetRepository
import com.example.ui.theme.CalculatorThemes
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Locale
import kotlin.random.Random

data class CalculatorUiState(
    val mode: CalculatorMode = CalculatorMode.STANDARD,
    val currentThemeId: ThemeId = ThemeId.BATMAN_DARK_KNIGHT,
    val customAccentColor: Long? = null,
    val customShapeType: ButtonShapeType? = null,
    val customDisplayFont: DisplayFontType? = null,
    val expression: String = "",
    val cursorPosition: Int = 0,
    val result: String = "0",
    val previewResult: String? = null,
    val angleMode: AngleMode = AngleMode.DEG,
    val isSecondFunction: Boolean = false,
    val memoryValue: Double = 0.0,
    val hasMemory: Boolean = false,
    val lastEvaluated: Boolean = false,
    
    // Sound & Haptic
    val isSoundEnabled: Boolean = true,
    val isHapticsEnabled: Boolean = true,
    
    // App Language
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    
    // Display Configuration & Preferences
    val displayConfig: DisplayConfig = DisplayConfig(),
    
    // Sheets & Dialogs
    val showThemeSheet: Boolean = false,
    val showHistorySheet: Boolean = false,
    val showModeSelector: Boolean = false,
    val showSettingsSheet: Boolean = false,
    val showMoreModesSheet: Boolean = false,
    val showDecimalConverterSheet: Boolean = false,
    val decimalConverterTarget: String = "0",
    val editingNoteFor: CalculationHistory? = null,
    val historySearchQuery: String = "",
    val historyOnlyFavorites: Boolean = false,
    
    // GST Calculator State (Casio MJ-120GST inspired)
    val gstAmountInput: String = "0",
    val gstCalculationType: GstCalculationType = GstCalculationType.EXCLUSIVE,
    val gstSelectedSlabId: Int = 3, // Default to GST+3 (18%)
    val gstSlabs: List<GstSlab> = GstEngine.DEFAULT_SLABS,
    val gstCurrentResult: GstResult? = null,
    val gstGrandTotalGross: Double = 0.0,
    val gstGrandTotalGst: Double = 0.0,
    val gstCalculationCount: Int = 0,
    
    // Age & Rashi Calculator State
    val ageBirthDateTime: LocalDateTime = LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.of(9, 0)),
    val ageTargetDateTime: LocalDateTime = LocalDateTime.now(),
    val ageCurrentPersonName: String = "My Age",
    val ageProfileNotes: String = "",
    val ageSelectedProfile: AgeProfile? = null,
    
    // BMI (Body Mass Index) Calculator State
    val bmiWeightInput: String = "68",
    val bmiHeightInput: String = "172",
    val bmiAgeInput: String = "25",
    val bmiIsMetric: Boolean = true, // kg/cm vs lbs/ft+in
    val bmiIsMale: Boolean = true,
    
    // Live Currency & Indian Rupee (INR) Converter State
    val currencyFromCode: String = "USD",
    val currencyToCode: String = "INR",
    val currencyInput: String = "100",
    val currencyOutput: String = "8720.00",
    val isCurrencyLoading: Boolean = false,
    val currencyStatusText: String = "● Live Forex Rates Active",
    val currencyIsOnline: Boolean = true,
    val currencyRatesMap: Map<String, Double> = CurrencyRepository.getRatesMap(),
    
    // EMI & Loan Calculator State
    val emiPrincipalInput: String = "1000000",
    val emiInterestRateInput: String = "8.5",
    val emiTenureInput: String = "20",
    val emiIsTenureInYears: Boolean = true,

    // Programmer Mode State
    val progValue: Long = 0L,
    val progInput: String = "0",
    val progBase: NumberBase = NumberBase.DEC,
    val progWordSize: WordSize = WordSize.QWORD,
    val progPendingOp: String? = null,
    val progStoredValue: Long? = null,
    
    // Unit Converter State
    val convCategory: UnitCategory = UnitCategory.LENGTH,
    val convFromUnit: ConversionUnit = UnitConverterData.categories[UnitCategory.LENGTH]!![0],
    val convToUnit: ConversionUnit = UnitConverterData.categories[UnitCategory.LENGTH]!![1],
    val convInput: String = "1",
    val convOutput: String = "0.001",
    
    // Tip Splitter State
    val tipBillInput: String = "50.00",
    val tipPercent: Float = 18f,
    val tipPeopleCount: Int = 2,
    val tipAmount: Double = 9.0,
    val tipTotal: Double = 59.0,
    val tipPerPerson: Double = 29.5,

    // Custom Calculator Engine State
    val customCalculators: List<CustomCalculator> = CustomCalculatorEngine.BUILTIN_CALCULATORS,
    val activeCustomCalculator: CustomCalculator = CustomCalculatorEngine.BUILTIN_CALCULATORS[0],

    // Calculation Chains State
    val calculationChains: List<CalculationChain> = CalculationChainEngine.BUILTIN_CHAINS,
    val activeCalculationChain: CalculationChain = CalculationChainEngine.BUILTIN_CHAINS[0],

    // Worksheet & Paper Tape Calculator State
    val worksheetDocuments: List<WorksheetDocument> = emptyList(),
    val worksheetSettings: com.example.model.WorksheetSettings = com.example.model.WorksheetSettings(),
    val activeWorksheetDocument: WorksheetDocument = WorksheetDocument(
        title = "Candle Cost Calculation",
        lines = WorksheetTapeEngine.getDefaultTemplates()[0].lines,
        grandTotal = 446.25
    )
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HistoryRepository
    private val ageProfileDao = AppDatabase.getDatabase(application).ageProfileDao()
    private val worksheetRepository = WorksheetRepository(AppDatabase.getDatabase(application).worksheetDao())
    val soundHapticHelper: SoundHapticHelper = SoundHapticHelper(application)
    private val prefs: SharedPreferences = application.getSharedPreferences("chromacalc_prefs", Context.MODE_PRIVATE)

    private val _uiState: MutableStateFlow<CalculatorUiState>
    val uiState: StateFlow<CalculatorUiState>

    private val _searchQuery = MutableStateFlow("")
    private val _onlyFavorites = MutableStateFlow(false)

    val historyList: StateFlow<List<CalculationHistory>>
    val ageProfilesList: StateFlow<List<AgeProfile>> = ageProfileDao.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        val savedThemeName = prefs.getString("saved_theme_id", null)
        val hasAppliedDarkKnightDefault = prefs.getBoolean("has_applied_dark_knight_default_v10", false)
        val initialTheme = if (!hasAppliedDarkKnightDefault) {
            prefs.edit()
                .putBoolean("has_applied_dark_knight_default_v10", true)
                .putString("saved_theme_id", ThemeId.BATMAN_DARK_KNIGHT.name)
                .apply()
            ThemeId.BATMAN_DARK_KNIGHT
        } else if (savedThemeName != null) {
            try {
                ThemeId.valueOf(savedThemeName)
            } catch (e: Exception) {
                ThemeId.BATMAN_DARK_KNIGHT
            }
        } else {
            ThemeId.BATMAN_DARK_KNIGHT
        }
        val savedSound = prefs.getBoolean("saved_sound_enabled", true)
        val savedHaptics = prefs.getBoolean("saved_haptics_enabled", true)
        val savedCustomAccent = if (prefs.contains("custom_accent_color")) prefs.getLong("custom_accent_color", 0L) else null
        val savedShapeName = prefs.getString("custom_shape_type", null)
        val savedShape = savedShapeName?.let { try { ButtonShapeType.valueOf(it) } catch (e: Exception) { null } }
        val savedFontName = prefs.getString("custom_display_font", null)
        val savedFont = savedFontName?.let { try { DisplayFontType.valueOf(it) } catch (e: Exception) { null } }

        // Saved Display Preferences
        val savedSepName = prefs.getString("display_separator", null)
        val savedSep = savedSepName?.let { try { DisplaySeparatorStyle.valueOf(it) } catch (e: Exception) { null } } ?: DisplaySeparatorStyle.INDIAN
        val savedPrecName = prefs.getString("display_precision", null)
        val savedPrec = savedPrecName?.let { try { DisplayPrecisionMode.valueOf(it) } catch (e: Exception) { null } } ?: DisplayPrecisionMode.AUTO
        val savedScaleName = prefs.getString("display_scale", null)
        val savedScale = savedScaleName?.let { try { DisplayScaleSize.valueOf(it) } catch (e: Exception) { null } } ?: DisplayScaleSize.STANDARD
        val savedNotationName = prefs.getString("display_notation", null)
        val savedNotation = savedNotationName?.let { try { DisplayNotation.valueOf(it) } catch (e: Exception) { null } } ?: DisplayNotation.STANDARD
        val savedLivePreview = prefs.getBoolean("display_live_preview", true)
        val savedBadges = prefs.getBoolean("display_badges", true)
        val savedScanlines = if (prefs.contains("display_scanlines")) prefs.getBoolean("display_scanlines", false) else null
        val savedCopyTap = prefs.getBoolean("display_copy_tap", true)
        val savedLangCode = prefs.getString("app_language", "en") ?: "en"
        val savedLanguage = AppLanguage.fromCode(savedLangCode)

        val initialDisplayConfig = DisplayConfig(
            separatorStyle = savedSep,
            precisionMode = savedPrec,
            scaleSize = savedScale,
            notation = savedNotation,
            showLivePreview = savedLivePreview,
            showStatusBadges = savedBadges,
            showScanlinesOverride = savedScanlines,
            copyOnTap = savedCopyTap
        )

        // Load saved GST slabs
        val initialSlabs = GstEngine.DEFAULT_SLABS.map { defSlab ->
            val key = "gst_slab_rate_${defSlab.id}"
            if (prefs.contains(key)) {
                val rate = prefs.getFloat(key, defSlab.ratePercent.toFloat()).toDouble()
                defSlab.copy(ratePercent = rate, label = GstEngine.formatRateLabel(rate))
            } else {
                defSlab
            }
        }
        val initialSlabId = prefs.getInt("gst_selected_slab_id", 3)
        val initialSlab = initialSlabs.firstOrNull { it.id == initialSlabId } ?: initialSlabs[3]

        // Initial GST calculation
        val initialGstRes = GstEngine.calculate(0.0, initialSlab.ratePercent, GstCalculationType.EXCLUSIVE)

        _uiState = MutableStateFlow(
            CalculatorUiState(
                currentThemeId = initialTheme,
                customAccentColor = savedCustomAccent,
                customShapeType = savedShape,
                customDisplayFont = savedFont,
                isSoundEnabled = savedSound,
                isHapticsEnabled = savedHaptics,
                currentLanguage = savedLanguage,
                displayConfig = initialDisplayConfig,
                gstSlabs = initialSlabs,
                gstSelectedSlabId = initialSlab.id,
                gstCurrentResult = initialGstRes
            )
        )
        uiState = _uiState.asStateFlow()

        val db = AppDatabase.getDatabase(application)
        repository = HistoryRepository(db.historyDao())

        historyList = combine(
            repository.allHistory,
            _searchQuery,
            _onlyFavorites
        ) { all, query, favsOnly ->
            var list = all
            if (favsOnly) {
                list = list.filter { it.isFavorite }
            }
            if (query.isNotBlank()) {
                val q = query.lowercase(Locale.ROOT)
                list = list.filter {
                    it.expression.lowercase(Locale.ROOT).contains(q) ||
                    it.result.lowercase(Locale.ROOT).contains(q) ||
                    it.note.lowercase(Locale.ROOT).contains(q)
                }
            }
            list
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Initialize Live Forex Rates via real network
        fetchLiveCurrencyRates()

        // Initialize Worksheets & Paper Tape
        viewModelScope.launch {
            worksheetRepository.getAllWorksheets().collect { docs ->
                if (docs.isEmpty()) {
                    worksheetRepository.populateInitialDataIfEmpty(0)
                } else {
                    _uiState.update { state ->
                        val currentActive = state.activeWorksheetDocument
                        val matched = docs.find { it.id == currentActive.id } ?: docs.first()
                        state.copy(
                            worksheetDocuments = docs,
                            activeWorksheetDocument = matched
                        )
                    }
                }
            }
        }
    }

    val currentTheme: ThemePalette
        get() = CalculatorThemes.getThemeById(_uiState.value.currentThemeId)

    fun setMode(mode: CalculatorMode) {
        _uiState.update { it.copy(mode = mode, showModeSelector = false) }
    }

    fun setTheme(themeId: ThemeId) {
        prefs.edit().putString("saved_theme_id", themeId.name).apply()
        _uiState.update { it.copy(currentThemeId = themeId) }
    }

    fun cycleNextTheme() {
        val all = CalculatorThemes.allThemes
        val currentIndex = all.indexOfFirst { it.id == _uiState.value.currentThemeId }
        val nextIndex = if (currentIndex >= 0) (currentIndex + 1) % all.size else 0
        setTheme(all[nextIndex].id)
    }

    fun cyclePrevTheme() {
        val all = CalculatorThemes.allThemes
        val currentIndex = all.indexOfFirst { it.id == _uiState.value.currentThemeId }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else all.size - 1
        setTheme(all[prevIndex].id)
    }

    fun randomizeTheme() {
        val all = CalculatorThemes.allThemes
        val current = _uiState.value.currentThemeId
        val candidates = all.filter { it.id != current }
        if (candidates.isNotEmpty()) {
            val randomTheme = candidates[Random.nextInt(candidates.size)]
            setTheme(randomTheme.id)
        }
    }

    fun toggleSound() {
        _uiState.update {
            val nextVal = !it.isSoundEnabled
            prefs.edit().putBoolean("saved_sound_enabled", nextVal).apply()
            it.copy(isSoundEnabled = nextVal)
        }
    }

    fun toggleHaptics() {
        _uiState.update {
            val nextVal = !it.isHapticsEnabled
            prefs.edit().putBoolean("saved_haptics_enabled", nextVal).apply()
            it.copy(isHapticsEnabled = nextVal)
        }
    }

    fun toggleAngleMode() {
        _uiState.update {
            val newMode = if (it.angleMode == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG
            val preview = CalculatorEngine.evaluatePreview(it.expression, newMode)
            it.copy(angleMode = newMode, previewResult = preview)
        }
    }

    fun toggleSecondFunction() {
        _uiState.update { it.copy(isSecondFunction = !it.isSecondFunction) }
    }

    fun setShowThemeSheet(show: Boolean) {
        _uiState.update { it.copy(showThemeSheet = show) }
    }

    fun setShowHistorySheet(show: Boolean) {
        _uiState.update { it.copy(showHistorySheet = show) }
    }

    fun setShowModeSelector(show: Boolean) {
        _uiState.update { it.copy(showModeSelector = show) }
    }

    fun setShowSettingsSheet(show: Boolean) {
        _uiState.update { it.copy(showSettingsSheet = show) }
    }

    fun setShowMoreModesSheet(show: Boolean) {
        _uiState.update { it.copy(showMoreModesSheet = show) }
    }

    fun setCustomAccentColor(color: Long?) {
        if (color != null) {
            prefs.edit().putLong("custom_accent_color", color).apply()
        } else {
            prefs.edit().remove("custom_accent_color").apply()
        }
        _uiState.update { it.copy(customAccentColor = color) }
    }

    fun setCustomShapeType(shape: ButtonShapeType?) {
        if (shape != null) {
            prefs.edit().putString("custom_shape_type", shape.name).apply()
        } else {
            prefs.edit().remove("custom_shape_type").apply()
        }
        _uiState.update { it.copy(customShapeType = shape) }
    }

    fun setCustomDisplayFont(font: DisplayFontType?) {
        if (font != null) {
            prefs.edit().putString("custom_display_font", font.name).apply()
        } else {
            prefs.edit().remove("custom_display_font").apply()
        }
        _uiState.update { it.copy(customDisplayFont = font) }
    }

    fun resetAppearanceCustomizations() {
        prefs.edit()
            .remove("custom_accent_color")
            .remove("custom_shape_type")
            .remove("custom_display_font")
            .apply()
        _uiState.update {
            it.copy(
                customAccentColor = null,
                customShapeType = null,
                customDisplayFont = null
            )
        }
    }

    fun getEffectiveTheme(state: CalculatorUiState): ThemePalette {
        val base = CalculatorThemes.getThemeById(state.currentThemeId)
        val customAccent = state.customAccentColor?.let { Color(it.toInt()) }
        val shape = state.customShapeType ?: base.shapeType
        val font = state.customDisplayFont ?: base.displayFont
        val scanlines = state.displayConfig.showScanlinesOverride ?: base.hasScanlines
        val radius = when (state.customShapeType) {
            ButtonShapeType.PILL -> 24.dp
            ButtonShapeType.CIRCLE -> 24.dp
            ButtonShapeType.SQUIRCLE -> 16.dp
            ButtonShapeType.ROUNDED_SQUARE -> 8.dp
            ButtonShapeType.BRUTALIST_RECT -> 4.dp
            ButtonShapeType.PIXEL_BLOCK -> 3.dp
            ButtonShapeType.GLOSSY_JELLY -> 16.dp
            ButtonShapeType.NEKO_EARS -> 18.dp
            null -> base.cornerRadiusDp
        }
        val contrastText = if (customAccent != null) {
            val lum = (0.299 * customAccent.red + 0.587 * customAccent.green + 0.114 * customAccent.blue)
            if (lum > 0.55) Color(0xFF0F172A) else Color.White
        } else null

        return base.copy(
            accentColor = customAccent ?: base.accentColor,
            shapeType = shape,
            displayFont = font,
            cornerRadiusDp = radius,
            hasScanlines = scanlines,
            operatorButtonBg = customAccent ?: base.operatorButtonBg,
            operatorButtonText = contrastText ?: base.operatorButtonText,
            operatorButtonBorder = if (customAccent != null) {
                if (base.borderWidthDp > 0.dp && base.isBrutalistShadow) base.operatorButtonBorder else Color.Transparent
            } else base.operatorButtonBorder,
            equalsButtonBrush = if (customAccent != null) {
                Brush.linearGradient(listOf(customAccent, customAccent.copy(alpha = 0.85f)))
            } else base.equalsButtonBrush,
            equalsButtonText = contrastText ?: base.equalsButtonText,
            glowColor = if (customAccent != null) customAccent.copy(alpha = 0.45f) else base.glowColor
        )
    }

    fun setDisplaySeparator(separator: DisplaySeparatorStyle) {
        prefs.edit().putString("display_separator", separator.name).apply()
        _uiState.update { it.copy(displayConfig = it.displayConfig.copy(separatorStyle = separator)) }
    }

    fun setDisplayPrecision(precision: DisplayPrecisionMode) {
        prefs.edit().putString("display_precision", precision.name).apply()
        _uiState.update { it.copy(displayConfig = it.displayConfig.copy(precisionMode = precision)) }
    }

    fun setDisplayScale(scale: DisplayScaleSize) {
        prefs.edit().putString("display_scale", scale.name).apply()
        _uiState.update { it.copy(displayConfig = it.displayConfig.copy(scaleSize = scale)) }
    }

    fun setDisplayNotation(notation: DisplayNotation) {
        prefs.edit().putString("display_notation", notation.name).apply()
        _uiState.update { it.copy(displayConfig = it.displayConfig.copy(notation = notation)) }
    }

    fun toggleDisplayNotation(haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)
        val nextNotation = when (_uiState.value.displayConfig.notation) {
            DisplayNotation.STANDARD -> DisplayNotation.SCIENTIFIC
            DisplayNotation.SCIENTIFIC -> DisplayNotation.ENGINEERING
            DisplayNotation.ENGINEERING -> DisplayNotation.STANDARD
        }
        setDisplayNotation(nextNotation)
    }

    fun onEngKey(haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)
        val current = _uiState.value.displayConfig.notation
        val next = if (current == DisplayNotation.ENGINEERING) DisplayNotation.STANDARD else DisplayNotation.ENGINEERING
        setDisplayNotation(next)
    }

    fun onAnsKey(haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)

        _uiState.update { state ->
            val ansVal = if (state.result != "Error" && state.result.isNotBlank() && state.result != "0") {
                state.result.replace(",", "").replace(" ", "")
            } else "0"

            val pos = state.cursorPosition.coerceIn(0, state.expression.length)
            val newExpr = if (state.lastEvaluated) ansVal else {
                state.expression.substring(0, pos) + ansVal + state.expression.substring(pos)
            }
            val newCursorPos = if (state.lastEvaluated) ansVal.length else pos + ansVal.length
            val preview = CalculatorEngine.evaluatePreview(newExpr, state.angleMode)
            state.copy(
                expression = newExpr,
                cursorPosition = newCursorPos,
                previewResult = preview,
                lastEvaluated = false
            )
        }
    }

    fun toggleLivePreview() {
        _uiState.update {
            val nextVal = !it.displayConfig.showLivePreview
            prefs.edit().putBoolean("display_live_preview", nextVal).apply()
            it.copy(displayConfig = it.displayConfig.copy(showLivePreview = nextVal))
        }
    }

    fun toggleStatusBadges() {
        _uiState.update {
            val nextVal = !it.displayConfig.showStatusBadges
            prefs.edit().putBoolean("display_badges", nextVal).apply()
            it.copy(displayConfig = it.displayConfig.copy(showStatusBadges = nextVal))
        }
    }

    fun toggleScanlinesOverride() {
        _uiState.update {
            val nextVal = when (it.displayConfig.showScanlinesOverride) {
                null -> true
                true -> false
                false -> null
            }
            if (nextVal == null) {
                prefs.edit().remove("display_scanlines").apply()
            } else {
                prefs.edit().putBoolean("display_scanlines", nextVal).apply()
            }
            it.copy(displayConfig = it.displayConfig.copy(showScanlinesOverride = nextVal))
        }
    }

    fun toggleCopyOnTap() {
        _uiState.update {
            val nextVal = !it.displayConfig.copyOnTap
            prefs.edit().putBoolean("display_copy_tap", nextVal).apply()
            it.copy(displayConfig = it.displayConfig.copy(copyOnTap = nextVal))
        }
    }

    fun resetDisplaySettings() {
        prefs.edit()
            .remove("display_separator")
            .remove("display_precision")
            .remove("display_scale")
            .remove("display_notation")
            .remove("display_live_preview")
            .remove("display_badges")
            .remove("display_scanlines")
            .remove("display_copy_tap")
            .apply()
        _uiState.update {
            it.copy(displayConfig = DisplayConfig(separatorStyle = DisplaySeparatorStyle.INDIAN))
        }
    }

    fun resetAllSettingsToDefaults() {
        prefs.edit()
            .remove("saved_theme_id")
            .remove("custom_accent_color")
            .remove("custom_shape_type")
            .remove("custom_display_font")
            .remove("display_separator")
            .remove("display_precision")
            .remove("display_scale")
            .remove("display_notation")
            .remove("display_live_preview")
            .remove("display_badges")
            .remove("display_scanlines")
            .remove("display_copy_tap")
            .remove("saved_sound_enabled")
            .remove("saved_haptics_enabled")
            .remove("app_language")
            .apply()

        _uiState.update {
            it.copy(
                currentThemeId = ThemeId.BATMAN_DARK_KNIGHT,
                customAccentColor = null,
                customShapeType = null,
                customDisplayFont = null,
                displayConfig = DisplayConfig(separatorStyle = DisplaySeparatorStyle.INDIAN),
                isSoundEnabled = true,
                isHapticsEnabled = true,
                currentLanguage = AppLanguage.ENGLISH
            )
        }
    }

    fun setShowDecimalConverterSheet(show: Boolean, targetValue: String? = null) {
        _uiState.update {
            val target = targetValue ?: (if (it.result != "0" && it.result != "Error") it.result else if (it.expression.isNotBlank()) it.expression else "0")
            it.copy(showDecimalConverterSheet = show, decimalConverterTarget = target)
        }
    }

    fun setEditingNoteFor(history: CalculationHistory?) {
        _uiState.update { it.copy(editingNoteFor = history) }
    }

    fun setHistorySearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(historySearchQuery = query) }
    }

    fun setHistoryOnlyFavorites(onlyFavs: Boolean) {
        _onlyFavorites.value = onlyFavs
        _uiState.update { it.copy(historyOnlyFavorites = onlyFavs) }
    }

    // --- Standard & Scientific Calculation Inputs ---

    fun onInput(char: String, haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)

        _uiState.update { state ->
            var newExpr = state.expression
            val newResult = state.result
            val wasEvaluated = state.lastEvaluated
            var newCursorPos = state.cursorPosition.coerceIn(0, state.expression.length)

            if (wasEvaluated) {
                if ("+−×÷^%".contains(char)) {
                    val rawResult = if (newResult != "Error") newResult.replace(",", "").replace(" ", "") else ""
                    newExpr = if (rawResult.isNotEmpty()) rawResult + char else char
                } else {
                    newExpr = char
                }
                newCursorPos = newExpr.length
            } else {
                if (char == "%" && newExpr.isEmpty()) {
                    newExpr = "0%"
                    newCursorPos = 2
                } else {
                    val pos = newCursorPos.coerceIn(0, newExpr.length)
                    if (pos > 0 && "+−×÷^".contains(newExpr[pos - 1]) && "+×÷^".contains(char)) {
                        newExpr = newExpr.substring(0, pos - 1) + char + newExpr.substring(pos)
                        newCursorPos = pos
                    } else if (pos > 0 && newExpr[pos - 1] == '−' && char == "−") {
                        // Do not repeat minus
                    } else {
                        newExpr = newExpr.substring(0, pos) + char + newExpr.substring(pos)
                        newCursorPos = pos + char.length
                    }
                }
            }

            val preview = CalculatorEngine.evaluatePreview(newExpr, state.angleMode)
            state.copy(
                expression = newExpr,
                cursorPosition = newCursorPos,
                previewResult = preview,
                lastEvaluated = false
            )
        }
    }

    fun onFunction(func: String, haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)

        _uiState.update { state ->
            var newExpr = state.expression
            var newCursorPos = state.cursorPosition.coerceIn(0, state.expression.length)
            val insertion = "$func("
            if (state.lastEvaluated) {
                val rawResult = if (state.result != "Error") state.result.replace(",", "").replace(" ", "") else ""
                newExpr = if (rawResult.isNotEmpty()) "$func($rawResult)" else insertion
                newCursorPos = newExpr.length
            } else {
                val pos = newCursorPos.coerceIn(0, newExpr.length)
                newExpr = newExpr.substring(0, pos) + insertion + newExpr.substring(pos)
                newCursorPos = pos + insertion.length
            }
            val preview = CalculatorEngine.evaluatePreview(newExpr, state.angleMode)
            state.copy(
                expression = newExpr,
                cursorPosition = newCursorPos,
                previewResult = preview,
                lastEvaluated = false
            )
        }
    }

    fun onConstant(constant: String, haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)

        _uiState.update { state ->
            val pos = state.cursorPosition.coerceIn(0, state.expression.length)
            val newExpr = if (state.lastEvaluated) constant else {
                state.expression.substring(0, pos) + constant + state.expression.substring(pos)
            }
            val newCursorPos = if (state.lastEvaluated) constant.length else pos + constant.length
            val preview = CalculatorEngine.evaluatePreview(newExpr, state.angleMode)
            state.copy(
                expression = newExpr,
                cursorPosition = newCursorPos,
                previewResult = preview,
                lastEvaluated = false
            )
        }
    }

    fun onNegate(haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)

        _uiState.update { state ->
            val newExpr = if (state.lastEvaluated && state.result != "Error" && state.result.isNotBlank()) {
                val clean = state.result.replace(",", "").replace(" ", "")
                if (clean.startsWith("-")) clean.removePrefix("-") else "-$clean"
            } else {
                val expr = state.expression
                if (expr.startsWith("-(")) {
                    expr.removePrefix("-(").removeSuffix(")")
                } else if (expr.startsWith("-")) {
                    expr.removePrefix("-")
                } else if (expr.isNotEmpty()) {
                    "-($expr)"
                } else {
                    "-"
                }
            }
            val preview = CalculatorEngine.evaluatePreview(newExpr, state.angleMode)
            state.copy(
                expression = newExpr,
                cursorPosition = newExpr.length,
                previewResult = preview,
                lastEvaluated = false
            )
        }
    }

    fun onBackspace(haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled, isClear = true)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)

        _uiState.update { state ->
            if (state.lastEvaluated) {
                state.copy(lastEvaluated = false, cursorPosition = state.expression.length)
            } else if (state.expression.isNotEmpty()) {
                val pos = state.cursorPosition.coerceIn(0, state.expression.length)
                if (pos > 0) {
                    val newExpr = state.expression.substring(0, pos - 1) + state.expression.substring(pos)
                    val newCursorPos = pos - 1
                    val preview = CalculatorEngine.evaluatePreview(newExpr, state.angleMode)
                    state.copy(
                        expression = newExpr,
                        cursorPosition = newCursorPos,
                        previewResult = preview,
                        lastEvaluated = false
                    )
                } else {
                    state
                }
            } else {
                state.copy(result = "0", cursorPosition = 0, previewResult = null, lastEvaluated = false)
            }
        }
    }

    fun onClear(haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled, isClear = true)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled, isHeavy = true)

        _uiState.update {
            it.copy(
                expression = "",
                cursorPosition = 0,
                result = "0",
                previewResult = null,
                lastEvaluated = false
            )
        }
    }

    fun onEquals(haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled, isEquals = true)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled, isHeavy = true)

        val state = _uiState.value
        if (state.expression.isBlank()) return

        val evaluated = CalculatorEngine.evaluate(state.expression, state.angleMode)
        if (evaluated != "Error" && evaluated.isNotBlank()) {
            viewModelScope.launch {
                repository.insert(
                    expression = state.expression,
                    result = evaluated,
                    mode = state.mode.name
                )
            }
            try {
                com.example.widget.CalculatorAppWidgetProvider.sendUpdateBroadcast(
                    getApplication(),
                    state.expression,
                    evaluated
                )
            } catch (e: Exception) {
                // Ignore widget broadcast errors if any
            }
        }

        _uiState.update {
            it.copy(
                result = evaluated,
                cursorPosition = evaluated.length,
                previewResult = null,
                lastEvaluated = true
            )
        }
    }

    fun setCursorPosition(pos: Int) {
        _uiState.update {
            it.copy(
                cursorPosition = pos.coerceIn(0, it.expression.length),
                lastEvaluated = false
            )
        }
    }

    fun moveCursorLeft() {
        _uiState.update {
            it.copy(
                cursorPosition = (it.cursorPosition - 1).coerceAtLeast(0),
                lastEvaluated = false
            )
        }
    }

    fun moveCursorRight() {
        _uiState.update {
            it.copy(
                cursorPosition = (it.cursorPosition + 1).coerceAtMost(it.expression.length),
                lastEvaluated = false
            )
        }
    }

    fun setExpressionFromWidget(expr: String) {
        if (expr.isBlank()) return
        _uiState.update {
            it.copy(
                expression = expr,
                cursorPosition = expr.length,
                result = expr,
                lastEvaluated = false
            )
        }
    }

    fun setAppLanguage(language: AppLanguage) {
        prefs.edit().putString("app_language", language.code).apply()
        _uiState.update { it.copy(currentLanguage = language) }
    }

    // --- GST Calculator Actions (Casio MJ-120GST Style with Expression Support) ---

    fun onGstInputDigit(digit: String, haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)

        _uiState.update { state ->
            val cur = state.gstAmountInput
            val isOperator = digit in listOf("+", "−", "-", "×", "*", "÷", "/", "%")
            val endsWithOperator = cur.isNotEmpty() && cur.last() in listOf('+', '−', '-', '×', '*', '÷', '/', '%')

            val newInput = when {
                // If user entered an operator and string already ends with an operator, replace operator
                isOperator && endsWithOperator -> {
                    cur.dropLast(1) + digit
                }
                // If user entered an operator on initial 0 or empty
                isOperator && (cur.isEmpty() || cur == "0") -> {
                    "0$digit"
                }
                // When current input is "0" or empty and user enters a number or decimal
                (cur == "0" || cur.isEmpty()) && !isOperator -> {
                    when (digit) {
                        ".", "0." -> "0."
                        "0", "00" -> "0"
                        else -> digit
                    }
                }
                // If user enters "." check if current number segment already has a decimal point
                digit == "." -> {
                    val lastNumberPart = cur.split('+', '−', '-', '×', '*', '÷', '/', '%').lastOrNull() ?: ""
                    if (lastNumberPart.contains('.')) cur else cur + digit
                }
                // Avoid multiple leading zeros after operator (e.g. 100+00 -> 100+0)
                endsWithOperator && digit == "00" -> {
                    cur + "0"
                }
                else -> {
                    cur + digit
                }
            }

            val amt = GstEngine.evaluateAmountOrExpression(newInput)
            val slab = state.gstSlabs.firstOrNull { it.id == state.gstSelectedSlabId } ?: state.gstSlabs[3]
            val res = GstEngine.calculate(amt, slab.ratePercent, state.gstCalculationType)
            state.copy(gstAmountInput = newInput, gstCurrentResult = res)
        }
    }

    fun onGstEquals(haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)

        _uiState.update { state ->
            val cur = state.gstAmountInput
            val amt = GstEngine.evaluateAmountOrExpression(cur)
            val slab = state.gstSlabs.firstOrNull { it.id == state.gstSelectedSlabId } ?: state.gstSlabs[3]
            val res = GstEngine.calculate(amt, slab.ratePercent, state.gstCalculationType)

            val solvedAmountStr = if (amt == amt.toLong().toDouble()) amt.toLong().toString() else amt.toString()

            val shouldAccumulate = amt > 0.0
            val newGtGross = if (shouldAccumulate) state.gstGrandTotalGross + res.grossAmount else state.gstGrandTotalGross
            val newGtGst = if (shouldAccumulate) state.gstGrandTotalGst + res.gstAmount else state.gstGrandTotalGst
            val newCount = if (shouldAccumulate) state.gstCalculationCount + 1 else state.gstCalculationCount

            if (shouldAccumulate) {
                viewModelScope.launch {
                    val label = if (state.gstCalculationType == GstCalculationType.EXCLUSIVE) "GST+ (${slab.label})" else "GST- (${slab.label})"
                    val exprDesc = if (cur != solvedAmountStr && cur.isNotBlank()) "$cur = $solvedAmountStr ($label)" else "$label on ${GstEngine.formatCurrency(amt)}"
                    repository.insert(
                        expression = exprDesc,
                        result = "Gross: ${GstEngine.formatCurrency(res.grossAmount)} (Tax: ${GstEngine.formatCurrency(res.gstAmount)})",
                        mode = "GST_TAX"
                    )
                }
            }

            state.copy(
                gstAmountInput = solvedAmountStr,
                gstCurrentResult = res,
                gstGrandTotalGross = newGtGross,
                gstGrandTotalGst = newGtGst,
                gstCalculationCount = newCount
            )
        }
    }

    fun onGstClear(haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled, isClear = true)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled, isHeavy = true)
        _uiState.update { state ->
            val slab = state.gstSlabs.firstOrNull { it.id == state.gstSelectedSlabId } ?: state.gstSlabs[3]
            val zeroRes = GstEngine.calculate(0.0, slab.ratePercent, state.gstCalculationType)
            state.copy(gstAmountInput = "0", gstCurrentResult = zeroRes)
        }
    }

    fun onGstBackspace(haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled, isClear = true)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)
        _uiState.update { state ->
            val cur = state.gstAmountInput
            val newInput = if (cur.length > 1) cur.dropLast(1) else "0"
            val amt = GstEngine.evaluateAmountOrExpression(newInput)
            val slab = state.gstSlabs.firstOrNull { it.id == state.gstSelectedSlabId } ?: state.gstSlabs[3]
            val res = GstEngine.calculate(amt, slab.ratePercent, state.gstCalculationType)
            state.copy(gstAmountInput = newInput, gstCurrentResult = res)
        }
    }

    fun onGstToggleType(haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)
        _uiState.update { state ->
            val nextType = if (state.gstCalculationType == GstCalculationType.EXCLUSIVE) GstCalculationType.INCLUSIVE else GstCalculationType.EXCLUSIVE
            val amt = GstEngine.evaluateAmountOrExpression(state.gstAmountInput)
            val slab = state.gstSlabs.firstOrNull { it.id == state.gstSelectedSlabId } ?: state.gstSlabs[3]
            val res = GstEngine.calculate(amt, slab.ratePercent, nextType)
            state.copy(gstCalculationType = nextType, gstCurrentResult = res)
        }
    }

    fun onGstSelectSlab(slab: GstSlab, haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)
        prefs.edit().putInt("gst_selected_slab_id", slab.id).apply()

        _uiState.update { state ->
            val cur = state.gstAmountInput
            val amt = GstEngine.evaluateAmountOrExpression(cur)
            val res = GstEngine.calculate(amt, slab.ratePercent, state.gstCalculationType)
            val solvedAmountStr = if (amt == amt.toLong().toDouble()) amt.toLong().toString() else amt.toString()

            val shouldAccumulate = amt > 0.0
            val newGtGross = if (shouldAccumulate) state.gstGrandTotalGross + res.grossAmount else state.gstGrandTotalGross
            val newGtGst = if (shouldAccumulate) state.gstGrandTotalGst + res.gstAmount else state.gstGrandTotalGst
            val newCount = if (shouldAccumulate) state.gstCalculationCount + 1 else state.gstCalculationCount

            if (shouldAccumulate) {
                viewModelScope.launch {
                    val label = if (state.gstCalculationType == GstCalculationType.EXCLUSIVE) "GST+ (${slab.label})" else "GST- (${slab.label})"
                    val exprDesc = if (cur != solvedAmountStr && cur.isNotBlank()) "$cur = $solvedAmountStr ($label)" else "$label on ${GstEngine.formatCurrency(amt)}"
                    repository.insert(
                        expression = exprDesc,
                        result = "Gross: ${GstEngine.formatCurrency(res.grossAmount)} (Tax: ${GstEngine.formatCurrency(res.gstAmount)})",
                        mode = "GST_TAX"
                    )
                }
            }

            state.copy(
                gstSelectedSlabId = slab.id,
                gstAmountInput = if (cur.isEmpty() || cur == "0") "0" else solvedAmountStr,
                gstCurrentResult = res,
                gstGrandTotalGross = newGtGross,
                gstGrandTotalGst = newGtGst,
                gstCalculationCount = newCount
            )
        }
    }

    fun onGstClearGrandTotal() {
        _uiState.update {
            it.copy(gstGrandTotalGross = 0.0, gstGrandTotalGst = 0.0, gstCalculationCount = 0)
        }
    }

    fun onGstUpdateSlabRate(slabId: Int, newRate: Double) {
        prefs.edit().putFloat("gst_slab_rate_$slabId", newRate.toFloat()).apply()
        _uiState.update { state ->
            val updatedList = state.gstSlabs.map { slab ->
                if (slab.id == slabId) {
                    slab.copy(ratePercent = newRate, label = GstEngine.formatRateLabel(newRate))
                } else slab
            }
            val amt = GstEngine.evaluateAmountOrExpression(state.gstAmountInput)
            val selectedSlab = updatedList.firstOrNull { it.id == state.gstSelectedSlabId } ?: updatedList[3]
            val res = GstEngine.calculate(amt, selectedSlab.ratePercent, state.gstCalculationType)
            state.copy(gstSlabs = updatedList, gstCurrentResult = res)
        }
    }

    fun onGstApplyPreset(preset: TaxPreset) {
        val editor = prefs.edit()
        val updatedList = GstEngine.DEFAULT_SLABS.mapIndexed { idx, defSlab ->
            val rate = preset.rates.getOrElse(idx) { 0.0 }
            editor.putFloat("gst_slab_rate_${defSlab.id}", rate.toFloat())
            defSlab.copy(
                ratePercent = rate,
                label = GstEngine.formatRateLabel(rate),
                name = "${preset.taxName}+$idx"
            )
        }
        editor.apply()

        _uiState.update { state ->
            val amt = GstEngine.evaluateAmountOrExpression(state.gstAmountInput)
            val selectedSlab = updatedList.firstOrNull { it.id == state.gstSelectedSlabId } ?: updatedList.getOrElse(3) { updatedList[0] }
            val res = GstEngine.calculate(amt, selectedSlab.ratePercent, state.gstCalculationType)
            state.copy(gstSlabs = updatedList, gstCurrentResult = res)
        }
    }

    // --- Age Calculator Actions ---

    fun onAgeUpdateBirthDateTime(dateTime: LocalDateTime) {
        _uiState.update { it.copy(ageBirthDateTime = dateTime) }
    }

    fun onAgeUpdateTargetDateTime(dateTime: LocalDateTime) {
        _uiState.update { it.copy(ageTargetDateTime = dateTime) }
    }

    // --- EMI & Loan Calculator Actions ---

    fun onEmiPrincipalChange(amount: String) {
        _uiState.update { it.copy(emiPrincipalInput = amount) }
    }

    fun onEmiRateChange(rate: String) {
        _uiState.update { it.copy(emiInterestRateInput = rate) }
    }

    fun onEmiTenureChange(tenure: String) {
        _uiState.update { it.copy(emiTenureInput = tenure) }
    }

    fun onEmiToggleTenureUnit(inYears: Boolean) {
        _uiState.update { it.copy(emiIsTenureInYears = inYears) }
    }

    // --- Memory Operations ---

    fun onMemoryAdd(haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)
        val currentVal = _uiState.value.result.replace(",", "").toDoubleOrNull() ?: 0.0
        val newMem = _uiState.value.memoryValue + currentVal
        _uiState.update { it.copy(memoryValue = newMem, hasMemory = true) }
    }

    fun onMemorySubtract(haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)
        val currentVal = _uiState.value.result.replace(",", "").toDoubleOrNull() ?: 0.0
        val newMem = _uiState.value.memoryValue - currentVal
        _uiState.update { it.copy(memoryValue = newMem, hasMemory = true) }
    }

    fun onMemoryRecall(haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)
        if (!_uiState.value.hasMemory) return
        val memStr = CalculatorEngine.formatWithoutCommas(_uiState.value.memoryValue)
        onInput(memStr, haptics)
    }

    fun onMemoryClear(haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled, isClear = true)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)
        _uiState.update { it.copy(memoryValue = 0.0, hasMemory = false) }
    }

    // --- History actions ---

    fun useHistoryItem(history: CalculationHistory, reuseAsExpression: Boolean = false) {
        _uiState.update {
            if (reuseAsExpression) {
                it.copy(
                    expression = history.expression,
                    cursorPosition = history.expression.length,
                    result = history.result,
                    previewResult = null,
                    lastEvaluated = true,
                    showHistorySheet = false
                )
            } else {
                val newExpr = it.expression + history.result.replace(",", "")
                it.copy(
                    expression = newExpr,
                    cursorPosition = newExpr.length,
                    previewResult = CalculatorEngine.evaluatePreview(newExpr, it.angleMode),
                    lastEvaluated = false,
                    showHistorySheet = false
                )
            }
        }
    }

    fun toggleFavorite(history: CalculationHistory) {
        viewModelScope.launch {
            repository.toggleFavorite(history)
        }
    }

    fun saveHistoryNote(history: CalculationHistory, note: String) {
        viewModelScope.launch {
            repository.updateNote(history, note)
            setEditingNoteFor(null)
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    // --- Programmer Mode Inputs ---

    fun setProgBase(base: NumberBase) {
        _uiState.update {
            val formatted = ProgrammerEngine.formatInBase(it.progValue, base, it.progWordSize)
            it.copy(progBase = base, progInput = formatted)
        }
    }

    fun setProgWordSize(wordSize: WordSize) {
        _uiState.update {
            val masked = ProgrammerEngine.applyWordSizeMask(it.progValue, wordSize)
            val formatted = ProgrammerEngine.formatInBase(masked, it.progBase, wordSize)
            it.copy(progWordSize = wordSize, progValue = masked, progInput = formatted)
        }
    }

    fun onProgDigit(digit: String, haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)

        _uiState.update { state ->
            val newInput = if (state.progInput == "0" || state.lastEvaluated) digit else state.progInput + digit
            val newValue = ProgrammerEngine.parseValue(newInput, state.progBase, state.progWordSize)
            state.copy(
                progInput = newInput,
                progValue = newValue,
                lastEvaluated = false
            )
        }
    }

    fun onProgBitwiseOp(op: String, haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)

        _uiState.update { state ->
            if (op == "NOT") {
                val notVal = ProgrammerEngine.performNot(state.progValue, state.progWordSize)
                val formatted = ProgrammerEngine.formatInBase(notVal, state.progBase, state.progWordSize)
                state.copy(progValue = notVal, progInput = formatted, lastEvaluated = true)
            } else {
                state.copy(
                    progPendingOp = op,
                    progStoredValue = state.progValue,
                    progInput = "0"
                )
            }
        }
    }

    fun onProgEquals(haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled, isEquals = true)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled, isHeavy = true)

        _uiState.update { state ->
            val op = state.progPendingOp
            val first = state.progStoredValue
            if (op != null && first != null) {
                val result = ProgrammerEngine.performBitwise(first, state.progValue, op, state.progWordSize)
                val formatted = ProgrammerEngine.formatInBase(result, state.progBase, state.progWordSize)
                
                viewModelScope.launch {
                    repository.insert(
                        expression = "$first $op ${state.progValue}",
                        result = "$result ($formatted)",
                        mode = CalculatorMode.PROGRAMMER.name
                    )
                }

                state.copy(
                    progValue = result,
                    progInput = formatted,
                    progPendingOp = null,
                    progStoredValue = null,
                    lastEvaluated = true
                )
            } else {
                state
            }
        }
    }

    fun onProgBitToggle(bitIndex: Int, haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)

        _uiState.update { state ->
            val toggled = ProgrammerEngine.toggleBit(state.progValue, bitIndex, state.progWordSize)
            val formatted = ProgrammerEngine.formatInBase(toggled, state.progBase, state.progWordSize)
            state.copy(progValue = toggled, progInput = formatted)
        }
    }

    fun onProgClear(haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled, isClear = true)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled, isHeavy = true)

        _uiState.update {
            it.copy(
                progValue = 0L,
                progInput = "0",
                progPendingOp = null,
                progStoredValue = null
            )
        }
    }

    fun onProgBackspace(haptics: HapticFeedback? = null) {
        soundHapticHelper.playClick(_uiState.value.isSoundEnabled, isClear = true)
        soundHapticHelper.triggerHaptic(haptics, _uiState.value.isHapticsEnabled)

        _uiState.update { state ->
            if (state.progInput.length > 1) {
                val drop = state.progInput.dropLast(1)
                val v = ProgrammerEngine.parseValue(drop, state.progBase, state.progWordSize)
                state.copy(progInput = drop, progValue = v)
            } else {
                state.copy(progInput = "0", progValue = 0L)
            }
        }
    }

    // --- Unit Converter Inputs ---

    fun setUnitCategory(category: UnitCategory) {
        val units = UnitConverterData.categories[category] ?: return
        val from = units[0]
        val to = if (units.size > 1) units[1] else units[0]
        _uiState.update {
            val convResult = calculateUnitConversion(it.convInput, category, from, to)
            it.copy(
                convCategory = category,
                convFromUnit = from,
                convToUnit = to,
                convOutput = convResult
            )
        }
    }

    fun setFromUnit(unit: ConversionUnit) {
        _uiState.update {
            val convResult = calculateUnitConversion(it.convInput, it.convCategory, unit, it.convToUnit)
            it.copy(convFromUnit = unit, convOutput = convResult)
        }
    }

    fun setToUnit(unit: ConversionUnit) {
        _uiState.update {
            val convResult = calculateUnitConversion(it.convInput, it.convCategory, it.convFromUnit, unit)
            it.copy(convToUnit = unit, convOutput = convResult)
        }
    }

    fun swapUnits() {
        _uiState.update {
            val from = it.convFromUnit
            val to = it.convToUnit
            val convResult = calculateUnitConversion(it.convInput, it.convCategory, to, from)
            it.copy(convFromUnit = to, convToUnit = from, convOutput = convResult)
        }
    }

    fun onConverterInput(text: String) {
        _uiState.update {
            val convResult = calculateUnitConversion(text, it.convCategory, it.convFromUnit, it.convToUnit)
            it.copy(convInput = text, convOutput = convResult)
        }
    }

    private fun calculateUnitConversion(
        input: String,
        category: UnitCategory,
        from: ConversionUnit,
        to: ConversionUnit
    ): String {
        val v = input.toDoubleOrNull() ?: return "0"
        val res = UnitConverterData.convert(category, v, from, to)
        val df = DecimalFormat("#,##0.######", DecimalFormatSymbols(Locale.US))
        return df.format(res)
    }

    // --- Tip & Split Inputs ---

    fun onTipBillChange(bill: String) {
        _uiState.update {
            val (tip, total, perPerson) = computeTipValues(bill, it.tipPercent, it.tipPeopleCount)
            it.copy(
                tipBillInput = bill,
                tipAmount = tip,
                tipTotal = total,
                tipPerPerson = perPerson
            )
        }
    }

    fun onTipPercentChange(percent: Float) {
        _uiState.update {
            val (tip, total, perPerson) = computeTipValues(it.tipBillInput, percent, it.tipPeopleCount)
            it.copy(
                tipPercent = percent,
                tipAmount = tip,
                tipTotal = total,
                tipPerPerson = perPerson
            )
        }
    }

    fun onTipPeopleChange(count: Int) {
        val safeCount = count.coerceAtLeast(1)
        _uiState.update {
            val (tip, total, perPerson) = computeTipValues(it.tipBillInput, it.tipPercent, safeCount)
            it.copy(
                tipPeopleCount = safeCount,
                tipAmount = tip,
                tipTotal = total,
                tipPerPerson = perPerson
            )
        }
    }

    private fun computeTipValues(billStr: String, tipPct: Float, people: Int): Triple<Double, Double, Double> {
        val bill = billStr.toDoubleOrNull() ?: 0.0
        val tip = bill * (tipPct / 100.0)
        val total = bill + tip
        val perPerson = if (people > 0) total / people else total
        return Triple(tip, total, perPerson)
    }

    // --- Age Calculator & Horoscope (Rashi) Profiles ---

    fun setAgePersonName(name: String) {
        _uiState.update { it.copy(ageCurrentPersonName = name) }
    }

    fun setAgeProfileNotes(notes: String) {
        _uiState.update { it.copy(ageProfileNotes = notes) }
    }

    fun saveCurrentAgeProfile(name: String, relation: String = "Self", notes: String = "") {
        viewModelScope.launch {
            val state = _uiState.value
            val profile = AgeProfile(
                name = name.ifBlank { "Profile" },
                birthYear = state.ageBirthDateTime.year,
                birthMonth = state.ageBirthDateTime.monthValue,
                birthDay = state.ageBirthDateTime.dayOfMonth,
                birthHour = state.ageBirthDateTime.hour,
                birthMinute = state.ageBirthDateTime.minute,
                notes = notes,
                relationship = relation
            )
            ageProfileDao.insertProfile(profile)
            _uiState.update {
                it.copy(
                    ageCurrentPersonName = profile.name,
                    ageProfileNotes = profile.notes,
                    ageSelectedProfile = profile
                )
            }
        }
    }

    fun loadAgeProfile(profile: AgeProfile) {
        val birthDate = LocalDate.of(profile.birthYear, profile.birthMonth, profile.birthDay)
        val birthTime = LocalTime.of(profile.birthHour, profile.birthMinute)
        _uiState.update {
            it.copy(
                ageBirthDateTime = LocalDateTime.of(birthDate, birthTime),
                ageCurrentPersonName = profile.name,
                ageProfileNotes = profile.notes,
                ageSelectedProfile = profile
            )
        }
    }

    fun deleteAgeProfile(profile: AgeProfile) {
        viewModelScope.launch {
            ageProfileDao.deleteProfileById(profile.id)
            if (_uiState.value.ageSelectedProfile?.id == profile.id) {
                _uiState.update { it.copy(ageSelectedProfile = null) }
            }
        }
    }

    // --- BMI (Body Mass Index) Calculator Handlers ---

    fun onBmiWeightChange(weight: String) {
        _uiState.update { it.copy(bmiWeightInput = weight) }
    }

    fun onBmiHeightChange(height: String) {
        _uiState.update { it.copy(bmiHeightInput = height) }
    }

    fun onBmiAgeChange(age: String) {
        _uiState.update { it.copy(bmiAgeInput = age) }
    }

    fun onBmiToggleMetric(isMetric: Boolean) {
        _uiState.update {
            if (it.bmiIsMetric == isMetric) return@update it
            if (isMetric) {
                // Was imperial (lbs, inches), convert to metric (kg, cm)
                val lbs = it.bmiWeightInput.toDoubleOrNull() ?: 150.0
                val inches = it.bmiHeightInput.toDoubleOrNull() ?: 68.0
                val kg = (lbs * 0.45359237).roundToInt()
                val cm = (inches * 2.54).roundToInt()
                it.copy(bmiIsMetric = true, bmiWeightInput = kg.toString(), bmiHeightInput = cm.toString())
            } else {
                // Was metric (kg, cm), convert to imperial (lbs, inches)
                val kg = it.bmiWeightInput.toDoubleOrNull() ?: 68.0
                val cm = it.bmiHeightInput.toDoubleOrNull() ?: 172.0
                val lbs = (kg * 2.20462).roundToInt()
                val inches = (cm / 2.54).roundToInt()
                it.copy(bmiIsMetric = false, bmiWeightInput = lbs.toString(), bmiHeightInput = inches.toString())
            }
        }
    }

    fun onBmiToggleGender(isMale: Boolean) {
        _uiState.update { it.copy(bmiIsMale = isMale) }
    }

    // --- Live Real-Time Currency & Indian Rupee (INR) Handlers ---

    fun fetchLiveCurrencyRates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCurrencyLoading = true) }
            val result = CurrencyRepository.fetchLiveRates()
            if (result.isSuccess) {
                val rates = result.getOrNull() ?: CurrencyRepository.getRatesMap()
                _uiState.update { state ->
                    val converted = calculateForexConversion(state.currencyInput, state.currencyFromCode, state.currencyToCode)
                    state.copy(
                        isCurrencyLoading = false,
                        currencyRatesMap = rates,
                        currencyOutput = converted,
                        currencyStatusText = CurrencyRepository.lastUpdatedText,
                        currencyIsOnline = CurrencyRepository.isLiveOnline
                    )
                }
            } else {
                _uiState.update { state ->
                    val converted = calculateForexConversion(state.currencyInput, state.currencyFromCode, state.currencyToCode)
                    state.copy(
                        isCurrencyLoading = false,
                        currencyOutput = converted,
                        currencyStatusText = "Offline Mode • Standard Forex Rates",
                        currencyIsOnline = false
                    )
                }
            }
        }
    }

    fun onCurrencyInputChange(amount: String) {
        _uiState.update {
            val converted = calculateForexConversion(amount, it.currencyFromCode, it.currencyToCode)
            it.copy(currencyInput = amount, currencyOutput = converted)
        }
    }

    fun setCurrencyFrom(code: String) {
        _uiState.update {
            val converted = calculateForexConversion(it.currencyInput, code, it.currencyToCode)
            it.copy(currencyFromCode = code, currencyOutput = converted)
        }
    }

    fun setCurrencyTo(code: String) {
        _uiState.update {
            val converted = calculateForexConversion(it.currencyInput, it.currencyFromCode, code)
            it.copy(currencyToCode = code, currencyOutput = converted)
        }
    }

    fun swapCurrencies() {
        _uiState.update {
            val from = it.currencyFromCode
            val to = it.currencyToCode
            val converted = calculateForexConversion(it.currencyInput, to, from)
            it.copy(currencyFromCode = to, currencyToCode = from, currencyOutput = converted)
        }
    }

    fun setQuickInrAmount(inrAmount: Double) {
        val df = DecimalFormat("#.##", DecimalFormatSymbols(Locale.US))
        val amountStr = df.format(inrAmount)
        _uiState.update {
            val converted = calculateForexConversion(amountStr, "INR", it.currencyToCode)
            it.copy(currencyFromCode = "INR", currencyInput = amountStr, currencyOutput = converted)
        }
    }

    private fun calculateForexConversion(amountStr: String, from: String, to: String): String {
        val amount = amountStr.toDoubleOrNull() ?: return "0.00"
        val converted = CurrencyRepository.convert(amount, from, to)
        val df = DecimalFormat("#,##0.00##", DecimalFormatSymbols(Locale.US))
        return df.format(converted)
    }

    fun selectCustomCalculator(calculator: CustomCalculator) {
        _uiState.update { it.copy(activeCustomCalculator = calculator) }
    }

    fun saveCustomCalculator(calculator: CustomCalculator) {
        _uiState.update {
            val updatedList = if (it.customCalculators.any { c -> c.id == calculator.id }) {
                it.customCalculators.map { c -> if (c.id == calculator.id) calculator else c }
            } else {
                listOf(calculator) + it.customCalculators
            }
            it.copy(customCalculators = updatedList, activeCustomCalculator = calculator)
        }
    }

    fun deleteCustomCalculator(calculatorId: String) {
        _uiState.update {
            val updatedList = it.customCalculators.filter { c -> c.id != calculatorId }
            val fallback = updatedList.firstOrNull() ?: CustomCalculatorEngine.BUILTIN_CALCULATORS[0]
            it.copy(
                customCalculators = updatedList.ifEmpty { CustomCalculatorEngine.BUILTIN_CALCULATORS },
                activeCustomCalculator = fallback
            )
        }
    }

    fun selectCalculationChain(chain: CalculationChain) {
        _uiState.update { it.copy(activeCalculationChain = chain) }
    }

    fun onLoadCopilotExpression(expr: String) {
        _uiState.update {
            it.copy(
                expression = expr,
                cursorPosition = expr.length,
                result = "0",
                mode = CalculatorMode.STANDARD
            )
        }
    }

    fun saveWorksheetDocument(doc: WorksheetDocument) {
        viewModelScope.launch {
            worksheetRepository.saveWorksheet(doc)
            _uiState.update { it.copy(activeWorksheetDocument = doc) }
        }
    }

    fun selectWorksheetDocument(doc: WorksheetDocument) {
        _uiState.update { it.copy(activeWorksheetDocument = doc) }
    }

    fun deleteWorksheetDocument(id: String) {
        viewModelScope.launch {
            worksheetRepository.deleteWorksheet(id)
        }
    }

    fun updateWorksheetSettings(settings: com.example.model.WorksheetSettings) {
        _uiState.update { it.copy(worksheetSettings = settings) }
    }

    fun createNewWorksheetDocument(customTitle: String = "") {
        viewModelScope.launch {
            val count = _uiState.value.worksheetDocuments.size + 1
            val resolvedTitle = customTitle.ifBlank { "Calculation($count)" }
            val newDoc = WorksheetDocument(
                title = resolvedTitle,
                lines = listOf(
                    WorksheetLine(
                        operator = "+",
                        rawValue = "0",
                        note = ""
                    )
                ),
                grandTotal = 0.0,
                headerNote = ""
            )
            worksheetRepository.saveWorksheet(newDoc)
            _uiState.update { it.copy(activeWorksheetDocument = newDoc) }
        }
    }

    fun duplicateWorksheetDocument(doc: WorksheetDocument) {
        viewModelScope.launch {
            val duplicated = doc.copy(
                id = java.util.UUID.randomUUID().toString(),
                title = "${doc.title} (Copy)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            worksheetRepository.saveWorksheet(duplicated)
            _uiState.update { it.copy(activeWorksheetDocument = duplicated) }
        }
    }

    fun restoreWorksheets(docs: List<WorksheetDocument>) {
        viewModelScope.launch {
            docs.forEach { doc ->
                worksheetRepository.saveWorksheet(doc)
            }
            if (docs.isNotEmpty()) {
                _uiState.update { it.copy(activeWorksheetDocument = docs.last()) }
            }
        }
    }

    fun applyWorksheetTemplate(template: WorksheetTemplate) {
        viewModelScope.launch {
            val lines = WorksheetTapeEngine.recalculate(template.lines)
            val grandTotal = lines.lastOrNull()?.runningTotal ?: 0.0
            val newDoc = WorksheetDocument(
                title = template.title,
                lines = lines,
                grandTotal = grandTotal
            )
            worksheetRepository.saveWorksheet(newDoc)
            _uiState.update { it.copy(activeWorksheetDocument = newDoc) }
        }
    }

    private fun Double.roundToInt(): Int = kotlin.math.round(this).toInt()

    override fun onCleared() {
        super.onCleared()
        soundHapticHelper.release()
    }
}

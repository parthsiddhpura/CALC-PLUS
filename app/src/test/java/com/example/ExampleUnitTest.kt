package com.example

import com.example.domain.CalculatorEngine
import com.example.model.AngleMode
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun percentage_calculations() {
    // 7x10% as requested by user
    assertEquals("0.7", CalculatorEngine.evaluate("7*10%", AngleMode.DEG))
    assertEquals("0.7", CalculatorEngine.evaluate("7×10%", AngleMode.DEG))
    assertEquals("0.7", CalculatorEngine.evaluate("7x10%", AngleMode.DEG))

    // Previews
    assertEquals("0.7", CalculatorEngine.evaluatePreview("7×10%", AngleMode.DEG))
    assertEquals("0.7", CalculatorEngine.evaluatePreview("7*10%", AngleMode.DEG))

    // Standalone percent
    assertEquals("0.1", CalculatorEngine.evaluate("10%", AngleMode.DEG))
    assertEquals("0.5", CalculatorEngine.evaluate("50%", AngleMode.DEG))

    // Markup / discount
    assertEquals("110", CalculatorEngine.evaluate("100+10%", AngleMode.DEG))
    assertEquals("90", CalculatorEngine.evaluate("100-10%", AngleMode.DEG))
    assertEquals("80", CalculatorEngine.evaluate("100-20%", AngleMode.DEG))

    // Multiplications & divisions
    assertEquals("10", CalculatorEngine.evaluate("100*10%", AngleMode.DEG))
    assertEquals("1,000", CalculatorEngine.evaluate("100/10%", AngleMode.DEG))
    assertEquals("100", CalculatorEngine.evaluate("50/50%", AngleMode.DEG))
    assertEquals("0.2", CalculatorEngine.evaluate("(10+10)%", AngleMode.DEG))
  }

  @Test
  fun chained_calculations_and_formatting() {
    // Evaluation with commas from previous result (e.g., user result was 21,672,730 and multiplies by 5)
    assertEquals("21,672,730", CalculatorEngine.evaluate("4334546 × 5", AngleMode.DEG))
    assertEquals("108,363,650", CalculatorEngine.evaluate("21,672,730 × 5", AngleMode.DEG))
    assertEquals("108,363,650", CalculatorEngine.evaluate("2,16,72,730 × 5", AngleMode.DEG))
    assertEquals("108,363,650", CalculatorEngine.evaluatePreview("21,672,730 × 5", AngleMode.DEG))
    assertEquals("108,363,650", CalculatorEngine.evaluatePreview("2,16,72,730 × 5", AngleMode.DEG))

    // Display Formatter Indian, Space, None, Comma formatting
    val indianFormatted = com.example.model.DisplayFormatter.formatNumber("21672730", com.example.model.DisplaySeparatorStyle.INDIAN)
    assertEquals("2,16,72,730", indianFormatted)

    // Formatter handles numbers that already had commas
    val indianFromComma = com.example.model.DisplayFormatter.formatNumber("21,672,730", com.example.model.DisplaySeparatorStyle.INDIAN)
    assertEquals("2,16,72,730", indianFromComma)

    val spaceFormatted = com.example.model.DisplayFormatter.formatNumber("21,672,730", com.example.model.DisplaySeparatorStyle.SPACE)
    assertEquals("21 672 730", spaceFormatted)

    val noneFormatted = com.example.model.DisplayFormatter.formatNumber("21,672,730", com.example.model.DisplaySeparatorStyle.NONE)
    assertEquals("21672730", noneFormatted)

    // Expression formatting with Indian separator
    val exprIndian = com.example.model.DisplayFormatter.formatExpression("4334546×5", com.example.model.DisplaySeparatorStyle.INDIAN)
    assertEquals("43,34,546×5", exprIndian)

    // Implicit multiplication & functions
    assertEquals("14", CalculatorEngine.evaluate("2(3+4)", AngleMode.DEG))
    assertEquals("35", CalculatorEngine.evaluate("(3+4)5", AngleMode.DEG))
    assertEquals("3", CalculatorEngine.evaluate("√9", AngleMode.DEG))
    assertEquals("6", CalculatorEngine.evaluate("2√9", AngleMode.DEG))
    assertEquals("8", CalculatorEngine.evaluate("+5 + +3", AngleMode.DEG))
    assertEquals("-8", CalculatorEngine.evaluate("-(5+3)", AngleMode.DEG))
  }

  @Test
  fun themes_allRegisteredAndValid() {
    val themes = com.example.ui.theme.CalculatorThemes.allThemes
    assertTrue("Themes count should be 49", themes.size >= 49)

    // Ensure all theme IDs are unique
    val ids = themes.map { it.id }
    assertEquals(ids.size, ids.toSet().size)

    // Ensure new image themes are present
    assertNotNull(com.example.ui.theme.CalculatorThemes.getThemeById(com.example.model.ThemeId.NEUMORPHIC_ICE_LIGHT))
    assertNotNull(com.example.ui.theme.CalculatorThemes.getThemeById(com.example.model.ThemeId.NEUMORPHIC_MIDNIGHT_AZURE))
    assertNotNull(com.example.ui.theme.CalculatorThemes.getThemeById(com.example.model.ThemeId.OBSIDIAN_TANGERINE))
    assertNotNull(com.example.ui.theme.CalculatorThemes.getThemeById(com.example.model.ThemeId.KAWAII_CLAY_PINK))
    assertNotNull(com.example.ui.theme.CalculatorThemes.getThemeById(com.example.model.ThemeId.RETRO_MACARON_TYPEWRITER))
    assertNotNull(com.example.ui.theme.CalculatorThemes.getThemeById(com.example.model.ThemeId.INDUSTRIAL_AMBER_BEZEL))
    assertNotNull(com.example.ui.theme.CalculatorThemes.getThemeById(com.example.model.ThemeId.MIDNIGHT_OCEAN_RADIAL))
    assertNotNull(com.example.ui.theme.CalculatorThemes.getThemeById(com.example.model.ThemeId.MINIMAL_POWDER_BLUE))
    
    val batmanTheme = com.example.ui.theme.CalculatorThemes.getThemeById(com.example.model.ThemeId.BATMAN_DARK_KNIGHT)
    assertNotNull(batmanTheme)
    assertTrue("Batman theme should have hasBatSignal=true", batmanTheme.hasBatSignal)
    assertTrue("Batman theme should be dark", batmanTheme.isDark)
  }

  @Test
  fun pressureCalculator_scientificAccuracy() {
    val calc = com.example.domain.CustomCalculatorEngine.buildFromPrompt("measure pressure")
    assertEquals("Fluid & Mechanical Pressure Calculator", calc.title)
    assertFalse("Static physics calculator should not claim live internet connection", calc.hasInternetData)

    // Check inputs: force_n, area_cm2, fluid_depth_m, fluid_density
    val inputIds = calc.inputs.map { it.id }
    assertTrue(inputIds.contains("force_n"))
    assertTrue(inputIds.contains("area_cm2"))
    assertTrue(inputIds.contains("fluid_depth_m"))
    assertTrue(inputIds.contains("fluid_density"))

    // Calculate outputs for 500 N on 25 cm²
    val inputs = mapOf(
      "force_n" to 500.0,
      "area_cm2" to 25.0,
      "fluid_depth_m" to 10.0,
      "fluid_density" to 1000.0
    )
    val outputs = com.example.domain.CustomCalculatorEngine.calculateOutputs(calc, inputs)

    // P = 500 N / 0.0025 m² = 200,000 Pa = 200 kPa = 2.0 bar
    assertEquals(200.0, outputs["pressure_kpa"] ?: 0.0, 0.01)
    assertEquals(2.0, outputs["pressure_bar"] ?: 0.0, 0.01)

    // Hydrostatic = 1000 kg/m³ * 9.80665 m/s² * 10 m = 98066.5 Pa = 98.0665 kPa
    assertEquals(98.0665, outputs["hydrostatic_kpa"] ?: 0.0, 0.1)
  }

  @Test
  fun thorough_calculation_accuracy_tests() {
    // 1. Core Arithmetic & Floating Point Precision
    assertEquals("0.3", CalculatorEngine.evaluate("0.1 + 0.2", AngleMode.DEG))
    assertEquals("0.1", CalculatorEngine.evaluate("1.4 - 1.3", AngleMode.DEG))
    assertEquals("7", CalculatorEngine.evaluate("100 * 0.07", AngleMode.DEG))
    assertEquals("3", CalculatorEngine.evaluate("0.3 / 0.1", AngleMode.DEG))
    assertEquals("0.05", CalculatorEngine.evaluate("0.2 - 0.15", AngleMode.DEG))

    // 2. Order of Operations (PEMDAS)
    assertEquals("14", CalculatorEngine.evaluate("2 + 3 * 4", AngleMode.DEG))
    assertEquals("20", CalculatorEngine.evaluate("(2 + 3) * 4", AngleMode.DEG))
    assertEquals("11", CalculatorEngine.evaluate("20 / 2 + 1", AngleMode.DEG))
    assertEquals("2", CalculatorEngine.evaluate("20 / (2 + 8)", AngleMode.DEG))

    // 3. Exact Trigonometry in DEG Mode
    assertEquals("0", CalculatorEngine.evaluate("sin(0)", AngleMode.DEG))
    assertEquals("0.5", CalculatorEngine.evaluate("sin(30)", AngleMode.DEG))
    assertEquals("1", CalculatorEngine.evaluate("sin(90)", AngleMode.DEG))
    assertEquals("0", CalculatorEngine.evaluate("sin(180)", AngleMode.DEG))
    assertEquals("-1", CalculatorEngine.evaluate("sin(270)", AngleMode.DEG))
    assertEquals("0", CalculatorEngine.evaluate("sin(360)", AngleMode.DEG))

    assertEquals("1", CalculatorEngine.evaluate("cos(0)", AngleMode.DEG))
    assertEquals("0.5", CalculatorEngine.evaluate("cos(60)", AngleMode.DEG))
    assertEquals("0", CalculatorEngine.evaluate("cos(90)", AngleMode.DEG))
    assertEquals("-1", CalculatorEngine.evaluate("cos(180)", AngleMode.DEG))
    assertEquals("0", CalculatorEngine.evaluate("cos(270)", AngleMode.DEG))
    assertEquals("1", CalculatorEngine.evaluate("cos(360)", AngleMode.DEG))

    assertEquals("0", CalculatorEngine.evaluate("tan(0)", AngleMode.DEG))
    assertEquals("1", CalculatorEngine.evaluate("tan(45)", AngleMode.DEG))
    assertEquals("0", CalculatorEngine.evaluate("tan(180)", AngleMode.DEG))
    assertEquals("Error", CalculatorEngine.evaluate("tan(90)", AngleMode.DEG)) // Undefined

    // 4. Inverse Trig
    assertEquals("30", CalculatorEngine.evaluate("arcsin(0.5)", AngleMode.DEG))
    assertEquals("90", CalculatorEngine.evaluate("arcsin(1)", AngleMode.DEG))
    assertEquals("0", CalculatorEngine.evaluate("arcsin(0)", AngleMode.DEG))
    assertEquals("60", CalculatorEngine.evaluate("arccos(0.5)", AngleMode.DEG))
    assertEquals("90", CalculatorEngine.evaluate("arccos(0)", AngleMode.DEG))
    assertEquals("45", CalculatorEngine.evaluate("arctan(1)", AngleMode.DEG))

    // 5. Roots & Powers
    assertEquals("8", CalculatorEngine.evaluate("2^3", AngleMode.DEG))
    assertEquals("-8", CalculatorEngine.evaluate("(-2)^3", AngleMode.DEG))
    assertEquals("16", CalculatorEngine.evaluate("(-2)^4", AngleMode.DEG))
    assertEquals("3", CalculatorEngine.evaluate("sqrt(9)", AngleMode.DEG))
    assertEquals("2", CalculatorEngine.evaluate("cbrt(8)", AngleMode.DEG))
    assertEquals("-2", CalculatorEngine.evaluate("cbrt(-8)", AngleMode.DEG))

    // 6. Factorials
    assertEquals("1", CalculatorEngine.evaluate("0!", AngleMode.DEG))
    assertEquals("1", CalculatorEngine.evaluate("1!", AngleMode.DEG))
    assertEquals("120", CalculatorEngine.evaluate("5!", AngleMode.DEG))
    assertEquals("720", CalculatorEngine.evaluate("6!", AngleMode.DEG))

    // 7. Error & Division by Zero Safety
    assertEquals("Error", CalculatorEngine.evaluate("5 / 0", AngleMode.DEG))
    assertEquals("Error", CalculatorEngine.evaluate("sqrt(-4)", AngleMode.DEG))
    assertEquals("Error", CalculatorEngine.evaluate("ln(0)", AngleMode.DEG))
    assertEquals("Error", CalculatorEngine.evaluate("log(-10)", AngleMode.DEG))
    assertEquals("Error", CalculatorEngine.evaluate("arcsin(2)", AngleMode.DEG))

    // 8. Logarithms
    assertEquals("2", CalculatorEngine.evaluate("log10(100)", AngleMode.DEG))
    assertEquals("1", CalculatorEngine.evaluate("log10(10)", AngleMode.DEG))
    assertEquals("0", CalculatorEngine.evaluate("log10(1)", AngleMode.DEG))
    assertEquals("0", CalculatorEngine.evaluate("ln(1)", AngleMode.DEG))

    // 9. GST Engine Checks
    val exclResult = com.example.domain.GstEngine.calculate(1000.0, 18.0, com.example.model.GstCalculationType.EXCLUSIVE)
    assertEquals(1000.0, exclResult.netAmount, 0.001)
    assertEquals(180.0, exclResult.gstAmount, 0.001)
    assertEquals(90.0, exclResult.cgstAmount, 0.001)
    assertEquals(90.0, exclResult.sgstAmount, 0.001)
    assertEquals(1180.0, exclResult.grossAmount, 0.001)

    val inclResult = com.example.domain.GstEngine.calculate(1180.0, 18.0, com.example.model.GstCalculationType.INCLUSIVE)
    assertEquals(1000.0, inclResult.netAmount, 0.001)
    assertEquals(180.0, inclResult.gstAmount, 0.001)
    assertEquals(90.0, inclResult.cgstAmount, 0.001)
    assertEquals(90.0, inclResult.sgstAmount, 0.001)
    assertEquals(1180.0, inclResult.grossAmount, 0.001)
  }

  @Test
  fun currency_and_custom_calculator_no_bluff_tests() {
    // 1. General "currency" / "currency calculator" / "forex" prompt builds Live Multi-Currency Forex Calculator (zero bluff!)
    val multiCurrencyCalc = com.example.domain.CustomCalculatorEngine.buildFromPrompt("currency")
    assertEquals("Live Multi-Currency Forex Calculator", multiCurrencyCalc.title)
    assertEquals("Live Forex & Currency", multiCurrencyCalc.category)
    assertTrue(multiCurrencyCalc.hasInternetData)
    val multiOutputs = com.example.domain.CustomCalculatorEngine.calculateOutputs(
      multiCurrencyCalc,
      multiCurrencyCalc.inputs.associate { it.id to it.defaultValue }
    )
    assertTrue("USD to INR output must be > 5000 for 100 USD", (multiOutputs["usd_to_inr"] ?: 0.0) > 5000.0)
    assertTrue("EUR to INR output must be > 5000 for 100 EUR", (multiOutputs["eur_to_inr"] ?: 0.0) > 5000.0)
    assertTrue("GBP to INR output must be > 5000 for 100 GBP", (multiOutputs["gbp_to_inr"] ?: 0.0) > 5000.0)

    // 2. Specific currency pair detection preserves user order (e.g., EUR to USD, GBP to INR)
    val eurToUsdCalc = com.example.domain.CustomCalculatorEngine.buildFromPrompt("convert eur to usd")
    assertEquals("EUR to USD Live Currency Converter", eurToUsdCalc.title)
    assertTrue(eurToUsdCalc.hasInternetData)
    val eurToUsdOutputs = com.example.domain.CustomCalculatorEngine.calculateOutputs(
      eurToUsdCalc,
      mapOf("source_amount" to 100.0, "exchange_rate" to 1.08, "bank_spread_pct" to 0.0, "flat_fee" to 0.0)
    )
    assertEquals(108.0, eurToUsdOutputs["gross_amount"] ?: 0.0, 0.001)
    assertEquals(108.0, eurToUsdOutputs["target_amount"] ?: 0.0, 0.001)

    // 3. Non-currency prompts with 3-letter substrings ("cupcake", "geometry", "electricity") never falsely trigger currency
    val bakeryCalc = com.example.domain.CustomCalculatorEngine.buildFromPrompt("cupcake bakery recipe")
    assertNotEquals("Live Forex & Currency", bakeryCalc.category)

    // 4. Validate all built-in calculators with FormulaEvaluator.validateFormula
    for (calc in com.example.domain.CustomCalculatorEngine.BUILTIN_CALCULATORS) {
      val vars = calc.inputs.associate { it.id to it.defaultValue }.toMutableMap()
      for (out in calc.outputs) {
        assertTrue(
          "Formula '${out.formula}' in '${calc.title}' must be strictly valid",
          com.example.domain.FormulaEvaluator.validateFormula(out.formula, vars)
        )
        val valRes = com.example.domain.FormulaEvaluator.evaluate(out.formula, vars)
        vars[out.id] = valRes
        vars["prev"] = valRes
      }
    }

    // 5. FormulaEvaluator rejects unknown variables and supports scientific functions
    assertFalse(com.example.domain.FormulaEvaluator.validateFormula("unknown_var * 2", mapOf("known_var" to 10.0)))
    assertTrue(com.example.domain.FormulaEvaluator.validateFormula("sqrt(known_var) + 2 ^ -3", mapOf("known_var" to 16.0)))
    assertEquals(4.125, com.example.domain.FormulaEvaluator.evaluate("sqrt(known_var) + 2 ^ -3", mapOf("known_var" to 16.0)), 1e-6)
  }

  @Test
  fun ai_math_copilot_accuracy_tests() {
    // 1. GST + Discount
    val gstRes = com.example.domain.AiMathCopilotEngine.solveQuery("18% GST on ₹45,000 plus 10% discount")
    // 45,000 - 10% = 40,500; + 18% GST (7,290) = 47,790
    assertEquals("₹47,790", gstRes.finalAnswer)

    // 2. Tip & Bill Split
    val tipRes = com.example.domain.AiMathCopilotEngine.solveQuery("25% tip on ₹4,800 bill split among 5 people")
    // 4,800 + 25% (1,200) = 6,000 / 5 = 1,200 / person
    assertEquals("₹1,200 / person", tipRes.finalAnswer)

    // 3. Monthly SIP Compound Growth
    val sipRes = com.example.domain.AiMathCopilotEngine.solveQuery("Compound growth of ₹10,000 monthly SIP for 15 years at 14%")
    assertTrue("SIP answer should start with ₹", sipRes.finalAnswer.startsWith("₹"))
    assertFalse("SIP answer should not be ₹0", sipRes.finalAnswer == "₹0")

    // 4. Quadratic Equation
    val quadRes = com.example.domain.AiMathCopilotEngine.solveQuery("Solve 2x² + 5x - 3 = 0")
    assertTrue(quadRes.finalAnswer.contains("0.5") && quadRes.finalAnswer.contains("-3"))
  }

  @Test
  fun widget_dynamic_theme_sync_tests() {
    // Verify every app theme converts to a 100% solid (non-glass) WidgetDynamicTheme
    for (palette in com.example.ui.theme.CalculatorThemes.allThemes) {
      val widgetTheme = com.example.widget.WidgetDynamicTheme.fromThemePalette(palette)
      assertEquals(palette.id, widgetTheme.themeId)
      assertEquals(palette.name, widgetTheme.title)
      assertEquals(com.example.R.drawable.widget_bg_minimal, widgetTheme.bgRes)
      // Ensure alpha channel is 0xFF (100% opaque, no glass translucency)
      assertEquals(0xFF, (widgetTheme.bgColorHex ushr 24) and 0xFF)
      assertEquals(0xFF, (widgetTheme.screenBgColorHex ushr 24) and 0xFF)
      assertEquals(0xFF, (widgetTheme.btnBgColorHex ushr 24) and 0xFF)
    }
  }
}




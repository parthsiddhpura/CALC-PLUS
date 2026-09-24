package com.example.domain

import com.example.BuildConfig
import com.example.data.CurrencyRepository
import com.example.model.CustomCalculator
import com.example.model.CustomInputField
import com.example.model.CustomOutputField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

object CustomCalculatorEngine {

    val BUILTIN_CALCULATORS = listOf(
        CustomCalculator(
            id = "live_usd_inr_remittance",
            title = "Live USD to INR Remittance",
            description = "Real-time Forex conversion with live interbank rate, card markup & remittance fees",
            category = "Live Forex & Currency",
            iconName = "CurrencyExchange",
            hasInternetData = true,
            dataSourceLabel = "Live Internet Forex • open.er-api.com",
            inputs = listOf(
                CustomInputField("usd_amount", "Transfer Amount", 500.0, 500.0, 10.0, 50000.0, 25.0, "$", true, "Amount in US Dollars to convert or remit"),
                CustomInputField("live_rate", "Live Interbank Rate (USD to INR)", 87.25, 87.25, 70.0, 110.0, 0.05, "₹/$", true, "Official live currency exchange rate"),
                CustomInputField("bank_markup_pct", "Bank / Card FX Spread Fee", 1.5, 1.5, 0.0, 5.0, 0.1, "%", true, "Bank currency conversion fee (typical 1.5% to 3.5%)"),
                CustomInputField("wire_fixed_fee", "Fixed Wire Transfer Fee", 5.0, 5.0, 0.0, 50.0, 1.0, "$", true, "SWIFT / ACH flat transaction fee")
            ),
            outputs = listOf(
                CustomOutputField("effective_rate", "Net Effective Rate per Dollar", "live_rate * (1 - bank_markup_pct / 100)", "₹/$", false, "Rate after deducting percentage bank spread"),
                CustomOutputField("gross_inr", "Gross Market Value", "usd_amount * live_rate", "₹", false, "Official full market conversion value"),
                CustomOutputField("total_fee_inr", "Total Fees Deducted (Wire + Spread)", "((usd_amount * (bank_markup_pct / 100)) + wire_fixed_fee) * live_rate", "₹", false, "Combined value of all bank commissions"),
                CustomOutputField("net_inr_received", "Net In-Hand INR Received", "(usd_amount - wire_fixed_fee) * (live_rate * (1 - bank_markup_pct / 100))", "₹", true, "Exact Rupee amount deposited into recipient account")
            )
        ),

        CustomCalculator(
            id = "gold_jewellery_bullion",
            title = "Gold 24K vs 22K Jewellery",
            description = "Pure bullion value, 22K/18K ornament pricing, jeweler making charges & 3% GST",
            category = "Precious Metals & Bullion",
            iconName = "AutoAwesome",
            hasInternetData = true,
            dataSourceLabel = "Indian Bullion & Jewellers Association (IBJA) Standards",
            inputs = listOf(
                CustomInputField("gold_weight", "Gold Weight", 10.0, 10.0, 0.5, 250.0, 0.5, "g", true, "Ornament weight in grams (8g = 1 Sovereign / Pavan, 11.66g = 1 Tola)"),
                CustomInputField("rate_24k_10g", "24K Pure Gold Rate (per 10g)", 88500.0, 88500.0, 50000.0, 150000.0, 100.0, "₹", true, "Current live 24-Karat spot market rate for 10 grams"),
                CustomInputField("purity_factor", "Karat Purity Ratio", 0.916, 0.916, 0.750, 1.0, 0.001, "factor", true, "22K Hallmark = 0.916 | 18K = 0.750 | 24K = 1.000"),
                CustomInputField("making_charge_pct", "Jeweler Making Charges", 12.0, 12.0, 0.0, 30.0, 0.5, "%", true, "Artisan craftsmanship charge"),
                CustomInputField("gst_pct", "Government GST", 3.0, 3.0, 0.0, 10.0, 0.5, "%", true, "Standard 3% gold jewelry GST")
            ),
            outputs = listOf(
                CustomOutputField("rate_per_gram", "Base Gold Rate per Gram (Purity Adjusted)", "(rate_24k_10g / 10) * purity_factor", "₹/g", false, "Raw gold value for selected karat per gram"),
                CustomOutputField("raw_gold_cost", "Raw Gold Value of Ornament", "gold_weight * ((rate_24k_10g / 10) * purity_factor)", "₹", false, "Pure metal value before labor & taxes"),
                CustomOutputField("making_charges", "Making Charges Cost", "(gold_weight * ((rate_24k_10g / 10) * purity_factor)) * (making_charge_pct / 100)", "₹", false, "Jeweler craftsmanship deduction"),
                CustomOutputField("gst_amount", "3% GST Payable", "((gold_weight * ((rate_24k_10g / 10) * purity_factor)) * (1 + making_charge_pct / 100)) * (gst_pct / 100)", "₹", false, "Statutory tax on metal + making charges"),
                CustomOutputField("final_bill", "Final Jewellery Invoice Total", "((gold_weight * ((rate_24k_10g / 10) * purity_factor)) * (1 + making_charge_pct / 100)) * (1 + gst_pct / 100)", "₹", true, "Total invoice price payable at checkout")
            )
        ),

        CustomCalculator(
            id = "factory_production_cost",
            title = "Factory Production Cost",
            description = "Monthly manufacturing overhead, unit costs, breakeven & selling price with target margin",
            category = "Manufacturing & Business",
            iconName = "Factory",
            inputs = listOf(
                CustomInputField("raw_material", "Raw Material Cost", 250000.0, 250000.0, 0.0, 2000000.0, 5000.0, "₹", true, "Direct material purchase cost"),
                CustomInputField("labour", "Direct Labour & Wages", 120000.0, 120000.0, 0.0, 1000000.0, 2000.0, "₹", true, "Factory worker payroll & salaries"),
                CustomInputField("electricity", "Electricity & Power", 35000.0, 35000.0, 0.0, 300000.0, 1000.0, "₹", true, "Factory utility & heavy power bill"),
                CustomInputField("maintenance", "Equipment Maintenance", 15000.0, 15000.0, 0.0, 150000.0, 500.0, "₹", true, "Machinery servicing & spare parts"),
                CustomInputField("overhead", "Factory Rent & Overhead", 50000.0, 50000.0, 0.0, 500000.0, 1000.0, "₹", true, "Facility lease, supervisor & admin costs"),
                CustomInputField("units", "Units Produced", 5000.0, 5000.0, 1.0, 50000.0, 50.0, "units", true, "Total monthly units manufactured"),
                CustomInputField("profit_margin", "Target Profit Margin", 25.0, 25.0, 0.0, 100.0, 0.5, "%", true, "Desired markup above total cost")
            ),
            outputs = listOf(
                CustomOutputField("total_cost", "Total Monthly Production Cost", "raw_material + labour + electricity + maintenance + overhead", "₹", false, "Sum of all manufacturing and operational expenses"),
                CustomOutputField("cost_per_unit", "Cost Per Unit", "(raw_material + labour + electricity + maintenance + overhead) / units", "₹", true, "Base manufacturing cost required per piece"),
                CustomOutputField("selling_price", "Recommended Selling Price / Unit", "((raw_material + labour + electricity + maintenance + overhead) / units) * (1 + profit_margin / 100)", "₹", true, "Unit price including your target profit margin"),
                CustomOutputField("total_revenue", "Projected Monthly Revenue", "units * (((raw_material + labour + electricity + maintenance + overhead) / units) * (1 + profit_margin / 100))", "₹", false, "Gross monthly sales if all units are sold"),
                CustomOutputField("monthly_profit", "Projected Net Profit", "(units * (((raw_material + labour + electricity + maintenance + overhead) / units) * (1 + profit_margin / 100))) - (raw_material + labour + electricity + maintenance + overhead)", "₹", true, "Net monthly profit generated")
            )
        ),

        CustomCalculator(
            id = "freelance_project_pricing",
            title = "Freelance & Agency Pricing",
            description = "Calculate project quote, buffer hours, platform fee deduction and take-home net rate",
            category = "Freelance & Career",
            iconName = "Work",
            inputs = listOf(
                CustomInputField("hourly_rate", "Target Hourly Rate", 1500.0, 1500.0, 100.0, 20000.0, 50.0, "₹", true, "Desired billing per hour"),
                CustomInputField("core_hours", "Estimated Core Hours", 40.0, 40.0, 1.0, 300.0, 1.0, "hrs", true, "Design and development time"),
                CustomInputField("buffer_percent", "Revision & Delay Buffer", 20.0, 20.0, 0.0, 100.0, 1.0, "%", true, "Safety margin for scope creep"),
                CustomInputField("expenses", "Software / Asset Licenses", 3000.0, 3000.0, 0.0, 100000.0, 500.0, "₹", true, "Fonts, stock assets, API subscriptions"),
                CustomInputField("platform_fee", "Platform & Gateway Fee", 10.0, 10.0, 0.0, 30.0, 0.5, "%", true, "Upwork, Stripe or Razorpay fee"),
                CustomInputField("tax_rate", "Income Tax / GST", 18.0, 18.0, 0.0, 40.0, 1.0, "%", true, "Estimated tax allocation")
            ),
            outputs = listOf(
                CustomOutputField("total_hours", "Total Billable Hours (with Buffer)", "core_hours * (1 + buffer_percent / 100)", "hrs", false, "Adjusted hours accounting for client feedback"),
                CustomOutputField("base_quote", "Base Labor Quote", "(core_hours * (1 + buffer_percent / 100)) * hourly_rate + expenses", "₹", false, "Quote before platform fee and taxes"),
                CustomOutputField("client_invoice", "Final Client Invoice Quote", "((core_hours * (1 + buffer_percent / 100)) * hourly_rate + expenses) / (1 - platform_fee / 100)", "₹", true, "Amount to quote client so fees don't cut into your rate"),
                CustomOutputField("net_takehome", "Net Take-Home (Post Tax & Fees)", "((core_hours * (1 + buffer_percent / 100)) * hourly_rate) * (1 - tax_rate / 100)", "₹", true, "Pure pocket earnings after all deductions"),
                CustomOutputField("effective_rate", "Effective Hourly Take-Home", "(((core_hours * (1 + buffer_percent / 100)) * hourly_rate) * (1 - tax_rate / 100)) / (core_hours * (1 + buffer_percent / 100))", "₹/hr", false, "Actual cash earned per hour spent")
            )
        ),

        CustomCalculator(
            id = "rental_property_roi",
            title = "Real Estate Rental ROI & Yield",
            description = "Gross yield, net cashflow, cap rate and payback timeline for rental property",
            category = "Real Estate & Investing",
            iconName = "Home",
            inputs = listOf(
                CustomInputField("purchase_price", "Property Purchase Price", 6500000.0, 6500000.0, 500000.0, 50000000.0, 50000.0, "₹", true, "Total acquisition cost"),
                CustomInputField("monthly_rent", "Expected Monthly Rent", 28000.0, 28000.0, 2000.0, 300000.0, 500.0, "₹", true, "Tenant rent received"),
                CustomInputField("property_tax", "Annual Property Tax", 12000.0, 12000.0, 0.0, 100000.0, 500.0, "₹", true, "Municipal property tax per year"),
                CustomInputField("monthly_maintenance", "Monthly Society Maintenance", 3500.0, 3500.0, 0.0, 30000.0, 100.0, "₹", true, "HOA / Society charges paid by owner"),
                CustomInputField("occupancy_rate", "Occupancy Rate", 92.0, 92.0, 50.0, 100.0, 1.0, "%", true, "Percentage of year property is occupied (e.g. 11/12 mos = 92%)")
            ),
            outputs = listOf(
                CustomOutputField("gross_annual_rent", "Effective Annual Rental Income", "(monthly_rent * 12) * (occupancy_rate / 100)", "₹", false, "Annual rent adjusted for vacancy"),
                CustomOutputField("annual_expenses", "Total Annual Operating Costs", "property_tax + (monthly_maintenance * 12)", "₹", false, "Taxes and maintenance fees"),
                CustomOutputField("net_annual_cashflow", "Net Annual Rental Cashflow", "((monthly_rent * 12) * (occupancy_rate / 100)) - (property_tax + (monthly_maintenance * 12))", "₹", true, "Cash profit generated per year"),
                CustomOutputField("gross_yield", "Gross Rental Yield", "((monthly_rent * 12) / purchase_price) * 100", "%", false, "Pre-expense rental return rate"),
                CustomOutputField("net_yield", "Net Rental Yield (Cap Rate)", "((((monthly_rent * 12) * (occupancy_rate / 100)) - (property_tax + (monthly_maintenance * 12))) / purchase_price) * 100", "%", true, "True net return on capital"),
                CustomOutputField("payback_years", "Capital Payback Period", "purchase_price / ((((monthly_rent * 12) * (occupancy_rate / 100)) - (property_tax + (monthly_maintenance * 12))))", "yrs", false, "Years to recover purchase price from rent")
            )
        ),

        CustomCalculator(
            id = "solar_rooftop_savings",
            title = "Solar Rooftop ROI & Payback",
            description = "Solar panel system sizing, energy bill offset, subsidy & lifetime electricity savings",
            category = "Energy & Sustainability",
            iconName = "WbSunny",
            inputs = listOf(
                CustomInputField("monthly_bill", "Current Monthly Electricity Bill", 4500.0, 4500.0, 500.0, 50000.0, 100.0, "₹", true, "Average monthly power bill"),
                CustomInputField("system_capacity", "Solar System Size", 3.0, 3.0, 1.0, 25.0, 0.5, "kW", true, "Rooftop system capacity in kW"),
                CustomInputField("cost_per_kw", "System Cost per kW", 55000.0, 55000.0, 30000.0, 90000.0, 1000.0, "₹", true, "Cost of panels, inverter & installation"),
                CustomInputField("subsidy_percent", "Government Subsidy", 20.0, 20.0, 0.0, 50.0, 1.0, "%", true, "Govt green energy grant"),
                CustomInputField("generation_units", "Monthly Generation per kW", 120.0, 120.0, 80.0, 160.0, 5.0, "kWh", true, "Units produced per kW per month"),
                CustomInputField("tariff_rate", "Grid Tariff per Unit", 8.5, 8.5, 3.0, 15.0, 0.1, "₹/kWh", true, "Cost per electricity unit")
            ),
            outputs = listOf(
                CustomOutputField("net_system_cost", "Net Solar Investment (After Subsidy)", "(system_capacity * cost_per_kw) * (1 - subsidy_percent / 100)", "₹", true, "Total out-of-pocket setup cost"),
                CustomOutputField("monthly_generation", "Total Monthly Energy Generated", "system_capacity * generation_units", "kWh", false, "Clean solar electricity generated"),
                CustomOutputField("monthly_savings", "Estimated Monthly Bill Savings", "min(monthly_bill, (system_capacity * generation_units) * tariff_rate)", "₹", true, "Direct money saved each month"),
                CustomOutputField("payback_years", "System Payback Period", "((system_capacity * cost_per_kw) * (1 - subsidy_percent / 100)) / (min(monthly_bill, (system_capacity * generation_units) * tariff_rate) * 12)", "yrs", true, "Years until solar system pays for itself"),
                CustomOutputField("lifetime_savings_25yr", "25-Year Net Lifetime Savings", "(min(monthly_bill, (system_capacity * generation_units) * tariff_rate) * 12 * 25) - ((system_capacity * cost_per_kw) * (1 - subsidy_percent / 100))", "₹", false, "Cumulative 25-year financial profit")
            )
        )
    )

    /**
     * Calculates all output fields for a custom calculator given current input values
     */
    fun calculateOutputs(
        calculator: CustomCalculator,
        inputValues: Map<String, Double>
    ): Map<String, Double> {
        val results = mutableMapOf<String, Double>()
        val currentContext = inputValues.toMutableMap()

        for (output in calculator.outputs) {
            val evaluated = FormulaEvaluator.evaluate(output.formula, currentContext)
            results[output.id] = evaluated
            currentContext[output.id] = evaluated
        }
        return results
    }

    /**
     * Asynchronously generates a custom calculator using real internet data (Forex rates, Gemini AI, universal unit conversion).
     */
    suspend fun buildFromPromptWithInternet(prompt: String): CustomCalculator = withContext(Dispatchers.IO) {
        val lower = prompt.lowercase(Locale.ROOT).trim()

        // 1. Check for Live Currency & Forex Conversion
        val currencyPair = detectCurrencyPair(lower)
        if (currencyPair != null) {
            val (fromCode, toCode) = currencyPair
            // Fetch live rates from internet
            CurrencyRepository.fetchLiveRates()
            val currentRate = CurrencyRepository.convert(1.0, fromCode, toCode)
            val effectiveRate = if (currentRate > 0.0) currentRate else 87.25

            val fromInfo = CurrencyRepository.getCurrencyInfo(fromCode)
            val toInfo = CurrencyRepository.getCurrencyInfo(toCode)

            return@withContext CustomCalculator(
                id = "ai_currency_${System.currentTimeMillis()}",
                title = "$fromCode to $toCode Live Currency Converter",
                description = "Real-time Forex calculator using live online exchange rates for ${fromInfo.name} to ${toInfo.name}",
                category = "Live Forex & Currency",
                iconName = "CurrencyExchange",
                hasInternetData = true,
                dataSourceLabel = "Live Forex via open.er-api.com • Rate: 1 $fromCode = ${String.format(Locale.US, "%.4f", effectiveRate)} $toCode",
                inputs = listOf(
                    CustomInputField("source_amount", "Amount in $fromCode", 100.0, 100.0, 1.0, 100000.0, 10.0, fromInfo.symbol, true, "Quantity to convert"),
                    CustomInputField("exchange_rate", "Live Exchange Rate ($fromCode → $toCode)", effectiveRate, effectiveRate, effectiveRate * 0.5, effectiveRate * 2.0, 0.01, "${toInfo.symbol}/$fromCode", true, "Official live interbank Forex rate"),
                    CustomInputField("bank_spread_pct", "Bank Spread / Card Markup Fee", 1.5, 1.5, 0.0, 5.0, 0.1, "%", true, "Foreign transaction or bank margin fee"),
                    CustomInputField("flat_fee", "Flat Transfer / Wire Fee", 0.0, 0.0, 0.0, 100.0, 1.0, fromInfo.symbol, true, "Fixed transaction charges if applicable")
                ),
                outputs = listOf(
                    CustomOutputField("target_amount", "Converted Total in $toCode", "(source_amount - flat_fee) * (exchange_rate * (1 - bank_spread_pct / 100))", toInfo.symbol, true, "Net amount received in $toCode after all fees"),
                    CustomOutputField("gross_amount", "Market Value Before Fees", "source_amount * exchange_rate", toInfo.symbol, false, "Value at raw interbank rate"),
                    CustomOutputField("effective_rate", "Effective Net Rate Received", "(exchange_rate * (1 - bank_spread_pct / 100))", "${toInfo.symbol}/$fromCode", false, "Actual rate obtained per 1 $fromCode"),
                    CustomOutputField("total_fee", "Total Fees & Margin Deducted", "(source_amount * exchange_rate) - ((source_amount - flat_fee) * (exchange_rate * (1 - bank_spread_pct / 100)))", toInfo.symbol, false, "Combined value of all bank fees")
                ),
                isUserCreated = true
            )
        }

        // 2. Check for Gold & Precious Metals
        if (lower.contains("gold") || lower.contains("sovereign") || lower.contains("pavan") || lower.contains("tola") || lower.contains("24k") || lower.contains("22k") || lower.contains("bullion")) {
            return@withContext CustomCalculator(
                id = "ai_gold_${System.currentTimeMillis()}",
                title = "Gold Jewellery & Bullion Calculator",
                description = "Live purity calculation, making charges and GST for gold ornaments & coins",
                category = "Precious Metals & Bullion",
                iconName = "AutoAwesome",
                hasInternetData = true,
                dataSourceLabel = "Live Indian Bullion Standard • 24K, 22K & 18K",
                inputs = listOf(
                    CustomInputField("weight_grams", "Gold Weight in Grams", 8.0, 8.0, 0.1, 200.0, 0.5, "g", true, "Weight (8g = 1 Sovereign/Pavan, 11.66g = 1 Tola)"),
                    CustomInputField("rate_24k_10g", "24K Gold Rate (per 10g)", 88500.0, 88500.0, 60000.0, 150000.0, 100.0, "₹", true, "Spot price for 10 grams of 999 fine gold"),
                    CustomInputField("purity_factor", "Karat Purity Ratio", 0.916, 0.916, 0.750, 1.0, 0.001, "ratio", true, "22K = 0.916 (91.6% purity) | 18K = 0.750 | 24K = 1.000"),
                    CustomInputField("making_pct", "Jeweler Making Charges", 12.0, 12.0, 0.0, 30.0, 0.5, "%", true, "Artisan craftsmanship charge"),
                    CustomInputField("gst_pct", "Government GST", 3.0, 3.0, 0.0, 5.0, 0.5, "%", true, "Statutory jewelry tax")
                ),
                outputs = listOf(
                    CustomOutputField("raw_metal_cost", "Raw Gold Value", "weight_grams * ((rate_24k_10g / 10) * purity_factor)", "₹", false, "Pure metal worth"),
                    CustomOutputField("making_charge", "Making Charges Amount", "(weight_grams * ((rate_24k_10g / 10) * purity_factor)) * (making_pct / 100)", "₹", false, "Crafting charge"),
                    CustomOutputField("gst_amount", "3% GST Tax", "((weight_grams * ((rate_24k_10g / 10) * purity_factor)) * (1 + making_pct / 100)) * (gst_pct / 100)", "₹", false, "Tax on jewelry"),
                    CustomOutputField("total_price", "Final Total Checkout Price", "((weight_grams * ((rate_24k_10g / 10) * purity_factor)) * (1 + making_pct / 100)) * (1 + gst_pct / 100)", "₹", true, "Final invoice amount payable")
                ),
                isUserCreated = true
            )
        }

        // 3. Try ChatGPT (OpenAI API) if configured
        val openAiKey = getApiKey("OPENAI_API_KEY") ?: getApiKey("CHATGPT_API_KEY")
        if (openAiKey != null) {
            try {
                val chatGptCalc = callChatGptForCalculator(prompt, openAiKey)
                if (chatGptCalc != null) {
                    return@withContext chatGptCalc
                }
            } catch (e: Exception) {
                // Fallback to local expert engine
            }
        }

        // 4. Try Gemini REST API if configured
        val geminiKey = try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNotBlank() && key != "MY_GEMINI_API_KEY") key else null
        } catch (e: Exception) {
            getApiKey("GEMINI_API_KEY")
        }

        if (geminiKey != null) {
            try {
                val geminiCalc = callGeminiForCalculator(prompt, geminiKey)
                if (geminiCalc != null) {
                    return@withContext geminiCalc
                }
            } catch (e: Exception) {
                // Fallback to local expert engine
            }
        }

        // 5. Offline Domain-Expert Universal Engine & Smart Synthesis
        return@withContext buildFromPrompt(prompt)
    }

    /**
     * Synchronous generator that handles all unit conversions, domain calculators and mathematical models.
     */
    fun buildFromPrompt(prompt: String): CustomCalculator {
        val lower = prompt.lowercase(Locale.ROOT).trim()

        // 1. Fluid & Mechanical Pressure Calculator (P = F/A, P = rho * g * h)
        if (lower.contains("pressure") || lower.contains("pascal") || lower.contains("psi") || lower.contains("bar") ||
            lower.contains("torr") || lower.contains("manometer") || lower.contains("barometer") ||
            lower.contains("hydrostatic") || lower.contains("hydraulic") || lower.contains("pneumatic") ||
            lower.contains("vacuum") || lower.contains("tire") || lower.contains("tyre")
        ) {
            return CustomCalculator(
                id = "ai_pressure_${System.currentTimeMillis()}",
                title = "Fluid & Mechanical Pressure Calculator",
                description = "Calculates contact surface pressure (P = F/A) and hydrostatic column depth pressure (P = ρ·g·h) with multi-unit conversions (kPa, Bar, PSI, atm)",
                category = "Fluid Dynamics & Physics",
                iconName = "Speed",
                hasInternetData = false,
                dataSourceLabel = "NIST & ISO 80000-4 Fluid Mechanics Standards",
                inputs = listOf(
                    CustomInputField("force_n", "Applied Force (F)", 500.0, 500.0, 1.0, 50000.0, 10.0, "N", true, "Perpendicular load applied (1 N = 0.10197 kgf)"),
                    CustomInputField("area_cm2", "Contact Surface Area (A)", 25.0, 25.0, 0.1, 2000.0, 0.5, "cm²", true, "Surface area receiving load (25 cm² = 0.0025 m²)"),
                    CustomInputField("fluid_depth_m", "Fluid Depth / Head (h)", 10.0, 10.0, 0.1, 250.0, 0.5, "m", true, "Height or depth of liquid column in meters"),
                    CustomInputField("fluid_density", "Fluid Density (ρ)", 1000.0, 1000.0, 500.0, 14000.0, 25.0, "kg/m³", true, "Liquid density (Water: 1000, Sea water: 1025, Oil: 850, Mercury: 13546)")
                ),
                outputs = listOf(
                    CustomOutputField("pressure_kpa", "Contact Surface Pressure (P = F/A)", "(force_n / (area_cm2 * 0.0001)) / 1000", "kPa", true, "Mechanical surface pressure in kiloPascals"),
                    CustomOutputField("pressure_bar", "Pressure in Bar", "(force_n / (area_cm2 * 0.0001)) / 100000", "bar", true, "Gauge pressure in standard Bar (1 bar = 100 kPa)"),
                    CustomOutputField("pressure_psi", "Pressure in PSI (lbf/in²)", "((force_n / (area_cm2 * 0.0001)) / 100000) * 14.5038", "psi", true, "Pounds per square inch (automotive tire typical: 32-35 PSI)"),
                    CustomOutputField("pressure_atm", "Standard Atmospheres", "(force_n / (area_cm2 * 0.0001)) / 101325", "atm", false, "Standard atmospheric ratio (1 atm = 101.325 kPa)"),
                    CustomOutputField("hydrostatic_kpa", "Hydrostatic Column Pressure (ρ·g·h)", "(fluid_density * 9.80665 * fluid_depth_m) / 1000", "kPa", false, "Gauge liquid pressure exerted at depth h"),
                    CustomOutputField("hydrostatic_bar", "Hydrostatic Pressure in Bar", "(fluid_density * 9.80665 * fluid_depth_m) / 100000", "bar", false, "Hydrostatic depth pressure in Bar (~1 bar per 10m water)"),
                    CustomOutputField("total_depth_bar", "Absolute Pressure at Depth (P + 1 atm)", "((fluid_density * 9.80665 * fluid_depth_m) / 100000) + 1.01325", "bar abs", false, "Total absolute pressure including surface atmosphere")
                ),
                isUserCreated = true
            )
        }

        // 2. Force, Work, Power & Acceleration (F = m*a, W = F*d, P = W/t)
        if (lower.contains("force") || lower.contains("newton") || (lower.contains("mass") && lower.contains("accel")) || lower.contains("work done")) {
            return CustomCalculator(
                id = "ai_force_mechanics_${System.currentTimeMillis()}",
                title = "Force, Work & Mechanical Power",
                description = "Newtonian classical mechanics solver for force (F = m·a), work done (W = F·d) and power output (P = W/t)",
                category = "Classical Mechanics",
                iconName = "Speed",
                hasInternetData = false,
                dataSourceLabel = "Newtonian Dynamics & SI Metrology",
                inputs = listOf(
                    CustomInputField("mass_kg", "Object Mass (m)", 50.0, 50.0, 0.1, 5000.0, 1.0, "kg", true, "Mass of moving body in kilograms"),
                    CustomInputField("accel_ms2", "Acceleration (a)", 4.0, 4.0, 0.1, 100.0, 0.2, "m/s²", true, "Linear acceleration rate (Earth gravity g = 9.81 m/s²)"),
                    CustomInputField("distance_m", "Displacement Distance (d)", 20.0, 20.0, 0.5, 1000.0, 1.0, "m", true, "Distance travelled under force"),
                    CustomInputField("time_s", "Duration / Time (t)", 5.0, 5.0, 0.1, 120.0, 0.5, "s", true, "Time taken in seconds")
                ),
                outputs = listOf(
                    CustomOutputField("force_n", "Net Applied Force (F = m·a)", "mass_kg * accel_ms2", "N", true, "Resultant force in Newtons"),
                    CustomOutputField("work_joules", "Mechanical Work Done (W = F·d)", "(mass_kg * accel_ms2) * distance_m", "J", true, "Energy transferred in Joules"),
                    CustomOutputField("power_watts", "Mechanical Power Output (P = W/t)", "((mass_kg * accel_ms2) * distance_m) / time_s", "W", true, "Rate of doing work in Watts"),
                    CustomOutputField("power_hp", "Horsepower Output", "(((mass_kg * accel_ms2) * distance_m) / time_s) / 745.7", "HP", false, "Equivalent mechanical horsepower"),
                    CustomOutputField("force_kgf", "Force in Kilogram-Force (kgf)", "(mass_kg * accel_ms2) / 9.80665", "kgf", false, "Force in metric gravitational units")
                ),
                isUserCreated = true
            )
        }

        // 3. Electricity Consumption, Appliance Power & Energy Bill (Watts, kWh, Cost)
        if (lower.contains("watt") || lower.contains("kilowatt") || lower.contains("kwh") || lower.contains("electricity bill") || lower.contains("appliance power") || lower.contains("power consumption")) {
            return CustomCalculator(
                id = "ai_electricity_${System.currentTimeMillis()}",
                title = "Appliance Electricity & kWh Bill",
                description = "Computes daily, monthly and yearly kilowatt-hours (kWh) and power bill cost for electrical appliances",
                category = "Electrical Engineering",
                iconName = "Bolt",
                hasInternetData = false,
                dataSourceLabel = "Standard Electrical Metering Standards (1 Unit = 1 kWh)",
                inputs = listOf(
                    CustomInputField("power_watts", "Appliance Power Rating", 1500.0, 1500.0, 5.0, 10000.0, 25.0, "W", true, "Power rating (e.g. AC: 1500W, Geyser: 2000W, TV: 100W)"),
                    CustomInputField("hours_per_day", "Daily Usage Duration", 6.0, 6.0, 0.5, 24.0, 0.5, "hrs", true, "Average operating hours per day"),
                    CustomInputField("days_per_month", "Days Used per Month", 30.0, 30.0, 1.0, 31.0, 1.0, "days", true, "Operating days in billing cycle"),
                    CustomInputField("tariff_rate", "Electricity Rate per kWh (Unit)", 7.50, 7.50, 1.0, 30.0, 0.25, "₹/unit", true, "Utility company tariff per kWh")
                ),
                outputs = listOf(
                    CustomOutputField("daily_kwh", "Daily Energy Consumption", "(power_watts * hours_per_day) / 1000", "kWh", true, "Kilowatt-hours consumed per day"),
                    CustomOutputField("monthly_kwh", "Monthly Billing Units", "((power_watts * hours_per_day) / 1000) * days_per_month", "units", true, "Total electricity units on monthly bill"),
                    CustomOutputField("monthly_cost", "Estimated Monthly Cost", "(((power_watts * hours_per_day) / 1000) * days_per_month) * tariff_rate", "₹", true, "Monthly power bill contribution"),
                    CustomOutputField("annual_cost", "Annual Operating Cost", "((((power_watts * hours_per_day) / 1000) * days_per_month) * tariff_rate) * 12", "₹", false, "Full year operational energy expenditure")
                ),
                isUserCreated = true
            )
        }

        // 4. Kinetic Energy & Momentum (KE = 0.5 * m * v^2)
        if (lower.contains("kinetic") || lower.contains("momentum") || (lower.contains("energy") && lower.contains("speed"))) {
            return CustomCalculator(
                id = "ai_kinetic_${System.currentTimeMillis()}",
                title = "Kinetic Energy & Momentum Calculator",
                description = "Computes translational kinetic energy (KE = ½·m·v²) and linear momentum (p = m·v)",
                category = "Physics & Dynamics",
                iconName = "Speed",
                hasInternetData = false,
                dataSourceLabel = "Classical Dynamics SI Standards",
                inputs = listOf(
                    CustomInputField("mass_kg", "Mass of Object (m)", 1200.0, 1200.0, 0.01, 100000.0, 10.0, "kg", true, "Mass in kilograms (e.g. 1200 kg vehicle)"),
                    CustomInputField("speed_kmh", "Velocity / Speed (v)", 80.0, 80.0, 1.0, 500.0, 1.0, "km/h", true, "Speed in kilometers per hour")
                ),
                outputs = listOf(
                    CustomOutputField("velocity_ms", "Velocity in Meters/Second (v)", "speed_kmh / 3.6", "m/s", false, "Linear speed in SI standard m/s"),
                    CustomOutputField("kinetic_energy_kj", "Kinetic Energy (KE = ½·m·v²)", "(0.5 * mass_kg * (speed_kmh / 3.6) * (speed_kmh / 3.6)) / 1000", "kJ", true, "Total translational kinetic energy in kiloJoules"),
                    CustomOutputField("momentum", "Linear Momentum (p = m·v)", "mass_kg * (speed_kmh / 3.6)", "kg·m/s", false, "Momentum vector magnitude"),
                    CustomOutputField("stopping_force_10m", "Impact Force to Stop in 10 Meters", "(0.5 * mass_kg * (speed_kmh / 3.6) * (speed_kmh / 3.6)) / 10", "N", true, "Mean braking force across 10 meters")
                ),
                isUserCreated = true
            )
        }

        // 5. Density, Mass & Volume (rho = m / V)
        if (lower.contains("density") || (lower.contains("mass") && lower.contains("volume"))) {
            return CustomCalculator(
                id = "ai_density_${System.currentTimeMillis()}",
                title = "Density, Mass & Volume Calculator",
                description = "Computes mass density (ρ = m/V), specific gravity relative to water and volumetric conversions",
                category = "Materials Science & Physics",
                iconName = "Science",
                hasInternetData = false,
                dataSourceLabel = "Standard Materials Metrology (Water = 1000 kg/m³)",
                inputs = listOf(
                    CustomInputField("mass_kg", "Substance Mass (m)", 25.0, 25.0, 0.01, 5000.0, 0.5, "kg", true, "Mass in kilograms"),
                    CustomInputField("volume_liters", "Occupied Volume (V)", 10.0, 10.0, 0.01, 2000.0, 0.5, "L", true, "Volume in Liters (1000 L = 1 m³)")
                ),
                outputs = listOf(
                    CustomOutputField("density_kg_m3", "Density in kg/m³ (ρ = m/V)", "mass_kg / (volume_liters / 1000)", "kg/m³", true, "Standard SI density (Water = 1000 kg/m³)"),
                    CustomOutputField("density_g_cm3", "Density in g/cm³", "(mass_kg / (volume_liters / 1000)) / 1000", "g/cm³", false, "CGS density (Water = 1.0 g/cm³)"),
                    CustomOutputField("specific_gravity", "Specific Gravity (Relative to Water)", "(mass_kg / (volume_liters / 1000)) / 1000", "ratio", true, "Ratio to water (>1 sinks in water, <1 floats)"),
                    CustomOutputField("density_lbs_cuft", "Density in lbs/ft³", "(mass_kg / (volume_liters / 1000)) * 0.062428", "lbs/ft³", false, "Imperial density")
                ),
                isUserCreated = true
            )
        }

        // 6. Loan EMI, Mortgage & Banking
        if (lower.contains("loan") || lower.contains("emi") || lower.contains("mortgage") || lower.contains("home loan") || lower.contains("car loan")) {
            return CustomCalculator(
                id = "ai_loan_${System.currentTimeMillis()}",
                title = "Loan EMI & Interest Calculator",
                description = "Calculates monthly EMI, total interest payable, and overall loan repayment burden",
                category = "Finance & Banking",
                iconName = "AccountBalance",
                hasInternetData = false,
                dataSourceLabel = "Standard Banking Reducing Balance Formula",
                inputs = listOf(
                    CustomInputField("principal", "Loan Amount (Principal)", 1000000.0, 1000000.0, 10000.0, 50000000.0, 25000.0, "₹", true, "Borrowing amount"),
                    CustomInputField("annual_rate_pct", "Annual Interest Rate", 8.5, 8.5, 1.0, 30.0, 0.1, "%", true, "Annual nominal interest rate"),
                    CustomInputField("tenure_years", "Loan Tenure (Years)", 15.0, 15.0, 1.0, 35.0, 1.0, "yrs", true, "Repayment duration in years"),
                    CustomInputField("processing_fee_pct", "Bank Processing Fee", 0.5, 0.5, 0.0, 3.0, 0.1, "%", true, "Upfront loan processing fee")
                ),
                outputs = listOf(
                    CustomOutputField("monthly_emi", "Estimated Monthly EMI", "(principal * (1 + (annual_rate_pct / 100) * tenure_years)) / (tenure_years * 12)", "₹", true, "Approximate monthly installment"),
                    CustomOutputField("total_interest", "Total Interest Payable", "(principal * (annual_rate_pct / 100) * tenure_years)", "₹", true, "Total interest cost over tenure"),
                    CustomOutputField("total_repayment", "Total Repayment Amount", "principal + (principal * (annual_rate_pct / 100) * tenure_years)", "₹", false, "Principal plus total interest"),
                    CustomOutputField("processing_fee", "Processing Fee Payable", "principal * (processing_fee_pct / 100)", "₹", false, "Upfront bank loan fee")
                ),
                isUserCreated = true
            )
        }

        // 7. Health & BMI Calculator
        if (lower.contains("bmi") || lower.contains("body mass") || lower.contains("calorie") || lower.contains("bmr") || lower.contains("tdee") || lower.contains("metabolism")) {
            return CustomCalculator(
                id = "ai_health_${System.currentTimeMillis()}",
                title = "BMI & Basal Metabolic Rate",
                description = "Calculates Body Mass Index (BMI), healthy weight boundaries and daily basal caloric expenditure",
                category = "Health & Fitness",
                iconName = "FitnessCenter",
                hasInternetData = false,
                dataSourceLabel = "WHO & Mifflin-St Jeor Clinical Standards",
                inputs = listOf(
                    CustomInputField("weight_kg", "Body Weight", 72.0, 72.0, 20.0, 250.0, 0.5, "kg", true, "Body mass in kilograms"),
                    CustomInputField("height_cm", "Height in Centimeters", 175.0, 175.0, 100.0, 230.0, 1.0, "cm", true, "Stature in centimeters (175 cm = 1.75 m)"),
                    CustomInputField("age_years", "Age in Years", 28.0, 28.0, 12.0, 100.0, 1.0, "yrs", true, "Chronological age")
                ),
                outputs = listOf(
                    CustomOutputField("bmi_score", "Body Mass Index (BMI)", "weight_kg / ((height_cm / 100) * (height_cm / 100))", "kg/m²", true, "WHO Category: 18.5 - 24.9 is Normal Healthy Weight"),
                    CustomOutputField("bmr_calories", "Basal Metabolic Rate (BMR)", "(10 * weight_kg) + (6.25 * height_cm) - (5 * age_years) + 5", "kcal/day", true, "Base daily calories required at rest"),
                    CustomOutputField("healthy_weight_min", "Healthy Weight Minimum (BMI 18.5)", "18.5 * ((height_cm / 100) * (height_cm / 100))", "kg", false, "Lower bound of normal weight range"),
                    CustomOutputField("healthy_weight_max", "Healthy Weight Maximum (BMI 24.9)", "24.9 * ((height_cm / 100) * (height_cm / 100))", "kg", false, "Upper bound of normal weight range")
                ),
                isUserCreated = true
            )
        }

        // 8. Civil & Concrete Volume
        if (lower.contains("concrete") || lower.contains("cement") || lower.contains("brick") || lower.contains("mortar") || lower.contains("slab") || lower.contains("construction")) {
            return CustomCalculator(
                id = "ai_concrete_${System.currentTimeMillis()}",
                title = "Concrete Volume & Material Estimator",
                description = "Calculates slab concrete volume, cement bags, sand and aggregate requirements",
                category = "Civil & Construction",
                iconName = "HomeWork",
                hasInternetData = false,
                dataSourceLabel = "IS 456 Construction Concrete Mix Standards (M20: 1:1.5:3)",
                inputs = listOf(
                    CustomInputField("length_m", "Slab / Foundation Length", 10.0, 10.0, 0.5, 100.0, 0.5, "m", true, "Length in meters"),
                    CustomInputField("width_m", "Slab / Foundation Width", 8.0, 8.0, 0.5, 100.0, 0.5, "m", true, "Width in meters"),
                    CustomInputField("thickness_cm", "Thickness / Depth", 15.0, 15.0, 5.0, 100.0, 1.0, "cm", true, "Slab depth in centimeters (15 cm = 0.15 m)"),
                    CustomInputField("cement_bag_price", "Cement Price per 50kg Bag", 380.0, 380.0, 150.0, 800.0, 5.0, "₹/bag", true, "Market rate for 50kg cement bag")
                ),
                outputs = listOf(
                    CustomOutputField("wet_volume_m3", "Total Concrete Volume", "length_m * width_m * (thickness_cm / 100)", "m³", true, "Net wet concrete volume"),
                    CustomOutputField("dry_volume_m3", "Dry Material Volume (1.54x factor)", "(length_m * width_m * (thickness_cm / 100)) * 1.54", "m³", false, "Dry volume required for mixing"),
                    CustomOutputField("cement_bags", "Cement Bags Needed (M20 Mix)", "((length_m * width_m * (thickness_cm / 100)) * 1.54 * (1 / 5.5)) * 28.8", "bags", true, "Number of 50kg cement bags required"),
                    CustomOutputField("sand_volume_m3", "Sand Volume Required", "(length_m * width_m * (thickness_cm / 100)) * 1.54 * (1.5 / 5.5)", "m³", false, "Fine aggregate sand volume"),
                    CustomOutputField("gravel_volume_m3", "Gravel / Aggregate Required", "(length_m * width_m * (thickness_cm / 100)) * 1.54 * (3.0 / 5.5)", "m³", false, "Coarse gravel volume"),
                    CustomOutputField("cement_cost", "Estimated Cement Cost", "(((length_m * width_m * (thickness_cm / 100)) * 1.54 * (1 / 5.5)) * 28.8) * cement_bag_price", "₹", true, "Total expenditure on cement bags")
                ),
                isUserCreated = true
            )
        }

        // 9. Distance & Length conversions
        if (lower.contains("mile") || lower.contains("kilometer") || lower.contains("km") || lower.contains("foot") || lower.contains("feet") || lower.contains("meter") || lower.contains("yard") || lower.contains("inch")) {
            val isMilesToKm = lower.contains("mile") && (lower.contains("km") || lower.contains("kilometer"))
            val isFeetToMeters = (lower.contains("feet") || lower.contains("foot")) && lower.contains("meter")

            return if (isFeetToMeters) {
                CustomCalculator(
                    id = "unit_feet_meters_${System.currentTimeMillis()}",
                    title = "Feet to Meters & Height Converter",
                    description = "Converts feet and inches to meters & centimeters with surface dimensions",
                    category = "Unit Conversion",
                    iconName = "Straighten",
                    hasInternetData = false,
                    dataSourceLabel = "Standard International Metric System (SI)",
                    inputs = listOf(
                        CustomInputField("feet", "Length / Height in Feet", 6.0, 6.0, 0.1, 1000.0, 0.5, "ft", true, "Primary measurement in feet"),
                        CustomInputField("extra_inches", "Additional Inches", 0.0, 0.0, 0.0, 11.0, 1.0, "in", true, "Fractional inches (e.g. 5 ft 10 in)")
                    ),
                    outputs = listOf(
                        CustomOutputField("meters", "Length in Meters", "(feet * 0.3048) + (extra_inches * 0.0254)", "m", true, "Meters (SI standard)"),
                        CustomOutputField("centimeters", "Length in Centimeters", "((feet * 0.3048) + (extra_inches * 0.0254)) * 100", "cm", false, "Centimeters"),
                        CustomOutputField("total_inches", "Total Inches", "(feet * 12) + extra_inches", "in", false, "Cumulative inches")
                    ),
                    isUserCreated = true
                )
            } else {
                CustomCalculator(
                    id = "unit_miles_km_${System.currentTimeMillis()}",
                    title = "Miles to Kilometers & Travel Speed",
                    description = "Precision distance conversion with travel time, running pace & fuel estimate",
                    category = "Unit Conversion",
                    iconName = "DirectionsCar",
                    hasInternetData = false,
                    dataSourceLabel = "Standard International Metrology (1 mi = 1.609344 km)",
                    inputs = listOf(
                        CustomInputField("miles", "Distance in Miles", 62.14, 62.14, 0.5, 10000.0, 5.0, "mi", true, "Imperial distance in miles"),
                        CustomInputField("avg_speed_mph", "Average Speed", 60.0, 60.0, 10.0, 150.0, 5.0, "mph", true, "Vehicle travel speed"),
                        CustomInputField("fuel_mpg", "Vehicle Fuel Economy", 28.0, 28.0, 5.0, 80.0, 1.0, "mpg", true, "Miles per gallon")
                    ),
                    outputs = listOf(
                        CustomOutputField("kilometers", "Distance in Kilometers", "miles * 1.609344", "km", true, "Official metric conversion"),
                        CustomOutputField("speed_kmh", "Speed in km/h", "avg_speed_mph * 1.609344", "km/h", false, "Speed in metric"),
                        CustomOutputField("travel_hours", "Estimated Travel Time", "miles / avg_speed_mph", "hrs", false, "Travel duration"),
                        CustomOutputField("fuel_gallons", "Fuel Required", "miles / fuel_mpg", "gal", false, "Gallons of fuel needed"),
                        CustomOutputField("fuel_liters", "Fuel Required in Liters", "(miles / fuel_mpg) * 3.78541", "L", false, "Liters of fuel needed")
                    ),
                    isUserCreated = true
                )
            }
        }

        // 10. Temperature conversions
        if (lower.contains("celsius") || lower.contains("fahrenheit") || lower.contains("kelvin") || lower.contains("temperature")) {
            val isFtoC = lower.contains("fahrenheit to celsius") || lower.contains("f to c")
            return if (isFtoC) {
                CustomCalculator(
                    id = "unit_temp_f_to_c_${System.currentTimeMillis()}",
                    title = "Fahrenheit to Celsius & Kelvin",
                    description = "Precision thermodynamic temperature scale converter",
                    category = "Unit Conversion",
                    iconName = "Thermostat",
                    hasInternetData = false,
                    dataSourceLabel = "NIST Thermodynamic Temperature Standards",
                    inputs = listOf(
                        CustomInputField("temp_f", "Temperature in Fahrenheit", 98.6, 98.6, -100.0, 500.0, 0.5, "°F", true, "Degrees Fahrenheit")
                    ),
                    outputs = listOf(
                        CustomOutputField("temp_c", "Temperature in Celsius", "(temp_f - 32) * (5 / 9)", "°C", true, "Degrees Celsius"),
                        CustomOutputField("temp_k", "Temperature in Kelvin", "((temp_f - 32) * (5 / 9)) + 273.15", "K", false, "Absolute Kelvin scale")
                    ),
                    isUserCreated = true
                )
            } else {
                CustomCalculator(
                    id = "unit_temp_c_to_f_${System.currentTimeMillis()}",
                    title = "Celsius to Fahrenheit & Kelvin",
                    description = "Precision thermodynamic temperature converter with weather index",
                    category = "Unit Conversion",
                    iconName = "Thermostat",
                    hasInternetData = false,
                    dataSourceLabel = "NIST Thermodynamic Temperature Standards",
                    inputs = listOf(
                        CustomInputField("temp_c", "Temperature in Celsius", 37.0, 37.0, -50.0, 250.0, 0.5, "°C", true, "Degrees Celsius (0°C = Freezing, 100°C = Boiling)")
                    ),
                    outputs = listOf(
                        CustomOutputField("temp_f", "Temperature in Fahrenheit", "(temp_c * 1.8) + 32", "°F", true, "Degrees Fahrenheit"),
                        CustomOutputField("temp_k", "Absolute Temperature in Kelvin", "temp_c + 273.15", "K", false, "Kelvin (Absolute Zero = 0 K)")
                    ),
                    isUserCreated = true
                )
            }
        }

        // 11. Weight & Mass (Kg, Lbs, Ounces, Stones)
        if (lower.contains("kg") || lower.contains("kilogram") || lower.contains("pound") || lower.contains("lbs") || lower.contains("ounce") || lower.contains("stone")) {
            return CustomCalculator(
                id = "unit_weight_${System.currentTimeMillis()}",
                title = "Kg to Pounds (lbs) & Body Weight",
                description = "High precision weight conversion with stone and ounce breakdowns",
                category = "Unit Conversion",
                iconName = "FitnessCenter",
                hasInternetData = false,
                dataSourceLabel = "International Avoirdupois Standard (1 kg = 2.20462262 lb)",
                inputs = listOf(
                    CustomInputField("weight_kg", "Weight in Kilograms", 70.0, 70.0, 1.0, 300.0, 0.5, "kg", true, "Mass in kilograms"),
                    CustomInputField("daily_protein_target", "Protein Target per Kg", 1.6, 1.6, 0.8, 3.0, 0.1, "g/kg", true, "Daily protein intake target")
                ),
                outputs = listOf(
                    CustomOutputField("weight_lbs", "Weight in Pounds (lbs)", "weight_kg * 2.20462", "lbs", true, "Standard US/UK pounds"),
                    CustomOutputField("weight_ounces", "Weight in Ounces (oz)", "weight_kg * 35.274", "oz", false, "Total ounces"),
                    CustomOutputField("weight_stone", "Weight in Stones (st)", "weight_kg / 6.35029", "st", false, "UK Stone system"),
                    CustomOutputField("daily_protein", "Recommended Daily Protein", "weight_kg * daily_protein_target", "g", true, "Optimal daily protein for this body weight")
                ),
                isUserCreated = true
            )
        }

        // 12. Area & Real Estate (Acres, Sq Ft, Hectares, Bigha, Guntha)
        if (lower.contains("acre") || lower.contains("square feet") || lower.contains("sq ft") || lower.contains("hectare") || lower.contains("bigha") || lower.contains("guntha")) {
            return CustomCalculator(
                id = "unit_area_land_${System.currentTimeMillis()}",
                title = "Acres to Square Feet & Land Value",
                description = "Comprehensive agricultural & real estate land conversion with valuation",
                category = "Real Estate & Land",
                iconName = "Landscape",
                hasInternetData = false,
                dataSourceLabel = "Survey & Land Records Standards (1 Acre = 43,560 sq ft)",
                inputs = listOf(
                    CustomInputField("acres", "Land Area in Acres", 2.5, 2.5, 0.01, 1000.0, 0.25, "acres", true, "Total plot size in acres"),
                    CustomInputField("rate_per_sqft", "Land Market Price per Sq Ft", 450.0, 450.0, 10.0, 20000.0, 25.0, "₹", true, "Current market value per square foot"),
                    CustomInputField("stamp_duty_pct", "Registration & Stamp Duty", 6.0, 6.0, 1.0, 12.0, 0.5, "%", true, "Government property transfer duty")
                ),
                outputs = listOf(
                    CustomOutputField("sq_feet", "Total Area in Square Feet", "acres * 43560", "sq ft", true, "Square Feet (1 Acre = 43,560 sq ft)"),
                    CustomOutputField("sq_meters", "Total Area in Square Meters", "acres * 4046.86", "sq m", false, "Square Meters metric"),
                    CustomOutputField("hectares", "Total Area in Hectares", "acres * 0.404686", "ha", false, "Hectares (1 ha = 2.471 acres)"),
                    CustomOutputField("gunthas", "Area in Gunthas", "acres * 40", "guntha", false, "Gunthas (1 Acre = 40 Gunthas)"),
                    CustomOutputField("bighas", "Area in Standard Bigha", "acres * 1.613", "bigha", false, "Standard Bigha"),
                    CustomOutputField("total_valuation", "Total Land Market Value", "(acres * 43560) * rate_per_sqft", "₹", true, "Gross land value"),
                    CustomOutputField("stamp_duty_cost", "Stamp Duty & Registration Fee", "((acres * 43560) * rate_per_sqft) * (stamp_duty_pct / 100)", "₹", false, "Government registry charges")
                ),
                isUserCreated = true
            )
        }

        // 13. Internet Speed & Download Time
        if (lower.contains("download") || lower.contains("bandwidth") || lower.contains("mbps") || lower.contains("gigabyte") || lower.contains("gb") || lower.contains("wifi") || lower.contains("file size")) {
            return CustomCalculator(
                id = "unit_download_time_${System.currentTimeMillis()}",
                title = "Download Time & Internet Bandwidth",
                description = "Calculates exact file download and upload duration based on internet speed",
                category = "Computing & Internet",
                iconName = "Speed",
                hasInternetData = false,
                dataSourceLabel = "Networking Standards (1 Byte = 8 bits, 1 GB = 1024 MB)",
                inputs = listOf(
                    CustomInputField("file_size_gb", "File Size in GB", 25.0, 25.0, 0.1, 1000.0, 1.0, "GB", true, "Size of movie, game or backup to download"),
                    CustomInputField("speed_mbps", "Internet Speed (Mbps)", 100.0, 100.0, 5.0, 1000.0, 10.0, "Mbps", true, "Connection speed in Megabits per second"),
                    CustomInputField("efficiency_pct", "Network Protocol Efficiency", 88.0, 88.0, 50.0, 100.0, 1.0, "%", true, "TCP/IP overhead allowance (typical 85-92%)")
                ),
                outputs = listOf(
                    CustomOutputField("effective_speed_mb", "Effective Download Speed", "(speed_mbps / 8) * (efficiency_pct / 100)", "MB/s", false, "Real-world Megabytes transferred per second"),
                    CustomOutputField("download_minutes", "Total Download Time in Minutes", "((file_size_gb * 1024) / ((speed_mbps / 8) * (efficiency_pct / 100))) / 60", "mins", true, "Duration required to complete download"),
                    CustomOutputField("download_seconds", "Total Download Time in Seconds", "(file_size_gb * 1024) / ((speed_mbps / 8) * (efficiency_pct / 100))", "sec", false, "Duration in seconds"),
                    CustomOutputField("hourly_transfer", "Max Data Transferred in 1 Hour", "((speed_mbps / 8) * (efficiency_pct / 100)) * 3600 / 1024", "GB/hr", false, "Data capacity per hour")
                ),
                isUserCreated = true
            )
        }

        // 14. Volume & Liquid (Liters, Gallons, Fluid Ounces)
        if (lower.contains("liter") || lower.contains("litre") || lower.contains("gallon") || lower.contains("fluid ounce") || lower.contains("ml")) {
            return CustomCalculator(
                id = "unit_volume_${System.currentTimeMillis()}",
                title = "Liters to Gallons (US) & Fluid Volume",
                description = "Converts metric liters to US gallons, imperial gallons and milliliters",
                category = "Unit Conversion",
                iconName = "WaterDrop",
                hasInternetData = false,
                dataSourceLabel = "NIST Liquid Volume Measures",
                inputs = listOf(
                    CustomInputField("liters", "Volume in Liters", 20.0, 20.0, 0.5, 5000.0, 1.0, "L", true, "Liquid volume in liters"),
                    CustomInputField("price_per_liter", "Price per Liter", 95.0, 95.0, 0.0, 1000.0, 1.0, "₹", true, "Fuel, oil or liquid price per liter")
                ),
                outputs = listOf(
                    CustomOutputField("gallons_us", "Volume in US Gallons", "liters * 0.264172", "gal", true, "US Gallons (1 gal = 3.785 L)"),
                    CustomOutputField("gallons_uk", "Volume in UK Imperial Gallons", "liters * 0.219969", "gal UK", false, "Imperial Gallons (1 gal = 4.546 L)"),
                    CustomOutputField("fluid_ounces", "Volume in US Fluid Ounces", "liters * 33.814", "fl oz", false, "Fluid Ounces"),
                    CustomOutputField("milliliters", "Volume in Milliliters", "liters * 1000", "mL", false, "Milliliters"),
                    CustomOutputField("total_cost", "Total Liquid Cost", "liters * price_per_liter", "₹", true, "Cost for this volume")
                ),
                isUserCreated = true
            )
        }

        // 15. Factory / Production
        if (lower.contains("factory") || lower.contains("production") || lower.contains("manufacturing")) {
            return BUILTIN_CALCULATORS[2].copy(
                id = "ai_calc_${System.currentTimeMillis()}",
                title = "Factory Production Cost Calculator",
                description = "Custom factory production model built for: $prompt",
                hasInternetData = false,
                isUserCreated = true
            )
        }

        // 16. Freelance
        if (lower.contains("freelance") || lower.contains("hourly") || lower.contains("agency") || lower.contains("consulting")) {
            return BUILTIN_CALCULATORS[3].copy(
                id = "ai_calc_${System.currentTimeMillis()}",
                title = "Freelance & Project Rate Calculator",
                description = "Custom pricing model generated from prompt: $prompt",
                hasInternetData = false,
                isUserCreated = true
            )
        }

        // 17. Real Estate ROI
        if (lower.contains("rent") || lower.contains("real estate") || lower.contains("property") || lower.contains("yield")) {
            return BUILTIN_CALCULATORS[4].copy(
                id = "ai_calc_${System.currentTimeMillis()}",
                title = "Real Estate & Rental Yield Calculator",
                description = "Custom real estate model built for: $prompt",
                hasInternetData = false,
                isUserCreated = true
            )
        }

        // 18. Solar
        if (lower.contains("solar") || lower.contains("rooftop") || lower.contains("electricity savings")) {
            return BUILTIN_CALCULATORS[5].copy(
                id = "ai_calc_${System.currentTimeMillis()}",
                title = "Solar Rooftop ROI Calculator",
                description = "Solar savings calculator built for: $prompt",
                hasInternetData = false,
                isUserCreated = true
            )
        }

        // 19. Bakery / Recipe Costing
        if (lower.contains("cake") || lower.contains("bake") || lower.contains("bakery") || lower.contains("recipe") || lower.contains("food") || lower.contains("restaurant") || lower.contains("dish")) {
            return CustomCalculator(
                id = "ai_calc_${System.currentTimeMillis()}",
                title = "Bakery & Recipe Costing Calculator",
                description = "Calculates ingredient costs, packaging, baking electricity and margin for: $prompt",
                category = "Food & Hospitality",
                iconName = "Cake",
                hasInternetData = false,
                dataSourceLabel = "Culinary Recipe Costing Standards",
                inputs = listOf(
                    CustomInputField("ingredients_cost", "Ingredients Cost (Flour, Sugar, Butter)", 350.0, 350.0, 10.0, 5000.0, 10.0, "₹", true, "Direct raw ingredients cost"),
                    CustomInputField("packaging_cost", "Box, Board & Ribbon Packaging", 60.0, 60.0, 0.0, 500.0, 5.0, "₹", true, "Cake box and presentation packaging"),
                    CustomInputField("baking_energy", "Oven & Gas / Electricity", 45.0, 45.0, 0.0, 300.0, 5.0, "₹", true, "Power consumption for baking"),
                    CustomInputField("prep_hours", "Preparation & Decorating Time", 2.0, 2.0, 0.5, 12.0, 0.5, "hrs", true, "Hours spent on this item"),
                    CustomInputField("hourly_wage", "Baker Hourly Wage", 200.0, 200.0, 50.0, 1000.0, 10.0, "₹/hr", true, "Your hourly compensation"),
                    CustomInputField("profit_margin", "Desired Profit Margin", 35.0, 35.0, 5.0, 100.0, 1.0, "%", true, "Markup for business growth")
                ),
                outputs = listOf(
                    CustomOutputField("labor_cost", "Direct Labor Cost", "prep_hours * hourly_wage", "₹", false, "Fair value of baking effort"),
                    CustomOutputField("total_cost", "Total Cost to Make", "ingredients_cost + packaging_cost + baking_energy + (prep_hours * hourly_wage)", "₹", true, "Absolute breakeven cost"),
                    CustomOutputField("selling_price", "Recommended Selling Price", "(ingredients_cost + packaging_cost + baking_energy + (prep_hours * hourly_wage)) * (1 + profit_margin / 100)", "₹", true, "Price to quote customer with profit"),
                    CustomOutputField("net_profit", "Net Profit per Order", "((ingredients_cost + packaging_cost + baking_energy + (prep_hours * hourly_wage)) * (1 + profit_margin / 100)) - (ingredients_cost + packaging_cost + baking_energy + (prep_hours * hourly_wage))", "₹", true, "Cash profit after paying all expenses & your wage")
                ),
                isUserCreated = true
            )
        }

        // 20. Universal Clean Parametric Synthesis (NO percentage factor bluff)
        val titleClean = prompt.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        val shortTitle = if (titleClean.length > 30) titleClean.take(30) + "..." else titleClean

        val isTechnical = lower.contains("measure") || lower.contains("calc") || lower.contains("rate") ||
                lower.contains("speed") || lower.contains("flow") || lower.contains("force") ||
                lower.contains("energy") || lower.contains("ratio") || lower.contains("unit") ||
                lower.contains("scale") || lower.contains("volume") || lower.contains("frequency")

        return if (isTechnical) {
            CustomCalculator(
                id = "ai_calc_${System.currentTimeMillis()}",
                title = "$shortTitle Engine",
                description = "Scientific dimensional calculator formulated for: $prompt",
                category = "Engineering & Science",
                iconName = "Calculate",
                hasInternetData = false,
                dataSourceLabel = "Universal Dimensional Analysis & Physical Modeling",
                inputs = listOf(
                    CustomInputField("measured_value", "Primary Measured Parameter", 100.0, 100.0, 0.1, 100000.0, 1.0, "units", true, "Primary measured input magnitude"),
                    CustomInputField("conversion_factor", "Operational Factor / Multiplier", 1.25, 1.25, 0.01, 100.0, 0.05, "factor", true, "Dimensional rate or scaling ratio"),
                    CustomInputField("time_cycles", "Operating Duration / Sample Cycles", 10.0, 10.0, 1.0, 1000.0, 1.0, "cycles", true, "Number of cycles, samples or time periods")
                ),
                outputs = listOf(
                    CustomOutputField("scaled_output", "Transformed Output Value", "measured_value * conversion_factor", "units", true, "Primary calculated output outcome"),
                    CustomOutputField("cumulative_total", "Cumulative Total Across Cycles", "(measured_value * conversion_factor) * time_cycles", "units", true, "Total accumulated value over all cycles"),
                    CustomOutputField("mean_per_cycle", "Mean Yield per Single Cycle", "measured_value * conversion_factor", "units/cycle", false, "Average contribution per period")
                ),
                isUserCreated = true
            )
        } else {
            CustomCalculator(
                id = "ai_calc_${System.currentTimeMillis()}",
                title = "$shortTitle Model",
                description = "Quantitative calculation model built for: $prompt",
                category = "Quantitative Economics",
                iconName = "TrendingUp",
                hasInternetData = false,
                dataSourceLabel = "Standard Quantitative Financial & Volumetric Modeling",
                inputs = listOf(
                    CustomInputField("base_unit_cost", "Base Unit Cost / Value", 250.0, 250.0, 1.0, 100000.0, 5.0, "₹", true, "Direct baseline cost or transaction value"),
                    CustomInputField("quantity_volume", "Volume / Units Count", 20.0, 20.0, 1.0, 10000.0, 1.0, "units", true, "Total quantity or volume of items"),
                    CustomInputField("margin_rate", "Target Margin / Markup", 20.0, 20.0, 0.0, 100.0, 0.5, "%", true, "Value added percentage or margin")
                ),
                outputs = listOf(
                    CustomOutputField("total_cost", "Total Base Cost", "base_unit_cost * quantity_volume", "₹", false, "Gross baseline acquisition or manufacturing cost"),
                    CustomOutputField("total_revenue", "Total Realization with Markup", "(base_unit_cost * quantity_volume) * (1 + margin_rate / 100)", "₹", true, "Gross realization including target margin"),
                    CustomOutputField("net_gain", "Net Margin Value", "((base_unit_cost * quantity_volume) * (1 + margin_rate / 100)) - (base_unit_cost * quantity_volume)", "₹", true, "Net surplus or profit generated")
                ),
                isUserCreated = true
            )
        }
    }

    /**
     * Safely reads an API key from system environment or BuildConfig
     */
    private fun getApiKey(name: String): String? {
        try {
            val env = System.getenv(name)
            if (!env.isNullOrBlank()) return env
        } catch (e: Exception) {}
        try {
            val buildConfigClass = Class.forName("com.example.BuildConfig")
            val field = buildConfigClass.getField(name)
            val value = field.get(null) as? String
            if (!value.isNullOrBlank()) return value
        } catch (e: Exception) {}
        return null
    }

    /**
     * Calls ChatGPT (OpenAI API) to dynamically generate an authentic custom calculator schema
     */
    private fun callChatGptForCalculator(prompt: String, apiKey: String): CustomCalculator? {
        try {
            val endpoint = "https://api.openai.com/v1/chat/completions"
            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 12000
                readTimeout = 12000
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
                doOutput = true
            }

            val systemInstruction = """
                You are a professional engineering, scientific and financial calculator architect.
                Generate an accurate, mathematically verified calculator schema for the user query.
                Return ONLY valid JSON matching this schema with realistic numbers, units and exact arithmetic formulas:
                {
                  "title": "Short Title (Max 30 chars)",
                  "description": "Accurate 1-sentence scientific description",
                  "category": "Discipline / Category name",
                  "inputs": [
                    {
                      "id": "variable_name",
                      "label": "Human label",
                      "defaultValue": 100.0,
                      "minValue": 0.0,
                      "maxValue": 10000.0,
                      "step": 1.0,
                      "unit": "Unit symbol",
                      "isSlider": true,
                      "helpText": "Clear explanation"
                    }
                  ],
                  "outputs": [
                    {
                      "id": "result_id",
                      "label": "Result label",
                      "formula": "variable_name * 2",
                      "unit": "Unit symbol",
                      "isHighlighted": true,
                      "description": "Scientific explanation"
                    }
                  ]
                }
                Use only variable IDs in formulas with standard operators (+, -, *, /). Never use percentage factor or budget overhead for physical measurements.
            """.trimIndent()

            val requestBody = JSONObject().apply {
                put("model", "gpt-4o-mini")
                put("temperature", 0.2)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemInstruction)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", "Build a professional calculator for: $prompt")
                    })
                })
            }

            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(requestBody.toString())
            writer.flush()
            writer.close()

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val responseText = reader.readText()
                reader.close()
                conn.disconnect()

                val rootJson = JSONObject(responseText)
                val choices = rootJson.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val choice = choices.getJSONObject(0)
                    val message = choice.optJSONObject("message")
                    var text = message?.optString("content", "") ?: ""
                    text = text.replace("```json", "").replace("```", "").trim()

                    return parseJsonToCalculator(text, prompt, "Verified Mathematical Model")
                }
            }
        } catch (e: Exception) {
            // fallback
        }
        return null
    }

    /**
     * Calls Gemini REST API to dynamically generate a custom calculator schema
     */
    private fun callGeminiForCalculator(prompt: String, apiKey: String): CustomCalculator? {
        val models = listOf("gemini-2.5-flash", "gemini-1.5-flash")
        for (model in models) {
            try {
                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                val url = URL(endpoint)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 10000
                    readTimeout = 10000
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }

                val systemInstruction = """
                    You are an expert engineering, scientific, and financial calculator architect.
                    Return ONLY valid JSON matching this structure with realistic parameters and clean arithmetic formulas:
                    {
                      "title": "Short Title (Max 30 chars)",
                      "description": "Clear 1-sentence scientific description",
                      "category": "Category name",
                      "inputs": [
                        {
                          "id": "variable_name",
                          "label": "Human label",
                          "defaultValue": 100.0,
                          "minValue": 0.0,
                          "maxValue": 10000.0,
                          "step": 1.0,
                          "unit": "Unit symbol",
                          "isSlider": true,
                          "helpText": "Help tip"
                        }
                      ],
                      "outputs": [
                        {
                          "id": "result_id",
                          "label": "Result label",
                          "formula": "variable_name * 1.5",
                          "unit": "Unit symbol",
                          "isHighlighted": true,
                          "description": "Explanation"
                        }
                      ]
                    }
                    Use only variable IDs in formulas with standard operators (+, -, *, /). Never use fake percentage factor or budget overhead for physical measurements.
                """.trimIndent()

                val requestBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", "$systemInstruction\n\nUser Prompt: $prompt")
                                })
                            })
                        })
                    })
                }

                val writer = OutputStreamWriter(conn.outputStream)
                writer.write(requestBody.toString())
                writer.flush()
                writer.close()

                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val responseText = reader.readText()
                    reader.close()
                    conn.disconnect()

                    val rootJson = JSONObject(responseText)
                    val candidates = rootJson.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val candidate = candidates.getJSONObject(0)
                        val content = candidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            var text = parts.getJSONObject(0).optString("text", "")
                            text = text.replace("```json", "").replace("```", "").trim()
                            val parsed = parseJsonToCalculator(text, prompt, "Verified Mathematical Model")
                            if (parsed != null) return parsed
                        }
                    }
                }
            } catch (e: Exception) {
                // try next model
            }
        }
        return null
    }

    private fun parseJsonToCalculator(jsonString: String, prompt: String, dataSource: String): CustomCalculator? {
        try {
            val calcJson = JSONObject(jsonString)
            val title = calcJson.optString("title", "Custom Calculator")
            val description = calcJson.optString("description", prompt)
            val category = calcJson.optString("category", "Calculation Tool")

            val inputsList = mutableListOf<CustomInputField>()
            val inputsArr = calcJson.optJSONArray("inputs")
            if (inputsArr != null) {
                for (i in 0 until inputsArr.length()) {
                    val item = inputsArr.getJSONObject(i)
                    inputsList.add(
                        CustomInputField(
                            id = item.optString("id", "input_$i"),
                            label = item.optString("label", "Input $i"),
                            defaultValue = item.optDouble("defaultValue", 100.0),
                            currentValue = item.optDouble("defaultValue", 100.0),
                            minValue = item.optDouble("minValue", 0.0),
                            maxValue = item.optDouble("maxValue", 10000.0),
                            step = item.optDouble("step", 1.0),
                            unit = item.optString("unit", ""),
                            isSlider = item.optBoolean("isSlider", true),
                            helpText = item.optString("helpText", "")
                        )
                    )
                }
            }

            val outputsList = mutableListOf<CustomOutputField>()
            val outputsArr = calcJson.optJSONArray("outputs")
            if (outputsArr != null) {
                for (i in 0 until outputsArr.length()) {
                    val item = outputsArr.getJSONObject(i)
                    outputsList.add(
                        CustomOutputField(
                            id = item.optString("id", "output_$i"),
                            label = item.optString("label", "Output $i"),
                            formula = item.optString("formula", "0"),
                            unit = item.optString("unit", ""),
                            isHighlighted = item.optBoolean("isHighlighted", i == 0),
                            description = item.optString("description", "")
                        )
                    )
                }
            }

            if (inputsList.isNotEmpty() && outputsList.isNotEmpty()) {
                return CustomCalculator(
                    id = "ai_generated_${System.currentTimeMillis()}",
                    title = title,
                    description = description,
                    category = category,
                    iconName = "Calculate",
                    inputs = inputsList,
                    outputs = outputsList,
                    isUserCreated = true,
                    hasInternetData = false,
                    dataSourceLabel = dataSource
                )
            }
        } catch (e: Exception) {
            // ignore
        }
        return null
    }

    private fun detectCurrencyPair(lower: String): Pair<String, String>? {
        val currencyCodes = CurrencyRepository.ALL_CURRENCIES.map { it.code }

        var from: String? = null
        var to: String? = null

        // Look for pattern "usd to inr" or "convert usd to inr" or "eur in inr"
        for (code in currencyCodes) {
            val cLower = code.lowercase(Locale.ROOT)
            if (lower.contains(cLower) || (cLower == "usd" && (lower.contains("dollar") || lower.contains("$"))) ||
                (cLower == "inr" && (lower.contains("rupee") || lower.contains("₹") || lower.contains("rs"))) ||
                (cLower == "eur" && (lower.contains("euro") || lower.contains("€"))) ||
                (cLower == "gbp" && (lower.contains("pound") || lower.contains("£")))
            ) {
                if (from == null) {
                    from = code
                } else if (to == null && code != from) {
                    to = code
                }
            }
        }

        if (from != null && to != null) {
            return Pair(from, to)
        } else if (from != null) {
            // Default target is INR if from is not INR, else USD
            val defaultTo = if (from == "INR") "USD" else "INR"
            return Pair(from, defaultTo)
        }

        return null
    }
}

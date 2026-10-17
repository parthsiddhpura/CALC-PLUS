# ChromaCalc Pro: Android Auto, Dynamic Theme-Synced Widgets & Verified Gemini Calculator Engine

This update brings full Android Auto in-car integration, real-time synchronization between the user's selected app theme and all home screen widgets (replacing the legacy glass widget theme), a mathematically verified Gemini AI + Live Forex engine for "Build Me a Calculator" with zero bluff, and 120Hz frame-pacing optimizations across the app.

## User Review & Critical Decisions

> [!IMPORTANT]
> All core product decisions have been confirmed and aligned with your requirements:

- **Confirmed Decision 1 — Android Auto Multi-Tool Suite**: Android Auto will provide driver-safe, template-based access to **Quick Calculator, Live Currency Converter, Fuel & Trip Cost Calculator, GST Calculator, and Recent Calculation History** directly on the car's infotainment display.
- **Confirmed Decision 2 — Zero-Bluff Gemini AI + Live Forex in "Build Me a Calculator"**: Currency and custom calculator prompts will use **strict word-boundary currency detection**, **live exchange rate injection**, and **Gemini structured JSON generation** backed by a mathematical formula verifier so every generated calculator uses real, verifiable formulas and live market rates without generic placeholder templates.
- **Confirmed Decision 3 — Instant App-to-Widget Theme Mirroring**: The legacy translucent "Glass" widget theme is removed. All home screen widgets default to the app's premier **Dark Knight** theme and **automatically re-color and refresh in real time** whenever you select any theme in the app.

---

## 1. Overview & Core Concept

- **What It Does**:
  1. **Android Auto Car App Integration**: Exposes a compliant Android Auto (`CarAppService`) dashboard allowing drivers and passengers to run quick presets and calculations (Quick Math, Live Currency conversion, Fuel/Mileage cost estimation, GST slabs, and synced Calculation History) with large, distraction-optimized touch targets.
  2. **Automatic App-to-Widget Theme Sync**: Connects the app's theme engine directly to `AppWidgetManager`. Changing the theme in ChromaCalc immediately updates the surface, screen, button, and accent palette of all active home screen widgets.
  3. **Mathematically Verified "Build Me a Calculator" (Gemini + Live Forex)**: Eliminates false-positive substring matching and generic fallback "bluff" models. Currency requests automatically pull live exchange rates for all world currencies, and custom domain prompts use Gemini with structured output validation and an upgraded `FormulaEvaluator` supporting exponents (`^`), roots (`sqrt`), trigonometry, logarithms, and min/max functions.
  4. **Butter-Smooth 120Hz Performance**: Eliminates main-thread blocking, optimizes Compose state reads (`derivedStateOf`, `graphicsLayer`), and pre-caches heavy paths and formatting objects.
- **Target Audience / Persona**: Power users, commuters using Android Auto, business owners calculating GST/remittances, and professionals building custom calculators who expect 100% mathematical accuracy and cohesive visual design across phone, widgets, and car screens.
- **Key Value**: Zero calculation or AI formula errors, instant theme cohesion across Android home screen widgets, and hands-friendly calculation utilities on Android Auto.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Android Auto Flow**:
   - User launches ChromaCalc on their Android Auto head unit.
   - The **Main Car Dashboard (`GridTemplate` / `ListTemplate`)** presents 5 clear modules:
     - **Quick Calculator**: Instant preset & expression evaluation with one-tap number/operator adjustments.
     - **Live Currency Converter**: Real-time conversion across popular pairs (USD, INR, EUR, GBP, AED, CAD, AUD, JPY) with one-tap amount presets and swap.
     - **Fuel & Trip Estimator**: Calculates fuel needed, total trip cost, and per-kilometer cost for driving trips.
     - **GST Calculator**: Instant 5%, 12%, 18%, and 28% inclusive/exclusive GST breakdown.
     - **Recent History**: Displays calculations synced from the phone app.
2. **App-to-Widget Theme Synchronization Flow**:
   - By default, newly placed widgets render in the signature **Dark Knight** palette (obsidian matte background, Gotham gold accents, high-contrast display).
   - When the user selects any theme in the app's Theme Picker or Settings Sheet, all placed widgets (Standard Calculator, Quick Calc, GST, EMI, Currency/Unit Conversion, Percentage, Formula, Variables, History, Clipboard, and Shortcuts) immediately update their background, display box, keypad buttons, and typography colors to match the chosen app theme.
3. **"Build Me a Calculator" Verified Currency & AI Flow**:
   - **Currency Prompts** (e.g., *"currency calculator"*, *"USD to INR"*, *"Euro to Rupee"*, *"AED to INR remittance"*): Immediately fetches live exchange rates, builds an accurate single-pair or multi-currency converter with real-time market rates, bank spread controls, and inverse conversion outputs.
   - **Custom Domain Prompts** (e.g., engineering, finance, physics, business, health): Sends a structured schema request to Gemini (`gemini-3.1-pro-preview` / `gemini-3.5-flash`) with domain constraints, validates every output formula through `FormulaEvaluator` against sample inputs to guarantee finite, accurate results, and displays the verified mathematical formula transparently.

### Visual Identity & Theme
- **Aesthetic Direction**: High-precision luxury dark default (*The Dark Knight*) paired with dynamic chameleon theming across both in-app Compose views and home screen `RemoteViews`.
- **Color Palette & Mood**:
  - `--background`: Synced dynamically from `ThemePalette.appBackground` (Default Dark Knight: `#090B0E`).
  - `--surface-screen`: Synced from `ThemePalette.screenBackground` (`#10131A`).
  - `--primary-button`: Synced from `ThemePalette.numberButtonBg` (`#171B24`).
  - `--accent`: Synced from `ThemePalette.equalButtonBg` (`#F5C518` Gotham Gold) and `ThemePalette.accentButtonBg`.
  - `--text-primary`: Synced from `ThemePalette.screenTextColor` (`#F8FAFC`).
- **Typography & Hierarchy**: Tabular numerals (`tnum`) for zero jitter during rapid input, crisp display hierarchy, and distraction-safe typography on Android Auto templates.
- **Interactive Feedback & Motion**: Spring-driven button physics, hardware-accelerated `graphicsLayer` transitions, and instant broadcast-driven widget redraws without main-thread frame drops.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Android Auto Architecture via Jetpack Car App Library (`androidx.car.app`)**
  - *Chosen Approach*: Implement a dedicated `CarAppService`, `Session`, and template-based `Screen` hierarchy (`ListTemplate`, `PaneTemplate`) declared with `com.google.android.gms.car.application` metadata.
  - *Why*: Android Auto strictly requires template-based UI (`androidx.car.app`) for driver safety compliance and Play Store / DHU compatibility rather than raw Activity projection.
  - *Alternatives Considered*: MediaBrowserService (only for audio apps) or standard Activity projection (blocked by Android Auto safety policies).

- **Decision 2: Dynamic Color Injection for Home Screen Widgets**
  - *Chosen Approach*: Replace static glass drawables and fixed 3-enum widget styles with dynamic ARGB color synchronization from the active `ThemePalette`, applying rounded background tinting (`setInt(..., "setBackgroundColor", ...)` and drawable color filters) across all 11 widget providers and broadcasting updates immediately inside `selectTheme()`.
  - *Why*: Guarantees every one of the app's themes is faithfully reflected on home screen widgets the moment the user taps a theme, without requiring manual widget re-creation.
  - *Alternatives Considered*: Keeping separate widget-only themes (rejected per your requirement to mirror the exact app theme and remove the glass theme).

- **Decision 3: Multi-Layer Verification for Gemini + Live Forex Calculator Builder**
  - *Chosen Approach*:
    1. Use strict word-boundary (`\b`) regex and currency alias dictionaries so currency detection never false-triggers on ordinary English words (like *"cars"* matching `ARS` or *"expense"* matching `PEN`) while reliably catching all real currency/forex queries.
    2. Upgrade Gemini integration to `gemini-3.1-pro-preview` and `gemini-3.5-flash` with `responseMimeType = "application/json"` and live rate context injection.
    3. Upgrade `FormulaEvaluator` to support exponents (`^`), `sqrt()`, `abs()`, `min()`, `max()`, `round()`, `log()`, `ln()`, `sin()`, `cos()`, `tan()`, and run a post-generation verification pass on every output formula before rendering.
  - *Why*: Eliminates any possibility of hallucinated syntax, `0.0` / `NaN` outputs, or irrelevant cost-markup "bluff" calculators.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌───────────────────────────────────────────────────────────────────────────┐
│                         ChromaCalc Pro Application                        │
└─────────────────────────────────────┬─────────────────────────────────────┘
                                      │
        ┌─────────────────────────────┼─────────────────────────────┐
        ▼                             ▼                             ▼
┌───────────────────────┐   ┌───────────────────────┐   ┌───────────────────────┐
│  In-App Compose UI    │   │ Home Screen Widgets   │   │  Android Auto Suite   │
│  & Theme Engine       │   │ (11 Widget Providers) │   │ (CarAppService/Screen)│
├───────────────────────┤   ├───────────────────────┤   ├───────────────────────┤
│ • CalculatorViewModel │──▶│ • WidgetDataManager   │   │ • ChromaCarService    │
│ • ThemePalette Sync   │   │ • Dynamic ARGB Palette│   │ • QuickCalcCarScreen  │
│ • 120Hz GraphicsLayer │   │   Mirroring (No Glass)│   │ • CurrencyCarScreen   │
│ • CustomCalcBuilder   │   │ • Instant Broadcast   │   │ • Fuel & GST Screens  │
└───────────┬───────────┘   └───────────────────────┘   │ • HistoryCarScreen    │
            │                                           └───────────┬───────────┘
            ▼                                                       │
┌───────────────────────────────────────────────────────────────────┴───────┐
│               Verified Domain & AI Calculation Engine                     │
├───────────────────────────────────────────────────────────────────────────┤
│ • CurrencyRepository (Live Forex Rates + Word-Boundary Currency Matcher)  │
│ • Gemini Structured Schema Client (gemini-3.1-pro-preview / 3.5-flash)    │
│ • FormulaEvaluator (Exponents ^, sqrt, trig, log, min/max + AST Verifier) │
└───────────────────────────────────────────────────────────────────────────┘
```

### Data Model & State
- **Synced Widget Palette State**: Stored in `WidgetDataManager` (`SharedPreferences`) containing the selected `ThemePalette` ID and exact ARGB integers (`bgColor`, `screenBgColor`, `buttonBgColor`, `accentColor`, `equalButtonColor`, `textColor`, `subtextColor`, `buttonTextColor`).
- **Custom Calculator Verification Pipeline**:
  - `CustomCalculator` schema parsed from Gemini JSON response -> each `CustomOutputField.formula` is dry-run evaluated with the generated `inputs` default values via `FormulaEvaluator.evaluate()` -> verified finite results are accepted; invalid formulas are automatically repaired or synthesized with exact domain equations.
- **Android Auto Session State**: Reads shared calculation history, live cached currency rates from `CurrencyRepository`, and interactive state inside `CarScreen` instances.

### Interactive Component & State Mapping
- **Theme Change in App (`CalculatorViewModel.selectTheme`)**: Updates Compose `_currentTheme` state -> persists theme ID and exact ARGB color tokens to `WidgetDataManager` -> triggers `WidgetDataManager.notifyAllWidgets(context)` -> all 11 `AppWidgetProvider` classes rebuild `RemoteViews` with the new theme colors immediately.
- **"Build Me a Calculator" Submission (`CustomCalculatorBuilderView`)**: Captures prompt -> checks live connectivity -> fetches latest Forex rates if currency-related -> invokes Gemini structured JSON generation with live rate context -> validates all formulas through `FormulaEvaluator` -> updates active calculator state smoothly on `Dispatchers.IO`.
- **Android Auto Car Navigation**: Root `ChromaCarMainScreen` routes to `QuickCalcCarScreen`, `CurrencyCarScreen`, `FuelTripCarScreen`, `GstCarScreen`, and `HistoryCarScreen` using `ScreenManager.push()`, with interactive row actions that recalculate values on the spot and push results to shared history.

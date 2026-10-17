package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CurrencyRepository
import com.example.domain.CustomCalculatorEngine
import com.example.model.CustomCalculator
import com.example.model.CustomInputField
import com.example.model.ThemePalette
import com.example.util.NetworkUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

val AI_BUILDER_LOADING_MEMES = listOf(
    "🤖 Asking the AI not to divide by zero this time...",
    "⚡ Bribing the electrons with coffee & cookies...",
    "🧠 Overclocking quantum hamsters on math wheels...",
    "🛸 Transmitting formula signals to the math mothership...",
    "🍕 Calculating how many extra pizzas fit in your budget...",
    "☕ Converting pure caffeine into calculus derivatives...",
    "🧙‍♂️ Summoning Pythagoras from the mathematical shadow realm...",
    "🏎️ Shifting gears into maximum algorithmic overdrive...",
    "🚀 Aligning quantum sliders and consulting math deities...",
    "🕶️ Asking Batman for his secret Batcave financial formulas...",
    "🎯 Solving for X (and hoping X doesn't text back)...",
    "🪄 Casting Wingardium Leviosa on your variables...",
    "📈 Convincing the neural net that 2 + 2 = 4 (almost done)...",
    "🧪 Mixing decimal potions in the Wayne Tech laboratory...",
    "🧩 Assembling custom formula gears at warp speed..."
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomCalculatorBuilderView(
    theme: ThemePalette,
    calculatorsList: List<CustomCalculator>,
    activeCalculator: CustomCalculator,
    onSelectCalculator: (CustomCalculator) -> Unit,
    onSaveCustomCalculator: (CustomCalculator) -> Unit,
    onDeleteCustomCalculator: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var aiPromptText by remember { mutableStateOf("") }
    var isPromptExpanded by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    var isRefreshingLiveRates by remember { mutableStateOf(false) }
    var showFormulaDetails by remember { mutableStateOf(false) }
    var editingInputForKeyboard by remember { mutableStateOf<CustomInputField?>(null) }
    var keyboardInputText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var isOnline by remember { mutableStateOf(NetworkUtils.isInternetConnected(context)) }
    var currentLoadingMeme by remember { mutableStateOf(AI_BUILDER_LOADING_MEMES.random()) }

    LaunchedEffect(isGenerating) {
        if (isGenerating) {
            isOnline = NetworkUtils.isInternetConnected(context)
            currentLoadingMeme = AI_BUILDER_LOADING_MEMES.random()
            while (isGenerating) {
                delay(2400)
                currentLoadingMeme = AI_BUILDER_LOADING_MEMES.random()
            }
        }
    }

    // Local mutable state for current active calculator inputs
    val inputValues = remember(activeCalculator.id) {
        mutableStateMapOf<String, Double>().apply {
            activeCalculator.inputs.forEach { put(it.id, it.currentValue) }
        }
    }

    // Beta Version Note with "Don't ask again" option
    BetaFeatureNoticeDialog(
        featureKey = "custom_calculator_builder",
        featureTitle = "Build Me a Calculator",
        theme = theme,
        noteText = "Build Me a Calculator creates custom mathematical models, formulas, and dynamic inputs from plain English prompts. This feature is in active Beta—please verify custom formulas and test edge cases before using them for commercial or critical needs."
    )

    // Recalculate outputs in real time
    val outputResults = remember(activeCalculator.id, inputValues.toMap()) {
        CustomCalculatorEngine.calculateOutputs(activeCalculator, inputValues)
    }

    val numberFormatter = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }
    }

    val forexPresets = listOf(
        "Convert USD to INR live rate",
        "EUR to USD currency conversion",
        "Gold 24K vs 22K jewellery price"
    )

    val unitPresets = listOf(
        "Miles to kilometers & speed",
        "Feet to meters & height",
        "Celsius to Fahrenheit",
        "Kg to pounds & protein target",
        "Acres to square feet & land value",
        "Download time for 25 GB at 100 Mbps",
        "Liters to gallons (US) volume"
    )

    val physicsPresets = listOf(
        "Measure fluid pressure",
        "Hydrostatic liquid depth pressure",
        "Appliance electricity bill & kWh",
        "Kinetic energy & momentum",
        "Material density & volume",
        "Loan EMI & interest calculator",
        "Concrete slab & cement bags"
    )

    val businessPresets = listOf(
        "Monthly factory production cost",
        "Bakery cake & recipe pricing",
        "Solar rooftop ROI & savings",
        "Freelance project quote with buffer"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        // Top Bar: Active Selector & AI Generator Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Custom Calculator Builder",
                    color = theme.screenTextColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "AI-generated & bespoke formula tools",
                    color = theme.screenExpressionColor,
                    fontSize = 11.sp
                )
            }

            val aiPulseTransition = rememberInfiniteTransition(label = "ai_button_pulse")
            val aiPulseAlpha by aiPulseTransition.animateFloat(
                initialValue = 0.45f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "ai_pulse_alpha"
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isPromptExpanded) {
                    theme.accentColor.copy(alpha = 0.28f)
                } else {
                    theme.accentColor.copy(alpha = 0.12f + (aiPulseAlpha * 0.12f))
                },
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isPromptExpanded) 1.5.dp else (1.dp + (aiPulseAlpha * 0.8f).dp),
                    color = if (isPromptExpanded) theme.accentColor else theme.accentColor.copy(alpha = aiPulseAlpha)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isPromptExpanded = !isPromptExpanded }
                    .testTag("btn_toggle_ai_builder")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = theme.accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isPromptExpanded) "Close AI" else "Build with AI",
                        color = theme.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Expandable "Build Me a Calculator" AI Prompt Panel
        AnimatedVisibility(
            visible = isPromptExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.accentColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = theme.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "“Build Me a Calculator” AI Engine",
                            color = theme.screenTextColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = "Internet & Real Data",
                                    color = Color(0xFF10B981),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = "Describe any calculation or unit conversion in plain English. CALC + connects to live internet data, exchange rates, and synthesizes an interactive calculator.",
                        color = theme.screenExpressionColor,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    if (isGenerating) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = theme.accentColor,
                                trackColor = theme.cardBackground
                            )
                            AnimatedContent(
                                targetState = currentLoadingMeme,
                                transitionSpec = {
                                    (fadeIn(tween(220)) + androidx.compose.animation.scaleIn(initialScale = 0.95f))
                                        .togetherWith(fadeOut(tween(160)))
                                },
                                label = "loading_meme_transition"
                            ) { meme ->
                                Text(
                                    text = meme,
                                    color = theme.accentColor,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }

                    if (!isOnline) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = "Not connected to internet",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Not connected to internet — AI synthesis will generate offline formula models.",
                                    color = Color(0xFFEF4444),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = aiPromptText,
                        onValueChange = { aiPromptText = it },
                        enabled = !isGenerating,
                        placeholder = {
                            Text(
                                "e.g. Convert USD to INR with live rate, or Miles to km, or Factory cost",
                                fontSize = 12.sp,
                                color = theme.screenExpressionColor.copy(alpha = 0.6f)
                            )
                        },
                        trailingIcon = {
                            if (aiPromptText.isNotBlank() && !isGenerating) {
                                IconButton(
                                    onClick = {
                                        val query = aiPromptText
                                        focusManager.clearFocus()
                                        coroutineScope.launch {
                                            isGenerating = true
                                            val newCalc = CustomCalculatorEngine.buildFromPromptWithInternet(query)
                                            onSaveCustomCalculator(newCalc)
                                            onSelectCalculator(newCalc)
                                            aiPromptText = ""
                                            isGenerating = false
                                            isPromptExpanded = false
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Build Calculator",
                                        tint = theme.accentColor
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (aiPromptText.isNotBlank() && !isGenerating) {
                                val query = aiPromptText
                                focusManager.clearFocus()
                                coroutineScope.launch {
                                    isGenerating = true
                                    val newCalc = CustomCalculatorEngine.buildFromPromptWithInternet(query)
                                    onSaveCustomCalculator(newCalc)
                                    onSelectCalculator(newCalc)
                                    aiPromptText = ""
                                    isGenerating = false
                                    isPromptExpanded = false
                                }
                            }
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.accentColor,
                            unfocusedBorderColor = theme.screenBorderColor.copy(alpha = 0.4f),
                            focusedTextColor = theme.screenTextColor,
                            unfocusedTextColor = theme.screenTextColor,
                            focusedContainerColor = theme.cardBackground,
                            unfocusedContainerColor = theme.cardBackground
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // 1. Live Internet & Bullion Presets
                    Text(
                        text = "🌐 Live Internet & Bullion:",
                        color = Color(0xFF10B981),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        forexPresets.forEach { prompt ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = theme.cardBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = !isGenerating) {
                                        aiPromptText = prompt
                                        coroutineScope.launch {
                                            isGenerating = true
                                            val newCalc = CustomCalculatorEngine.buildFromPromptWithInternet(prompt)
                                            onSaveCustomCalculator(newCalc)
                                            onSelectCalculator(newCalc)
                                            isGenerating = false
                                            isPromptExpanded = false
                                        }
                                    }
                            ) {
                                Text(
                                    text = prompt,
                                    color = theme.screenTextColor,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // 2. Universal Unit Conversions
                    Text(
                        text = "📐 Universal Unit Conversions:",
                        color = theme.accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        unitPresets.forEach { prompt ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = theme.cardBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.screenBorderColor.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = !isGenerating) {
                                        aiPromptText = prompt
                                        coroutineScope.launch {
                                            isGenerating = true
                                            val newCalc = CustomCalculatorEngine.buildFromPromptWithInternet(prompt)
                                            onSaveCustomCalculator(newCalc)
                                            onSelectCalculator(newCalc)
                                            isGenerating = false
                                            isPromptExpanded = false
                                        }
                                    }
                            ) {
                                Text(
                                    text = prompt,
                                    color = theme.screenTextColor,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // 3. Science & Physics Models
                    Text(
                        text = "🔬 Science, Physics & Pressure:",
                        color = theme.accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        physicsPresets.forEach { prompt ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = theme.cardBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = !isGenerating) {
                                        aiPromptText = prompt
                                        coroutineScope.launch {
                                            isGenerating = true
                                            val newCalc = CustomCalculatorEngine.buildFromPromptWithInternet(prompt)
                                            onSaveCustomCalculator(newCalc)
                                            onSelectCalculator(newCalc)
                                            isGenerating = false
                                            isPromptExpanded = false
                                        }
                                    }
                            ) {
                                Text(
                                    text = prompt,
                                    color = theme.screenTextColor,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // 4. Business & Costing Models
                    Text(
                        text = "💼 Business & Engineering Models:",
                        color = theme.screenExpressionColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        businessPresets.forEach { prompt ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = theme.cardBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.screenBorderColor.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = !isGenerating) {
                                        aiPromptText = prompt
                                        coroutineScope.launch {
                                            isGenerating = true
                                            val newCalc = CustomCalculatorEngine.buildFromPromptWithInternet(prompt)
                                            onSaveCustomCalculator(newCalc)
                                            onSelectCalculator(newCalc)
                                            isGenerating = false
                                            isPromptExpanded = false
                                        }
                                    }
                            ) {
                                Text(
                                    text = prompt,
                                    color = theme.screenTextColor,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Horizontal Carousel / Chips of Available Calculators
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(calculatorsList, key = { it.id }) { calc ->
                val isSelected = calc.id == activeCalculator.id
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) theme.accentColor else theme.surfaceColor,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) theme.accentColor else theme.screenBorderColor.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectCalculator(calc) }
                        .testTag("chip_calc_${calc.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val chipSelectedColor = if (theme.accentColor.luminance() > 0.6f) Color(0xFF211118) else Color.White
                        if (calc.isUserCreated) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = if (isSelected) chipSelectedColor else theme.accentColor,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = calc.title,
                            color = if (isSelected) chipSelectedColor else theme.screenTextColor,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Scrollable Interactive Calculator View
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Info Card of Current Calculator
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeCalculator.title,
                                    color = theme.screenTextColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = activeCalculator.description,
                                    color = theme.screenExpressionColor,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        // Reset to defaults
                                        activeCalculator.inputs.forEach {
                                            inputValues[it.id] = it.defaultValue
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reset Values",
                                        tint = theme.screenExpressionColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                if (activeCalculator.isUserCreated) {
                                    IconButton(
                                        onClick = { onDeleteCustomCalculator(activeCalculator.id) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Custom Calculator",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Live Real-Time Internet & Data Source Indicator
                        if (activeCalculator.hasInternetData) {
                            val isConnected = isOnline && (CurrencyRepository.isLiveOnline || NetworkUtils.isInternetConnected(context))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isConnected) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isConnected) Color(0xFF10B981).copy(alpha = 0.45f) else Color(0xFFEF4444).copy(alpha = 0.45f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isConnected) Icons.Default.Language else Icons.Default.CloudOff,
                                        contentDescription = if (isConnected) "Live Internet Connected" else "Not Connected to Internet",
                                        tint = if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isConnected) "REAL-TIME INTERNET CONNECTED" else "NOT CONNECTED TO INTERNET",
                                            color = if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = if (isConnected) {
                                                activeCalculator.dataSourceLabel.ifEmpty { "Connected to Live Web Data" }
                                            } else {
                                                "Offline Mode • Not connected to internet. Real-time rates unavailable (using offline backup rates & formulas)."
                                            },
                                            color = theme.screenTextColor,
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                    if (isRefreshingLiveRates) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444)
                                        )
                                    } else {
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    isRefreshingLiveRates = true
                                                    isOnline = NetworkUtils.isInternetConnected(context)
                                                    val updated = CustomCalculatorEngine.refreshCalculatorLiveRates(activeCalculator)
                                                    onSaveCustomCalculator(updated)
                                                    onSelectCalculator(updated)
                                                    isOnline = NetworkUtils.isInternetConnected(context)
                                                    isRefreshingLiveRates = false
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Refresh Live Data",
                                                tint = if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (activeCalculator.dataSourceLabel.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = theme.cardBackground,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    theme.screenBorderColor.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = theme.accentColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = activeCalculator.dataSourceLabel,
                                        color = theme.screenExpressionColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Results Output Section (LCD-like High-Contrast Grid)
            item {
                Text(
                    text = "CALCULATED OUTPUTS & METRICS",
                    color = theme.accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            items(activeCalculator.outputs, key = { it.id }) { output ->
                val resultVal = outputResults[output.id] ?: 0.0
                val formattedVal = numberFormatter.format(resultVal)
                val isHighlighted = output.isHighlighted

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isHighlighted) theme.accentColor.copy(alpha = 0.15f) else theme.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isHighlighted) 1.5.dp else 1.dp,
                        color = if (isHighlighted) theme.accentColor else theme.screenBorderColor.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = output.label,
                                color = if (isHighlighted) theme.accentColor else theme.screenTextColor,
                                fontSize = 13.sp,
                                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium
                            )
                            if (output.description.isNotBlank()) {
                                Text(
                                    text = output.description,
                                    color = theme.screenExpressionColor,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            if (output.unit.isNotEmpty() && output.unit == "₹" || output.unit == "$") {
                                Text(
                                    text = output.unit,
                                    color = if (isHighlighted) theme.accentColor else theme.screenTextColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = formattedVal,
                                color = if (isHighlighted) theme.accentColor else theme.screenTextColor,
                                fontSize = if (isHighlighted) 20.sp else 17.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            if (output.unit.isNotEmpty() && output.unit != "₹" && output.unit != "$") {
                                Text(
                                    text = output.unit,
                                    color = theme.screenExpressionColor,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Input Parameters Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "INPUT PARAMETERS & SLIDERS",
                    color = theme.accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            items(activeCalculator.inputs, key = { it.id }) { input ->
                val currentVal = inputValues[input.id] ?: input.defaultValue

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = input.label,
                                    color = theme.screenTextColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (input.helpText.isNotBlank()) {
                                    Text(
                                        text = input.helpText,
                                        color = theme.screenExpressionColor,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Directly Interactive Value Badge - Tap to edit amount via keyboard
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = theme.accentColor.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        editingInputForKeyboard = input
                                        keyboardInputText = if (currentVal == currentVal.toLong().toDouble())
                                            currentVal.toLong().toString()
                                        else currentVal.toString()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${if (input.unit == "₹" || input.unit == "$") input.unit else ""}${numberFormatter.format(currentVal)}${if (input.unit.isNotEmpty() && input.unit != "₹" && input.unit != "$") " " + input.unit else ""}",
                                        color = theme.accentColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Tap to edit amount",
                                        tint = theme.accentColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        // Slider Control
                        Slider(
                            value = currentVal.toFloat().coerceIn(input.minValue.toFloat(), input.maxValue.toFloat()),
                            onValueChange = { inputValues[input.id] = it.toDouble() },
                            valueRange = input.minValue.toFloat()..input.maxValue.toFloat(),
                            steps = if (input.step > 0 && (input.maxValue - input.minValue) / input.step < 100) {
                                (((input.maxValue - input.minValue) / input.step).toInt() - 1).coerceAtLeast(0)
                            } else 0,
                            colors = SliderDefaults.colors(
                                thumbColor = theme.accentColor,
                                activeTrackColor = theme.accentColor,
                                inactiveTrackColor = theme.cardBackground
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp)
                        )

                        // Quick step adjustments
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${input.minValue.toInt()} ${input.unit}",
                                color = theme.screenExpressionColor,
                                fontSize = 9.sp
                            )
                            Text(
                                text = "Step: ${input.step} ${input.unit}",
                                color = theme.screenExpressionColor,
                                fontSize = 9.sp
                            )
                            Text(
                                text = "${input.maxValue.toInt()} ${input.unit}",
                                color = theme.screenExpressionColor,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            // Formula Breakdown Transparency
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = theme.surfaceColor.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showFormulaDetails = !showFormulaDetails }
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = theme.accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Mathematical Formulas & Variables",
                                    color = theme.screenTextColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = if (showFormulaDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = theme.screenTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (showFormulaDetails) {
                            Spacer(modifier = Modifier.height(6.dp))
                            activeCalculator.outputs.forEach { out ->
                                Text(
                                    text = "• ${out.label}: ${out.formula}",
                                    color = theme.screenExpressionColor,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Direct Keyboard Input Dialog for Custom Calculator Inputs
        if (editingInputForKeyboard != null) {
            val targetInput = editingInputForKeyboard!!
            AlertDialog(
                onDismissRequest = { editingInputForKeyboard = null },
                containerColor = theme.surfaceColor,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = theme.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Edit ${targetInput.label}",
                            color = theme.screenTextColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Enter any exact custom value or amount:",
                            color = theme.screenExpressionColor,
                            fontSize = 12.sp
                        )

                        OutlinedTextField(
                            value = keyboardInputText,
                            onValueChange = { keyboardInputText = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                val parsed = keyboardInputText.toDoubleOrNull()
                                if (parsed != null) {
                                    inputValues[targetInput.id] = parsed
                                }
                                editingInputForKeyboard = null
                            }),
                            suffix = {
                                if (targetInput.unit.isNotBlank()) {
                                    Text(targetInput.unit, color = theme.screenExpressionColor)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = theme.accentColor,
                                unfocusedBorderColor = theme.screenBorderColor.copy(alpha = 0.5f),
                                focusedTextColor = theme.screenTextColor,
                                unfocusedTextColor = theme.screenTextColor,
                                focusedContainerColor = theme.cardBackground,
                                unfocusedContainerColor = theme.cardBackground
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Quick presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                targetInput.minValue,
                                targetInput.defaultValue,
                                targetInput.maxValue
                            ).distinct().forEach { presetVal ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = theme.cardBackground,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.screenBorderColor.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            keyboardInputText = if (presetVal == presetVal.toLong().toDouble())
                                                presetVal.toLong().toString()
                                            else presetVal.toString()
                                        }
                                ) {
                                    Text(
                                        text = numberFormatter.format(presetVal),
                                        color = theme.screenTextColor,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val parsed = keyboardInputText.toDoubleOrNull()
                            if (parsed != null) {
                                inputValues[targetInput.id] = parsed
                            }
                            editingInputForKeyboard = null
                        }
                    ) {
                        Text("Apply", color = theme.accentColor, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingInputForKeyboard = null }) {
                        Text("Cancel", color = theme.screenExpressionColor)
                    }
                }
            )
        }
    }
}

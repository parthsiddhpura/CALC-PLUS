package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.GstEngine
import com.example.domain.LanguageStrings
import com.example.domain.TaxPreset
import com.example.model.AppLanguage
import com.example.model.GstCalculationType
import com.example.model.GstResult
import com.example.model.GstSlab
import com.example.model.ThemePalette

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GstCalculatorView(
    theme: ThemePalette,
    amountInput: String,
    calculationType: GstCalculationType,
    selectedSlabId: Int,
    slabs: List<GstSlab>,
    currentResult: GstResult?,
    grandTotalGross: Double,
    grandTotalGst: Double,
    calculationCount: Int,
    language: AppLanguage,
    onInputDigit: (String) -> Unit,
    onEquals: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onToggleType: () -> Unit,
    onSelectSlab: (GstSlab) -> Unit,
    onClearGrandTotal: () -> Unit,
    onUpdateSlabRate: (Int, Double) -> Unit,
    onApplyPreset: (TaxPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    var showRateSetDialog by remember { mutableStateOf(false) }
    var showGtBreakdown by remember { mutableStateOf(false) }

    val activeSlab = slabs.firstOrNull { it.id == selectedSlabId } ?: slabs.getOrElse(3) { slabs[0] }
    val isExpression = amountInput.any { it in listOf('+', '−', '-', '×', '*', '÷', '/', '%') }
    val evaluatedAmount = remember(amountInput) { GstEngine.evaluateAmountOrExpression(amountInput) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- 1. Big Casio LCD Style GST Screen & Breakdown Card (Scrollable Up & Down for Large Amounts) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .border(theme.borderWidthDp, theme.screenBorderColor, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = theme.screenBackground)
        ) {
            val displayScrollState = rememberScrollState()
            val amountHorizontalScrollState = rememberScrollState()

            // Keep horizontal scroll pinned to end as user types new digits
            LaunchedEffect(amountInput) {
                amountHorizontalScrollState.scrollTo(amountHorizontalScrollState.maxValue)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(displayScrollState)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Status bar in GST display: Mode Toggle (GST+ / GST-), Active Slab indicator, GT badge, Rate Set
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Type selector pills (GST+ Added / GST- Removed)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (calculationType == GstCalculationType.EXCLUSIVE) theme.accentColor else theme.surfaceColor,
                                modifier = Modifier
                                    .clickable {
                                        if (calculationType != GstCalculationType.EXCLUSIVE) onToggleType()
                                    }
                                    .testTag("btn_gst_exclusive")
                            ) {
                                Text(
                                    text = LanguageStrings.gstAddText(language),
                                    color = if (calculationType == GstCalculationType.EXCLUSIVE) theme.backgroundColor else theme.screenExpressionColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (calculationType == GstCalculationType.INCLUSIVE) theme.secondaryAccent else theme.surfaceColor,
                                modifier = Modifier
                                    .clickable {
                                        if (calculationType != GstCalculationType.INCLUSIVE) onToggleType()
                                    }
                                    .testTag("btn_gst_inclusive")
                            ) {
                                Text(
                                    text = LanguageStrings.gstExtractText(language),
                                    color = if (calculationType == GstCalculationType.INCLUSIVE) theme.backgroundColor else theme.screenExpressionColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // GT & Rate Set buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (calculationCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFFB703).copy(alpha = 0.2f),
                                    modifier = Modifier.clickable { showGtBreakdown = !showGtBreakdown }
                                ) {
                                    Text(
                                        text = "GT: ${GstEngine.formatCurrency(grandTotalGross)}",
                                        color = Color(0xFFFFB703),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // RATE SET Button (Opens Country Presets & Slab Customization)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = theme.surfaceColor,
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .clickable { showRateSetDialog = true }
                                    .testTag("btn_gst_rate_set")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Rate Set",
                                        tint = theme.accentColor,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = LanguageStrings.rateSet(language),
                                        color = theme.accentColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Middle LCD Screen Section: Main Display Amount & Expression Subtitle
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        // Active Mode & Rate Subtitle Indicator
                        val modeLabel = if (calculationType == GstCalculationType.EXCLUSIVE) {
                            LanguageStrings.gstBaseAmountLabel(activeSlab.label, language)
                        } else {
                            LanguageStrings.gstGrossAmountLabel(activeSlab.label, language)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = modeLabel,
                                color = theme.screenExpressionColor.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )

                            if (isExpression) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = theme.accentColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "= ${GstEngine.formatCurrency(evaluatedAmount)}",
                                        color = theme.accentColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Primary Display Value (Horizontally scrollable for massive numbers + Vertically scrollable container)
                        val displayFontSize = when {
                            amountInput.length > 14 -> 24.sp
                            amountInput.length > 10 -> 28.sp
                            amountInput.length > 7 -> 34.sp
                            else -> 38.sp
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(amountHorizontalScrollState),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = if (amountInput.isEmpty()) "0" else amountInput,
                                color = theme.screenTextColor,
                                fontSize = displayFontSize,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                textAlign = TextAlign.End,
                                modifier = Modifier.testTag("gst_display_text")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bottom Breakdown Strip (Net Amount, CGST, SGST, Total Tax & Gross Amount)
                    // Always displayed with stable 2-column metrics to prevent blanking or layout shifts
                    val activeSlab = slabs.firstOrNull { it.id == selectedSlabId } ?: slabs.getOrElse(3) { slabs[0] }
                    val currentResultToDisplay = currentResult ?: GstEngine.calculate(0.0, activeSlab.ratePercent, calculationType)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HorizontalDivider(
                            color = theme.screenBorderColor.copy(alpha = 0.4f),
                            thickness = 1.dp
                        )

                        // Row 1: Net Amount (left) & Total Tax / Extracted Tax (right)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Net Amount
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = LanguageStrings.netAmount(language),
                                    color = theme.screenExpressionColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = GstEngine.formatCurrency(currentResultToDisplay.netAmount),
                                    color = theme.screenTextColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Total Tax / Extracted GST
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (currentResultToDisplay.type == GstCalculationType.INCLUSIVE)
                                        "GST (-${currentResultToDisplay.gstRate}%)"
                                    else
                                        "${LanguageStrings.totalTax(language)} (${currentResultToDisplay.gstRate}%)",
                                    color = theme.screenExpressionColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (currentResultToDisplay.type == GstCalculationType.INCLUSIVE)
                                        "-${GstEngine.formatCurrency(currentResultToDisplay.gstAmount)}"
                                    else
                                        "+${GstEngine.formatCurrency(currentResultToDisplay.gstAmount)}",
                                    color = theme.accentColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Row 2: CGST (left) & SGST (right)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // CGST
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CGST (${currentResultToDisplay.gstRate / 2}%)",
                                    color = theme.screenExpressionColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = GstEngine.formatCurrency(currentResultToDisplay.cgstAmount),
                                    color = theme.accentColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // SGST
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SGST (${currentResultToDisplay.gstRate / 2}%)",
                                    color = theme.screenExpressionColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = GstEngine.formatCurrency(currentResultToDisplay.sgstAmount),
                                    color = theme.accentColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Emphasized Gross Total Banner (Shows on both Extract & Add mode)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (currentResultToDisplay.type == GstCalculationType.INCLUSIVE)
                                theme.accentColor.copy(alpha = 0.15f)
                            else
                                theme.surfaceColor.copy(alpha = 0.7f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = LanguageStrings.totalGross(language),
                                        color = theme.screenTextColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    if (currentResultToDisplay.type == GstCalculationType.INCLUSIVE) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = theme.accentColor
                                        ) {
                                            Text(
                                                text = "EXTRACTED",
                                                color = theme.backgroundColor,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = GstEngine.formatCurrency(currentResultToDisplay.grossAmount),
                                    color = theme.secondaryAccent,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- 2. Casio Dedicated GST Slab Buttons (Elevated upper bar right below display) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Label indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageStrings.rateSelectionHeader(language),
                    color = theme.screenExpressionColor.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            // The Casio GST Slab Buttons row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                slabs.forEach { slab ->
                    val isSelected = slab.id == selectedSlabId
                    CalculatorButton(
                        text = "${slab.name}\n${slab.label}",
                        onClick = { onSelectSlab(slab) },
                        theme = theme,
                        backgroundColor = if (isSelected) theme.accentColor else theme.functionButtonBg,
                        textColor = if (isSelected) theme.backgroundColor else theme.functionButtonText,
                        borderColor = if (isSelected) theme.accentColor else theme.functionButtonBorder,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        testTag = "btn_slab_${slab.id}"
                    )
                }

                // GST GT (Grand Total) button
                CalculatorButton(
                    text = "GST GT",
                    onClick = { showGtBreakdown = true },
                    theme = theme,
                    backgroundColor = if (calculationCount > 0) Color(0xFFFFB703) else theme.functionButtonBg,
                    textColor = if (calculationCount > 0) Color.Black else theme.functionButtonText,
                    borderColor = theme.functionButtonBorder,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_gst_gt"
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- 3. Full Number Keypad for GST Entry (Supports arithmetic: +, −, ×, ÷, %, =, 00) ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Row 1: AC, ⌫, 00, ÷
            Row(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(
                    text = "AC",
                    onClick = onClear,
                    theme = theme,
                    backgroundColor = theme.functionButtonBg,
                    textColor = theme.functionButtonText,
                    borderColor = theme.functionButtonBorder,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_gst_ac"
                )
                CalculatorButton(
                    text = "⌫",
                    onClick = onBackspace,
                    theme = theme,
                    backgroundColor = theme.functionButtonBg,
                    textColor = theme.functionButtonText,
                    borderColor = theme.functionButtonBorder,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_gst_backspace"
                )
                CalculatorButton(
                    text = "00",
                    onClick = { onInputDigit("00") },
                    theme = theme,
                    fontSize = 19.sp,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_gst_00"
                )
                CalculatorButton(
                    text = "÷",
                    onClick = { onInputDigit("÷") },
                    theme = theme,
                    backgroundColor = theme.operatorButtonBg,
                    textColor = theme.operatorButtonText,
                    borderColor = theme.operatorButtonBorder,
                    fontSize = 22.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: 7, 8, 9, ×
            Row(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(
                    text = "7",
                    onClick = { onInputDigit("7") },
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                CalculatorButton(
                    text = "8",
                    onClick = { onInputDigit("8") },
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                CalculatorButton(
                    text = "9",
                    onClick = { onInputDigit("9") },
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                CalculatorButton(
                    text = "×",
                    onClick = { onInputDigit("×") },
                    theme = theme,
                    backgroundColor = theme.operatorButtonBg,
                    textColor = theme.operatorButtonText,
                    borderColor = theme.operatorButtonBorder,
                    fontSize = 22.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 3: 4, 5, 6, −
            Row(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(
                    text = "4",
                    onClick = { onInputDigit("4") },
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                CalculatorButton(
                    text = "5",
                    onClick = { onInputDigit("5") },
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                CalculatorButton(
                    text = "6",
                    onClick = { onInputDigit("6") },
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                CalculatorButton(
                    text = "−",
                    onClick = { onInputDigit("−") },
                    theme = theme,
                    backgroundColor = theme.operatorButtonBg,
                    textColor = theme.operatorButtonText,
                    borderColor = theme.operatorButtonBorder,
                    fontSize = 22.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 4: 1, 2, 3, +
            Row(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(
                    text = "1",
                    onClick = { onInputDigit("1") },
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                CalculatorButton(
                    text = "2",
                    onClick = { onInputDigit("2") },
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                CalculatorButton(
                    text = "3",
                    onClick = { onInputDigit("3") },
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                CalculatorButton(
                    text = "+",
                    onClick = { onInputDigit("+") },
                    theme = theme,
                    backgroundColor = theme.operatorButtonBg,
                    textColor = theme.operatorButtonText,
                    borderColor = theme.operatorButtonBorder,
                    fontSize = 22.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 5: 0, ., TAX-, =
            Row(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(
                    text = "0",
                    onClick = { onInputDigit("0") },
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                CalculatorButton(
                    text = ".",
                    onClick = { onInputDigit(".") },
                    theme = theme,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                CalculatorButton(
                    text = "TAX-",
                    onClick = onToggleType,
                    theme = theme,
                    backgroundColor = theme.functionButtonBg,
                    textColor = theme.functionButtonText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                CalculatorButton(
                    text = "=",
                    onClick = onEquals,
                    theme = theme,
                    backgroundBrush = theme.equalsButtonBrush,
                    textColor = theme.equalsButtonText,
                    borderColor = theme.equalsButtonBorder,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_gst_equals"
                )
            }
        }
    }

    // --- Enhanced Rate Set Dialog (Country Presets + Custom Rate Inputs) ---
    if (showRateSetDialog) {
        val slabRateMap = remember {
            mutableStateMapOf<Int, String>().apply {
                slabs.forEach { put(it.id, it.ratePercent.toString()) }
            }
        }

        LaunchedEffect(slabs) {
            slabs.forEach { slabRateMap[it.id] = it.ratePercent.toString() }
        }

        AlertDialog(
            onDismissRequest = { showRateSetDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = theme.accentColor
                    )
                    Text("GST & Tax Rates Configuration", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // Section 1: Country Tax Presets
                    Text(
                        text = "Global Tax Presets (1-Tap Apply):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        GstEngine.COUNTRY_PRESETS.forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.clickable {
                                    onApplyPreset(preset)
                                    preset.rates.forEachIndexed { idx, r ->
                                        slabRateMap[idx] = r.toString()
                                    }
                                }
                            ) {
                                Text(
                                    text = "${preset.flagEmoji} ${preset.countryName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Section 2: Custom Slab Editable Rates
                    Text(
                        text = "Custom Rates for Buttons (GST+0 to GST+4):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    slabs.forEach { slab ->
                        val currentText = slabRateMap[slab.id] ?: slab.ratePercent.toString()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = theme.accentColor.copy(alpha = 0.15f),
                                modifier = Modifier.width(68.dp)
                            ) {
                                Text(
                                    text = slab.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = theme.accentColor,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedTextField(
                                value = currentText,
                                onValueChange = { newVal ->
                                    slabRateMap[slab.id] = newVal
                                    val r = newVal.toDoubleOrNull()
                                    if (r != null && r >= 0.0) {
                                        onUpdateSlabRate(slab.id, r)
                                    }
                                },
                                trailingIcon = { Text("%", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRateSetDialog = false }) {
                    Text(LanguageStrings.done(language), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Grand Total (GT) Breakdown Dialog
    if (showGtBreakdown) {
        AlertDialog(
            onDismissRequest = { showGtBreakdown = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = Color(0xFFFFB703)
                    )
                    Text(LanguageStrings.grandTotal(language), fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Accumulated across $calculationCount GST invoices in this session:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Invoices:")
                                Text("$calculationCount", fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total GST Tax:")
                                Text(GstEngine.formatCurrency(grandTotalGst), fontWeight = FontWeight.Bold, color = theme.accentColor)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Grand Total (Gross):", fontWeight = FontWeight.Bold)
                                Text(GstEngine.formatCurrency(grandTotalGross), fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFFFFB703))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGtBreakdown = false }) {
                    Text(LanguageStrings.close(language))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onClearGrandTotal()
                        showGtBreakdown = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(LanguageStrings.clearGt(language))
                }
            }
        )
    }
}

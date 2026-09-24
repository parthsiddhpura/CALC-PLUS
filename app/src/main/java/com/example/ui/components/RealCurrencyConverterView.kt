package com.example.ui.components

import androidx.compose.ui.graphics.luminance
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CurrencyRepository
import com.example.model.CurrencyInfo
import com.example.model.ThemePalette
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RealCurrencyConverterView(
    theme: ThemePalette,
    fromCode: String,
    toCode: String,
    inputAmount: String,
    outputAmount: String,
    isLoading: Boolean,
    statusText: String,
    isOnline: Boolean,
    ratesMap: Map<String, Double>,
    onAmountChange: (String) -> Unit,
    onFromChange: (String) -> Unit,
    onToChange: (String) -> Unit,
    onSwap: () -> Unit,
    onQuickInrAmount: (Double) -> Unit,
    onRefreshRates: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    val fromCurrency = remember(fromCode) { CurrencyRepository.getCurrencyInfo(fromCode) }
    val toCurrency = remember(toCode) { CurrencyRepository.getCurrencyInfo(toCode) }

    val df = remember { DecimalFormat("#,##0.00##", DecimalFormatSymbols(Locale.US)) }
    val amountNum = inputAmount.toDoubleOrNull() ?: 0.0

    // Single unit exchange rate
    val singleRate = remember(fromCode, toCode, ratesMap) {
        CurrencyRepository.convert(1.0, fromCode, toCode)
    }
    val inverseRate = remember(fromCode, toCode, ratesMap) {
        CurrencyRepository.convert(1.0, toCode, fromCode)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Live Network Status Banner ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(theme.borderWidthDp, theme.screenBorderColor, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = theme.screenBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isOnline) Color(0xFF2EC4B6) else Color(0xFFFFB703),
                                CircleShape
                            )
                    )
                    Text(
                        text = statusText,
                        color = if (isOnline) Color(0xFF2EC4B6) else theme.screenExpressionColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = theme.accentColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = onRefreshRates,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Live Rates",
                            tint = theme.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // --- 2. Interactive Conversion Dual Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(theme.borderWidthDp, theme.screenBorderColor, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = theme.screenBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // FROM CURRENCY BOX
                CurrencyCardRow(
                    label = "YOU SEND / CONVERT",
                    amount = inputAmount,
                    onAmountChange = onAmountChange,
                    currency = fromCurrency,
                    isEditable = true,
                    onClickCurrency = { showFromPicker = true },
                    theme = theme
                )

                // SWAP BUTTON & LIVE RATE BADGE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = theme.surfaceColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "1 ${fromCurrency.code} = ${df.format(singleRate)} ${toCurrency.code}",
                            color = theme.screenTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onSwap,
                        modifier = Modifier
                            .size(36.dp)
                            .background(theme.accentColor, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Swap Currencies",
                            tint = if (theme.accentColor.luminance() > 0.6f) Color(0xFF211118) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Surface(
                        color = theme.surfaceColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "1 ${toCurrency.code} = ${df.format(inverseRate)} ${fromCurrency.code}",
                            color = theme.screenExpressionColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // TO CURRENCY BOX
                CurrencyCardRow(
                    label = "YOU RECEIVE (CONVERTED)",
                    amount = outputAmount,
                    onAmountChange = {},
                    currency = toCurrency,
                    isEditable = false,
                    onClickCurrency = { showToPicker = true },
                    theme = theme
                )

                // Indian Numbering System in Words (if INR involved)
                if (fromCode == "INR" || toCode == "INR") {
                    val inrVal = if (fromCode == "INR") amountNum else (outputAmount.replace(",", "").toDoubleOrNull() ?: 0.0)
                    if (inrVal > 0) {
                        Surface(
                            color = theme.surfaceColor,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🇮🇳", fontSize = 16.sp)
                                Column {
                                    Text("Indian Rupee Denomination", color = theme.screenExpressionColor, fontSize = 10.sp)
                                    Text(
                                        text = CurrencyRepository.formatIndianRupeeWord(inrVal),
                                        color = theme.accentColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 3. Quick Indian Rupee (₹ INR) Presets ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QUICK RUPEE (₹) PRESETS",
                        color = theme.accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text("Tap to convert", color = theme.screenExpressionColor, fontSize = 10.sp)
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickPresets = listOf(
                        "₹1,000" to 1000.0,
                        "₹5,000" to 5000.0,
                        "₹10,000" to 10000.0,
                        "₹50,000" to 50000.0,
                        "1 Lakh ₹" to 100000.0,
                        "5 Lakhs ₹" to 500000.0,
                        "10 Lakhs ₹" to 1000000.0,
                        "1 Crore ₹" to 10000000.0
                    )

                    quickPresets.forEach { (label, value) ->
                        Surface(
                            color = theme.screenBackground,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onQuickInrAmount(value) }
                        ) {
                            Text(
                                text = label,
                                color = theme.screenTextColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 4. Live Multi-Currency Global Comparison Board ---
        var comparisonBaseInr by remember { mutableStateOf(1.0) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GLOBAL CURRENCY COMPARISON",
                            color = theme.accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Base: ₹${if (comparisonBaseInr == 1.0) "1 Rupee (INR)" else df.format(comparisonBaseInr) + " INR"}",
                            color = theme.screenTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = theme.accentColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "₹1 INR Base",
                            color = theme.accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Quick Indian Rupee Comparison Base Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        1.0 to "₹1 (1 Rupee)",
                        100.0 to "₹100",
                        1000.0 to "₹1,000",
                        100000.0 to "1 Lakh ₹"
                    ).forEach { (amt, label) ->
                        val isSelected = comparisonBaseInr == amt
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) theme.accentColor else theme.cardBackground,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { comparisonBaseInr = amt }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) (if (theme.accentColor.luminance() > 0.6f) Color(0xFF211118) else Color.White) else theme.screenExpressionColor,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                val majorCurrencies = listOf("USD", "EUR", "GBP", "AED", "CAD", "AUD", "SAR", "KWD", "SGD", "JPY", "CNY", "CHF", "QAR", "MYR", "NZD", "THB")

                majorCurrencies.forEach { code ->
                    val info = CurrencyRepository.getCurrencyInfo(code)
                    val converted = CurrencyRepository.convert(comparisonBaseInr, "INR", code)
                    val oneForeignInInr = CurrencyRepository.convert(1.0, code, "INR")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(info.flag, fontSize = 18.sp)
                            Column {
                                Text(info.code, color = theme.screenTextColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(info.name, color = theme.screenExpressionColor, fontSize = 10.sp)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${info.symbol} ${if (converted < 0.01) String.format(Locale.US, "%.4f", converted) else if (converted < 1.0) String.format(Locale.US, "%.4f", converted) else df.format(converted)}",
                                color = theme.accentColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "1 ${info.code} = ₹${df.format(oneForeignInInr)}",
                                color = theme.screenExpressionColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    HorizontalDivider(color = theme.screenBorderColor.copy(alpha = 0.15f))
                }
            }
        }
    }

    // Modal Sheet for Currency Selection (From or To)
    if (showFromPicker) {
        CurrencyPickerBottomSheet(
            title = "Select Source Currency",
            activeCode = fromCode,
            theme = theme,
            onSelect = {
                onFromChange(it)
                showFromPicker = false
            },
            onDismiss = { showFromPicker = false }
        )
    }

    if (showToPicker) {
        CurrencyPickerBottomSheet(
            title = "Select Target Currency",
            activeCode = toCode,
            theme = theme,
            onSelect = {
                onToChange(it)
                showToPicker = false
            },
            onDismiss = { showToPicker = false }
        )
    }
}

@Composable
private fun CurrencyCardRow(
    label: String,
    amount: String,
    onAmountChange: (String) -> Unit,
    currency: CurrencyInfo,
    isEditable: Boolean,
    onClickCurrency: () -> Unit,
    theme: ThemePalette
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.surfaceColor, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = theme.screenExpressionColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Currency Selector Badge
            Surface(
                color = theme.screenBackground,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.screenBorderColor.copy(alpha = 0.5f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onClickCurrency() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(currency.flag, fontSize = 20.sp)
                    Column {
                        Text(
                            text = currency.code,
                            color = theme.screenTextColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = currency.name.take(12),
                            color = theme.screenExpressionColor,
                            fontSize = 9.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = theme.screenExpressionColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Amount Input or Output
            if (isEditable) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = theme.screenTextColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.End
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.screenTextColor,
                        unfocusedTextColor = theme.screenTextColor,
                        focusedBorderColor = theme.accentColor,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            } else {
                Text(
                    text = amount,
                    color = theme.accentColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPickerBottomSheet(
    title: String,
    activeCode: String,
    theme: ThemePalette,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredList = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            CurrencyRepository.ALL_CURRENCIES
        } else {
            val q = searchQuery.trim().lowercase(Locale.ROOT)
            CurrencyRepository.ALL_CURRENCIES.filter {
                it.code.lowercase(Locale.ROOT).contains(q) ||
                it.name.lowercase(Locale.ROOT).contains(q) ||
                it.country.lowercase(Locale.ROOT).contains(q)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = theme.screenBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .fillMaxHeight(0.85f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = theme.screenTextColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = theme.screenTextColor)
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by code, country, or name...", color = theme.screenExpressionColor) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = theme.accentColor) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.accentColor,
                    unfocusedBorderColor = theme.screenBorderColor.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredList, key = { it.code }) { item ->
                    val isSelected = item.code.equals(activeCode, ignoreCase = true)
                    Surface(
                        color = if (isSelected) theme.accentColor.copy(alpha = 0.18f) else theme.surfaceColor,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelect(item.code) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(item.flag, fontSize = 22.sp)
                                Column {
                                    Text(
                                        text = "${item.code} (${item.symbol})",
                                        color = if (isSelected) theme.accentColor else theme.screenTextColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${item.name} • ${item.country}",
                                        color = theme.screenExpressionColor,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (isSelected) {
                                Text(
                                    text = "SELECTED",
                                    color = theme.accentColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

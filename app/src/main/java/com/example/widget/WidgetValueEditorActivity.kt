package com.example.widget

import android.content.Context
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemes
import java.text.DecimalFormat

class WidgetValueEditorActivity : ComponentActivity() {

    companion object {
        const val EXTRA_WIDGET_TYPE = "extra_widget_type" // "GST", "EMI", "PERCENTAGE", "QUICK_CALC"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val widgetType = intent.getStringExtra(EXTRA_WIDGET_TYPE) ?: "GST"

        setContent {
            val context = LocalContext.current
            val view = LocalView.current
            val activeTheme = remember { WidgetDataManager.getActiveAppTheme(context) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable { finish() },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = activeTheme.surfaceColor,
                    shadowElevation = 16.dp,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, activeTheme.accentColor.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        when (widgetType) {
                            "GST" -> GstWidgetEditor(
                                activeTheme = activeTheme,
                                onSave = { amount, rate, isAdd ->
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    WidgetDataManager.setGstValues(context, amount, rate, isAdd)
                                    Toast.makeText(context, "GST Widget Updated: ₹${DecimalFormat("#,##,###.##").format(amount)}", Toast.LENGTH_SHORT).show()
                                    finish()
                                },
                                onCancel = { finish() }
                            )
                            "EMI" -> EmiWidgetEditor(
                                activeTheme = activeTheme,
                                onSave = { principal, rate, tenure ->
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    WidgetDataManager.setEmiValues(context, principal, rate, tenure)
                                    Toast.makeText(context, "EMI Widget Updated: ₹${DecimalFormat("#,##,###").format(principal)}", Toast.LENGTH_SHORT).show()
                                    finish()
                                },
                                onCancel = { finish() }
                            )
                            "PERCENTAGE" -> PercentageWidgetEditor(
                                activeTheme = activeTheme,
                                onSave = { base, rate ->
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    WidgetDataManager.setPercentageValues(context, base, rate)
                                    Toast.makeText(context, "Percentage Widget Updated: $rate% of $base", Toast.LENGTH_SHORT).show()
                                    finish()
                                },
                                onCancel = { finish() }
                            )
                            else -> QuickCalcWidgetEditor(
                                activeTheme = activeTheme,
                                onSave = { initialExpr ->
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    WidgetDataManager.setQuickCalcValues(context, initialExpr, initialExpr)
                                    Toast.makeText(context, "Quick Calc Widget Updated: $initialExpr", Toast.LENGTH_SHORT).show()
                                    finish()
                                },
                                onCancel = { finish() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GstWidgetEditor(
    activeTheme: com.example.model.ThemePalette,
    onSave: (amount: Double, rate: Double, isAdd: Boolean) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var amountText by remember { mutableStateOf(WidgetDataManager.getGstAmount(context).toInt().toString()) }
    var selectedRate by remember { mutableDoubleStateOf(WidgetDataManager.getGstRate(context)) }
    var isAdd by remember { mutableStateOf(WidgetDataManager.isGstAdd(context)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = activeTheme.accentColor.copy(alpha = 0.2f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = activeTheme.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column {
                Text(
                    text = "Edit GST Widget",
                    color = activeTheme.screenTextColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Custom home screen values",
                    color = activeTheme.screenExpressionColor,
                    fontSize = 11.sp
                )
            }
        }
        IconButton(onClick = onCancel) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = activeTheme.screenExpressionColor)
        }
    }

    OutlinedTextField(
        value = amountText,
        onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
        label = { Text("Base Invoice Amount (₹)", color = activeTheme.screenExpressionColor) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = activeTheme.screenTextColor,
            unfocusedTextColor = activeTheme.screenTextColor,
            focusedBorderColor = activeTheme.accentColor,
            unfocusedBorderColor = activeTheme.cardBackground
        )
    )

    // Quick Stepper Chips
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf(1000, 5000, 10000, 50000).forEach { delta ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = activeTheme.cardBackground,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        val current = amountText.toDoubleOrNull() ?: 0.0
                        amountText = (current + delta).toInt().toString()
                    }
            ) {
                Text(
                    text = "+₹${delta / 1000}k",
                    color = activeTheme.accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 6.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    // Rate Slabs
    Text(text = "GST Slab Rate", color = activeTheme.screenExpressionColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf(5.0, 12.0, 18.0, 28.0).forEach { rate ->
            val isSelected = selectedRate == rate
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) activeTheme.accentColor else activeTheme.cardBackground,
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedRate = rate }
            ) {
                Text(
                    text = "${rate.toInt()}%",
                    color = if (isSelected) Color.White else activeTheme.screenTextColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    // Toggle Mode (+GST / -GST)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isAdd) activeTheme.accentColor else activeTheme.cardBackground,
            modifier = Modifier
                .weight(1f)
                .clickable { isAdd = true }
        ) {
            Text(
                text = "+ GST (Add)",
                color = if (isAdd) Color.White else activeTheme.screenTextColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (!isAdd) activeTheme.accentColor else activeTheme.cardBackground,
            modifier = Modifier
                .weight(1f)
                .clickable { isAdd = false }
        ) {
            Text(
                text = "- GST (Remove)",
                color = if (!isAdd) Color.White else activeTheme.screenTextColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Save Button
    Button(
        onClick = {
            val amount = amountText.toDoubleOrNull() ?: 25000.0
            onSave(amount, selectedRate, isAdd)
        },
        colors = ButtonDefaults.buttonColors(containerColor = activeTheme.accentColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Save & Update Widget", fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun EmiWidgetEditor(
    activeTheme: com.example.model.ThemePalette,
    onSave: (principal: Double, rate: Double, tenureYears: Int) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var principalText by remember { mutableStateOf(WidgetDataManager.getEmiPrincipal(context).toInt().toString()) }
    var rateText by remember { mutableStateOf(WidgetDataManager.getEmiRate(context).toString()) }
    var tenureYears by remember { mutableIntStateOf(WidgetDataManager.getEmiTenureYears(context)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = activeTheme.accentColor.copy(alpha = 0.2f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = activeTheme.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column {
                Text(
                    text = "Edit Loan EMI Widget",
                    color = activeTheme.screenTextColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Custom Principal & Tenure",
                    color = activeTheme.screenExpressionColor,
                    fontSize = 11.sp
                )
            }
        }
        IconButton(onClick = onCancel) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = activeTheme.screenExpressionColor)
        }
    }

    OutlinedTextField(
        value = principalText,
        onValueChange = { principalText = it.filter { char -> char.isDigit() } },
        label = { Text("Loan Principal (₹)", color = activeTheme.screenExpressionColor) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = rateText,
            onValueChange = { rateText = it.filter { char -> char.isDigit() || char == '.' } },
            label = { Text("Rate %", color = activeTheme.screenExpressionColor) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = tenureYears.toString(),
            onValueChange = { tenureYears = it.filter { char -> char.isDigit() }.toIntOrNull() ?: 1 },
            label = { Text("Tenure (Years)", color = activeTheme.screenExpressionColor) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        )
    }

    Button(
        onClick = {
            val p = principalText.toDoubleOrNull() ?: 1000000.0
            val r = rateText.toDoubleOrNull() ?: 8.5
            onSave(p, r, tenureYears)
        },
        colors = ButtonDefaults.buttonColors(containerColor = activeTheme.accentColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Save & Update Widget", fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun PercentageWidgetEditor(
    activeTheme: com.example.model.ThemePalette,
    onSave: (base: Double, rate: Double) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var baseText by remember { mutableStateOf(WidgetDataManager.getPercentageBase(context).toInt().toString()) }
    var rateText by remember { mutableStateOf(WidgetDataManager.getPercentageRate(context).toInt().toString()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Edit Percentage Widget",
            color = activeTheme.screenTextColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onCancel) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = activeTheme.screenExpressionColor)
        }
    }

    OutlinedTextField(
        value = baseText,
        onValueChange = { baseText = it.filter { char -> char.isDigit() || char == '.' } },
        label = { Text("Base Number", color = activeTheme.screenExpressionColor) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )

    OutlinedTextField(
        value = rateText,
        onValueChange = { rateText = it.filter { char -> char.isDigit() || char == '.' } },
        label = { Text("Percentage (%)", color = activeTheme.screenExpressionColor) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )

    Button(
        onClick = {
            val b = baseText.toDoubleOrNull() ?: 8500.0
            val r = rateText.toDoubleOrNull() ?: 15.0
            onSave(b, r)
        },
        colors = ButtonDefaults.buttonColors(containerColor = activeTheme.accentColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Save & Update Widget", fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun QuickCalcWidgetEditor(
    activeTheme: com.example.model.ThemePalette,
    onSave: (initialExpr: String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var exprText by remember { mutableStateOf(WidgetDataManager.getLastExpression(context)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Quick Calc Preset",
            color = activeTheme.screenTextColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onCancel) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = activeTheme.screenExpressionColor)
        }
    }

    OutlinedTextField(
        value = exprText,
        onValueChange = { exprText = it },
        label = { Text("Default Calculation or Number", color = activeTheme.screenExpressionColor) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )

    Button(
        onClick = { onSave(exprText) },
        colors = ButtonDefaults.buttonColors(containerColor = activeTheme.accentColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Save to Widget", fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

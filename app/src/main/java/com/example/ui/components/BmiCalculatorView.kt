package com.example.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.BmiCalculatorEngine
import com.example.domain.BmiCategory
import com.example.domain.BmiResult
import com.example.model.ThemePalette
import java.util.Locale
import kotlin.math.abs

@Composable
fun BmiCalculatorView(
    theme: ThemePalette,
    weightInput: String,
    heightInput: String,
    ageInput: String,
    isMetric: Boolean,
    isMale: Boolean,
    onWeightChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onToggleMetric: (Boolean) -> Unit,
    onToggleGender: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val weightKg = if (isMetric) {
        weightInput.replace(",", "").replace(" ", "").toDoubleOrNull() ?: 68.0
    } else {
        (weightInput.replace(",", "").replace(" ", "").toDoubleOrNull() ?: 150.0) * 0.45359237
    }

    val heightCm = if (isMetric) {
        heightInput.replace(",", "").replace(" ", "").toDoubleOrNull() ?: 172.0
    } else {
        (heightInput.replace(",", "").replace(" ", "").toDoubleOrNull() ?: 68.0) * 2.54
    }

    val age = ageInput.replace(",", "").replace(" ", "").toIntOrNull() ?: 25

    val bmiResult: BmiResult = remember(weightKg, heightCm, age, isMale) {
        BmiCalculatorEngine.calculate(weightKg, heightCm, age, isMale)
    }

    val categoryColor = Color(bmiResult.category.colorHex)
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // Landscape Mode: Result on LEFT, Parameters on RIGHT
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Pane: Results (Score Card, Visual Gauge, Health Insights)
            Column(
                modifier = Modifier
                    .weight(0.48f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BmiHeroScoreCard(
                    bmiResult = bmiResult,
                    categoryColor = categoryColor,
                    theme = theme,
                    isLandscape = true
                )
                BmiHealthInsightsCard(
                    bmiResult = bmiResult,
                    isMetric = isMetric,
                    theme = theme
                )
            }

            // Right Pane: Parameters (Unit/Gender, Height, Weight, Age)
            Column(
                modifier = Modifier
                    .weight(0.52f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BmiUnitAndGenderCard(
                    isMetric = isMetric,
                    isMale = isMale,
                    onToggleMetric = onToggleMetric,
                    onToggleGender = onToggleGender,
                    theme = theme
                )
                BmiMeasurementsCard(
                    heightInput = heightInput,
                    weightInput = weightInput,
                    ageInput = ageInput,
                    isMetric = isMetric,
                    onHeightChange = onHeightChange,
                    onWeightChange = onWeightChange,
                    onAgeChange = onAgeChange,
                    theme = theme
                )
            }
        }
    } else {
        // Portrait Mode: Parameters on top, Hero score, Measurements, Health insights
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BmiUnitAndGenderCard(
                isMetric = isMetric,
                isMale = isMale,
                onToggleMetric = onToggleMetric,
                onToggleGender = onToggleGender,
                theme = theme
            )
            BmiHeroScoreCard(
                bmiResult = bmiResult,
                categoryColor = categoryColor,
                theme = theme,
                isLandscape = false
            )
            BmiMeasurementsCard(
                heightInput = heightInput,
                weightInput = weightInput,
                ageInput = ageInput,
                isMetric = isMetric,
                onHeightChange = onHeightChange,
                onWeightChange = onWeightChange,
                onAgeChange = onAgeChange,
                theme = theme
            )
            BmiHealthInsightsCard(
                bmiResult = bmiResult,
                isMetric = isMetric,
                theme = theme
            )
        }
    }
}

@Composable
private fun BmiUnitAndGenderCard(
    isMetric: Boolean,
    isMale: Boolean,
    onToggleMetric: (Boolean) -> Unit,
    onToggleGender: (Boolean) -> Unit,
    theme: ThemePalette
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(theme.borderWidthDp, theme.screenBorderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = theme.screenBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Unit Switcher (Metric kg/cm vs Imperial lbs/in)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(theme.surfaceColor, RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                val metricSelected = isMetric
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (metricSelected) theme.accentColor else Color.Transparent)
                        .clickable { onToggleMetric(true) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Metric (kg, cm)",
                        color = if (metricSelected) (if (theme.accentColor.luminance() > 0.6f) Color(0xFF211118) else Color.White) else theme.screenExpressionColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!metricSelected) theme.accentColor else Color.Transparent)
                        .clickable { onToggleMetric(false) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "US (lbs, in)",
                        color = if (!metricSelected) (if (theme.accentColor.luminance() > 0.6f) Color(0xFF211118) else Color.White) else theme.screenExpressionColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Gender Switcher
            Row(
                modifier = Modifier
                    .background(theme.surfaceColor, RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isMale) theme.accentColor.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { onToggleGender(true) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Male,
                        contentDescription = "Male",
                        tint = if (isMale) theme.accentColor else theme.screenExpressionColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isMale) theme.secondaryAccent.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { onToggleGender(false) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Female,
                        contentDescription = "Female",
                        tint = if (!isMale) theme.secondaryAccent else theme.screenExpressionColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BmiHeroScoreCard(
    bmiResult: BmiResult,
    categoryColor: Color,
    theme: ThemePalette,
    isLandscape: Boolean
) {
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
                .padding(if (isLandscape) 12.dp else 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BODY MASS INDEX (BMI)",
                    color = theme.accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Surface(
                    color = categoryColor.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = bmiResult.category.label,
                        color = categoryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 14.dp))

            // Big Numerical BMI Display
            Text(
                text = String.format(Locale.US, "%.1f", bmiResult.bmi),
                color = categoryColor,
                fontSize = if (isLandscape) 44.sp else 54.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = bmiResult.category.description,
                color = theme.screenExpressionColor,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(if (isLandscape) 10.dp else 16.dp))

            // Continuous Visual Multi-Segment BMI Gauge
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(18.5f - 12f).fillMaxSize().background(Color(0xFF00B4D8))) // Underweight
                        Box(modifier = Modifier.weight(24.9f - 18.5f).fillMaxSize().background(Color(0xFF2EC4B6))) // Normal
                        Box(modifier = Modifier.weight(29.9f - 24.9f).fillMaxSize().background(Color(0xFFFFB703))) // Overweight
                        Box(modifier = Modifier.weight(34.9f - 29.9f).fillMaxSize().background(Color(0xFFFB8500))) // Obese 1
                        Box(modifier = Modifier.weight(42f - 34.9f).fillMaxSize().background(Color(0xFFE63946))) // Obese 2/3
                    }
                }

                // Indicator Needle Triangle / Dot
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxWidth(bmiResult.gaugePercent)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(10.dp)
                                .background(theme.screenTextColor, CircleShape)
                        )
                    }
                }

                // Gauge Scale Labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("16.0", color = theme.screenExpressionColor, fontSize = 9.sp)
                    Text("18.5", color = theme.screenExpressionColor, fontSize = 9.sp)
                    Text("25.0", color = theme.screenExpressionColor, fontSize = 9.sp)
                    Text("30.0", color = theme.screenExpressionColor, fontSize = 9.sp)
                    Text("40.0", color = theme.screenExpressionColor, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun BmiMeasurementsCard(
    heightInput: String,
    weightInput: String,
    ageInput: String,
    isMetric: Boolean,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    theme: ThemePalette
) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "BODY MEASUREMENTS",
                color = theme.accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Height Input Row
            BmiInputRow(
                label = if (isMetric) "Height (cm)" else "Height (in)",
                value = heightInput,
                onValueChange = onHeightChange,
                onIncrement = {
                    val v = (heightInput.toDoubleOrNull() ?: 170.0) + 1.0
                    onHeightChange(v.toInt().toString())
                },
                onDecrement = {
                    val v = ((heightInput.toDoubleOrNull() ?: 170.0) - 1.0).coerceAtLeast(30.0)
                    onHeightChange(v.toInt().toString())
                },
                icon = Icons.Default.Height,
                theme = theme
            )

            HorizontalDivider(color = theme.screenBorderColor.copy(alpha = 0.2f))

            // Weight Input Row
            BmiInputRow(
                label = if (isMetric) "Weight (kg)" else "Weight (lbs)",
                value = weightInput,
                onValueChange = onWeightChange,
                onIncrement = {
                    val v = (weightInput.toDoubleOrNull() ?: 65.0) + 1.0
                    onWeightChange(v.toInt().toString())
                },
                onDecrement = {
                    val v = ((weightInput.toDoubleOrNull() ?: 65.0) - 1.0).coerceAtLeast(10.0)
                    onWeightChange(v.toInt().toString())
                },
                icon = Icons.Default.Scale,
                theme = theme
            )

            HorizontalDivider(color = theme.screenBorderColor.copy(alpha = 0.2f))

            // Age Input Row
            BmiInputRow(
                label = "Age (years)",
                value = ageInput,
                onValueChange = onAgeChange,
                onIncrement = {
                    val v = (ageInput.toIntOrNull() ?: 25) + 1
                    onAgeChange(v.toString())
                },
                onDecrement = {
                    val v = ((ageInput.toIntOrNull() ?: 25) - 1).coerceAtLeast(1)
                    onAgeChange(v.toString())
                },
                icon = Icons.Default.FitnessCenter,
                theme = theme
            )
        }
    }
}

@Composable
private fun BmiHealthInsightsCard(
    bmiResult: BmiResult,
    isMetric: Boolean,
    theme: ThemePalette
) {
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
            Text(
                text = "HEALTH & WEIGHT INSIGHTS",
                color = theme.accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            val weightUnit = if (isMetric) "kg" else "lbs"
            val minHealthy = if (isMetric) bmiResult.minHealthyWeightKg else (bmiResult.minHealthyWeightKg * 2.20462)
            val maxHealthy = if (isMetric) bmiResult.maxHealthyWeightKg else (bmiResult.maxHealthyWeightKg * 2.20462)
            val idealWeight = if (isMetric) bmiResult.idealWeightKg else (bmiResult.idealWeightKg * 2.20462)
            val weightDiff = if (isMetric) bmiResult.weightDifferenceKg else (bmiResult.weightDifferenceKg * 2.20462)

            HealthStatRow(
                label = "Healthy Weight Range",
                value = String.format(Locale.US, "%.1f - %.1f %s", minHealthy, maxHealthy, weightUnit),
                theme = theme
            )

            HealthStatRow(
                label = "Target Ideal Weight",
                value = String.format(Locale.US, "%.1f %s (BMI 21.7)", idealWeight, weightUnit),
                theme = theme
            )

            val diffText = if (abs(weightDiff) <= 0.5) {
                "Perfect (Within Ideal Range)"
            } else if (weightDiff > 0) {
                String.format(Locale.US, "Lose %.1f %s for BMI 21.7", weightDiff, weightUnit)
            } else {
                String.format(Locale.US, "Gain %.1f %s for BMI 21.7", abs(weightDiff), weightUnit)
            }

            HealthStatRow(
                label = "Weight Goal",
                value = diffText,
                theme = theme,
                highlightColor = if (abs(weightDiff) <= 0.5) Color(0xFF2EC4B6) else theme.secondaryAccent
            )

            HorizontalDivider(color = theme.screenBorderColor.copy(alpha = 0.2f))

            HealthStatRow(
                label = "Basal Metabolic Rate (BMR)",
                value = "${bmiResult.bmrKcal} kcal/day",
                theme = theme
            )

            HealthStatRow(
                label = "Daily Maintenance Energy",
                value = "${bmiResult.maintenanceCaloriesKcal} kcal/day",
                theme = theme
            )

            HealthStatRow(
                label = "BMI Prime Ratio",
                value = "${bmiResult.primeBmi} (Optimal: 0.74 - 1.00)",
                theme = theme
            )

            HealthStatRow(
                label = "Ponderal Index",
                value = String.format(Locale.US, "%.1f kg/m³", bmiResult.ponderalIndex),
                theme = theme
            )
        }
    }
}

@Composable
private fun BmiInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    theme: ThemePalette
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = theme.accentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                color = theme.screenTextColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = onDecrement,
                modifier = Modifier
                    .size(32.dp)
                    .background(theme.surfaceColor, CircleShape)
                    .border(1.dp, theme.screenBorderColor.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = theme.screenTextColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(72.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = theme.screenTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = theme.screenTextColor,
                    unfocusedTextColor = theme.screenTextColor,
                    focusedContainerColor = theme.cardBackground,
                    unfocusedContainerColor = theme.cardBackground,
                    focusedBorderColor = theme.accentColor,
                    unfocusedBorderColor = theme.screenBorderColor.copy(alpha = 0.5f)
                )
            )

            IconButton(
                onClick = onIncrement,
                modifier = Modifier
                    .size(32.dp)
                    .background(theme.surfaceColor, CircleShape)
                    .border(1.dp, theme.screenBorderColor.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = theme.screenTextColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun HealthStatRow(
    label: String,
    value: String,
    theme: ThemePalette,
    highlightColor: Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = theme.screenExpressionColor,
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = highlightColor ?: theme.screenTextColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

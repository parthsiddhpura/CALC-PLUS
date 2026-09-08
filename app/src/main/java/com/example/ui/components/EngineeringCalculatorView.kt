package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.EngineeringCalculationResult
import com.example.domain.EngineeringCategory
import com.example.domain.EngineeringConstantItem
import com.example.domain.EngineeringEngine
import com.example.model.ThemePalette

private fun String.toCleanDoubleOrNull(): Double? =
    this.replace(",", "").replace(" ", "").toDoubleOrNull()

@Composable
fun EngineeringCalculatorView(
    theme: ThemePalette,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(EngineeringCategory.OHMS_LAW) }
    val scrollState = rememberScrollState()
    val categoryScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Top Category Bar (Horizontally scrollable) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(categoryScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EngineeringCategory.values().forEach { cat ->
                val isSelected = cat == selectedCategory
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) theme.accentColor else theme.surfaceColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedCategory = cat }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(cat.iconName, fontSize = 14.sp)
                        Text(
                            text = cat.displayName,
                            color = if (isSelected) theme.backgroundColor else theme.screenTextColor,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // --- 2. Category Content ---
        when (selectedCategory) {
            EngineeringCategory.OHMS_LAW -> OhmsLawSection(theme)
            EngineeringCategory.CIRCUITS -> CircuitsSection(theme)
            EngineeringCategory.RLC_RESONANCE -> RlcResonanceSection(theme)
            EngineeringCategory.THREE_PHASE_POWER -> ThreePhasePowerSection(theme)
            EngineeringCategory.MECHANICS -> MechanicsSection(theme)
            EngineeringCategory.KINEMATICS -> KinematicsSection(theme)
            EngineeringCategory.ENERGY_POWER -> EnergyPowerSection(theme)
            EngineeringCategory.STRESS_STRAIN -> StressStrainSection(theme)
            EngineeringCategory.STRUCTURAL -> StructuralBeamSection(theme)
            EngineeringCategory.REYNOLDS_PIPE -> ReynoldsFlowSection(theme)
            EngineeringCategory.FLUID_THERMAL -> FluidThermalSection(theme)
            EngineeringCategory.THERMAL_EXPANSION -> ThermalExpansionSection(theme)
            EngineeringCategory.GEARS_PULLEYS -> GearsTransmissionSection(theme)
            EngineeringCategory.ENG_PREFIXES -> EngPrefixSection(theme)
            EngineeringCategory.CONSTANTS -> EngineeringConstantsSection(theme)
        }
    }
}

// ---------------------- 1. OHM'S LAW ----------------------
@Composable
private fun OhmsLawSection(theme: ThemePalette) {
    var voltageInput by remember { mutableStateOf("12") }
    var currentInput by remember { mutableStateOf("2") }
    var resistanceInput by remember { mutableStateOf("") }
    var powerInput by remember { mutableStateOf("") }

    val v = voltageInput.toCleanDoubleOrNull()
    val i = currentInput.toCleanDoubleOrNull()
    val r = resistanceInput.toCleanDoubleOrNull()
    val p = powerInput.toCleanDoubleOrNull()

    val result = remember(v, i, r, p) {
        EngineeringEngine.calcOhmsLaw(v, i, r, p)
    }

    EngineeringCalculatorCard(
        title = "Ohm's Law & Power (V, I, R, P)",
        icon = "⚡",
        theme = theme
    ) {
        Text(
            text = "Fill in any 2 values. The engine automatically solves the remaining electrical parameters.",
            color = theme.screenExpressionColor,
            fontSize = 11.sp
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EngineeringInput(
                value = voltageInput,
                onValueChange = { voltageInput = it },
                label = "Voltage (V)",
                unit = "Volts",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            EngineeringInput(
                value = currentInput,
                onValueChange = { currentInput = it },
                label = "Current (I)",
                unit = "Amps",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EngineeringInput(
                value = resistanceInput,
                onValueChange = { resistanceInput = it },
                label = "Resistance (R)",
                unit = "Ohms (Ω)",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            EngineeringInput(
                value = powerInput,
                onValueChange = { powerInput = it },
                label = "Power (P)",
                unit = "Watts (W)",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }

        EngineeringResultBox(result = result, theme = theme)
    }
}

// ---------------------- 2. CIRCUITS & RC ----------------------
@Composable
private fun CircuitsSection(theme: ThemePalette) {
    var r1Input by remember { mutableStateOf("100") }
    var r2Input by remember { mutableStateOf("220") }
    var r3Input by remember { mutableStateOf("0") }

    var rFilterInput by remember { mutableStateOf("1000") }
    var cFilterInput by remember { mutableStateOf("10") }

    val r1 = r1Input.toCleanDoubleOrNull() ?: 0.0
    val r2 = r2Input.toCleanDoubleOrNull() ?: 0.0
    val r3 = r3Input.toCleanDoubleOrNull() ?: 0.0
    val rFilter = rFilterInput.toCleanDoubleOrNull() ?: 1000.0
    val cFilter = cFilterInput.toCleanDoubleOrNull() ?: 10.0

    val resistorResult = remember(r1, r2, r3) {
        EngineeringEngine.calcSeriesParallel(r1, r2, r3)
    }

    val rcResult = remember(rFilter, cFilter) {
        EngineeringEngine.calcRcCircuit(rFilter, cFilter)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        EngineeringCalculatorCard(
            title = "Series & Parallel Resistors",
            icon = "🔌",
            theme = theme
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EngineeringInput(
                    value = r1Input,
                    onValueChange = { r1Input = it },
                    label = "Resistor R1",
                    unit = "Ω",
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                EngineeringInput(
                    value = r2Input,
                    onValueChange = { r2Input = it },
                    label = "Resistor R2",
                    unit = "Ω",
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                EngineeringInput(
                    value = r3Input,
                    onValueChange = { r3Input = it },
                    label = "Resistor R3",
                    unit = "Ω (opt)",
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
            }
            EngineeringResultBox(result = resistorResult, theme = theme)
        }

        EngineeringCalculatorCard(
            title = "RC Low-Pass / High-Pass Cutoff Filter",
            icon = "📡",
            theme = theme
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EngineeringInput(
                    value = rFilterInput,
                    onValueChange = { rFilterInput = it },
                    label = "Resistance",
                    unit = "Ω",
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                EngineeringInput(
                    value = cFilterInput,
                    onValueChange = { cFilterInput = it },
                    label = "Capacitance",
                    unit = "µF",
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
            }
            EngineeringResultBox(result = rcResult, theme = theme)
        }
    }
}

// ---------------------- 2B. RLC RESONANCE & AC REACTANCE ----------------------
@Composable
private fun RlcResonanceSection(theme: ThemePalette) {
    var rInput by remember { mutableStateOf("50") }
    var lInput by remember { mutableStateOf("10") }
    var cInput by remember { mutableStateOf("1") }
    var freqInput by remember { mutableStateOf("1000") }

    val r = rInput.toCleanDoubleOrNull() ?: 50.0
    val l = lInput.toCleanDoubleOrNull() ?: 10.0
    val c = cInput.toCleanDoubleOrNull() ?: 1.0
    val f = freqInput.toCleanDoubleOrNull() ?: 1000.0

    val rlcResult = remember(r, l, c, f) {
        EngineeringEngine.calcRlcResonance(r, l, c, f)
    }

    EngineeringCalculatorCard(
        title = "RLC Resonance & AC Impedance",
        icon = "📻",
        theme = theme
    ) {
        Text(
            text = "Calculate resonant frequency f₀, inductive reactance XL, capacitive reactance XC, and total impedance Z.",
            color = theme.screenExpressionColor,
            fontSize = 11.sp
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EngineeringInput(
                value = rInput,
                onValueChange = { rInput = it },
                label = "Resistance (R)",
                unit = "Ω",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            EngineeringInput(
                value = lInput,
                onValueChange = { lInput = it },
                label = "Inductance (L)",
                unit = "mH",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EngineeringInput(
                value = cInput,
                onValueChange = { cInput = it },
                label = "Capacitance (C)",
                unit = "µF",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            EngineeringInput(
                value = freqInput,
                onValueChange = { freqInput = it },
                label = "AC Frequency (f)",
                unit = "Hz",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Presets:", color = theme.screenExpressionColor, fontSize = 11.sp)
            listOf("50" to "50 Hz", "60" to "60 Hz", "1000" to "1 kHz", "10000" to "10 kHz").forEach { (v, label) ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (freqInput == v) theme.accentColor else theme.surfaceColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { freqInput = v }
                ) {
                    Text(
                        text = label,
                        color = if (freqInput == v) theme.backgroundColor else theme.screenTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        EngineeringResultBox(result = rlcResult, theme = theme)
    }
}

// ---------------------- 2C. 3-PHASE AC POWER ----------------------
@Composable
private fun ThreePhasePowerSection(theme: ThemePalette) {
    var lineVoltageInput by remember { mutableStateOf("415") }
    var lineCurrentInput by remember { mutableStateOf("25") }
    var powerFactorInput by remember { mutableStateOf("0.85") }

    val vl = lineVoltageInput.toCleanDoubleOrNull() ?: 415.0
    val il = lineCurrentInput.toCleanDoubleOrNull() ?: 25.0
    val pf = powerFactorInput.toCleanDoubleOrNull() ?: 0.85

    val powerResult = remember(vl, il, pf) {
        EngineeringEngine.calcThreePhasePower(vl, il, pf)
    }

    EngineeringCalculatorCard(
        title = "3-Phase Industrial AC Power & PF",
        icon = "🏭",
        theme = theme
    ) {
        Text(
            text = "Solves symmetrical 3-phase Active (kW), Apparent (kVA), Reactive (kVAR) and power factor capacitor correction.",
            color = theme.screenExpressionColor,
            fontSize = 11.sp
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EngineeringInput(
                value = lineVoltageInput,
                onValueChange = { lineVoltageInput = it },
                label = "Line Voltage (V_L)",
                unit = "Volts",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            EngineeringInput(
                value = lineCurrentInput,
                onValueChange = { lineCurrentInput = it },
                label = "Line Current (I_L)",
                unit = "Amps",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }

        EngineeringInput(
            value = powerFactorInput,
            onValueChange = { powerFactorInput = it },
            label = "Power Factor (cos φ, 0.1 to 1.0)",
            unit = "0.85",
            theme = theme,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("PF presets:", color = theme.screenExpressionColor, fontSize = 11.sp)
            listOf("0.80", "0.85", "0.90", "0.95", "1.0").forEach { v ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (powerFactorInput == v) theme.accentColor else theme.surfaceColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { powerFactorInput = v }
                ) {
                    Text(
                        text = v,
                        color = if (powerFactorInput == v) theme.backgroundColor else theme.screenTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        EngineeringResultBox(result = powerResult, theme = theme)
    }
}

// ---------------------- 3. MECHANICS ----------------------
@Composable
private fun MechanicsSection(theme: ThemePalette) {
    var massInput by remember { mutableStateOf("15") }
    var accelInput by remember { mutableStateOf("9.81") }

    var forceInput by remember { mutableStateOf("50") }
    var radiusInput by remember { mutableStateOf("0.25") }
    var rpmInput by remember { mutableStateOf("1500") }

    val m = massInput.toCleanDoubleOrNull() ?: 0.0
    val a = accelInput.toCleanDoubleOrNull() ?: 0.0
    val f = forceInput.toCleanDoubleOrNull() ?: 0.0
    val r = radiusInput.toCleanDoubleOrNull() ?: 0.0
    val rpm = rpmInput.toCleanDoubleOrNull() ?: 0.0

    val forceResult = remember(m, a) { EngineeringEngine.calcNewtonForce(m, a) }
    val torqueResult = remember(f, r, rpm) { EngineeringEngine.calcTorquePower(f, r, rpm) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        EngineeringCalculatorCard(
            title = "Newton's 2nd Law Force (F = m · a)",
            icon = "⚙️",
            theme = theme
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EngineeringInput(
                    value = massInput,
                    onValueChange = { massInput = it },
                    label = "Mass (m)",
                    unit = "kg",
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                EngineeringInput(
                    value = accelInput,
                    onValueChange = { accelInput = it },
                    label = "Acceleration (a)",
                    unit = "m/s²",
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
            }
            EngineeringResultBox(result = forceResult, theme = theme)
        }

        EngineeringCalculatorCard(
            title = "Torque & Shaft Rotational Power",
            icon = "🔄",
            theme = theme
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EngineeringInput(
                    value = forceInput,
                    onValueChange = { forceInput = it },
                    label = "Force (F)",
                    unit = "N",
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                EngineeringInput(
                    value = radiusInput,
                    onValueChange = { radiusInput = it },
                    label = "Moment Arm (r)",
                    unit = "meters",
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                EngineeringInput(
                    value = rpmInput,
                    onValueChange = { rpmInput = it },
                    label = "Speed",
                    unit = "RPM",
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
            }
            EngineeringResultBox(result = torqueResult, theme = theme)
        }
    }
}

// ---------------------- 3B. KINEMATICS & PROJECTILE MOTION ----------------------
@Composable
private fun KinematicsSection(theme: ThemePalette) {
    var velocityInput by remember { mutableStateOf("45") }
    var angleInput by remember { mutableStateOf("45") }
    var heightInput by remember { mutableStateOf("0") }

    val v = velocityInput.toCleanDoubleOrNull() ?: 45.0
    val ang = angleInput.toCleanDoubleOrNull() ?: 45.0
    val h = heightInput.toCleanDoubleOrNull() ?: 0.0

    val kinResult = remember(v, ang, h) {
        EngineeringEngine.calcKinematicsProjectile(v, ang, h)
    }

    EngineeringCalculatorCard(
        title = "Kinematics & Ballistic Projectile",
        icon = "🎯",
        theme = theme
    ) {
        Text(
            text = "Solves trajectory, apex height, total hang time, horizontal range, and impact velocity under gravity.",
            color = theme.screenExpressionColor,
            fontSize = 11.sp
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EngineeringInput(
                value = velocityInput,
                onValueChange = { velocityInput = it },
                label = "Launch Speed (v₀)",
                unit = "m/s",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            EngineeringInput(
                value = angleInput,
                onValueChange = { angleInput = it },
                label = "Launch Angle (θ)",
                unit = "degrees",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }

        EngineeringInput(
            value = heightInput,
            onValueChange = { heightInput = it },
            label = "Initial Launch Height (h₀, optional)",
            unit = "meters (e.g. cliff)",
            theme = theme,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Angle:", color = theme.screenExpressionColor, fontSize = 11.sp)
            listOf("30", "45", "60", "75").forEach { angVal ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (angleInput == angVal) theme.accentColor else theme.surfaceColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { angleInput = angVal }
                ) {
                    Text(
                        text = "$angVal°",
                        color = if (angleInput == angVal) theme.backgroundColor else theme.screenTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        EngineeringResultBox(result = kinResult, theme = theme)
    }
}

// ---------------------- 4. ENERGY & POWER ----------------------
@Composable
private fun EnergyPowerSection(theme: ThemePalette) {
    var massInput by remember { mutableStateOf("70") }
    var velocityInput by remember { mutableStateOf("25") }
    var heightInput by remember { mutableStateOf("10") }

    val m = massInput.toCleanDoubleOrNull() ?: 0.0
    val v = velocityInput.toCleanDoubleOrNull() ?: 0.0
    val h = heightInput.toCleanDoubleOrNull() ?: 0.0

    val energyResult = remember(m, v, h) {
        EngineeringEngine.calcKineticPotentialEnergy(m, v, h)
    }

    EngineeringCalculatorCard(
        title = "Kinetic & Potential Energy Conservation",
        icon = "🔋",
        theme = theme
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EngineeringInput(
                value = massInput,
                onValueChange = { massInput = it },
                label = "Mass (m)",
                unit = "kg",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            EngineeringInput(
                value = velocityInput,
                onValueChange = { velocityInput = it },
                label = "Velocity (v)",
                unit = "m/s",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            EngineeringInput(
                value = heightInput,
                onValueChange = { heightInput = it },
                label = "Height (h)",
                unit = "meters",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }
        EngineeringResultBox(result = energyResult, theme = theme)
    }
}

// ---------------------- 5. STRESS & STRAIN ----------------------
@Composable
private fun StressStrainSection(theme: ThemePalette) {
    var forceInput by remember { mutableStateOf("25000") }
    var areaInput by remember { mutableStateOf("150") }
    var origLenInput by remember { mutableStateOf("1000") }
    var deltaLenInput by remember { mutableStateOf("1.2") }

    val f = forceInput.toCleanDoubleOrNull() ?: 0.0
    val a = areaInput.toCleanDoubleOrNull() ?: 0.0
    val l0 = origLenInput.toCleanDoubleOrNull() ?: 1000.0
    val dl = deltaLenInput.toCleanDoubleOrNull() ?: 1.0

    val stressResult = remember(f, a, l0, dl) {
        EngineeringEngine.calcStressStrain(f, a, l0, dl)
    }

    EngineeringCalculatorCard(
        title = "Mechanical Stress, Strain & Young's Modulus",
        icon = "🧱",
        theme = theme
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EngineeringInput(
                value = forceInput,
                onValueChange = { forceInput = it },
                label = "Applied Load (F)",
                unit = "N",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            EngineeringInput(
                value = areaInput,
                onValueChange = { areaInput = it },
                label = "Cross-Section Area (A)",
                unit = "mm²",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EngineeringInput(
                value = origLenInput,
                onValueChange = { origLenInput = it },
                label = "Initial Length (L₀)",
                unit = "mm",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            EngineeringInput(
                value = deltaLenInput,
                onValueChange = { deltaLenInput = it },
                label = "Elongation (ΔL)",
                unit = "mm",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }

        EngineeringResultBox(result = stressResult, theme = theme)
    }
}

// ---------------------- 6. FLUIDS & THERMAL ----------------------
@Composable
private fun FluidThermalSection(theme: ThemePalette) {
    var forceInput by remember { mutableStateOf("5000") }
    var areaInput by remember { mutableStateOf("25") }
    var depthInput by remember { mutableStateOf("5") }

    var heatMassInput by remember { mutableStateOf("10") }
    var specificHeatInput by remember { mutableStateOf("4184") } // Water = 4184 J/kg°C
    var deltaTInput by remember { mutableStateOf("40") }

    val f = forceInput.toCleanDoubleOrNull() ?: 0.0
    val a = areaInput.toCleanDoubleOrNull() ?: 0.0
    val d = depthInput.toCleanDoubleOrNull() ?: 0.0

    val hm = heatMassInput.toCleanDoubleOrNull() ?: 0.0
    val cp = specificHeatInput.toCleanDoubleOrNull() ?: 4184.0
    val dt = deltaTInput.toCleanDoubleOrNull() ?: 0.0

    val fluidResult = remember(f, a, d) { EngineeringEngine.calcHydraulicPressure(f, a, d) }
    val heatResult = remember(hm, cp, dt) { EngineeringEngine.calcThermalHeatTransfer(hm, cp, dt) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        EngineeringCalculatorCard(
            title = "Hydraulic Pressure & Hydrostatic Depth",
            icon = "💧",
            theme = theme
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EngineeringInput(
                    value = forceInput,
                    onValueChange = { forceInput = it },
                    label = "Piston Force",
                    unit = "N",
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                EngineeringInput(
                    value = areaInput,
                    onValueChange = { areaInput = it },
                    label = "Piston Area",
                    unit = "cm²",
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                EngineeringInput(
                    value = depthInput,
                    onValueChange = { depthInput = it },
                    label = "Depth (h)",
                    unit = "meters",
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
            }
            EngineeringResultBox(result = fluidResult, theme = theme)
        }

        EngineeringCalculatorCard(
            title = "Thermodynamic Heat Transfer (Q = m · c · ΔT)",
            icon = "🌡️",
            theme = theme
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EngineeringInput(
                    value = heatMassInput,
                    onValueChange = { heatMassInput = it },
                    label = "Mass (m)",
                    unit = "kg",
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                EngineeringInput(
                    value = specificHeatInput,
                    onValueChange = { specificHeatInput = it },
                    label = "Specific Heat (c)",
                    unit = "J/(kg·°C)",
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                EngineeringInput(
                    value = deltaTInput,
                    onValueChange = { deltaTInput = it },
                    label = "Temp Rise (ΔT)",
                    unit = "°C",
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
            }
            EngineeringResultBox(result = heatResult, theme = theme)
        }
    }
}

// ---------------------- 7. STRUCTURAL BEAM ----------------------
@Composable
private fun StructuralBeamSection(theme: ThemePalette) {
    var loadKnInput by remember { mutableStateOf("15") }
    var lengthInput by remember { mutableStateOf("6") }
    var isUdl by remember { mutableStateOf(false) }
    var modulusGpaInput by remember { mutableStateOf("200") }
    var inertiaInput by remember { mutableStateOf("5000") }

    val load = loadKnInput.toCleanDoubleOrNull() ?: 0.0
    val len = lengthInput.toCleanDoubleOrNull() ?: 0.0
    val mod = modulusGpaInput.toCleanDoubleOrNull() ?: 200.0
    val i = inertiaInput.toCleanDoubleOrNull() ?: 5000.0

    val beamResult = remember(load, len, isUdl, mod, i) {
        EngineeringEngine.calcBeamBending(load, len, isUdl, mod, i)
    }

    EngineeringCalculatorCard(
        title = "Beam Bending Moment & Deflection",
        icon = "🏗️",
        theme = theme
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (!isUdl) theme.accentColor else theme.surfaceColor,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { isUdl = false }
            ) {
                Text(
                    text = "Point Load (P)",
                    color = if (!isUdl) theme.backgroundColor else theme.screenTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isUdl) theme.accentColor else theme.surfaceColor,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { isUdl = true }
            ) {
                Text(
                    text = "Uniform Load (w)",
                    color = if (isUdl) theme.backgroundColor else theme.screenTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EngineeringInput(
                value = loadKnInput,
                onValueChange = { loadKnInput = it },
                label = if (isUdl) "Load w (kN/m)" else "Load P (kN)",
                unit = if (isUdl) "kN/m" else "kN",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            EngineeringInput(
                value = lengthInput,
                onValueChange = { lengthInput = it },
                label = "Span Length (L)",
                unit = "meters",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EngineeringInput(
                value = modulusGpaInput,
                onValueChange = { modulusGpaInput = it },
                label = "Young's Modulus",
                unit = "GPa (Steel=200)",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            EngineeringInput(
                value = inertiaInput,
                onValueChange = { inertiaInput = it },
                label = "Moment of Inertia (I)",
                unit = "cm⁴",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }

        EngineeringResultBox(result = beamResult, theme = theme)
    }
}

// ---------------------- 7B. REYNOLDS NUMBER & FLUID FLOW ----------------------
@Composable
private fun ReynoldsFlowSection(theme: ThemePalette) {
    var velocityInput by remember { mutableStateOf("2.5") }
    var diameterInput by remember { mutableStateOf("50") }
    var densityInput by remember { mutableStateOf("1000") }
    var viscosityInput by remember { mutableStateOf("0.001") }

    val v = velocityInput.toCleanDoubleOrNull() ?: 2.5
    val d = diameterInput.toCleanDoubleOrNull() ?: 50.0
    val rho = densityInput.toCleanDoubleOrNull() ?: 1000.0
    val mu = viscosityInput.toCleanDoubleOrNull() ?: 0.001

    val flowResult = remember(v, d, rho, mu) {
        EngineeringEngine.calcReynoldsPipe(v, d, rho, mu)
    }

    EngineeringCalculatorCard(
        title = "Reynolds Number & Pipe Fluid Dynamics",
        icon = "🌊",
        theme = theme
    ) {
        Text(
            text = "Calculates Reynolds dimensionless number Re, Flow Regime (Laminar / Transitional / Turbulent), and flow rates.",
            color = theme.screenExpressionColor,
            fontSize = 11.sp
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EngineeringInput(
                value = velocityInput,
                onValueChange = { velocityInput = it },
                label = "Flow Velocity (v)",
                unit = "m/s",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            EngineeringInput(
                value = diameterInput,
                onValueChange = { diameterInput = it },
                label = "Pipe Diameter (D)",
                unit = "mm",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EngineeringInput(
                value = densityInput,
                onValueChange = { densityInput = it },
                label = "Fluid Density (ρ)",
                unit = "kg/m³",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            EngineeringInput(
                value = viscosityInput,
                onValueChange = { viscosityInput = it },
                label = "Dynamic Viscosity (μ)",
                unit = "Pa·s",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Fluid:", color = theme.screenExpressionColor, fontSize = 11.sp)
            listOf(
                Triple("Water", "1000", "0.001"),
                Triple("Air", "1.2", "0.000018"),
                Triple("Oil (SAE 30)", "900", "0.2"),
                Triple("Glycerol", "1260", "1.4")
            ).forEach { (name, rhoVal, muVal) ->
                val isSelected = densityInput == rhoVal && viscosityInput == muVal
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) theme.accentColor else theme.surfaceColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            densityInput = rhoVal
                            viscosityInput = muVal
                        }
                ) {
                    Text(
                        text = name,
                        color = if (isSelected) theme.backgroundColor else theme.screenTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        EngineeringResultBox(result = flowResult, theme = theme)
    }
}

// ---------------------- 7C. THERMAL EXPANSION & CONDUCTION ----------------------
@Composable
private fun ThermalExpansionSection(theme: ThemePalette) {
    var lengthInput by remember { mutableStateOf("10") }
    var deltaTempInput by remember { mutableStateOf("40") }
    var alphaInput by remember { mutableStateOf("12") }
    var conductKInput by remember { mutableStateOf("50") }
    var wallThickInput by remember { mutableStateOf("0.2") }

    val l0 = lengthInput.toCleanDoubleOrNull() ?: 10.0
    val dt = deltaTempInput.toCleanDoubleOrNull() ?: 40.0
    val alpha = alphaInput.toCleanDoubleOrNull() ?: 12.0
    val k = conductKInput.toCleanDoubleOrNull() ?: 50.0
    val thick = wallThickInput.toCleanDoubleOrNull() ?: 0.2

    val thermResult = remember(l0, dt, alpha, k, thick) {
        EngineeringEngine.calcThermalExpansionConduction(
            origLengthMeters = l0,
            tempDeltaC = dt,
            alphaPpmPerC = alpha,
            thermalConductivityK = k,
            areaM2 = 1.0,
            thicknessM = thick
        )
    }

    EngineeringCalculatorCard(
        title = "Thermal Expansion & 1D Heat Conduction",
        icon = "🌡️",
        theme = theme
    ) {
        Text(
            text = "Calculates linear elongation ΔL, thermal strain ε, and 1D Fourier heat conduction through structural materials.",
            color = theme.screenExpressionColor,
            fontSize = 11.sp
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EngineeringInput(
                value = lengthInput,
                onValueChange = { lengthInput = it },
                label = "Initial Length (L₀)",
                unit = "meters",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            EngineeringInput(
                value = deltaTempInput,
                onValueChange = { deltaTempInput = it },
                label = "Temp Change (ΔT)",
                unit = "°C / K",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }

        EngineeringInput(
            value = alphaInput,
            onValueChange = { alphaInput = it },
            label = "Expansion Coeff (α × 10⁻⁶ /°C)",
            unit = "ppm/°C",
            theme = theme,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Material:", color = theme.screenExpressionColor, fontSize = 11.sp)
            listOf(
                Triple("Steel", "12", "50"),
                Triple("Aluminum", "23", "205"),
                Triple("Copper", "17", "385"),
                Triple("Concrete", "12", "1.4"),
                Triple("Glass", "9", "0.8")
            ).forEach { (mat, aVal, kVal) ->
                val isSelected = alphaInput == aVal
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) theme.accentColor else theme.surfaceColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            alphaInput = aVal
                            conductKInput = kVal
                        }
                ) {
                    Text(
                        text = mat,
                        color = if (isSelected) theme.backgroundColor else theme.screenTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EngineeringInput(
                value = conductKInput,
                onValueChange = { conductKInput = it },
                label = "Conductivity (k)",
                unit = "W/m·K",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            EngineeringInput(
                value = wallThickInput,
                onValueChange = { wallThickInput = it },
                label = "Wall Thickness (L)",
                unit = "meters",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }

        EngineeringResultBox(result = thermResult, theme = theme)
    }
}

// ---------------------- 7D. GEARS & MECHANICAL TRANSMISSION ----------------------
@Composable
private fun GearsTransmissionSection(theme: ThemePalette) {
    var teethDriverInput by remember { mutableStateOf("15") }
    var teethDrivenInput by remember { mutableStateOf("60") }
    var inputRpmInput by remember { mutableStateOf("1440") }
    var inputTorqueInput by remember { mutableStateOf("25") }
    var efficiencyInput by remember { mutableStateOf("95") }

    val z1 = teethDriverInput.toCleanDoubleOrNull() ?: 15.0
    val z2 = teethDrivenInput.toCleanDoubleOrNull() ?: 60.0
    val rpm1 = inputRpmInput.toCleanDoubleOrNull() ?: 1440.0
    val t1 = inputTorqueInput.toCleanDoubleOrNull() ?: 25.0
    val eff = efficiencyInput.toCleanDoubleOrNull() ?: 95.0

    val gearResult = remember(z1, z2, rpm1, t1, eff) {
        EngineeringEngine.calcGearsTransmission(z1, z2, rpm1, t1, eff)
    }

    EngineeringCalculatorCard(
        title = "Gear Train, Torque & Mechanical Power",
        icon = "⚙️",
        theme = theme
    ) {
        Text(
            text = "Solves gear speed reduction / overdrive ratio i, output torque multiplication, output RPM, and mechanical power.",
            color = theme.screenExpressionColor,
            fontSize = 11.sp
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EngineeringInput(
                value = teethDriverInput,
                onValueChange = { teethDriverInput = it },
                label = "Driver Teeth (Z₁)",
                unit = "input teeth",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            EngineeringInput(
                value = teethDrivenInput,
                onValueChange = { teethDrivenInput = it },
                label = "Driven Teeth (Z₂)",
                unit = "output teeth",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EngineeringInput(
                value = inputRpmInput,
                onValueChange = { inputRpmInput = it },
                label = "Input Speed (RPM₁)",
                unit = "RPM",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            EngineeringInput(
                value = inputTorqueInput,
                onValueChange = { inputTorqueInput = it },
                label = "Input Torque (T₁)",
                unit = "N·m",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }

        EngineeringInput(
            value = efficiencyInput,
            onValueChange = { efficiencyInput = it },
            label = "Transmission Efficiency (η %)",
            unit = "e.g. 95%",
            theme = theme,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ratio:", color = theme.screenExpressionColor, fontSize = 11.sp)
            listOf(
                Triple("2:1", "20", "40"),
                Triple("3:1", "15", "45"),
                Triple("4:1", "15", "60"),
                Triple("5:1", "12", "60")
            ).forEach { (label, z1Val, z2Val) ->
                val isSelected = teethDriverInput == z1Val && teethDrivenInput == z2Val
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) theme.accentColor else theme.surfaceColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            teethDriverInput = z1Val
                            teethDrivenInput = z2Val
                        }
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) theme.backgroundColor else theme.screenTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        EngineeringResultBox(result = gearResult, theme = theme)
    }
}

// ---------------------- 7E. SI METRIC PREFIXES EXPLORER ----------------------
@Composable
private fun EngPrefixSection(theme: ThemePalette) {
    var rawInput by remember { mutableStateOf("1500000") }
    val context = LocalContext.current

    val num = rawInput.toCleanDoubleOrNull() ?: 1500000.0
    val prefixSteps = remember(num) {
        EngineeringEngine.calcMetricPrefixes(num)
    }

    EngineeringCalculatorCard(
        title = "SI Metric Engineering Prefixes & Scaler",
        icon = "🔢",
        theme = theme
    ) {
        Text(
            text = "Convert any value to standard engineering powers-of-ten (10³ steps: Mega, kilo, milli, micro, nano, pico).",
            color = theme.screenExpressionColor,
            fontSize = 11.sp
        )

        EngineeringInput(
            value = rawInput,
            onValueChange = { rawInput = it },
            label = "Value to Scale",
            unit = "e.g. 1500000",
            theme = theme,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = theme.surfaceColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        rawInput = (num / 1000.0).toString()
                    }
            ) {
                Text(
                    text = "÷ 1,000 (10⁻³)",
                    color = theme.accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = theme.surfaceColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        rawInput = (num * 1000.0).toString()
                    }
            ) {
                Text(
                    text = "× 1,000 (10⁺³)",
                    color = theme.accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = theme.surfaceColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                prefixSteps.forEach { step ->
                    val isNearUnit = step.power == 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isNearUnit) theme.accentColor.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Prefix Value", step.formatted)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied: ${step.formatted}", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (step.symbol.isNotEmpty()) step.symbol else "—",
                                color = theme.accentColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(26.dp)
                            )
                            Text(
                                text = "${step.prefix} (10^${step.power})",
                                color = theme.screenExpressionColor,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = step.formatted,
                            color = theme.screenTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

// ---------------------- 8. CONSTANTS ----------------------
@Composable
private fun EngineeringConstantsSection(theme: ThemePalette) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredConstants = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            EngineeringEngine.CONSTANTS_LIST
        } else {
            EngineeringEngine.CONSTANTS_LIST.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.symbol.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    EngineeringCalculatorCard(
        title = "Fundamental Engineering & Physical Constants",
        icon = "📐",
        theme = theme
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search constants (e.g., gravity, speed, planck, steel)", fontSize = 12.sp, color = theme.screenExpressionColor) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = theme.accentColor,
                unfocusedBorderColor = theme.screenBorderColor.copy(alpha = 0.3f)
            ),
            singleLine = true
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            filteredConstants.forEach { c ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = theme.surfaceColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = c.name,
                                color = theme.screenTextColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = theme.accentColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = c.symbol,
                                    color = theme.accentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${c.value} ${c.unit}",
                                color = theme.secondaryAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = c.description,
                            color = theme.screenExpressionColor,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// ---------------------- REUSABLE COMPONENTS ----------------------
@Composable
private fun EngineeringCalculatorCard(
    title: String,
    icon: String,
    theme: ThemePalette,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(theme.borderWidthDp, theme.screenBorderColor, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = theme.screenBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = icon, fontSize = 20.sp)
                Text(
                    text = title.uppercase(),
                    color = theme.accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            content()
        }
    }
}

@Composable
private fun EngineeringInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    unit: String,
    theme: ThemePalette,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        placeholder = { Text(unit, fontSize = 11.sp, color = theme.screenExpressionColor) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = theme.screenTextColor,
            unfocusedTextColor = theme.screenTextColor,
            focusedContainerColor = theme.cardBackground,
            unfocusedContainerColor = theme.cardBackground,
            focusedBorderColor = theme.accentColor,
            unfocusedBorderColor = theme.screenBorderColor.copy(alpha = 0.35f),
            focusedLabelColor = theme.accentColor,
            unfocusedLabelColor = theme.screenExpressionColor
        ),
        modifier = modifier
    )
}

@Composable
private fun EngineeringResultBox(
    result: EngineeringCalculationResult,
    theme: ThemePalette
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = theme.surfaceColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = result.primaryLabel.uppercase(),
                color = theme.screenExpressionColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = result.primaryValue,
                color = theme.secondaryAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            HorizontalDivider(color = theme.screenBorderColor.copy(alpha = 0.25f))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                result.secondaryResults.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = label, color = theme.screenExpressionColor, fontSize = 11.sp)
                        Text(
                            text = value,
                            color = theme.screenTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = theme.accentColor.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Formula: ${result.formulaUsed}",
                        color = theme.accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = result.explanation,
                        color = theme.screenTextColor,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

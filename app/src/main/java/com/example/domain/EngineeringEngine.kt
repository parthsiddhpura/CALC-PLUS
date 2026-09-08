package com.example.domain

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

enum class EngineeringCategory(val displayName: String, val iconName: String) {
    OHMS_LAW("Ohm's & Power", "⚡"),
    CIRCUITS("Circuits & RC", "🔌"),
    RLC_RESONANCE("RLC & AC Reactance", "📻"),
    THREE_PHASE_POWER("3-Phase Power", "🏭"),
    MECHANICS("Mechanics & Force", "⚙️"),
    KINEMATICS("Kinematics & Motion", "🎯"),
    ENERGY_POWER("Work & Energy", "🔋"),
    STRESS_STRAIN("Stress & Materials", "🧱"),
    STRUCTURAL("Beam & Structural", "🏗️"),
    REYNOLDS_PIPE("Reynolds & Flow", "🌊"),
    FLUID_THERMAL("Fluids & Pressure", "💧"),
    THERMAL_EXPANSION("Thermal Expansion", "🌡️"),
    GEARS_PULLEYS("Gears & Transmission", "⚙️"),
    ENG_PREFIXES("SI Metric Prefixes", "🔢"),
    CONSTANTS("Eng Constants", "📐")
}

data class EngineeringConstantItem(
    val name: String,
    val symbol: String,
    val value: String,
    val unit: String,
    val description: String
)

data class EngineeringCalculationResult(
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryResults: List<Pair<String, String>>,
    val formulaUsed: String,
    val explanation: String
)

object EngineeringEngine {

    val CONSTANTS_LIST = listOf(
        EngineeringConstantItem("Speed of Light in Vacuum", "c", "2.99792458 × 10⁸", "m/s", "Fundamental cosmic speed limit"),
        EngineeringConstantItem("Standard Gravitational Acceleration", "g", "9.80665", "m/s²", "Standard Earth surface gravity"),
        EngineeringConstantItem("Universal Gravitational Constant", "G", "6.67430 × 10⁻¹¹", "N·m²/kg²", "Newtonian gravity constant"),
        EngineeringConstantItem("Planck's Constant", "h", "6.62607015 × 10⁻³⁴", "J·s", "Quantum of electromagnetic action"),
        EngineeringConstantItem("Elementary Charge", "e", "1.602176634 × 10⁻¹⁹", "C", "Electric charge of a proton/electron"),
        EngineeringConstantItem("Boltzmann Constant", "k_B", "1.380649 × 10⁻²³", "J/K", "Relates thermal energy to temperature"),
        EngineeringConstantItem("Universal Gas Constant", "R", "8.314462618", "J/(mol·K)", "Molar ideal gas constant"),
        EngineeringConstantItem("Permittivity of Free Space", "ε₀", "8.8541878128 × 10⁻¹²", "F/m", "Electric constant of vacuum"),
        EngineeringConstantItem("Permeability of Free Space", "μ₀", "1.256637062 × 10⁻⁶", "N/A² (H/m)", "Magnetic constant of vacuum"),
        EngineeringConstantItem("Stefan-Boltzmann Constant", "σ", "5.670374419 × 10⁻⁸", "W/(m²·K⁴)", "Blackbody radiative heat flux"),
        EngineeringConstantItem("Avogadro's Number", "N_A", "6.02214076 × 10²³", "mol⁻¹", "Number of constituent particles per mole"),
        EngineeringConstantItem("Density of Water at 4°C", "ρ_water", "1000", "kg/m³", "Standard reference liquid density"),
        EngineeringConstantItem("Atmospheric Pressure (1 atm)", "P_atm", "101,325", "Pa (1.01325 bar)", "Standard sea level pressure"),
        EngineeringConstantItem("Steel Elastic Modulus (avg)", "E_steel", "200 × 10⁹", "Pa (200 GPa)", "Structural mild steel Young's modulus"),
        EngineeringConstantItem("Concrete Elastic Modulus (avg)", "E_conc", "30 × 10⁹", "Pa (30 GPa)", "Standard structural concrete modulus")
    )

    private val df = DecimalFormat("#,##0.####", DecimalFormatSymbols(Locale.US))

    fun formatNumber(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Invalid"
        return if (kotlin.math.abs(value) >= 1e7 || (kotlin.math.abs(value) < 1e-4 && value != 0.0)) {
            String.format(Locale.US, "%.4e", value)
        } else {
            df.format(value)
        }
    }

    // --- 1. Ohm's Law & DC/AC Power ---
    fun calcOhmsLaw(voltage: Double?, current: Double?, resistance: Double?, power: Double?): EngineeringCalculationResult {
        return when {
            voltage != null && current != null && voltage > 0 && current > 0 -> {
                val r = voltage / current
                val p = voltage * current
                EngineeringCalculationResult(
                    primaryLabel = "Resistance (R)",
                    primaryValue = "${formatNumber(r)} Ω",
                    secondaryResults = listOf(
                        "Power (P)" to "${formatNumber(p)} Watts (W)",
                        "Voltage (V)" to "${formatNumber(voltage)} V",
                        "Current (I)" to "${formatNumber(current)} A"
                    ),
                    formulaUsed = "R = V / I  |  P = V × I",
                    explanation = "Direct electrical impedance and power dissipated by the current under applied voltage."
                )
            }
            voltage != null && resistance != null && voltage > 0 && resistance > 0 -> {
                val i = voltage / resistance
                val p = (voltage * voltage) / resistance
                EngineeringCalculationResult(
                    primaryLabel = "Current (I)",
                    primaryValue = "${formatNumber(i)} Amperes (A)",
                    secondaryResults = listOf(
                        "Power (P)" to "${formatNumber(p)} Watts (W)",
                        "Voltage (V)" to "${formatNumber(voltage)} V",
                        "Resistance (R)" to "${formatNumber(resistance)} Ω"
                    ),
                    formulaUsed = "I = V / R  |  P = V² / R",
                    explanation = "Resulting electrical flow and Joule heating power across the specified resistor."
                )
            }
            current != null && resistance != null && current > 0 && resistance > 0 -> {
                val v = current * resistance
                val p = current * current * resistance
                EngineeringCalculationResult(
                    primaryLabel = "Voltage (V)",
                    primaryValue = "${formatNumber(v)} Volts (V)",
                    secondaryResults = listOf(
                        "Power (P)" to "${formatNumber(p)} Watts (W)",
                        "Current (I)" to "${formatNumber(current)} A",
                        "Resistance (R)" to "${formatNumber(resistance)} Ω"
                    ),
                    formulaUsed = "V = I × R  |  P = I² × R",
                    explanation = "Potential drop across the resistance and active heat dissipated."
                )
            }
            power != null && voltage != null && power > 0 && voltage > 0 -> {
                val i = power / voltage
                val r = (voltage * voltage) / power
                EngineeringCalculationResult(
                    primaryLabel = "Current (I)",
                    primaryValue = "${formatNumber(i)} Amperes (A)",
                    secondaryResults = listOf(
                        "Resistance (R)" to "${formatNumber(r)} Ω",
                        "Power (P)" to "${formatNumber(power)} W",
                        "Voltage (V)" to "${formatNumber(voltage)} V"
                    ),
                    formulaUsed = "I = P / V  |  R = V² / P",
                    explanation = "Derived operating current and effective load impedance for specified power consumption."
                )
            }
            else -> {
                EngineeringCalculationResult(
                    primaryLabel = "Ohm's Law",
                    primaryValue = "Enter any 2 parameters",
                    secondaryResults = listOf(
                        "Voltage (V)" to "V = I × R",
                        "Current (I)" to "I = V / R",
                        "Resistance (R)" to "R = V / I",
                        "Power (P)" to "P = V × I = I²R"
                    ),
                    formulaUsed = "V = I × R  |  P = V × I",
                    explanation = "Fundamental relationship governing linear electrical circuits."
                )
            }
        }
    }

    // --- 2. Resistors & RC Filter ---
    fun calcSeriesParallel(r1: Double, r2: Double, r3: Double = 0.0): EngineeringCalculationResult {
        val rSeries = r1 + r2 + r3
        val rParallel = if (r1 > 0 && r2 > 0) {
            if (r3 > 0) 1.0 / (1.0 / r1 + 1.0 / r2 + 1.0 / r3) else (r1 * r2) / (r1 + r2)
        } else 0.0

        return EngineeringCalculationResult(
            primaryLabel = "Equivalent Resistance",
            primaryValue = "Series: ${formatNumber(rSeries)} Ω",
            secondaryResults = listOf(
                "Parallel Equivalent" to "${formatNumber(rParallel)} Ω",
                "Total Conductance (G)" to "${formatNumber(if (rParallel > 0) 1.0 / rParallel else 0.0)} Siemens (S)",
                "R1" to "${formatNumber(r1)} Ω",
                "R2" to "${formatNumber(r2)} Ω"
            ),
            formulaUsed = "R_series = R1 + R2 + R3  |  1/R_parallel = 1/R1 + 1/R2 + 1/R3",
            explanation = "Network impedance reduction in parallel vs cumulative addition in series."
        )
    }

    fun calcRcCircuit(resistanceOhms: Double, capacitanceMicroFarads: Double): EngineeringCalculationResult {
        val cFarads = capacitanceMicroFarads * 1e-6
        val tauSeconds = resistanceOhms * cFarads
        val cutoffFreqHz = if (tauSeconds > 0) 1.0 / (2 * PI * tauSeconds) else 0.0

        return EngineeringCalculationResult(
            primaryLabel = "Cutoff Frequency (-3dB)",
            primaryValue = "${formatNumber(cutoffFreqHz)} Hz",
            secondaryResults = listOf(
                "Time Constant (τ = RC)" to "${formatNumber(tauSeconds * 1000)} ms (${formatNumber(tauSeconds)} s)",
                "5τ Full Charge Time (99.3%)" to "${formatNumber(5 * tauSeconds * 1000)} ms",
                "Capacitor Value" to "$capacitanceMicroFarads µF",
                "Resistor Value" to "$resistanceOhms Ω"
            ),
            formulaUsed = "f_c = 1 / (2π × R × C)  |  τ = R × C",
            explanation = "Corner frequency where output drops by 3dB (70.7% amplitude) in first-order passive low/high pass filter."
        )
    }

    // --- 3. Mechanics: Force, Torque, Energy ---
    fun calcNewtonForce(massKg: Double, accelMps2: Double): EngineeringCalculationResult {
        val forceN = massKg * accelMps2
        val forceLbf = forceN * 0.224809
        val weightN = massKg * 9.80665

        return EngineeringCalculationResult(
            primaryLabel = "Net Force (F)",
            primaryValue = "${formatNumber(forceN)} Newtons (N)",
            secondaryResults = listOf(
                "Force in Pound-force (lbf)" to "${formatNumber(forceLbf)} lbf",
                "Static Earth Weight (W = mg)" to "${formatNumber(weightN)} N",
                "Mass (m)" to "$massKg kg",
                "Acceleration (a)" to "$accelMps2 m/s²"
            ),
            formulaUsed = "F = m × a  |  W = m × g",
            explanation = "Newton's 2nd Law of Motion: Unbalanced force accelerates mass."
        )
    }

    fun calcTorquePower(forceN: Double, radiusMeters: Double, rpm: Double = 0.0): EngineeringCalculationResult {
        val torqueNm = forceN * radiusMeters
        val torqueFtLb = torqueNm * 0.737562
        val powerWatts = if (rpm > 0) (2 * PI * rpm * torqueNm) / 60.0 else 0.0
        val powerHp = powerWatts / 745.7

        return EngineeringCalculationResult(
            primaryLabel = "Torque (τ)",
            primaryValue = "${formatNumber(torqueNm)} N·m",
            secondaryResults = listOf(
                "Torque (ft-lb)" to "${formatNumber(torqueFtLb)} lbf·ft",
                "Rotational Power (P)" to if (rpm > 0) "${formatNumber(powerWatts)} W (${formatNumber(powerHp)} HP)" else "Enter RPM",
                "Speed (N)" to if (rpm > 0) "$rpm RPM" else "Static",
                "Moment Arm (r)" to "$radiusMeters m"
            ),
            formulaUsed = "τ = F × r  |  P = (2π × N × τ) / 60",
            explanation = "Rotational moment of force and continuous mechanical output power."
        )
    }

    fun calcKineticPotentialEnergy(massKg: Double, velocityMps: Double, heightMeters: Double): EngineeringCalculationResult {
        val ke = 0.5 * massKg * velocityMps.pow(2)
        val pe = massKg * 9.80665 * heightMeters
        val totalE = ke + pe

        return EngineeringCalculationResult(
            primaryLabel = "Total Mechanical Energy",
            primaryValue = "${formatNumber(totalE)} Joules (J)",
            secondaryResults = listOf(
                "Kinetic Energy (KE)" to "${formatNumber(ke)} J (${formatNumber(ke / 1000)} kJ)",
                "Potential Energy (PE)" to "${formatNumber(pe)} J (${formatNumber(pe / 1000)} kJ)",
                "Velocity" to "$velocityMps m/s (${formatNumber(velocityMps * 3.6)} km/h)",
                "Height" to "$heightMeters m"
            ),
            formulaUsed = "KE = ½ × m × v²  |  PE = m × g × h",
            explanation = "Conservation of mechanical energy during kinematic and gravitational motion."
        )
    }

    // --- 4. Stress, Strain & Young's Modulus ---
    fun calcStressStrain(forceN: Double, areaMm2: Double, origLengthMm: Double = 1000.0, changeLengthMm: Double = 1.0): EngineeringCalculationResult {
        val areaM2 = areaMm2 * 1e-6
        val stressPa = if (areaM2 > 0) forceN / areaM2 else 0.0
        val stressMpa = stressPa / 1e6
        val strain = if (origLengthMm > 0) changeLengthMm / origLengthMm else 0.0
        val youngsModulusGpa = if (strain > 0) (stressPa / strain) / 1e9 else 0.0

        return EngineeringCalculationResult(
            primaryLabel = "Tensile / Compressive Stress (σ)",
            primaryValue = "${formatNumber(stressMpa)} MPa (N/mm²)",
            secondaryResults = listOf(
                "Engineering Strain (ε)" to "${formatNumber(strain)} (or ${formatNumber(strain * 100)}%)",
                "Young's Modulus (E)" to "${formatNumber(youngsModulusGpa)} GPa",
                "Stress in PSI" to "${formatNumber(stressMpa * 145.038)} psi",
                "Applied Load (F)" to "$forceN N"
            ),
            formulaUsed = "σ = F / A  |  ε = ΔL / L₀  |  E = σ / ε",
            explanation = "Internal resistive force per unit cross-sectional area under structural tension or compression."
        )
    }

    // --- 5. Fluids & Thermal ---
    fun calcHydraulicPressure(forceN: Double, areaCm2: Double, depthMeters: Double = 0.0, fluidDensityKgM3: Double = 1000.0): EngineeringCalculationResult {
        val areaM2 = areaCm2 * 1e-4
        val appliedPressurePa = if (areaM2 > 0) forceN / areaM2 else 0.0
        val hydroPressurePa = fluidDensityKgM3 * 9.80665 * depthMeters
        val totalPressureBar = (appliedPressurePa + hydroPressurePa) / 100000.0

        return EngineeringCalculationResult(
            primaryLabel = "Total Fluid Pressure",
            primaryValue = "${formatNumber(totalPressureBar)} Bar (${formatNumber((appliedPressurePa + hydroPressurePa) / 1000)} kPa)",
            secondaryResults = listOf(
                "Applied Surface Pressure" to "${formatNumber(appliedPressurePa / 1000)} kPa (${formatNumber(appliedPressurePa * 0.000145038)} psi)",
                "Hydrostatic Depth Pressure" to "${formatNumber(hydroPressurePa / 1000)} kPa (${formatNumber(depthMeters)} m depth)",
                "Fluid Density" to "$fluidDensityKgM3 kg/m³",
                "Piston Area" to "$areaCm2 cm²"
            ),
            formulaUsed = "P = F / A + ρ × g × h",
            explanation = "Combined hydrostatic head pressure and mechanical piston actuation pressure in hydraulic systems."
        )
    }

    fun calcThermalHeatTransfer(massKg: Double, specificHeatJoulePerKgC: Double, tempDeltaC: Double): EngineeringCalculationResult {
        val heatJoules = massKg * specificHeatJoulePerKgC * tempDeltaC
        val heatKj = heatJoules / 1000.0
        val heatKcal = heatJoules / 4184.0
        val heatBtu = heatJoules * 0.000947817

        return EngineeringCalculationResult(
            primaryLabel = "Thermal Heat Energy (Q)",
            primaryValue = "${formatNumber(heatKj)} kJ",
            secondaryResults = listOf(
                "Heat in Kilocalories (kcal)" to "${formatNumber(heatKcal)} kcal",
                "Heat in BTU" to "${formatNumber(heatBtu)} BTU",
                "Mass (m)" to "$massKg kg",
                "Specific Heat (c)" to "$specificHeatJoulePerKgC J/(kg·°C)",
                "Temperature Rise (ΔT)" to "$tempDeltaC °C"
            ),
            formulaUsed = "Q = m × c × ΔT",
            explanation = "Calorimetric heat energy required to change mass temperature under constant pressure."
        )
    }

    // --- 6. Beam Bending & Structural ---
    fun calcBeamBending(
        loadKn: Double,
        lengthMeters: Double,
        isDistributedLoad: Boolean,
        modulusGpa: Double = 200.0,
        momentOfInertiaCm4: Double = 5000.0
    ): EngineeringCalculationResult {
        val loadN = loadKn * 1000.0
        val ePa = modulusGpa * 1e9
        val iM4 = momentOfInertiaCm4 * 1e-8

        val maxMomentNm = if (isDistributedLoad) {
            (loadN * lengthMeters.pow(2)) / 8.0
        } else {
            (loadN * lengthMeters) / 4.0
        }

        val maxDeflectionMm = if (isDistributedLoad) {
            val defM = (5.0 * loadN * lengthMeters.pow(4)) / (384.0 * ePa * iM4)
            defM * 1000.0
        } else {
            val defM = (loadN * lengthMeters.pow(3)) / (48.0 * ePa * iM4)
            defM * 1000.0
        }

        return EngineeringCalculationResult(
            primaryLabel = "Max Bending Moment (M_max)",
            primaryValue = "${formatNumber(maxMomentNm / 1000.0)} kN·m",
            secondaryResults = listOf(
                "Max Mid-Span Deflection (δ_max)" to "${formatNumber(maxDeflectionMm)} mm",
                "Load Configuration" to if (isDistributedLoad) "Uniformly Distributed (UDL w = $loadKn kN/m)" else "Point Load at Midspan (P = $loadKn kN)",
                "Span Length (L)" to "$lengthMeters meters",
                "Young's Modulus (E)" to "$modulusGpa GPa",
                "Second Moment of Area (I)" to "$momentOfInertiaCm4 cm⁴"
            ),
            formulaUsed = if (isDistributedLoad) "M = wL²/8  |  δ = 5wL⁴ / 384EI" else "M = PL/4  |  δ = PL³ / 48EI",
            explanation = "Simply-supported horizontal structural beam under transverse static loading."
        )
    }

    // --- 7. RLC Resonance & AC Reactance ---
    fun calcRlcResonance(
        resistanceOhms: Double,
        inductanceMilliHenries: Double,
        capacitanceMicroFarads: Double,
        frequencyHz: Double
    ): EngineeringCalculationResult {
        val lHenries = inductanceMilliHenries * 1e-3
        val cFarads = capacitanceMicroFarads * 1e-6
        val r = resistanceOhms.coerceAtLeast(0.001)

        val f0Hz = if (lHenries > 0 && cFarads > 0) {
            1.0 / (2.0 * PI * sqrt(lHenries * cFarads))
        } else 0.0

        val f = if (frequencyHz > 0) frequencyHz else f0Hz
        val xl = 2.0 * PI * f * lHenries
        val xc = if (f > 0 && cFarads > 0) 1.0 / (2.0 * PI * f * cFarads) else 0.0
        val netReactance = xl - xc
        val impedanceZ = sqrt(r * r + netReactance * netReactance)
        val qFactor = if (r > 0 && cFarads > 0) (1.0 / r) * sqrt(lHenries / cFarads) else 0.0
        val bandwidthHz = if (qFactor > 0) f0Hz / qFactor else 0.0
        val phaseAngleDeg = atan2(netReactance, r) * (180.0 / PI)

        val resonanceState = when {
            abs(xl - xc) < 0.001 -> "At Resonance (XL ≈ XC, Pure Resistive)"
            xl > xc -> "Inductive (Current lags Voltage by ${formatNumber(abs(phaseAngleDeg))}°)"
            else -> "Capacitive (Current leads Voltage by ${formatNumber(abs(phaseAngleDeg))}°)"
        }

        return EngineeringCalculationResult(
            primaryLabel = "Resonant Frequency (f₀)",
            primaryValue = if (f0Hz >= 1000) "${formatNumber(f0Hz / 1000.0)} kHz" else "${formatNumber(f0Hz)} Hz",
            secondaryResults = listOf(
                "Total AC Impedance (Z)" to "${formatNumber(impedanceZ)} Ω at ${formatNumber(f)} Hz",
                "Inductive Reactance (X_L)" to "${formatNumber(xl)} Ω (2πfL)",
                "Capacitive Reactance (X_C)" to "${formatNumber(xc)} Ω (1/2πfC)",
                "Quality Factor (Q)" to formatNumber(qFactor),
                "Bandwidth (-3dB)" to "${formatNumber(bandwidthHz)} Hz",
                "Circuit State" to resonanceState
            ),
            formulaUsed = "f₀ = 1 / (2π√(LC))  |  Z = √(R² + (X_L - X_C)²)  |  Q = (1/R)√(L/C)",
            explanation = "Series RLC circuit resonance where inductive and capacitive reactances cancel out, giving minimum impedance."
        )
    }

    // --- 8. 3-Phase AC Power & Power Factor ---
    fun calcThreePhasePower(
        lineVoltageV: Double,
        lineCurrentA: Double,
        powerFactor: Double
    ): EngineeringCalculationResult {
        val pf = powerFactor.coerceIn(0.01, 1.0)
        val sqrt3 = sqrt(3.0)
        val apparentPowerVa = sqrt3 * lineVoltageV * lineCurrentA
        val realPowerWatts = apparentPowerVa * pf
        val reactivePowerVar = apparentPowerVa * sqrt(1.0 - pf * pf)

        val vPhaseStar = lineVoltageV / sqrt3
        val iPhaseDelta = lineCurrentA / sqrt3

        // Capacitor kVAR needed to improve power factor to 0.98
        val targetPf = 0.98
        val currentTan = tan(acos(pf))
        val targetTan = tan(acos(targetPf))
        val kVarNeeded = if (pf < targetPf) {
            (realPowerWatts / 1000.0) * (currentTan - targetTan)
        } else 0.0

        return EngineeringCalculationResult(
            primaryLabel = "Active Real Power (P)",
            primaryValue = "${formatNumber(realPowerWatts / 1000.0)} kW",
            secondaryResults = listOf(
                "Apparent Power (S)" to "${formatNumber(apparentPowerVa / 1000.0)} kVA",
                "Reactive Power (Q)" to "${formatNumber(reactivePowerVar / 1000.0)} kVAR",
                "Power Factor (cos φ)" to "$pf (${if (pf >= 0.9) "Good" else "Low, needs correction"})",
                "Star Phase Voltage (V_ph)" to "${formatNumber(vPhaseStar)} V",
                "Delta Phase Current (I_ph)" to "${formatNumber(iPhaseDelta)} A",
                "Capacitor for 0.98 PF" to if (kVarNeeded > 0) "${formatNumber(kVarNeeded)} kVAR bank" else "Already optimal"
            ),
            formulaUsed = "P = √3 × V_L × I_L × cos(φ)  |  S = √3 × V_L × I_L  |  Q = √3 × V_L × I_L × sin(φ)",
            explanation = "Symmetrical three-phase industrial AC power relationship across line-to-line conductors."
        )
    }

    // --- 9. Kinematics & Projectile Motion ---
    fun calcKinematicsProjectile(
        velocityMps: Double,
        angleDeg: Double,
        initialHeightMeters: Double = 0.0
    ): EngineeringCalculationResult {
        val g = 9.80665
        val rad = angleDeg * (PI / 180.0)
        val vx = velocityMps * cos(rad)
        val vy = velocityMps * sin(rad)

        val apexHeight = initialHeightMeters + (vy * vy) / (2.0 * g)
        val timeToApex = vy / g

        val totalTime = if (initialHeightMeters <= 0.0) {
            (2.0 * vy) / g
        } else {
            (vy + sqrt(vy * vy + 2.0 * g * initialHeightMeters)) / g
        }

        val rangeMeters = vx * totalTime
        val impactVelocity = sqrt(velocityMps * velocityMps + 2.0 * g * initialHeightMeters)

        return EngineeringCalculationResult(
            primaryLabel = "Horizontal Range (R)",
            primaryValue = "${formatNumber(rangeMeters)} meters",
            secondaryResults = listOf(
                "Range in Feet" to "${formatNumber(rangeMeters * 3.28084)} ft",
                "Max Apex Height (H)" to "${formatNumber(apexHeight)} m (${formatNumber(apexHeight * 3.28084)} ft)",
                "Total Flight Hang Time" to "${formatNumber(totalTime)} seconds",
                "Horizontal Velocity (v_x)" to "${formatNumber(vx)} m/s",
                "Initial Vertical Velocity (v_y)" to "${formatNumber(vy)} m/s",
                "Terminal Impact Velocity" to "${formatNumber(impactVelocity)} m/s (${formatNumber(impactVelocity * 3.6)} km/h)"
            ),
            formulaUsed = "R = v_x × t_total  |  H = h₀ + (v_y)² / (2g)  |  t = (v_y + √(v_y² + 2gh₀)) / g",
            explanation = "Ballistic trajectory of an unpowered projectile under uniform gravitational acceleration (no air drag)."
        )
    }

    // --- 10. Reynolds Number & Pipe Flow ---
    fun calcReynoldsPipe(
        velocityMps: Double,
        diameterMm: Double,
        densityKgM3: Double = 1000.0,
        dynamicViscosityPaS: Double = 0.001
    ): EngineeringCalculationResult {
        val dMeters = diameterMm * 1e-3
        val re = if (dynamicViscosityPaS > 0) {
            (densityKgM3 * velocityMps * dMeters) / dynamicViscosityPaS
        } else 0.0

        val flowRegime = when {
            re < 2300.0 -> "Laminar Flow (Smooth, streamlined layers)"
            re <= 4000.0 -> "Transitional Flow (Unstable intermediate zone)"
            else -> "Turbulent Flow (Chaotic mixing, high friction)"
        }

        val areaM2 = (PI * dMeters * dMeters) / 4.0
        val volFlowM3s = areaM2 * velocityMps
        val volFlowLpm = volFlowM3s * 60000.0
        val massFlowKgS = volFlowM3s * densityKgM3

        return EngineeringCalculationResult(
            primaryLabel = "Reynolds Number (Re)",
            primaryValue = formatNumber(re),
            secondaryResults = listOf(
                "Flow Regime" to flowRegime,
                "Volumetric Flow Rate" to "${formatNumber(volFlowLpm)} L/min (${formatNumber(volFlowM3s * 1000.0)} L/s)",
                "Mass Flow Rate (ṁ)" to "${formatNumber(massFlowKgS)} kg/s (${formatNumber(massFlowKgS * 3600.0)} kg/h)",
                "Pipe Cross-Section Area" to "${formatNumber(areaM2 * 10000.0)} cm²",
                "Kinematic Viscosity (ν)" to "${formatNumber((dynamicViscosityPaS / densityKgM3) * 1e6)} cSt (mm²/s)"
            ),
            formulaUsed = "Re = (ρ × v × D) / μ  |  Q = A × v  |  ṁ = ρ × Q",
            explanation = "Dimensionless ratio of inertial forces to viscous forces, predicting internal fluid boundary layer behavior."
        )
    }

    // --- 11. Thermal Expansion & Fourier Conduction ---
    fun calcThermalExpansionConduction(
        origLengthMeters: Double,
        tempDeltaC: Double,
        alphaPpmPerC: Double,
        thermalConductivityK: Double = 50.0,
        areaM2: Double = 1.0,
        thicknessM: Double = 0.1
    ): EngineeringCalculationResult {
        val alpha = alphaPpmPerC * 1e-6
        val deltaLMeters = origLengthMeters * alpha * tempDeltaC
        val deltaLMm = deltaLMeters * 1000.0
        val finalLMeters = origLengthMeters + deltaLMeters

        val heatRateWatts = if (thicknessM > 0) {
            (thermalConductivityK * areaM2 * tempDeltaC) / thicknessM
        } else 0.0

        return EngineeringCalculationResult(
            primaryLabel = "Linear Thermal Elongation (ΔL)",
            primaryValue = "${formatNumber(deltaLMm)} mm",
            secondaryResults = listOf(
                "Expanded Final Length" to "${formatNumber(finalLMeters)} meters",
                "Strain Induced (ε_th)" to "${formatNumber(deltaLMeters / origLengthMeters)} (${formatNumber((deltaLMeters / origLengthMeters) * 100.0)}%)",
                "Fourier Conduction Heat (q)" to "${formatNumber(heatRateWatts / 1000.0)} kW (${formatNumber(heatRateWatts)} W)",
                "Coefficient of Expansion (α)" to "$alphaPpmPerC × 10⁻⁶ /°C",
                "Thermal Gradient (ΔT/L)" to "${formatNumber(tempDeltaC / thicknessM.coerceAtLeast(0.001))} °C/m"
            ),
            formulaUsed = "ΔL = α × L₀ × ΔT  |  q = (k × A × ΔT) / L_thick",
            explanation = "Dimensional response of engineering materials to thermal gradients and continuous 1D conductive heat flux."
        )
    }

    // --- 12. Gear Ratio, Output Torque & Speed ---
    fun calcGearsTransmission(
        teethDriverZ1: Double,
        teethDrivenZ2: Double,
        inputRpm: Double,
        inputTorqueNm: Double,
        efficiencyPercent: Double = 95.0
    ): EngineeringCalculationResult {
        val z1 = teethDriverZ1.coerceAtLeast(1.0)
        val z2 = teethDrivenZ2.coerceAtLeast(1.0)
        val eff = (efficiencyPercent / 100.0).coerceIn(0.1, 1.0)

        val gearRatio = z2 / z1
        val outputRpm = if (gearRatio > 0) inputRpm / gearRatio else 0.0
        val outputTorqueNm = inputTorqueNm * gearRatio * eff

        val inputPowerWatts = (2.0 * PI * inputRpm * inputTorqueNm) / 60.0
        val outputPowerWatts = inputPowerWatts * eff

        return EngineeringCalculationResult(
            primaryLabel = "Gear Ratio (i)",
            primaryValue = "${formatNumber(gearRatio)} : 1",
            secondaryResults = listOf(
                "Output Shaft Speed" to "${formatNumber(outputRpm)} RPM",
                "Output Torque" to "${formatNumber(outputTorqueNm)} N·m (${formatNumber(outputTorqueNm * 0.737562)} ft·lb)",
                "Torque Multiplication Factor" to "${formatNumber(gearRatio * eff)}x",
                "Input Power" to "${formatNumber(inputPowerWatts / 1000.0)} kW (${formatNumber(inputPowerWatts / 745.7)} HP)",
                "Delivered Output Power" to "${formatNumber(outputPowerWatts / 1000.0)} kW (${formatNumber(outputPowerWatts / 745.7)} HP)",
                "Efficiency" to "$efficiencyPercent %"
            ),
            formulaUsed = "i = Z₂ / Z₁  |  N₂ = N₁ / i  |  T₂ = T₁ × i × η  |  P = (2πNT) / 60",
            explanation = "Mechanical advantage through gear ratio reduction or overdrive with torque and angular velocity transfer."
        )
    }

    // --- 13. Metric Engineering Prefix Converter ---
    data class MetricPrefixStep(val prefix: String, val symbol: String, val power: Int, val formatted: String)

    fun calcMetricPrefixes(value: Double): List<MetricPrefixStep> {
        val prefixes = listOf(
            Triple("Tera", "T", 12),
            Triple("Giga", "G", 9),
            Triple("Mega", "M", 6),
            Triple("kilo", "k", 3),
            Triple("base unit", "", 0),
            Triple("milli", "m", -3),
            Triple("micro", "µ", -6),
            Triple("nano", "n", -9),
            Triple("pico", "p", -12),
            Triple("femto", "f", -15)
        )

        return prefixes.map { (name, sym, pow) ->
            val scaled = value / 10.0.pow(pow.toDouble())
            MetricPrefixStep(
                prefix = name,
                symbol = sym,
                power = pow,
                formatted = "${formatNumber(scaled)}${if (sym.isNotEmpty()) " $sym" else ""}"
            )
        }
    }
}

package com.example.ui.components

import android.app.TimePickerDialog
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.AgeCalculatorEngine
import com.example.domain.AgeResult
import com.example.domain.ComprehensiveAstrologyResult
import com.example.domain.RealRashiHoroscopeEngine
import com.example.domain.RashiInfo
import com.example.model.AgeProfile
import com.example.model.ThemePalette
import kotlinx.coroutines.delay
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class StepPickerStage {
    YEAR, MONTH, DAY
}

enum class RashiViewType {
    CHANDRA_MOON, NAAM_NAME, SURYA_SUN
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AgeCalculatorView(
    theme: ThemePalette,
    birthDateTime: LocalDateTime,
    targetDateTime: LocalDateTime,
    currentPersonName: String,
    notes: String,
    savedProfiles: List<AgeProfile>,
    selectedProfile: AgeProfile?,
    onUpdateBirthDateTime: (LocalDateTime) -> Unit,
    onUpdateTargetDateTime: (LocalDateTime) -> Unit,
    onPersonNameChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSaveProfile: (String, String, String) -> Unit,
    onLoadProfile: (AgeProfile) -> Unit,
    onDeleteProfile: (AgeProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val df = remember { DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)) }
    var showProfilesSheet by remember { mutableStateOf(false) }
    var showStepDatePicker by remember { mutableStateOf(false) }
    var stepPickerInitialStage by remember { mutableStateOf(StepPickerStage.YEAR) }
    var rashiViewType by remember { mutableStateOf(RashiViewType.CHANDRA_MOON) }

    // Live ticking trigger (every second)
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(1000)
        }
    }

    val effectiveTarget = if (targetDateTime.toLocalDate().isEqual(LocalDate.now())) {
        currentTime
    } else {
        targetDateTime
    }

    val ageResult: AgeResult = remember(birthDateTime, effectiveTarget) {
        AgeCalculatorEngine.calculateAge(birthDateTime, effectiveTarget)
    }

    val astrologyResult: ComprehensiveAstrologyResult = remember(birthDateTime, currentPersonName) {
        RealRashiHoroscopeEngine.calculateAstrology(
            date = birthDateTime.toLocalDate(),
            hour = birthDateTime.hour,
            minute = birthDateTime.minute,
            personName = currentPersonName
        )
    }

    val activeRashi: RashiInfo = when (rashiViewType) {
        RashiViewType.CHANDRA_MOON -> astrologyResult.vedicMoonRashi
        RashiViewType.NAAM_NAME -> astrologyResult.nameRashi ?: astrologyResult.vedicMoonRashi
        RashiViewType.SURYA_SUN -> astrologyResult.vedicSuryaRashi
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMMM yyyy (EEEE)", Locale.ENGLISH) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH) }

    // Export & Share function
    val shareAgeAndRashi = {
        val shareText = buildString {
            appendLine("🎂 CALC + Age & AstroSage Vedic Horoscope 🎂")
            appendLine("Name: ${currentPersonName.ifBlank { "Profile" }}")
            appendLine("-------------------------------------------")
            appendLine("📅 Date of Birth: ${birthDateTime.format(dateFormatter)} at ${birthDateTime.format(timeFormatter)}")
            appendLine("⏳ Exact Age: ${ageResult.years} Years, ${ageResult.months} Months, ${ageResult.days} Days")
            appendLine("⚡ Live Total Lived:")
            appendLine("   • ${df.format(ageResult.totalDays)} Days")
            appendLine("   • ${df.format(ageResult.totalHours)} Hours")
            appendLine("   • ${df.format(ageResult.totalMinutes)} Minutes")
            appendLine("   • ${df.format(ageResult.totalSeconds)} Seconds")
            appendLine("🎉 Next Birthday: in ${ageResult.nextBirthdayMonths}m ${ageResult.nextBirthdayDays}d (on ${ageResult.nextBirthdayDayOfWeek})")
            appendLine()
            appendLine("🔮 ASTROSAGE VEDIC JYOTISH REPORT")
            appendLine("   • Chandra Rashi (Moon Sign): ${astrologyResult.vedicMoonRashi.sanskritName} (${astrologyResult.vedicMoonRashi.hindiName}) ${astrologyResult.vedicMoonRashi.symbol}")
            appendLine("   • Moon Position: ${astrologyResult.rashiDegreeStr}")
            appendLine("   • Nakshatra: ${astrologyResult.nakshatraName} (Pada ${astrologyResult.nakshatraPada}, Lord: ${astrologyResult.nakshatraLord})")
            if (astrologyResult.nameRashi != null) {
                appendLine("   • Naam Rashi (By Name): ${astrologyResult.nameRashi.sanskritName} (${astrologyResult.nameRashi.hindiName})")
            }
            appendLine("   • Surya Rashi (Sun Sign): ${astrologyResult.vedicSuryaRashi.sanskritName} (${astrologyResult.vedicSuryaRashi.hindiName})")
            appendLine("   • Ruling Lord: ${activeRashi.rulingPlanet}")
            appendLine("   • Element: ${activeRashi.element} (${activeRashi.elementHindi})")
            appendLine("   • Lucky Gemstone: ${activeRashi.luckyGemstone} (${activeRashi.luckyGemstoneHindi})")
            appendLine("   • Lucky Numbers: ${activeRashi.luckyNumbers.joinToString(", ")}")
            appendLine("   • Lucky Day: ${activeRashi.luckyDay}")
            appendLine("   • Lucky Color: ${activeRashi.luckyColor}")
            appendLine("   • Western Zodiac: ${astrologyResult.westernSign} ${astrologyResult.westernSymbol}")
            appendLine("   • Chinese Zodiac: ${astrologyResult.chineseZodiac} (${astrologyResult.chineseElement})")
            appendLine("   • Birthstone: ${astrologyResult.birthStone}")
            appendLine()
            appendLine("✨ AstroSage Planetary Guidance:")
            appendLine("   ${astrologyResult.dailyHoroscopeReading}")
            appendLine("🕉️ Vedic Mantra: ${activeRashi.mantra}")
            appendLine()
            appendLine("— Generated with CALC +")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareChooser = Intent.createChooser(sendIntent, "Share Age & Horoscope Summary")
        context.startActivity(shareChooser)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Person Name, History & Share Header Bar ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(theme.borderWidthDp, theme.screenBorderColor, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = theme.screenBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = theme.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "PROFILE & NAME",
                            color = theme.accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Profiles History Button
                        Surface(
                            color = theme.surfaceColor,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showProfilesSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Saved Profiles",
                                    tint = theme.screenTextColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Saved (${savedProfiles.size})",
                                    color = theme.screenTextColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // PDF Export / Print Button
                        Surface(
                            color = theme.surfaceColor,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    Toast.makeText(context, "Generating PDF Report...", Toast.LENGTH_SHORT).show()
                                    com.example.domain.AgeReportPrintAdapter.printAgeReport(
                                        context = context,
                                        personName = currentPersonName,
                                        birthDateTime = birthDateTime,
                                        ageResult = ageResult,
                                        astrologyResult = astrologyResult
                                    )
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Print,
                                    contentDescription = "Export PDF",
                                    tint = theme.accentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "PDF",
                                    color = theme.accentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Share / Export Button
                        Surface(
                            color = theme.accentColor,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { shareAgeAndRashi() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Export & Share",
                                    tint = theme.backgroundColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Share",
                                    color = if (theme.accentColor.luminance() > 0.6f) Color(0xFF211118) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Name Input Field with Save Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = currentPersonName,
                        onValueChange = onPersonNameChange,
                        placeholder = { Text("Enter name (e.g. Aarav, Priya, Self)", color = theme.screenExpressionColor, fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.screenTextColor,
                            unfocusedTextColor = theme.screenTextColor,
                            focusedContainerColor = theme.cardBackground,
                            unfocusedContainerColor = theme.cardBackground,
                            focusedBorderColor = theme.accentColor,
                            unfocusedBorderColor = theme.screenBorderColor.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Surface(
                        color = theme.surfaceColor,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onSaveProfile(
                                    currentPersonName.ifBlank { "Profile" },
                                    "",
                                    notes
                                )
                                Toast.makeText(context, "Saved successfully!", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save Profile",
                                tint = theme.accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Save",
                                color = theme.accentColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- 2. Step-by-Step Date Selector (Year -> Month -> Day) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(theme.borderWidthDp, theme.screenBorderColor, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = theme.screenBackground)
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
                        text = "DATE OF BIRTH (YEAR ➔ MONTH ➔ DAY)",
                        color = theme.accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = theme.accentColor.copy(alpha = 0.15f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                stepPickerInitialStage = StepPickerStage.YEAR
                                showStepDatePicker = true
                            }
                    ) {
                        Text(
                            text = "Tap to Pick",
                            color = theme.accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Interactive 3-Segment Picker (Year, Month, Day)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Year Chip
                    Surface(
                        color = theme.surfaceColor,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                stepPickerInitialStage = StepPickerStage.YEAR
                                showStepDatePicker = true
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("1. YEAR", color = theme.accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${birthDateTime.year}",
                                color = theme.screenTextColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Month Chip
                    Surface(
                        color = theme.surfaceColor,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                stepPickerInitialStage = StepPickerStage.MONTH
                                showStepDatePicker = true
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("2. MONTH", color = theme.accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = birthDateTime.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                                color = theme.screenTextColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Day Chip
                    Surface(
                        color = theme.surfaceColor,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                stepPickerInitialStage = StepPickerStage.DAY
                                showStepDatePicker = true
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("3. DAY", color = theme.accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${birthDateTime.dayOfMonth}",
                                color = theme.screenTextColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Full Date & Birth Time Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Born: ${birthDateTime.format(dateFormatter)}",
                        color = theme.screenExpressionColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Surface(
                        color = theme.surfaceColor,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        onUpdateBirthDateTime(
                                            LocalDateTime.of(birthDateTime.toLocalDate(), LocalTime.of(h, m, 0))
                                        )
                                    },
                                    birthDateTime.hour,
                                    birthDateTime.minute,
                                    false
                                ).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = theme.secondaryAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = birthDateTime.format(timeFormatter),
                                color = theme.secondaryAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- 3. Hero Exact Age Live Ticking Display ---
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXACT CURRENT AGE (LIVE)",
                        color = theme.secondaryAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Surface(
                        color = theme.accentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Born on ${ageResult.dayOfWeekBorn}",
                            color = theme.accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Units Grid: Years, Months, Days
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AgeUnitBlock(value = ageResult.years.toString(), label = "Years", theme = theme)
                    AgeUnitBlock(value = ageResult.months.toString(), label = "Months", theme = theme)
                    AgeUnitBlock(value = ageResult.days.toString(), label = "Days", theme = theme)
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = theme.screenBorderColor.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                // Live Ticking Units Grid: Hours, Minutes, Seconds
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AgeUnitBlock(
                        value = String.format("%02d", ageResult.hours),
                        label = "Hours",
                        theme = theme,
                        isAccent = true
                    )
                    AgeUnitBlock(
                        value = String.format("%02d", ageResult.minutes),
                        label = "Minutes",
                        theme = theme,
                        isAccent = true
                    )
                    AgeUnitBlock(
                        value = String.format("%02d", ageResult.seconds),
                        label = "Seconds",
                        theme = theme,
                        isAccent = true
                    )
                }
            }
        }

        // --- 4. REAL ASTROSAGE VEDIC RASHI & HOROSCOPE MODULE ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(theme.borderWidthDp, theme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = theme.screenBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = theme.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "ASTROSAGE VEDIC HOROSCOPE",
                            color = theme.accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Surface(
                        color = theme.accentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "AstroSage Ephemeris",
                            color = theme.accentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Rashi Type Selector (Moon Sign / Naam Sign / Sun Sign)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val isChandra = rashiViewType == RashiViewType.CHANDRA_MOON
                    val isNaam = rashiViewType == RashiViewType.NAAM_NAME
                    val isSurya = rashiViewType == RashiViewType.SURYA_SUN
                    val activeTextColor = if (theme.accentColor.luminance() > 0.6f) Color(0xFF1E293B) else Color.White

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isChandra) theme.accentColor else theme.surfaceColor,
                        modifier = Modifier
                            .weight(1.3f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { rashiViewType = RashiViewType.CHANDRA_MOON }
                    ) {
                        Text(
                            text = "🌙 Moon Sign (Chandra)",
                            color = if (isChandra) activeTextColor else theme.screenTextColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isNaam) theme.accentColor else theme.surfaceColor,
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { rashiViewType = RashiViewType.NAAM_NAME }
                    ) {
                        Text(
                            text = "🔤 By Name",
                            color = if (isNaam) activeTextColor else theme.screenTextColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSurya) theme.accentColor else theme.surfaceColor,
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { rashiViewType = RashiViewType.SURYA_SUN }
                    ) {
                        Text(
                            text = "☀️ Sun Sign",
                            color = if (isSurya) activeTextColor else theme.screenTextColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }

                // Rashi Hero Badge
                Surface(
                    color = theme.surfaceColor,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(activeRashi.symbol, fontSize = 36.sp)
                            Column {
                                Text(
                                    text = "${activeRashi.sanskritName} (${activeRashi.hindiName} / ${activeRashi.englishName})",
                                    color = theme.screenTextColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = if (rashiViewType == RashiViewType.CHANDRA_MOON)
                                        "${astrologyResult.rashiDegreeStr} • ${activeRashi.rulingPlanet}"
                                    else
                                        "Lord: ${activeRashi.rulingPlanet} • ${activeRashi.element}",
                                    color = theme.accentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Key Astrological Traits Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AstroPill(
                        label = "Vedic Nakshatra",
                        value = "${astrologyResult.nakshatraName} (Pada ${astrologyResult.nakshatraPada})",
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                    AstroPill(
                        label = "Nakshatra Lord",
                        value = astrologyResult.nakshatraLord,
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AstroPill(
                        label = "Lucky Gemstone",
                        value = "${activeRashi.luckyGemstone} (${activeRashi.luckyGemstoneHindi})",
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                    AstroPill(
                        label = "Lucky Day & Color",
                        value = "${activeRashi.luckyDay.split(" ").first()} • ${activeRashi.luckyColor.split(",").first()}",
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AstroPill(
                        label = "Lucky Numbers",
                        value = activeRashi.luckyNumbers.joinToString(", "),
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                    AstroPill(
                        label = "Western & Chinese",
                        value = "${astrologyResult.westernSign} ${astrologyResult.westernSymbol} • ${astrologyResult.chineseZodiac}",
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                }

                // In-App AstroSage Daily Planetary Guidance
                Surface(
                    color = theme.surfaceColor,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "DAILY PLANETARY GUIDANCE (ASTROSAGE JYOTISH)",
                            color = theme.accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = astrologyResult.dailyHoroscopeReading,
                            color = theme.screenTextColor,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Text(
                            text = astrologyResult.planetaryTransitNote,
                            color = theme.screenExpressionColor,
                            fontSize = 10.sp
                        )
                    }
                }

                // Favorable Mantra
                Surface(
                    color = theme.accentColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("VEDIC BEEJ MANTRA", color = theme.accentColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        Text(
                            text = activeRashi.mantra,
                            color = theme.screenTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // --- 5. Next Birthday Countdown Card ---
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                            imageVector = Icons.Default.Cake,
                            contentDescription = null,
                            tint = theme.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Next Birthday Countdown",
                            color = theme.screenTextColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "on ${ageResult.nextBirthdayDayOfWeek}",
                        color = theme.screenExpressionColor,
                        fontSize = 12.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${ageResult.nextBirthdayMonths}m ${ageResult.nextBirthdayDays}d  ${String.format("%02d", ageResult.nextBirthdayHours)}h ${String.format("%02d", ageResult.nextBirthdayMinutes)}m ${String.format("%02d", ageResult.nextBirthdaySeconds)}s",
                        color = theme.accentColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // --- 6. Lifetime Summary Statistics Grid ---
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
                        text = "TOTAL LIFETIME LIVED",
                        color = theme.accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${astrologyResult.westernSymbol} ${astrologyResult.westernSign}",
                            color = theme.secondaryAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                LifetimeMetricRow(label = "Total Months", value = df.format(ageResult.totalMonths), theme = theme)
                LifetimeMetricRow(label = "Total Weeks", value = df.format(ageResult.totalWeeks), theme = theme)
                LifetimeMetricRow(label = "Total Days", value = df.format(ageResult.totalDays), theme = theme)
                LifetimeMetricRow(label = "Total Hours", value = df.format(ageResult.totalHours), theme = theme)
                LifetimeMetricRow(label = "Total Minutes", value = df.format(ageResult.totalMinutes), theme = theme)
                LifetimeMetricRow(label = "Total Seconds", value = df.format(ageResult.totalSeconds), theme = theme, isLive = true)
            }
        }
    }

    // Modal Sheet for Step-by-Step Date of Birth Selection (Year -> Month -> Day)
    if (showStepDatePicker) {
        StepDatePickerBottomSheet(
            currentDateTime = birthDateTime,
            initialStage = stepPickerInitialStage,
            theme = theme,
            onConfirmDate = { newDate ->
                onUpdateBirthDateTime(LocalDateTime.of(newDate, birthDateTime.toLocalTime()))
                showStepDatePicker = false
            },
            onDismiss = { showStepDatePicker = false }
        )
    }

    // Modal Sheet for Saved Profiles History
    if (showProfilesSheet) {
        SavedProfilesBottomSheet(
            profiles = savedProfiles,
            activeProfileId = selectedProfile?.id,
            theme = theme,
            onLoad = {
                onLoadProfile(it)
                showProfilesSheet = false
            },
            onDelete = { onDeleteProfile(it) },
            onDismiss = { showProfilesSheet = false }
        )
    }
}

// ---------------------- STEP-BY-STEP DATE PICKER (YEAR -> MONTH -> DAY) ----------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepDatePickerBottomSheet(
    currentDateTime: LocalDateTime,
    initialStage: StepPickerStage,
    theme: ThemePalette,
    onConfirmDate: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var stage by remember { mutableStateOf(initialStage) }
    var selectedYear by remember { mutableStateOf(currentDateTime.year) }
    var selectedMonth by remember { mutableStateOf(currentDateTime.monthValue) }
    var selectedDay by remember { mutableStateOf(currentDateTime.dayOfMonth) }

    val currentYearNow = LocalDate.now().year
    val yearsList = remember { (currentYearNow downTo 1920).toList() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = theme.surfaceColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxHeight(0.75f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (stage) {
                            StepPickerStage.YEAR -> "Step 1: Select Year of Birth"
                            StepPickerStage.MONTH -> "Step 2: Select Month of Birth"
                            StepPickerStage.DAY -> "Step 3: Select Day of Birth"
                        },
                        color = theme.screenTextColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Current: $selectedDay ${java.time.Month.of(selectedMonth).name.take(3)} $selectedYear",
                        color = theme.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = theme.screenTextColor)
                }
            }

            // Stage Navigation Indicator Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (stage == StepPickerStage.YEAR) theme.accentColor else theme.cardBackground,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { stage = StepPickerStage.YEAR }
                ) {
                    Text(
                        text = "1. Year ($selectedYear)",
                        color = if (stage == StepPickerStage.YEAR) theme.backgroundColor else theme.screenTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (stage == StepPickerStage.MONTH) theme.accentColor else theme.cardBackground,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { stage = StepPickerStage.MONTH }
                ) {
                    Text(
                        text = "2. Month (${java.time.Month.of(selectedMonth).name.take(3)})",
                        color = if (stage == StepPickerStage.MONTH) theme.backgroundColor else theme.screenTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (stage == StepPickerStage.DAY) theme.accentColor else theme.cardBackground,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { stage = StepPickerStage.DAY }
                ) {
                    Text(
                        text = "3. Day ($selectedDay)",
                        color = if (stage == StepPickerStage.DAY) theme.backgroundColor else theme.screenTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            HorizontalDivider(color = theme.screenBorderColor.copy(alpha = 0.25f))

            // Stage Content
            when (stage) {
                StepPickerStage.YEAR -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(yearsList) { yr ->
                            val isSelected = yr == selectedYear
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) theme.accentColor else theme.cardBackground,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedYear = yr
                                        stage = StepPickerStage.MONTH
                                    }
                            ) {
                                Text(
                                    text = "$yr",
                                    color = if (isSelected) theme.backgroundColor else theme.screenTextColor,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        }
                    }
                }

                StepPickerStage.MONTH -> {
                    val months = (1..12).map { java.time.Month.of(it) }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(months) { m ->
                            val isSelected = m.value == selectedMonth
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) theme.accentColor else theme.cardBackground,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedMonth = m.value
                                        stage = StepPickerStage.DAY
                                    }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 14.dp)
                                ) {
                                    Text(
                                        text = String.format("%02d", m.value),
                                        color = if (isSelected) theme.backgroundColor.copy(alpha = 0.8f) else theme.screenExpressionColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = m.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                                        color = if (isSelected) theme.backgroundColor else theme.screenTextColor,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                StepPickerStage.DAY -> {
                    val daysInMonth = YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
                    val daysList = (1..daysInMonth).toList()

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(daysList) { d ->
                            val isSelected = d == selectedDay
                            val dateCandidate = LocalDate.of(selectedYear, selectedMonth, d)
                            val isFuture = dateCandidate.isAfter(LocalDate.now())

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) theme.accentColor else if (isFuture) theme.cardBackground.copy(alpha = 0.4f) else theme.cardBackground,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(enabled = !isFuture) {
                                        selectedDay = d
                                        onConfirmDate(LocalDate.of(selectedYear, selectedMonth, selectedDay))
                                    }
                            ) {
                                Text(
                                    text = "$d",
                                    color = if (isSelected) theme.backgroundColor else if (isFuture) theme.screenExpressionColor.copy(alpha = 0.4f) else theme.screenTextColor,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Confirm / Done Button
            Button(
                onClick = {
                    val maxDays = YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
                    val validDay = selectedDay.coerceAtMost(maxDays)
                    var target = LocalDate.of(selectedYear, selectedMonth, validDay)
                    if (target.isAfter(LocalDate.now())) {
                        target = LocalDate.now()
                    }
                    onConfirmDate(target)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)
            ) {
                Text(
                    text = "Apply Date ($selectedDay ${java.time.Month.of(selectedMonth).name.take(3)} $selectedYear)",
                    color = theme.backgroundColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AstroPill(
    label: String,
    value: String,
    theme: ThemePalette,
    modifier: Modifier = Modifier
) {
    Surface(
        color = theme.surfaceColor,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(label, color = theme.screenExpressionColor, fontSize = 9.sp)
            Text(
                text = value,
                color = theme.screenTextColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AgeUnitBlock(
    value: String,
    label: String,
    theme: ThemePalette,
    isAccent: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        Text(
            text = value,
            color = if (isAccent) theme.secondaryAccent else theme.screenTextColor,
            fontSize = if (isAccent) 24.sp else 28.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
        Text(
            text = label.uppercase(),
            color = theme.screenExpressionColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun LifetimeMetricRow(
    label: String,
    value: String,
    theme: ThemePalette,
    isLive: Boolean = false
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
            color = if (isLive) theme.accentColor else theme.screenTextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedProfilesBottomSheet(
    profiles: List<AgeProfile>,
    activeProfileId: Long?,
    theme: ThemePalette,
    onLoad: (AgeProfile) -> Unit,
    onDelete: (AgeProfile) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = theme.surfaceColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .fillMaxHeight(0.7f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved Age Profiles (${profiles.size})",
                    color = theme.screenTextColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = theme.screenTextColor)
                }
            }

            if (profiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved profiles yet.\nEnter a name above and tap 'Save'.",
                        color = theme.screenExpressionColor,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(profiles, key = { it.id }) { profile ->
                        val isCurrent = profile.id == activeProfileId
                        Surface(
                            color = if (isCurrent) theme.accentColor.copy(alpha = 0.18f) else theme.cardBackground,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onLoad(profile) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        color = if (isCurrent) theme.accentColor else theme.surfaceColor,
                                        shape = CircleShape,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = profile.name.take(1).uppercase(),
                                                color = if (isCurrent) theme.backgroundColor else theme.accentColor,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = profile.name,
                                            color = theme.screenTextColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        val displayRelOrNote = when {
                                            profile.relationship.isNotBlank() && !profile.relationship.equals("Friend", ignoreCase = true) -> " • ${profile.relationship}"
                                            profile.notes.isNotBlank() && !profile.notes.equals("Friend", ignoreCase = true) -> " • ${profile.notes}"
                                            else -> ""
                                        }
                                        Text(
                                            text = "${profile.birthDay}/${profile.birthMonth}/${profile.birthYear}$displayRelOrNote",
                                            color = theme.screenExpressionColor,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                IconButton(onClick = { onDelete(profile) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = theme.screenExpressionColor.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

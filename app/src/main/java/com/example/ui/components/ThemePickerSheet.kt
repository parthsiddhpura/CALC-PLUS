package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ThemeId
import com.example.model.ThemePalette
import com.example.model.onCardColor
import com.example.model.onCardSubtextColor
import com.example.model.onSurfaceSubtextColor
import com.example.model.onSurfaceTextColor
import com.example.ui.theme.CalculatorThemes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePickerSheet(
    activeTheme: ThemePalette,
    onSelectTheme: (ThemeId) -> Unit,
    onRandomTheme: () -> Unit,
    isSoundEnabled: Boolean,
    onToggleSound: () -> Unit,
    isHapticsEnabled: Boolean,
    onToggleHaptics: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf("All") } // "All", "Dark Only", "Light Only"

    val categories = listOf("All", "iOS & Nothing", "Neumorphic & Glass", "Kawaii & Cute", "Interactive Dark", "Retro", "Playful", "Minimal & Aesthetic", "Futuristic", "Modern Art", "Atmospheric", "Luxury & Clean")

    val filteredThemes = remember(selectedCategory, searchQuery, filterMode) {
        CalculatorThemes.allThemes.filter { theme ->
            val matchesCategory = if (selectedCategory == "All") true
            else if (selectedCategory == "iOS & Nothing") theme.isLiquidGlass || theme.isNothingOs || theme.id == ThemeId.IOS_LIQUID_GLASS || theme.id == ThemeId.NOTHING_OS_DARK || theme.id == ThemeId.NOTHING_OS_LIGHT
            else theme.category.contains(selectedCategory, ignoreCase = true)
            val matchesSearch = if (searchQuery.isBlank()) true else {
                theme.name.contains(searchQuery, ignoreCase = true) ||
                theme.subtitle.contains(searchQuery, ignoreCase = true) ||
                theme.category.contains(searchQuery, ignoreCase = true)
            }
            val matchesMode = when (filterMode) {
                "Dark Only" -> theme.isDark
                "Light Only" -> !theme.isDark
                else -> true
            }
            matchesCategory && matchesSearch && matchesMode
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = activeTheme.surfaceColor,
        contentColor = activeTheme.onSurfaceTextColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Header Row: Title, Surprise Me Randomizer, Close
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
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = activeTheme.accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Theme Switcher & Gallery",
                            color = activeTheme.onSurfaceTextColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${CalculatorThemes.allThemes.size} crafted visual styles",
                            color = activeTheme.onSurfaceSubtextColor,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Randomizer button
                    IconButton(
                        onClick = onRandomTheme,
                        modifier = Modifier.testTag("btn_sheet_random_theme")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Surprise Me",
                            tint = activeTheme.secondaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_theme_sheet")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close theme selector",
                            tint = activeTheme.onSurfaceSubtextColor
                        )
                    }
                }
            }

            // Quick Sound & Haptic Preferences Row
            Surface(
                color = activeTheme.cardBackground,
                shape = RoundedCornerShape(14.dp),
                border = if (activeTheme.surfaceColor.luminance() > 0.45f) {
                    androidx.compose.foundation.BorderStroke(1.dp, activeTheme.accentColor.copy(alpha = 0.3f))
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sound Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = activeTheme.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Sound",
                            color = activeTheme.onCardColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = isSoundEnabled,
                            onCheckedChange = { onToggleSound() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = activeTheme.backgroundColor,
                                checkedTrackColor = activeTheme.accentColor
                            ),
                            modifier = Modifier.testTag("switch_theme_sound")
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(activeTheme.surfaceColor)
                    )

                    // Haptics Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = activeTheme.secondaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Haptics",
                            color = activeTheme.onCardColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = isHapticsEnabled,
                            onCheckedChange = { onToggleHaptics() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = activeTheme.backgroundColor,
                                checkedTrackColor = activeTheme.secondaryAccent
                            ),
                            modifier = Modifier.testTag("switch_theme_haptics")
                        )
                    }
                }
            }

            // Search Bar & Dark/Light filter row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search themes by name or vibe...",
                            color = activeTheme.onCardSubtextColor.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = activeTheme.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = activeTheme.onCardSubtextColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeTheme.accentColor,
                        unfocusedBorderColor = if (activeTheme.surfaceColor.luminance() > 0.45f) activeTheme.accentColor.copy(alpha = 0.35f) else activeTheme.cardBackground,
                        focusedContainerColor = activeTheme.cardBackground,
                        unfocusedContainerColor = activeTheme.cardBackground,
                        focusedTextColor = activeTheme.onCardColor,
                        unfocusedTextColor = activeTheme.onCardColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("theme_search_input")
                )

                // Dark/Light toggle chip
                Surface(
                    color = activeTheme.cardBackground,
                    shape = RoundedCornerShape(12.dp),
                    border = if (activeTheme.surfaceColor.luminance() > 0.45f) {
                        androidx.compose.foundation.BorderStroke(1.dp, activeTheme.accentColor.copy(alpha = 0.35f))
                    } else null,
                    modifier = Modifier
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            filterMode = when (filterMode) {
                                "All" -> "Dark Only"
                                "Dark Only" -> "Light Only"
                                else -> "All"
                            }
                        }
                        .testTag("btn_filter_mode")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (filterMode) {
                                "Dark Only" -> Icons.Default.DarkMode
                                "Light Only" -> Icons.Default.LightMode
                                else -> Icons.Default.Palette
                            },
                            contentDescription = null,
                            tint = activeTheme.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = when (filterMode) {
                                "Dark Only" -> "Dark"
                                "Light Only" -> "Light"
                                else -> "All"
                            },
                            color = activeTheme.onCardColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                items(categories, key = { it }) { cat ->
                    val isSelected = cat == selectedCategory
                    Surface(
                        color = if (isSelected) activeTheme.accentColor else activeTheme.cardBackground,
                        shape = RoundedCornerShape(10.dp),
                        border = if (!isSelected && activeTheme.surfaceColor.luminance() > 0.45f) {
                            androidx.compose.foundation.BorderStroke(1.dp, activeTheme.accentColor.copy(alpha = 0.3f))
                        } else null,
                        modifier = Modifier
                            .clickable { selectedCategory = cat }
                            .testTag("theme_category_$cat")
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) {
                                if (activeTheme.accentColor.luminance() > 0.6f) Color(0xFF211118) else Color.White
                            } else activeTheme.onCardColor,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Themes Grid / List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.78f)
            ) {
                if (filteredThemes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No themes match \"$searchQuery\"",
                                color = activeTheme.screenExpressionColor,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(filteredThemes, key = { it.id.name }) { itemTheme ->
                        val isSelected = itemTheme.id == activeTheme.id
                        ThemeCard(
                            theme = itemTheme,
                            isSelected = isSelected,
                            onSelect = { onSelectTheme(itemTheme.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeCard(
    theme: ThemePalette,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)
    var testTapCounter by remember { mutableStateOf(0) }

    val isLightBg = theme.backgroundColor.luminance() > 0.45f
    val titleColor = if (isLightBg) {
        if (theme.id == ThemeId.NEKO_MOCHI_CAT) Color(0xFF3B1A23)
        else if (theme.screenTextColor.luminance() > 0.45f) Color(0xFF1E1E1E)
        else theme.screenTextColor
    } else {
        if (theme.screenTextColor.luminance() < 0.35f) Color(0xFFF5F5F5)
        else theme.screenTextColor
    }

    val subtitleColor = if (isLightBg) {
        if (theme.id == ThemeId.NEKO_MOCHI_CAT) Color(0xFF70404C)
        else if (theme.screenExpressionColor.luminance() > 0.45f) Color(0xFF555555)
        else theme.screenExpressionColor
    } else {
        if (theme.screenExpressionColor.luminance() < 0.35f) Color(0xFFBBBBBB)
        else theme.screenExpressionColor
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(theme.backgroundColor)
            .then(
                if (isSelected) {
                    Modifier.border(2.5.dp, theme.accentColor, cardShape)
                } else {
                    Modifier.border(1.dp, theme.surfaceColor.copy(alpha = 0.6f), cardShape)
                }
            )
            .clickable(onClick = onSelect)
            .padding(14.dp)
            .testTag("theme_card_${theme.id.name}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Theme Details & Swatches
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = theme.name,
                        color = titleColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (theme.hasBatSignal) {
                        Surface(
                            color = Color(0x33FFE500),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x88FFE500))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                BatmanLogoIcon(
                                    modifier = Modifier.size(10.dp),
                                    tint = Color(0xFFFFE500)
                                )
                                Text(
                                    text = "BAT-SIGNAL",
                                    color = Color(0xFFFFE500),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (theme.hasArcReactor) {
                        Surface(
                            color = Color(0x3300F0FF),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x8800F0FF))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                ArcReactorIcon(
                                    modifier = Modifier.size(10.dp),
                                    glowColor = Color(0xFF00F0FF),
                                    showOuterTabs = false
                                )
                                Text(
                                    text = "ARC REACTOR",
                                    color = Color(0xFF00F0FF),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (theme.isGirlMath) {
                        Surface(
                            color = Color(0x33FF5277),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x88FF5277))
                        ) {
                            Text(
                                text = "♡ GIRL MATH",
                                color = Color(0xFFFF5277),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (theme.isNekoMochi) {
                        Surface(
                            color = if (theme.isDark) Color(0x44FF4D79) else Color(0x33FF85A1),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, if (theme.isDark) Color(0xFFFF4D79) else Color(0x88FF85A1))
                        ) {
                            Text(
                                text = if (theme.isDark) "🐾 MIDNIGHT CAT" else "🐾 MOCHI CAT",
                                color = if (theme.isDark) Color(0xFFFF85A1) else Color(0xFFFF5277),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (theme.isY2kGlossy) {
                        Surface(
                            color = Color(0x33A2E8DD),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFFA2E8DD))
                        ) {
                            Text(
                                text = "✨ Y2K GLOSSY",
                                color = Color(0xFF144740),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (theme.isPixelArt) {
                        Surface(
                            color = Color(0x3300FF66),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x8800FF66))
                        ) {
                            Text(
                                text = "👾 8-BIT",
                                color = Color(0xFF00FF66),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (theme.isRetroCircuit) {
                        Surface(
                            color = theme.accentColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, theme.accentColor)
                        ) {
                            Text(
                                text = "⚡ RETRO CIRCUIT",
                                color = theme.accentColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (theme.isNothingDossier) {
                        Surface(
                            color = theme.accentColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, theme.accentColor)
                        ) {
                            Text(
                                text = "▫ NOTHING DOSSIER",
                                color = theme.accentColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (theme.isBauhausDossier) {
                        Surface(
                            color = theme.accentColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, theme.accentColor)
                        ) {
                            Text(
                                text = "▣ SWISS BAUHAUS",
                                color = theme.accentColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (theme.isTerracottaStudio) {
                        Surface(
                            color = theme.accentColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, theme.accentColor)
                        ) {
                            Text(
                                text = "⚪ TERRACOTTA",
                                color = theme.accentColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Surface(
                        color = theme.accentColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = theme.category,
                            color = theme.accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (theme.isDark) {
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "DARK",
                                color = Color(0xFF94A3B8),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "LIGHT",
                                color = Color(0xFFD97706),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = theme.subtitle,
                    color = subtitleColor,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Mini Keypad Preview Swatches (Interactive test area)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mini Screen preview box
                    Box(
                        modifier = Modifier
                            .size(38.dp, 24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(theme.screenBackground)
                            .border(0.5.dp, theme.screenBorderColor, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (theme.hasBatSignal) {
                            BatmanLogoIcon(
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFFFE500)
                            )
                        } else if (theme.hasArcReactor) {
                            ArcReactorIcon(
                                modifier = Modifier.size(16.dp),
                                glowColor = Color(0xFF00F0FF),
                                showOuterTabs = false
                            )
                        } else {
                            Text(
                                text = "${42 + testTapCounter}",
                                color = theme.screenTextColor,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Mini Number button
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(theme.getShape())
                            .background(theme.numberButtonBg)
                            .border(0.5.dp, theme.numberButtonBorder, theme.getShape())
                            .clickable { testTapCounter++ },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "7", color = theme.numberButtonText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Mini Operator button
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(theme.getShape())
                            .background(theme.operatorButtonBg)
                            .border(0.5.dp, theme.operatorButtonBorder, theme.getShape())
                            .clickable { testTapCounter++ },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "+", color = theme.operatorButtonText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Mini Function button
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(theme.getShape())
                            .background(theme.functionButtonBg)
                            .border(0.5.dp, theme.functionButtonBorder, theme.getShape())
                            .clickable { testTapCounter = 0 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "AC", color = theme.functionButtonText, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }

                    // Mini Equals button
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(theme.getShape())
                            .background(theme.equalsButtonBrush)
                            .border(0.5.dp, theme.equalsButtonBorder, theme.getShape())
                            .clickable { testTapCounter += 10 },
                        contentAlignment = Alignment.Center
                    ) {
                        if (theme.hasArcReactor) {
                            ArcReactorIcon(
                                modifier = Modifier.size(20.dp),
                                glowColor = Color(0xFF00F0FF),
                                showOuterTabs = false
                            )
                        } else {
                            Text(text = "=", color = theme.equalsButtonText, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            // Selection Indicator Checkmark or Apply Button
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(theme.accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active theme",
                        tint = theme.backgroundColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Surface(
                    color = theme.surfaceColor,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onSelect)
                ) {
                    Text(
                        text = "Apply",
                        color = theme.accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

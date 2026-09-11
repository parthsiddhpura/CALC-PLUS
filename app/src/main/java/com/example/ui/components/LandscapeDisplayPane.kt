package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AngleMode
import com.example.model.CalculatorMode
import com.example.model.DisplayConfig
import com.example.model.ThemePalette

/**
 * Modern, ergonomic left-pane container for landscape orientation.
 * Displays the high-contrast calculator screen with live preview and
 * an integrated quick-action dock for memory, history, and notation formatting.
 */
@Composable
fun LandscapeDisplayPane(
    expression: String,
    result: String,
    previewResult: String?,
    theme: ThemePalette,
    angleMode: AngleMode,
    hasMemory: Boolean,
    mode: CalculatorMode,
    displayConfig: DisplayConfig,
    historyCount: Int,
    cursorPosition: Int,
    onCursorChange: (Int) -> Unit,
    isEvaluated: Boolean,
    onToggleAngleMode: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenDecimalConverter: () -> Unit,
    onToggleNotation: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Calculator screen (occupies top/majority of left pane)
        CalculatorDisplay(
            expression = expression,
            result = result,
            previewResult = previewResult,
            theme = theme,
            angleMode = angleMode,
            hasMemory = hasMemory,
            mode = mode,
            displayConfig = displayConfig,
            historyCount = historyCount,
            cursorPosition = cursorPosition,
            onCursorChange = onCursorChange,
            isEvaluated = isEvaluated,
            onToggleAngleMode = onToggleAngleMode,
            onOpenHistory = onOpenHistory,
            onOpenDecimalConverter = onOpenDecimalConverter,
            onToggleNotation = onToggleNotation,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Landscape Memory & Quick Action Dock
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 0.8.dp,
                    color = if (theme.isDark) theme.screenBorderColor.copy(alpha = 0.4f) else theme.accentColor.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(12.dp)
                ),
            color = if (theme.isDark) theme.surfaceColor.copy(alpha = 0.75f) else theme.surfaceColor,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Memory Keys: MC, MR, M+, M-
                val memTextColor = if (theme.isDark) theme.screenTextColor.copy(alpha = 0.85f) else Color(0xFF1E293B)
                val activeMemColor = theme.accentColor

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // MC
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .clickable { onMemoryClear() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "MC",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasMemory) activeMemColor else memTextColor.copy(alpha = 0.45f)
                        )
                    }

                    // MR
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .clickable { onMemoryRecall() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "MR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasMemory) activeMemColor else memTextColor.copy(alpha = 0.45f)
                        )
                    }

                    // M+
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .clickable { onMemoryAdd() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "M+",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = memTextColor
                        )
                    }

                    // M-
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .clickable { onMemorySubtract() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "M−",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = memTextColor
                        )
                    }
                }

                // Quick Tools: History, Notation, Copy
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // History icon with badge
                    BadgedBox(
                        badge = {
                            if (historyCount > 0) {
                                Badge(
                                    containerColor = theme.accentColor,
                                    contentColor = theme.backgroundColor
                                ) {
                                    Text(
                                        text = if (historyCount > 99) "99+" else historyCount.toString(),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .clickable { onOpenHistory() }
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Calculation History",
                            tint = memTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Notation toggle
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (theme.isDark) Color(0xFF1E242B) else Color(0xFFE2E8F0),
                        modifier = Modifier
                            .clickable { onToggleNotation() }
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when (displayConfig.notation) {
                                com.example.model.DisplayNotation.STANDARD -> "NORM"
                                com.example.model.DisplayNotation.SCIENTIFIC -> "SCI"
                                com.example.model.DisplayNotation.ENGINEERING -> "ENG"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.accentColor
                        )
                    }

                    // Copy Result
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Result",
                        tint = memTextColor,
                        modifier = Modifier
                            .size(15.dp)
                            .clickable {
                                val cleanResult = result.replace(",", "").replace(" ", "")
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Calculator Result", cleanResult))
                                Toast.makeText(context, "Copied $cleanResult", Toast.LENGTH_SHORT).show()
                            }
                    )
                }
            }
        }
    }
}

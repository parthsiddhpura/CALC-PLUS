package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ThemePalette
import kotlinx.coroutines.launch

data class TutorialStep(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: Color,
    val description: String,
    val exampleTapeSnippet: List<Pair<String, String>>, // Operator/Type -> Text
    val tip: String
)

@Composable
fun PaperTapeTutorialDialog(
    theme: ThemePalette,
    onDismiss: () -> Unit
) {
    val isLight = theme.backgroundColor.luminance() > 0.45f
    val dialogBg = if (isLight) Color(0xFFF8FAFC) else Color(0xFF1B2028)
    val cardBg = if (isLight) Color(0xFFFFFFFF) else Color(0xFF242C37)
    val textColor = if (isLight) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val subtextColor = if (isLight) Color(0xFF64748B) else Color(0xFF94A3B8)
    val accentColor = theme.accentColor
    val borderColor = if (isLight) Color(0xFFE2E8F0) else Color(0xFF334155)

    val steps = remember {
        listOf(
            TutorialStep(
                title = "Welcome to Paper Tape",
                subtitle = "Continuous Audit Trail Calculator",
                icon = Icons.Default.ReceiptLong,
                iconTint = Color(0xFF38BDF8),
                description = "Unlike traditional calculators where intermediate numbers disappear, Paper Tape records every entry step-by-step on a scrollable digital ledger tape.",
                exampleTapeSnippet = listOf(
                    "+" to "5,000.00",
                    "+" to "2,500.00",
                    "=" to "7,500.00"
                ),
                tip = "All calculations are automatically saved and organized into worksheets."
            ),
            TutorialStep(
                title = "Live Interactive Tape",
                subtitle = "Edit Any Past Number Anytime",
                icon = Icons.Default.EditNote,
                iconTint = Color(0xFF10B981),
                description = "Made a typo 5 lines ago? Don't start over! Simply tap any previous number on the tape to correct it. All subsequent rows and totals recalculate automatically.",
                exampleTapeSnippet = listOf(
                    "+" to "1,200.00 (Tap to change)",
                    "-" to "200.00 discount",
                    "=" to "1,000.00"
                ),
                tip = "Tap the 🗑 icon next to any selected row to delete that line."
            ),
            TutorialStep(
                title = "Notes & Descriptions",
                subtitle = "Label Your Transactions",
                icon = Icons.Default.Comment,
                iconTint = Color(0xFFF59E0B),
                description = "Add itemized remarks to any line. Tap on the subtle 'note...' prompt next to any amount, or switch to ABC keyboard mode on the dock to type descriptions.",
                exampleTapeSnippet = listOf(
                    "+" to "15,000.00 Office Rent",
                    "+" to "3,200.00 Electricity",
                    "+" to "850.00 Internet"
                ),
                tip = "Type notes smoothly without lag; your notes are preserved when exporting or sharing."
            ),
            TutorialStep(
                title = "Subtotals & Comment Blocks",
                subtitle = "Organize Multi-Part Calculations",
                icon = Icons.Default.FormatListNumbered,
                iconTint = Color(0xFFA855F7),
                description = "Press the '=/↵' button on the keypad to insert an underline subtotal. A blank comment row is automatically added below so you can title your next section.",
                exampleTapeSnippet = listOf(
                    "+" to "2,400.00",
                    "=" to "2,400.00 Subtotal",
                    "#" to "Phase 2 Materials"
                ),
                tip = "Use subtotals to verify individual project phases before concluding the grand total."
            ),
            TutorialStep(
                title = "Optional Header Note",
                subtitle = "Custom Worksheet Titles",
                icon = Icons.Default.Title,
                iconTint = Color(0xFFEC4899),
                description = "You can add a title or header note at the top of your tape. If you don't need one, you can easily remove it with 1 tap using the remove button or overflow menu.",
                exampleTapeSnippet = listOf(
                    "HEADER" to "Monthly Budget (Removable ✕)",
                    "+" to "50,000.00 Salary"
                ),
                tip = "Header notes are completely optional—keep your tape clean or detailed as you prefer."
            ),
            TutorialStep(
                title = "Customizable Keypad",
                subtitle = "One-Tap Tax, GST & Custom Rates",
                icon = Icons.Default.Tune,
                iconTint = Color(0xFF06B6D4),
                description = "Tap the 'Customise Button' or open 'Edit Keyboard' from the 3-dots menu to map quick tax shortcuts like +GST 18%, -GST 12%, +VAT 20%, or 00 keys.",
                exampleTapeSnippet = listOf(
                    "+" to "10,000.00",
                    "+GST (18%)" to "1,800.00",
                    "=" to "11,800.00 Total"
                ),
                tip = "The customized button dynamically resizes its text so digits and rates look crisp and readable."
            ),
            TutorialStep(
                title = "Export, Share & Print",
                subtitle = "Professional Receipts in Seconds",
                icon = Icons.Default.Share,
                iconTint = Color(0xFFE11D48),
                description = "Export your calculation as a crisp PNG receipt photo directly to WhatsApp, save to gallery, print via Android print service, or copy formatted text.",
                exampleTapeSnippet = listOf(
                    "SHARE" to "Photo PNG Receipt",
                    "PRINT" to "Direct PDF / Printer",
                    "COPY" to "Plain text / HTML"
                ),
                tip = "Access all export tools anytime from the 3-dots menu on the top right."
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { steps.size })
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp)),
            color = dialogBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = accentColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Paper Tape Guide",
                                color = textColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Step ${pagerState.currentPage + 1} of ${steps.size}",
                                color = subtextColor,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = subtextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pager Content
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    val step = steps[page]
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Card Header with Icon
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = cardBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = step.iconTint.copy(alpha = 0.15f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = step.icon,
                                            contentDescription = null,
                                            tint = step.iconTint,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = step.title,
                                        color = textColor,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = step.subtitle,
                                        color = step.iconTint,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Explanation Text
                        Text(
                            text = step.description,
                            color = textColor,
                            fontSize = 14.sp,
                            lineHeight = 21.sp
                        )

                        // Interactive / Visual Tape Simulation Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isLight) Color(0xFFF1F5F9) else Color(0xFF13171E),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isLight) Color(0xFFCBD5E1) else Color(0xFF2E3846)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "TAPE PREVIEW",
                                        color = subtextColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "[ ⏱ LIVE ]",
                                        color = Color(0xFF10B981),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                HorizontalDivider(
                                    color = if (isLight) Color(0xFFE2E8F0) else Color(0xFF26303D),
                                    thickness = 1.dp
                                )

                                step.exampleTapeSnippet.forEach { (op, valText) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = op,
                                            color = if (op == "-") Color(0xFFE57373) else if (op == "=") accentColor else textColor,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = valText,
                                            color = if (op == "-") Color(0xFFE57373) else textColor,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = if (op == "=") FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Pro Tip Box
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = accentColor.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = step.tip,
                                    color = textColor,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Pagination & Navigation Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button / Skip
                    if (pagerState.currentPage > 0) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Back", color = textColor)
                        }
                    } else {
                        TextButton(onClick = onDismiss) {
                            Text("Skip", color = subtextColor)
                        }
                    }

                    // Dots indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(steps.size) { idx ->
                            val isSelected = pagerState.currentPage == idx
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) accentColor else subtextColor.copy(alpha = 0.4f)
                                    )
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(idx)
                                        }
                                    }
                            )
                        }
                    }

                    // Next button or Done
                    if (pagerState.currentPage < steps.size - 1) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Next", color = Color.White)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Got It!", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

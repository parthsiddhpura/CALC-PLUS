package com.example.ui.components

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.VolunteerActivism
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billing.PlayBillingManager
import com.example.billing.TipTier
import com.example.model.ThemePalette
import com.example.model.onCardColor
import com.example.model.onCardSubtextColor
import com.example.model.onSurfaceSubtextColor
import com.example.model.onSurfaceTextColor

/**
 * Clean, minimal, humble card placed prominently in the More section and Settings.
 */
@Composable
fun DeveloperSupportCard(
    theme: ThemePalette,
    onOpenSupportSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val supporterState by PlayBillingManager.supporterState.collectAsState()

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = theme.cardBackground,
        border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenSupportSheet() }
            .testTag("developer_support_card")
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = theme.accentColor.copy(alpha = 0.16f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Support",
                            tint = theme.accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (supporterState.isSupporter) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = theme.accentColor.copy(alpha = 0.18f),
                            border = BorderStroke(0.8.dp, theme.accentColor.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = "SUPPORTER 🏅",
                                color = theme.accentColor,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                            )
                        }
                    }
                    Text(
                        text = "Support the Developer",
                        color = theme.onCardColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "100% Free & Ad-Free • Help sustain updates",
                        color = theme.onCardSubtextColor,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            val pillBgColor = when {
                theme.accentColor.luminance() > 0.28f -> theme.accentColor
                theme.secondaryAccent.luminance() > 0.28f -> theme.secondaryAccent
                else -> Color(0xFFFFE500)
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = pillBgColor,
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Text(
                    text = "Tip / Donate",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }
    }
}

/**
 * Dedicated, minimal Voluntary Support Bottom Sheet with customizable tips (no minimum amount).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperSupportSheet(
    theme: ThemePalette,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val supporterState by PlayBillingManager.supporterState.collectAsState()

    var selectedTier by remember { mutableStateOf(TipTier.COFFEE) }
    var customAmountText by remember { mutableStateOf("50") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = theme.surfaceColor,
        contentColor = theme.onSurfaceTextColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
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
                        color = theme.accentColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.VolunteerActivism,
                                contentDescription = null,
                                tint = theme.accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Support Independent Work",
                            color = theme.onSurfaceTextColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Free • Privacy-First • Zero Ads",
                            color = theme.onSurfaceSubtextColor,
                            fontSize = 11.5.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = theme.onSurfaceSubtextColor
                    )
                }
            }

            // Humble Creator Note
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = theme.cardBackground,
                border = BorderStroke(0.8.dp, theme.screenBorderColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "From the Developer",
                        color = theme.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "CALC+ is crafted with care as an ad-free, high-precision digital instrument. Building, refining, and polishing it consumes a significant amount of time and energy. If this app helps your daily workflow or brings tactile joy to your screen, any voluntary tip is deeply appreciated and keeps the project alive.",
                        color = theme.onCardColor.copy(alpha = 0.92f),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            // Tip Tiers Selector
            Text(
                text = "Choose Contribution Tier",
                color = theme.screenTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            val onAccentColor = if (theme.accentColor.luminance() > 0.35f) Color(0xFF0A0A0A) else Color.White

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(TipTier.COFFEE, TipTier.LUNCH, TipTier.CHAMPION, TipTier.CUSTOM).forEach { tier ->
                    val isSelected = selectedTier == tier
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) theme.accentColor else theme.cardBackground,
                        border = if (isSelected) null else BorderStroke(1.dp, theme.screenBorderColor.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedTier = tier
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(text = tier.emoji, fontSize = 18.sp)
                            Text(
                                text = tier.title.replace("Buy a ", "").replace("Treat to ", ""),
                                color = if (isSelected) onAccentColor else theme.onCardColor,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Text(
                                text = if (tier == TipTier.CUSTOM) "Any" else tier.defaultPriceFormatted.split("/")[0].trim(),
                                color = if (isSelected) onAccentColor.copy(alpha = 0.88f) else theme.accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Custom Amount Input (If Custom is selected - No minimum amount!)
            AnimatedVisibility(visible = selectedTier == TipTier.CUSTOM) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Custom Tip Amount (No Minimum)",
                        color = theme.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = customAmountText,
                        onValueChange = { customAmountText = it.filter { char -> char.isDigit() } },
                        label = { Text("Enter custom amount (₹)", color = theme.screenExpressionColor) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.screenTextColor,
                            unfocusedTextColor = theme.screenTextColor,
                            focusedBorderColor = theme.accentColor,
                            unfocusedBorderColor = theme.cardBackground
                        )
                    )

                    // Quick Custom Increment Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(20, 50, 100, 250, 500).forEach { amount ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = theme.cardBackground,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { customAmountText = amount.toString() }
                            ) {
                                Text(
                                    text = "₹$amount",
                                    color = theme.accentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Action Button: Google Play Store Tipping
            val buttonAmount = if (selectedTier == TipTier.CUSTOM) {
                val amt = customAmountText.ifBlank { "50" }
                "₹$amt"
            } else {
                selectedTier.defaultPriceFormatted.split("/")[0].trim()
            }

            Button(
                onClick = {
                    val activity = context as? Activity ?: return@Button
                    PlayBillingManager.launchTipFlow(
                        activity = activity,
                        tier = selectedTier,
                        customAmount = if (selectedTier == TipTier.CUSTOM) customAmountText.ifBlank { "50" } else null
                    ) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        if (success) {
                            onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.accentColor,
                    contentColor = onAccentColor
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_complete_tip")
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Contribute $buttonAmount via Google Play",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

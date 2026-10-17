package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ThemePalette
import com.example.model.onSurfaceTextColor
import com.example.model.onSurfaceSubtextColor

@Composable
fun BetaFeatureNoticeDialog(
    featureKey: String,
    featureTitle: String,
    theme: ThemePalette,
    noteText: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_beta_feature_prefs", Context.MODE_PRIVATE) }
    val isPermanentlyDismissed = remember { prefs.getBoolean("dont_ask_again_$featureKey", false) }

    var isDialogVisible by remember { mutableStateOf(!isPermanentlyDismissed) }
    var dontAskAgainChecked by remember { mutableStateOf(false) }

    if (isDialogVisible && !isPermanentlyDismissed) {
        AlertDialog(
            onDismissRequest = {
                if (dontAskAgainChecked) {
                    prefs.edit().putBoolean("dont_ask_again_$featureKey", true).apply()
                }
                isDialogVisible = false
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFB703).copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, Color(0xFFFFB703))
                    ) {
                        Text(
                            text = "BETA",
                            color = Color(0xFFFFB703),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "$featureTitle Note",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.onSurfaceTextColor
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = theme.accentColor,
                            modifier = Modifier.size(18.dp).padding(top = 2.dp)
                        )
                        Text(
                            text = "Notice: This feature is currently in Beta version.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.onSurfaceTextColor
                        )
                    }

                    Text(
                        text = noteText,
                        fontSize = 13.sp,
                        color = theme.onSurfaceSubtextColor,
                        lineHeight = 18.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = theme.cardBackground,
                        border = BorderStroke(0.8.dp, theme.screenBorderColor.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { dontAskAgainChecked = !dontAskAgainChecked }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = dontAskAgainChecked,
                                onCheckedChange = { dontAskAgainChecked = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = theme.accentColor,
                                    checkmarkColor = theme.backgroundColor
                                )
                            )
                            Text(
                                text = "Don't show this again",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = theme.onSurfaceTextColor
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (dontAskAgainChecked) {
                            prefs.edit().putBoolean("dont_ask_again_$featureKey", true).apply()
                        }
                        isDialogVisible = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)
                ) {
                    Text(
                        text = "I Understand",
                        color = theme.backgroundColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = theme.surfaceColor,
            shape = RoundedCornerShape(18.dp),
            modifier = modifier
        )
    }
}

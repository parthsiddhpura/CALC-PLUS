package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.WorksheetTapeEngine
import com.example.model.ThemePalette
import com.example.model.WorksheetDocument
import com.example.model.WorksheetLine
import com.example.model.WorksheetLineType
import com.example.model.WorksheetSettings
import com.example.model.WorksheetTemplate
import com.example.model.WorksheetVariable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Slide-out Documents Navigation Drawer matching the CalcTape Pro look in the video.
 */
@Composable
fun DocumentsDrawerContent(
    documents: List<WorksheetDocument>,
    activeId: String,
    theme: ThemePalette,
    onSelect: (WorksheetDocument) -> Unit,
    onNew: () -> Unit,
    onRename: (WorksheetDocument) -> Unit = {},
    onSharePhoto: (WorksheetDocument) -> Unit = {},
    onDelete: (String) -> Unit,
    onDuplicate: (WorksheetDocument) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenInfo: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenRestore: () -> Unit,
    onOpenTutorial: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val isLightCanvas = theme.backgroundColor.luminance() > 0.45f
    val bgColor = if (isLightCanvas) Color(0xFFF1F5F9) else Color(0xFF181D26)
    val cardBg = if (isLightCanvas) Color(0xFFFFFFFF) else Color(0xFF222834)
    val activeCardBg = if (isLightCanvas) Color(0xFFE2E8F0) else Color(0xFF333E50)
    val textColor = if (isLightCanvas) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val subtextColor = if (isLightCanvas) Color(0xFF64748B) else Color(0xFF94A3B8)
    val accentColor = theme.accentColor

    val dateFormat = remember { SimpleDateFormat("d MMM yyyy h:mm:ss a", Locale.getDefault()) }
    var backupExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .background(bgColor)
            .padding(16.dp)
    ) {
        // Top Row: "Documents" title & "+" icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Documents",
                color = textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onNew,
                    modifier = Modifier
                        .size(36.dp)
                        .background(accentColor.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New document",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close drawer",
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Document list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(documents.size) { idx ->
                val doc = documents[idx]
                val isActive = doc.id == activeId

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isActive) activeCardBg else cardBg,
                    border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, accentColor) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelect(doc)
                            onDismiss()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = doc.title,
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dateFormat.format(Date(doc.updatedAt)),
                                color = subtextColor,
                                fontSize = 11.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            IconButton(
                                onClick = { onRename(doc) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Rename",
                                    tint = accentColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            IconButton(
                                onClick = { onSharePhoto(doc) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share as Photo",
                                    tint = textColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            IconButton(
                                onClick = { onDuplicate(doc) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Duplicate",
                                    tint = subtextColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            if (documents.size > 1) {
                                IconButton(
                                    onClick = { onDelete(doc.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(8.dp))

        // Bottom drawer actions
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Import documents
            DrawerActionRow(
                icon = Icons.Default.Description,
                title = "Import documents / Templates",
                textColor = textColor,
                onClick = {
                    onOpenTemplates()
                    onDismiss()
                }
            )

            // Backup / Restore Accordion
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { backupExpanded = !backupExpanded }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Backup,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(text = "Backup / Restore", color = textColor, fontSize = 14.sp)
                }
                Icon(
                    imageVector = if (backupExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = subtextColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = backupExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, end = 12.dp, bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                onOpenBackup()
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(text = "Create backup", color = textColor, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                onOpenRestore()
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(text = "Restore backup", color = textColor, fontSize = 13.sp)
                    }
                }
            }

            // Settings
            DrawerActionRow(
                icon = Icons.Default.Settings,
                title = "Settings",
                textColor = textColor,
                onClick = {
                    onOpenSettings()
                    onDismiss()
                }
            )

            // Tutorial & Guide
            DrawerActionRow(
                icon = Icons.Default.School,
                title = "Paper Tape Tutorial & Guide",
                textColor = textColor,
                onClick = {
                    onOpenTutorial()
                    onDismiss()
                }
            )

            // Info
            DrawerActionRow(
                icon = Icons.Default.Info,
                title = "Info",
                textColor = textColor,
                onClick = {
                    onOpenInfo()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun DrawerActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(20.dp))
        Text(text = title, color = textColor, fontSize = 14.sp)
    }
}

/**
 * "Create new document" dialog matching frame 01:06 of the video
 */
@Composable
fun CreateNewDocDialog(
    suggestedTitle: String,
    theme: ThemePalette,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var titleInput by remember { mutableStateOf(suggestedTitle) }
    val isLightCanvas = theme.backgroundColor.luminance() > 0.45f
    val textColor = if (isLightCanvas) Color(0xFF0F172A) else Color(0xFFF8FAFC)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Create new document", color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            OutlinedTextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                label = { Text("Document name") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = theme.accentColor
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(titleInput.ifBlank { "Calculation" }) },
                colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)
            ) {
                Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("Cancel", color = textColor)
            }
        }
    )
}

/**
 * "Create backup" dialog matching frame 03:14 of the video
 */
@Composable
fun CreateBackupDialog(
    documents: List<WorksheetDocument>,
    theme: ThemePalette,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current

    var cbWorksheet by remember { mutableStateOf(true) }
    var cbCalculations by remember { mutableStateOf(true) }
    var cbKeyboards by remember { mutableStateOf(true) }
    var cbSettings by remember { mutableStateOf(true) }

    val isLightCanvas = theme.backgroundColor.luminance() > 0.45f
    val textColor = if (isLightCanvas) Color(0xFF0F172A) else Color(0xFFF8FAFC)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Create backup", color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BackupCheckboxRow("Worksheet", cbWorksheet, theme.accentColor, textColor) { cbWorksheet = it }
                BackupCheckboxRow("All calculations", cbCalculations, theme.accentColor, textColor) { cbCalculations = it }
                BackupCheckboxRow("Keyboard layouts and buttons", cbKeyboards, theme.accentColor, textColor) { cbKeyboards = it }
                BackupCheckboxRow("Settings", cbSettings, theme.accentColor, textColor) { cbSettings = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val backupJson = WorksheetTapeEngine.createBackupJson(
                        documents = documents,
                        includeWorksheet = cbWorksheet,
                        includeAllCalculations = cbCalculations,
                        includeKeyboards = cbKeyboards,
                        includeSettings = cbSettings
                    )
                    clipboardManager.setText(AnnotatedString(backupJson))
                    haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)
            ) {
                Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("Cancel", color = textColor)
            }
        }
    )
}

@Composable
private fun BackupCheckboxRow(
    label: String,
    checked: Boolean,
    accentColor: Color,
    textColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = accentColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = textColor, fontSize = 14.sp)
    }
}

/**
 * Restore Backup Dialog
 */
@Composable
fun RestoreBackupDialog(
    theme: ThemePalette,
    onRestore: (List<WorksheetDocument>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var jsonInput by remember { mutableStateOf("") }
    val isLightCanvas = theme.backgroundColor.luminance() > 0.45f
    val textColor = if (isLightCanvas) Color(0xFF0F172A) else Color(0xFFF8FAFC)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Restore backup", color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Paste the backup JSON below to restore calculations and settings:",
                    color = textColor,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = jsonInput,
                    onValueChange = { jsonInput = it },
                    label = { Text("Backup JSON") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = theme.accentColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val restored = WorksheetTapeEngine.restoreBackupJson(jsonInput)
                    if (restored.isNotEmpty()) {
                        onRestore(restored)
                        Toast.makeText(context, "Restored ${restored.size} document(s) successfully!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Invalid backup format", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)
            ) {
                Text("Restore", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("Cancel", color = textColor)
            }
        }
    )
}

/**
 * Comprehensive Settings Screen matching frames 02:15 - 02:20 & 03:22 - 03:33 of the video.
 */
@Composable
fun WorksheetSettingsSheet(
    settings: WorksheetSettings,
    theme: ThemePalette,
    onUpdate: (WorksheetSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val isLightCanvas = theme.backgroundColor.luminance() > 0.45f
    val bgColor = if (isLightCanvas) Color(0xFFF8FAFC) else Color(0xFF141922)
    val cardBg = if (isLightCanvas) Color(0xFFFFFFFF) else Color(0xFF1E2530)
    val textColor = if (isLightCanvas) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val subtextColor = if (isLightCanvas) Color(0xFF64748B) else Color(0xFF94A3B8)
    val accentColor = theme.accentColor

    var currentSettings by remember { mutableStateOf(settings) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .background(bgColor)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Settings", color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = textColor)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section: Calculation
            item {
                SettingsSectionHeader("Calculation", accentColor)
            }

            item {
                Surface(shape = RoundedCornerShape(10.dp), color = cardBg) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Decimals
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Decimals", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(text = "${currentSettings.decimals} decimal places", color = subtextColor, fontSize = 11.sp)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(0, 2, 3, 4).forEach { dec ->
                                    val isSel = currentSettings.decimals == dec
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSel) accentColor else Color.Gray.copy(alpha = 0.2f),
                                        modifier = Modifier.clickable {
                                            currentSettings = currentSettings.copy(decimals = dec)
                                            onUpdate(currentSettings)
                                        }
                                    ) {
                                        Text(
                                            text = "$dec",
                                            color = if (isSel) Color.White else textColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

                        // Thousands Separator
                        SettingsToggleRow(
                            title = "Thousands separator",
                            subtitle = "Insert commas between groups of thousands",
                            checked = currentSettings.thousandsSeparator,
                            textColor = textColor,
                            subtextColor = subtextColor,
                            accentColor = accentColor,
                            onCheckedChange = {
                                currentSettings = currentSettings.copy(thousandsSeparator = it)
                                onUpdate(currentSettings)
                            }
                        )

                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

                        // Indian Digit Grouping (Critical feature from video!)
                        SettingsToggleRow(
                            title = "Indian digit grouping",
                            subtitle = "Example: 50,00,000.00",
                            checked = currentSettings.indianDigitGrouping,
                            textColor = textColor,
                            subtextColor = subtextColor,
                            accentColor = accentColor,
                            onCheckedChange = {
                                currentSettings = currentSettings.copy(indianDigitGrouping = it)
                                onUpdate(currentSettings)
                            }
                        )
                    }
                }
            }

            // Section: Appearance
            item {
                SettingsSectionHeader("Appearance", accentColor)
            }

            item {
                Surface(shape = RoundedCornerShape(10.dp), color = cardBg) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Show lines (ruled paper lines - critical from video frame 03:34!)
                        SettingsToggleRow(
                            title = "Show lines",
                            subtitle = "Horizontal ruled paper lines between rows",
                            checked = currentSettings.showLines,
                            textColor = textColor,
                            subtextColor = subtextColor,
                            accentColor = accentColor,
                            onCheckedChange = {
                                currentSettings = currentSettings.copy(showLines = it)
                                onUpdate(currentSettings)
                            }
                        )

                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

                        // Line color
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Line color", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(text = currentSettings.lineColorName, color = subtextColor, fontSize = 11.sp)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Light blue", "Slate", "Gray").forEach { colName ->
                                    val isSel = currentSettings.lineColorName == colName
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSel) accentColor else Color.Gray.copy(alpha = 0.2f),
                                        modifier = Modifier.clickable {
                                            currentSettings = currentSettings.copy(lineColorName = colName)
                                            onUpdate(currentSettings)
                                        }
                                    ) {
                                        Text(
                                            text = colName,
                                            color = if (isSel) Color.White else textColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

                        // Font size
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Font size", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(text = "${currentSettings.fontSize} sp", color = accentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = currentSettings.fontSize.toFloat(),
                                onValueChange = {
                                    currentSettings = currentSettings.copy(fontSize = it.toInt())
                                    onUpdate(currentSettings)
                                },
                                valueRange = 12f..24f,
                                steps = 11,
                                colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor)
                            )
                        }

                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

                        // Show quick insert button
                        SettingsToggleRow(
                            title = "Show quick insert button",
                            subtitle = "Display button for variables & constants in the editor",
                            checked = currentSettings.showQuickInsert,
                            textColor = textColor,
                            subtextColor = subtextColor,
                            accentColor = accentColor,
                            onCheckedChange = {
                                currentSettings = currentSettings.copy(showQuickInsert = it)
                                onUpdate(currentSettings)
                            }
                        )
                    }
                }
            }

            // Section: Keyboard & Behaviour
            item {
                SettingsSectionHeader("Keyboard & Behaviour", accentColor)
            }

            item {
                Surface(shape = RoundedCornerShape(10.dp), color = cardBg) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Modern key style
                        SettingsToggleRow(
                            title = "Modern key style",
                            subtitle = "Rounded corners and spacing between keys",
                            checked = currentSettings.modernKeyStyle,
                            textColor = textColor,
                            subtextColor = subtextColor,
                            accentColor = accentColor,
                            onCheckedChange = {
                                currentSettings = currentSettings.copy(modernKeyStyle = it)
                                onUpdate(currentSettings)
                            }
                        )

                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

                        // Vibration
                        SettingsToggleRow(
                            title = "Vibration",
                            subtitle = "Haptic feedback for the numeric keypad",
                            checked = currentSettings.hapticsEnabled,
                            textColor = textColor,
                            subtextColor = subtextColor,
                            accentColor = accentColor,
                            onCheckedChange = {
                                currentSettings = currentSettings.copy(hapticsEnabled = it)
                                onUpdate(currentSettings)
                            }
                        )

                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

                        // Keypress sound
                        SettingsToggleRow(
                            title = "Keypress sound",
                            subtitle = "Click sound for the numeric keypad",
                            checked = currentSettings.soundEnabled,
                            textColor = textColor,
                            subtextColor = subtextColor,
                            accentColor = accentColor,
                            onCheckedChange = {
                                currentSettings = currentSettings.copy(soundEnabled = it)
                                onUpdate(currentSettings)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String, accentColor: Color) {
    Text(
        text = title.uppercase(),
        color = accentColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    textColor: Color,
    subtextColor: Color,
    accentColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(text = title, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = subtextColor, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor)
        )
    }
}

/**
 * Info Screen matching frame 02:22 of the video
 */
@Composable
fun WorksheetInfoDialog(
    theme: ThemePalette,
    onDismiss: () -> Unit
) {
    val isLightCanvas = theme.backgroundColor.luminance() > 0.45f
    val textColor = if (isLightCanvas) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val subtextColor = if (isLightCanvas) Color(0xFF64748B) else Color(0xFF94A3B8)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "CalcTape Pro", color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Calculation core: 7.0.2", color = theme.accentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "Copyright © 2026. All rights reserved.", color = subtextColor, fontSize = 12.sp)
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                Text(text = "• Support & Feedback: support@calctape.app", color = textColor, fontSize = 12.sp)
                Text(text = "• User manual & quick reference included", color = textColor, fontSize = 12.sp)
                Text(text = "• Open source licenses and privacy policy compliant", color = textColor, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)
            ) {
                Text("Close", color = Color.White)
            }
        }
    )
}

/**
 * Upgrade to Pro Dialog matching frame 02:55 of the video
 */
@Composable
fun UpgradeToProDialog(
    theme: ThemePalette,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isLightCanvas = theme.backgroundColor.luminance() > 0.45f
    val textColor = if (isLightCanvas) Color(0xFF0F172A) else Color(0xFFF8FAFC)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = theme.accentColor)
                Text(text = "Upgrade to Pro", color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Unlock all features:", color = textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "• Manage multiple documents (create, copy, manage)", color = textColor, fontSize = 12.sp)
                Text(text = "• Share calculations via WhatsApp / Email", color = textColor, fontSize = 12.sp)
                Text(text = "• Unlimited variables and constants", color = textColor, fontSize = 12.sp)
                Text(text = "• Programmable custom buttons", color = textColor, fontSize = 12.sp)
                Text(text = "• Import and export documents", color = textColor, fontSize = 12.sp)
                Text(text = "• Full backup and restore", color = textColor, fontSize = 12.sp)
                Text(text = "• Native print & PDF documents", color = textColor, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "One-time purchase – no subscription – for only ₹250.00",
                    color = theme.accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Toast.makeText(context, "Pro features are fully unlocked in this app!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)
            ) {
                Text("Upgrade (₹250.00)", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("Maybe later", color = textColor)
            }
        }
    )
}

/**
 * Variables & Quick Insert Sheet matching frame 01:49 of the video
 */
@Composable
fun VariablesSheetContent(
    existingVariables: List<WorksheetVariable>,
    theme: ThemePalette,
    onInsertVariable: (WorksheetVariable) -> Unit,
    onAddNewVariable: (String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    val isLightCanvas = theme.backgroundColor.luminance() > 0.45f
    val textColor = if (isLightCanvas) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val subtextColor = if (isLightCanvas) Color(0xFF64748B) else Color(0xFF94A3B8)
    val cardBg = if (isLightCanvas) Color(0xFFFFFFFF) else Color(0xFF1E2530)

    var showNewVarDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Variables & Quick Insert", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = textColor)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Info card
        Surface(shape = RoundedCornerShape(8.dp), color = cardBg) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Working with variables:", color = theme.accentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    text = "You can define variables like Quantity = 25 or Price = 500.00. Tap any variable to insert its value directly into the calculation line.",
                    color = subtextColor,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Add variable button
        Button(
            onClick = { showNewVarDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(42.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("+ New variable", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Variables list
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(existingVariables.size) { idx ->
                val v = existingVariables[idx]
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = cardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onInsertVariable(v)
                            onDismiss()
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${v.name} = ${WorksheetTapeEngine.formatNumber(v.value)}",
                            color = textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = theme.accentColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Insert",
                                color = theme.accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNewVarDialog) {
        var nameInput by remember { mutableStateOf("") }
        var valInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewVarDialog = false },
            title = { Text("Add New Variable", color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Variable name (e.g. Tax, Discount)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = valInput,
                        onValueChange = { valInput = it },
                        label = { Text("Value (e.g. 18, 500)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = valInput.toDoubleOrNull() ?: 0.0
                        if (nameInput.isNotBlank()) {
                            onAddNewVariable(nameInput.trim(), num)
                            showNewVarDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)
                ) {
                    Text("Add", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showNewVarDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("Cancel", color = textColor)
                }
            }
        )
    }
}

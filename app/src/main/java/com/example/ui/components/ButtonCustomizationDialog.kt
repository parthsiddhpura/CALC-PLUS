package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ThemePalette

data class UserCustomKeyOption(
    val id: String,
    val title: String,
    val displayLabel: String,
    val actionType: String,
    val rate: String,
    val isSystemPreset: Boolean = true
)

@Composable
fun ButtonCustomizationDialog(
    keyName: String,
    currentCustomType: String,
    currentCustomRate: String,
    theme: ThemePalette,
    onSave: (displayLabel: String, actionType: String, rate: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(1) } // Default to USER tab as seen in video frame 00:10
    var selectedUserKeyId by remember {
        mutableStateOf(
            if (currentCustomType.contains("GST") || currentCustomType.contains("Reduced")) currentCustomType
            else "CUSTOMISE_BUTTON"
        )
    }
    var selectedSystemKey by remember { mutableStateOf<String?>(null) }
    var selectedLayoutType by remember { mutableStateOf("Single") }

    // User Tab Presets matching video frame 00:10
    var userKeysList by remember {
        mutableStateOf(
            listOf(
                UserCustomKeyOption("GST_PLUS_18", "+GST (18%)", "+GST\n(18%)", "+GST", "18"),
                UserCustomKeyOption("RED_PLUS_12", "+Reduced (12%)", "+Red\n(12%)", "+Reduced", "12"),
                UserCustomKeyOption("GST_MINUS_18", "-GST (18%)", "-GST\n(18%)", "-GST", "18"),
                UserCustomKeyOption("RED_MINUS_12", "-Reduced (12%)", "-Red\n(12%)", "-Reduced", "12"),
                UserCustomKeyOption("CUSTOMISE_BUTTON", "Customise Button", "Customise\nButton", "Customise Button", "18")
            )
        )
    }

    // New/Edit dialog state
    var showNewKeyDialog by remember { mutableStateOf(false) }
    var newKeyName by remember { mutableStateOf("") }
    var newKeyRate by remember { mutableStateOf("18") }
    var newKeyIsTax by remember { mutableStateOf(true) }

    // System keys grid matching video frames 00:12 - 00:15
    val systemKeys = remember {
        listOf(
            "1", "2", "3",
            "4", "5", "6",
            "7", "8", "9",
            "0", ",", "+",
            "-", "x", "÷",
            "x²", "x³", "xⁿ",
            "√", "1/x", "%",
            "+/-", "M+", "M-",
            "MR", "MC", "=/↵",
            "⌫", "AC", "↶",
            "↷", "⌘", "📋",
            "←", "↑", "↓",
            "⇥", "⌨"
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF161A22))
        ) {
            // --- TOP APP BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF1E242F))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Button [$keyName]",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                TextButton(
                    onClick = {
                        if (selectedTabIndex == 0 && selectedSystemKey != null) {
                            val sysKey = selectedSystemKey!!
                            onSave(sysKey, "SYSTEM_$sysKey", "0")
                        } else {
                            val selected = userKeysList.find { it.id == selectedUserKeyId }
                                ?: userKeysList.last()
                            onSave(selected.displayLabel, selected.actionType, selected.rate)
                        }
                    }
                ) {
                    Text(
                        text = "Save",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            // --- TABS (SYSTEM | USER | LAYOUT) ---
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFF1E242F),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFF38BDF8),
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = "SYSTEM",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == 0) Color.White else Color(0xFF94A3B8)
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = "USER",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == 1) Color.White else Color(0xFF94A3B8)
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = {
                        Text(
                            text = "LAYOUT",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == 2) Color.White else Color(0xFF94A3B8)
                        )
                    }
                )
            }

            // --- TAB CONTENT BODY ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTabIndex) {
                    0 -> {
                        // SYSTEM TAB: 3-column keypad matrix
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(systemKeys) { sysKey ->
                                val isSelected = selectedSystemKey == sysKey
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Color(0xFF38BDF8).copy(alpha = 0.35f) else Color(0xFF293240),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF38BDF8) else Color(0xFF3D4B60)
                                    ),
                                    modifier = Modifier
                                        .height(52.dp)
                                        .clickable { selectedSystemKey = sysKey }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = sysKey,
                                            color = Color.White,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // USER TAB: User custom presets grid + bottom actions
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(userKeysList) { item ->
                                    val isSelected = selectedUserKeyId == item.id
                                    Box(
                                        modifier = Modifier
                                            .height(60.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) Color(0xFF38BDF8).copy(alpha = 0.3f)
                                                else Color(0xFF2B3444)
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) Color(0xFF38BDF8) else Color(0xFF3D4B60),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                selectedUserKeyId = item.id
                                                selectedSystemKey = null
                                            }
                                            .padding(6.dp)
                                    ) {
                                        // Orange SYS / USER badge
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFD97706),
                                            modifier = Modifier.align(Alignment.TopStart)
                                        ) {
                                            Text(
                                                text = if (item.isSystemPreset) "SYS" else "USER",
                                                color = Color.White,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }

                                        Text(
                                            text = item.title,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            }

                            // Bottom Action Bar: New | Edit | Delete matching video frame 00:10
                            Column(modifier = Modifier.fillMaxWidth()) {
                                HorizontalDivider(color = Color(0xFF293240), thickness = 1.dp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { showNewKeyDialog = true }) {
                                        Text("✧ New", color = Color(0xFFE2E8F0), fontSize = 14.sp)
                                    }
                                    TextButton(onClick = {
                                        val cur = userKeysList.find { it.id == selectedUserKeyId }
                                        if (cur != null) {
                                            newKeyName = cur.title
                                            newKeyRate = cur.rate
                                            showNewKeyDialog = true
                                        }
                                    }) {
                                        Text("Edit", color = Color(0xFFE2E8F0), fontSize = 14.sp)
                                    }
                                    TextButton(onClick = {
                                        selectedUserKeyId = "CUSTOMISE_BUTTON"
                                    }) {
                                        Text("Reset", color = Color(0xFFE2E8F0), fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // LAYOUT TAB: Size variants matching video frame 00:19 - 00:20
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            val activeLabel = userKeysList.find { it.id == selectedUserKeyId }?.title ?: "Customise Button"

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Single", color = Color(0xFF94A3B8), fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selectedLayoutType == "Single") Color(0xFF4A657D) else Color(0xFF293240),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedLayoutType == "Single") Color(0xFF759AB4) else Color.Transparent),
                                        modifier = Modifier
                                            .size(72.dp, 50.dp)
                                            .clickable { selectedLayoutType = "Single" }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(activeLabel, color = Color.White, fontSize = 10.sp, textAlign = TextAlign.Center, maxLines = 2)
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Double width", color = Color(0xFF94A3B8), fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selectedLayoutType == "Double width") Color(0xFF4A657D) else Color(0xFF293240),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedLayoutType == "Double width") Color(0xFF759AB4) else Color.Transparent),
                                        modifier = Modifier
                                            .size(120.dp, 50.dp)
                                            .clickable { selectedLayoutType = "Double width" }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(activeLabel, color = Color.White, fontSize = 10.sp, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Double height", color = Color(0xFF94A3B8), fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selectedLayoutType == "Double height") Color(0xFF4A657D) else Color(0xFF293240),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedLayoutType == "Double height") Color(0xFF759AB4) else Color.Transparent),
                                        modifier = Modifier
                                            .size(72.dp, 90.dp)
                                            .clickable { selectedLayoutType = "Double height" }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(activeLabel, color = Color.White, fontSize = 10.sp, textAlign = TextAlign.Center)
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Double width & height", color = Color(0xFF94A3B8), fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selectedLayoutType == "Double width & height") Color(0xFF4A657D) else Color(0xFF293240),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedLayoutType == "Double width & height") Color(0xFF759AB4) else Color.Transparent),
                                        modifier = Modifier
                                            .size(120.dp, 90.dp)
                                            .clickable { selectedLayoutType = "Double width & height" }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(activeLabel, color = Color.White, fontSize = 10.sp, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New/Edit Key Customizer Sub-Dialog
    if (showNewKeyDialog) {
        AlertDialog(
            onDismissRequest = { showNewKeyDialog = false },
            title = { Text("Custom User Key", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newKeyName,
                        onValueChange = { newKeyName = it },
                        label = { Text("Key Label (e.g. +VAT)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newKeyRate,
                        onValueChange = { newKeyRate = it },
                        label = { Text("Rate % (e.g. 5, 12, 18)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { newKeyIsTax = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (newKeyIsTax) theme.accentColor else Color(0xFF334155)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ Addition")
                        }
                        Button(
                            onClick = { newKeyIsTax = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!newKeyIsTax) theme.accentColor else Color(0xFF334155)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("− Deduction")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sign = if (newKeyIsTax) "+" else "-"
                        val rate = newKeyRate.ifBlank { "18" }
                        val title = newKeyName.ifBlank { "$sign GST ($rate%)" }
                        val id = "CUSTOM_${System.currentTimeMillis()}"
                        val newOpt = UserCustomKeyOption(
                            id = id,
                            title = title,
                            displayLabel = "$sign$rate%\n${newKeyName.take(6)}",
                            actionType = if (newKeyIsTax) "+GST" else "-GST",
                            rate = rate,
                            isSystemPreset = false
                        )
                        userKeysList = userKeysList + newOpt
                        selectedUserKeyId = id
                        showNewKeyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)
                ) {
                    Text("Save Key", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewKeyDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E242F)
        )
    }
}

package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CalculationHistory
import com.example.model.ThemePalette
import com.example.model.onCardColor
import com.example.model.onCardSubtextColor
import com.example.model.onSurfaceSubtextColor
import com.example.model.onSurfaceTextColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
    historyList: List<CalculationHistory>,
    theme: ThemePalette,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onlyFavorites: Boolean,
    onToggleOnlyFavorites: (Boolean) -> Unit,
    onUseItem: (CalculationHistory) -> Unit,
    onToggleFavorite: (CalculationHistory) -> Unit,
    onEditNote: (CalculationHistory) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showClearConfirmation by remember { mutableStateOf(false) }

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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Title & Actions Row
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
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = theme.accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Calculation History",
                        color = theme.onSurfaceTextColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (historyList.isNotEmpty()) {
                        IconButton(onClick = {
                            com.example.domain.CalculationHistoryPrintAdapter.printHistory(context, historyList)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = "Export History to PDF or Print",
                                tint = theme.accentColor
                            )
                        }
                        IconButton(onClick = { showClearConfirmation = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear all history",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close history",
                            tint = theme.onSurfaceSubtextColor
                        )
                    }
                }
            }

            // Search Bar & Favorites Filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search calculations or notes...", color = theme.onCardSubtextColor.copy(alpha = 0.7f), fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = theme.accentColor) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.onCardColor,
                        unfocusedTextColor = theme.onCardColor,
                        focusedBorderColor = theme.accentColor,
                        unfocusedBorderColor = if (theme.surfaceColor.luminance() > 0.45f) theme.accentColor.copy(alpha = 0.35f) else theme.cardBackground,
                        focusedContainerColor = theme.cardBackground,
                        unfocusedContainerColor = theme.cardBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = if (onlyFavorites) theme.accentColor else theme.cardBackground,
                    shape = RoundedCornerShape(12.dp),
                    border = if (!onlyFavorites && theme.surfaceColor.luminance() > 0.45f) {
                        androidx.compose.foundation.BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.35f))
                    } else null,
                    modifier = Modifier
                        .clickable { onToggleOnlyFavorites(!onlyFavorites) }
                        .height(52.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (onlyFavorites) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Filter favorites",
                            tint = if (onlyFavorites) {
                                if (theme.accentColor.luminance() > 0.6f) Color(0xFF211118) else Color.White
                            } else theme.accentColor
                        )
                    }
                }
            }

            // History List
            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (searchQuery.isNotEmpty() || onlyFavorites) "No matching calculations found" else "No history yet",
                            color = theme.onSurfaceTextColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Calculate something to see your calculation tape here",
                            color = theme.onSurfaceSubtextColor,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, start = 24.dp, end = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.75f)
                ) {
                    items(historyList, key = { it.id }) { item ->
                        HistoryCard(
                            item = item,
                            theme = theme,
                            onUse = { onUseItem(item) },
                            onToggleFavorite = { onToggleFavorite(item) },
                            onEditNote = { onEditNote(item) },
                            onDelete = { onDeleteItem(item.id) },
                            onCopy = {
                                val clip = ClipData.newPlainText("Calculation", "${item.expression} = ${item.result}")
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied result: ${item.result}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // Confirmation dialog for clearing all history
    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Clear All History?", color = theme.onSurfaceTextColor, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove all your past calculations and notes.", color = theme.onSurfaceSubtextColor) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showClearConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Clear All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel", color = theme.accentColor)
                }
            },
            containerColor = theme.surfaceColor
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryCard(
    item: CalculationHistory,
    theme: ThemePalette,
    onUse: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEditNote: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(14.dp)
    val timeFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
    val formattedTime = remember(item.timestamp) { timeFormat.format(Date(item.timestamp)) }

    Surface(
        color = theme.cardBackground,
        shape = cardShape,
        border = if (theme.surfaceColor.luminance() > 0.45f) {
            androidx.compose.foundation.BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.3f))
        } else null,
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onUse,
                onLongClick = onCopy
            )
            .testTag("history_item_${item.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Mode tag, timestamp & Favorite button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = theme.accentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.mode,
                            color = theme.accentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = formattedTime,
                        color = theme.onCardSubtextColor.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEditNote,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Edit note",
                            tint = if (item.note.isNotEmpty()) theme.secondaryAccent else theme.onCardSubtextColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorite calculation",
                            tint = if (item.isFavorite) Color(0xFFFFB703) else theme.onCardSubtextColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Note tag if attached
            if (item.note.isNotBlank()) {
                Surface(
                    color = theme.secondaryAccent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "📝 ${item.note}",
                        color = theme.secondaryAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Expression
            Text(
                text = item.expression,
                color = theme.onCardSubtextColor,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Result
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "= ${item.result}",
                    color = theme.onCardColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = "Tap to reuse",
                    color = theme.accentColor.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EditNoteDialog(
    history: CalculationHistory,
    theme: ThemePalette,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var noteText by remember { mutableStateOf(history.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Note to Calculation", color = theme.screenTextColor, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "${history.expression} = ${history.result}",
                    color = theme.screenExpressionColor,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("e.g. Flight ticket, Groceries, Physics lab", color = theme.screenExpressionColor.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.screenTextColor,
                        unfocusedTextColor = theme.screenTextColor,
                        focusedBorderColor = theme.accentColor,
                        unfocusedBorderColor = theme.cardBackground
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(noteText) },
                colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)
            ) {
                Text("Save", color = theme.backgroundColor, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = theme.screenExpressionColor)
            }
        },
        containerColor = theme.surfaceColor
    )
}

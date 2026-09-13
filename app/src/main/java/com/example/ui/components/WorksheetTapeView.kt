package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import android.content.res.Configuration
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.WorksheetPhotoExporter
import com.example.domain.WorksheetTapeEngine
import com.example.model.ThemePalette
import com.example.model.WorksheetDocument
import com.example.model.WorksheetLine
import com.example.model.WorksheetLineType
import com.example.model.WorksheetSettings
import com.example.model.WorksheetTemplate
import com.example.model.WorksheetVariable
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorksheetTapeView(
    theme: ThemePalette,
    documents: List<WorksheetDocument>,
    activeDocument: WorksheetDocument,
    settings: WorksheetSettings = WorksheetSettings(),
    onUpdateSettings: (WorksheetSettings) -> Unit = {},
    onSaveDocument: (WorksheetDocument) -> Unit,
    onSelectDocument: (WorksheetDocument) -> Unit,
    onDeleteDocument: (String) -> Unit,
    onNewDocument: (String) -> Unit,
    onDuplicateDocument: (WorksheetDocument) -> Unit = {},
    onRestoreDocuments: (List<WorksheetDocument>) -> Unit = {},
    onApplyTemplate: (WorksheetTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Undo / Redo history
    var undoStack by remember(activeDocument.id) { mutableStateOf(listOf<WorksheetDocument>()) }
    var redoStack by remember(activeDocument.id) { mutableStateOf(listOf<WorksheetDocument>()) }

    // Selection & Tape Weight
    val keyboardController = LocalSoftwareKeyboardController.current
    var inputMode by remember { mutableStateOf(KeypadInputMode.NUMERIC_K1) }
    var selectedLineIndex by remember { mutableIntStateOf(-1) }
    var isKeyboardVisible by remember { mutableStateOf(true) }
    var tapeWeight by remember { mutableFloatStateOf(0.38f) }

    // Memory accumulator
    var memoryValue by remember { mutableStateOf(0.0) }

    // Bottom sheets & Dialog state
    var showDrawer by remember { mutableStateOf(false) }
    var showCreateDocDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var documentBeingRenamed by remember { mutableStateOf<WorksheetDocument?>(null) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showUpgradeDialog by remember { mutableStateOf(false) }
    var showTemplatesSheet by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var showVariablesSheet by remember { mutableStateOf(false) }
    var showCreateBackupDialog by remember { mutableStateOf(false) }
    var showRestoreBackupDialog by remember { mutableStateOf(false) }
    var showCustomKeyDialog by remember { mutableStateOf(false) }
    var isEditKeyboardMode by remember { mutableStateOf(false) }
    var keyNameToCustomize by remember { mutableStateOf<String?>(null) }
    var showHeaderEditDialog by remember { mutableStateOf(false) }
    var overflowMenuExpanded by remember { mutableStateOf(false) }

    // Color canvas setup (Dark Slate look from video)
    val isLightCanvas = theme.backgroundColor.luminance() > 0.45f
    val paperBg = if (isLightCanvas) Color(0xFFFAFAFA) else Color(0xFF191D24)
    val textColor = if (isLightCanvas) Color(0xFF0F172A) else Color(0xFFF1F5F9)
    val subtextColor = if (isLightCanvas) Color(0xFF64748B) else Color(0xFF94A3B8)
    val deductionColor = Color(0xFFE57373) // Authentic salmon red deduction color from video
    val ruledLineColor = when (settings.lineColorName) {
        "Slate" -> if (isLightCanvas) Color(0xFFCBD5E1) else Color(0xFF263242)
        "Gray" -> Color.Gray.copy(alpha = 0.25f)
        else -> if (isLightCanvas) Color(0xFFBFDBFE) else Color(0xFF1E3A5F) // Light blue default
    }
    val accentColor = theme.accentColor

    fun pushHistory(doc: WorksheetDocument) {
        undoStack = undoStack + activeDocument
        redoStack = emptyList()
        onSaveDocument(doc)
    }

    fun applyUndo() {
        if (undoStack.isNotEmpty()) {
            val prev = undoStack.last()
            undoStack = undoStack.dropLast(1)
            redoStack = redoStack + activeDocument
            onSaveDocument(prev)
        }
    }

    fun applyRedo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.last()
            redoStack = redoStack.dropLast(1)
            undoStack = undoStack + activeDocument
            onSaveDocument(next)
        }
    }

    // Scroll to end when lines are added
    LaunchedEffect(activeDocument.lines.size) {
        if (activeDocument.lines.isNotEmpty()) {
            listState.animateScrollToItem(activeDocument.lines.size - 1)
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val tapePaneContent = @Composable { canvasModifier: Modifier ->
        // --- TOP APP BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(if (isLightCanvas) Color(0xFFF1F5F9) else Color(0xFF151921))
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Hamburger Menu Button
            IconButton(
                onClick = { showDrawer = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Documents drawer",
                    tint = textColor
                )
            }

            // Title (clickable to rename)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showRenameDialog = true }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activeDocument.title,
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            // Right Pill: [ ⏱ EVAL ]
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E2824),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50)),
                modifier = Modifier
                    .clickable {
                        Toast.makeText(context, "Calculation dynamically evaluated & valid", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "[ ⏱ EVAL ]",
                        color = Color(0xFF4ADE80),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 3-Dots Overflow Menu
            Box {
                IconButton(
                    onClick = { overflowMenuExpanded = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", tint = textColor)
                }

                DropdownMenu(
                    expanded = overflowMenuExpanded,
                    onDismissRequest = { overflowMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Share as Photo (PNG)") },
                        leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF38BDF8)) },
                        onClick = {
                            overflowMenuExpanded = false
                            WorksheetPhotoExporter.shareAsPhoto(context, activeDocument, settings)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Save Photo to Gallery") },
                        leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF4CAF50)) },
                        onClick = {
                            overflowMenuExpanded = false
                            WorksheetPhotoExporter.savePhotoToGallery(context, activeDocument, settings)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename worksheet") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = accentColor) },
                        onClick = {
                            overflowMenuExpanded = false
                            documentBeingRenamed = activeDocument
                            showRenameDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("New worksheet") },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = accentColor) },
                        onClick = {
                            overflowMenuExpanded = false
                            showCreateDocDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        onClick = {
                            overflowMenuExpanded = false
                            showShareSheet = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Print") },
                        leadingIcon = { Icon(Icons.Default.Print, contentDescription = null, tint = accentColor) },
                        onClick = {
                            overflowMenuExpanded = false
                            WorksheetTapeEngine.printDocument(context, activeDocument, settings.indianDigitGrouping)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Copy calculation") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                        onClick = {
                            overflowMenuExpanded = false
                            val text = WorksheetTapeEngine.exportToPlainText(activeDocument)
                            clipboardManager.setText(AnnotatedString(text))
                            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            Toast.makeText(context, "Calculation copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Copy calculation (HTML)") },
                        leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) },
                        onClick = {
                            overflowMenuExpanded = false
                            val html = WorksheetTapeEngine.exportToHtml(activeDocument, settings.indianDigitGrouping)
                            clipboardManager.setText(AnnotatedString(html))
                            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            Toast.makeText(context, "HTML calculation copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Copy grand total") },
                        leadingIcon = { Icon(Icons.Default.Functions, contentDescription = null) },
                        onClick = {
                            overflowMenuExpanded = false
                            val totalStr = WorksheetTapeEngine.formatNumber(activeDocument.grandTotal, settings.indianDigitGrouping, settings.decimals)
                            clipboardManager.setText(AnnotatedString(totalStr))
                            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            Toast.makeText(context, "Copied total: $totalStr", Toast.LENGTH_SHORT).show()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit keyboard") },
                        leadingIcon = { Icon(Icons.Default.Keyboard, contentDescription = null) },
                        onClick = {
                            overflowMenuExpanded = false
                            isEditKeyboardMode = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("What's New") },
                        leadingIcon = { Icon(Icons.Default.NewReleases, contentDescription = null) },
                        onClick = {
                            overflowMenuExpanded = false
                            Toast.makeText(context, "CalcTape Pro: Ruled notebook lines, Indian digit grouping & Native PDF Print!", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
        }

        // --- SCROLLABLE PAPER TAPE CANVAS ---
        Box(
            modifier = canvasModifier
                .background(paperBg)
        ) {
            // Optional horizontal ruled notebook lines (matching frame 03:34)
            if (settings.showLines) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 32.dp.toPx()
                    var curY = step
                    while (curY < size.height) {
                        drawLine(
                            color = ruledLineColor,
                            start = Offset(0f, curY),
                            end = Offset(size.width, curY),
                            strokeWidth = 1.dp.toPx()
                        )
                        curY += step
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // Top Header note (e.g. "Total - 70,000/-" in video)
                item {
                    val headerText = activeDocument.headerNote.ifBlank { "Total - 70,000/-" }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showHeaderEditDialog = true }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = headerText,
                            color = textColor,
                            fontSize = (settings.fontSize + 1).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Tape calculation rows
                itemsIndexed(activeDocument.lines) { index, line ->
                    val isSelected = index == selectedLineIndex

                    TapeLineRow(
                        line = line,
                        index = index,
                        isSelected = isSelected,
                        isTextMode = inputMode == KeypadInputMode.TEXT_ABC,
                        settings = settings,
                        textColor = textColor,
                        subtextColor = subtextColor,
                        deductionColor = deductionColor,
                        accentColor = accentColor,
                        onSelect = {
                            selectedLineIndex = index
                            if (line.lineType == WorksheetLineType.COMMENT_HEADER) {
                                inputMode = KeypadInputMode.TEXT_ABC
                                keyboardController?.show()
                            }
                        },
                        onOpenKeyboard = {
                            selectedLineIndex = index
                            inputMode = KeypadInputMode.TEXT_ABC
                            keyboardController?.show()
                        },
                        onUpdateNote = { newNote ->
                            val lines = activeDocument.lines.toMutableList()
                            if (index in lines.indices) {
                                lines[index] = lines[index].copy(note = newNote)
                                pushHistory(activeDocument.copy(lines = lines))
                            }
                        },
                        onDelete = {
                            val updatedLines = activeDocument.lines.filterIndexed { idx, _ -> idx != index }
                            val recalculated = WorksheetTapeEngine.recalculate(updatedLines)
                            val grandTotal = recalculated.lastOrNull { it.lineType != WorksheetLineType.COMMENT_HEADER }?.runningTotal ?: 0.0
                            pushHistory(activeDocument.copy(lines = recalculated, grandTotal = grandTotal))
                            selectedLineIndex = -1
                        }
                    )
                }

                // Empty row space at bottom to allow scrolling past
                item {
                    Spacer(modifier = Modifier.height(56.dp))
                }
            }

            // Floating Keyboard Toggle Button (Bottom Right, portrait only)
            if (!isLandscape) {
                Surface(
                    shape = CircleShape,
                    color = if (isLightCanvas) Color(0xFFE2E8F0) else Color(0xFF242C38),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .clickable {
                            if (inputMode == KeypadInputMode.TEXT_ABC) {
                                inputMode = KeypadInputMode.NUMERIC_K1
                                keyboardController?.hide()
                            } else {
                                isKeyboardVisible = !isKeyboardVisible
                            }
                        }
                ) {
                    Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isKeyboardVisible && inputMode == KeypadInputMode.NUMERIC_K1) Icons.Default.KeyboardHide else Icons.Default.Keyboard,
                            contentDescription = "Toggle Keyboard",
                            tint = textColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    val keypadPaneContent = @Composable { kMod: Modifier ->
        WorksheetKeypadView(
            theme = theme,
            settings = settings,
            grandTotal = activeDocument.grandTotal,
            inputMode = inputMode,
            onInputModeChange = { mode ->
                inputMode = mode
                if (mode == KeypadInputMode.TEXT_ABC) {
                    isKeyboardVisible = true
                    if (selectedLineIndex !in activeDocument.lines.indices) {
                        selectedLineIndex = (activeDocument.lines.size - 1).coerceAtLeast(0)
                    }
                    val curr = activeDocument.lines.getOrNull(selectedLineIndex)
                    if (curr != null && curr.lineType == WorksheetLineType.SUB_TOTAL) {
                        val lines = activeDocument.lines.toMutableList()
                        lines.add(
                            WorksheetLine(
                                lineType = WorksheetLineType.COMMENT_HEADER,
                                operator = "",
                                rawValue = "",
                                note = ""
                            )
                        )
                        selectedLineIndex = lines.size - 1
                        val recalculated = WorksheetTapeEngine.recalculate(lines)
                        val newTotal = recalculated.lastOrNull { it.lineType != WorksheetLineType.COMMENT_HEADER }?.runningTotal ?: 0.0
                        pushHistory(activeDocument.copy(lines = recalculated, grandTotal = newTotal))
                    }
                    keyboardController?.show()
                } else {
                    keyboardController?.hide()
                }
            },
            isEditKeyboardMode = isEditKeyboardMode,
            onDoneEditKeyboard = { isEditKeyboardMode = false },
            onOpenKeyCustomizer = { keyName -> keyNameToCustomize = keyName },
            quickVariableValue = if (memoryValue != 0.0) memoryValue else null,
            canUndo = undoStack.isNotEmpty(),
            canRedo = redoStack.isNotEmpty(),
            onDigit = { digit ->
                val lines = activeDocument.lines.toMutableList()
                val targetIdx = if (selectedLineIndex in lines.indices) selectedLineIndex else lines.size - 1

                if (lines.isEmpty() || targetIdx < 0) {
                    lines.add(WorksheetLine(operator = "+", rawValue = digit))
                    selectedLineIndex = 0
                } else {
                    val curr = lines[targetIdx]
                    if (curr.lineType == WorksheetLineType.SUB_TOTAL) {
                        lines.add(WorksheetLine(operator = "+", rawValue = digit))
                        selectedLineIndex = lines.size - 1
                    } else if (curr.lineType == WorksheetLineType.COMMENT_HEADER) {
                        if (curr.note.isBlank()) {
                            lines[targetIdx] = WorksheetLine(operator = "+", rawValue = digit)
                        } else {
                            lines.add(WorksheetLine(operator = "+", rawValue = digit))
                            selectedLineIndex = lines.size - 1
                        }
                    } else {
                        val newRaw = if (curr.rawValue == "0" && digit != ".") digit else curr.rawValue + digit
                        lines[targetIdx] = curr.copy(rawValue = newRaw)
                    }
                }
                val recalculated = WorksheetTapeEngine.recalculate(lines)
                val newTotal = recalculated.lastOrNull { it.lineType != WorksheetLineType.COMMENT_HEADER }?.runningTotal ?: 0.0
                pushHistory(activeDocument.copy(lines = recalculated, grandTotal = newTotal))
            },
            onOperator = { op ->
                val lines = activeDocument.lines.toMutableList()
                val targetIdx = if (selectedLineIndex in lines.indices) selectedLineIndex else lines.size - 1

                if (lines.isEmpty() || targetIdx < 0) {
                    lines.add(WorksheetLine(operator = op, rawValue = "0"))
                    selectedLineIndex = 0
                } else {
                    val curr = lines[targetIdx]
                    if (curr.lineType == WorksheetLineType.COMMENT_HEADER && curr.note.isBlank()) {
                        lines[targetIdx] = WorksheetLine(operator = op, rawValue = "0")
                    } else {
                        lines.add(WorksheetLine(operator = op, rawValue = "0"))
                        selectedLineIndex = lines.size - 1
                    }
                }
                val recalculated = WorksheetTapeEngine.recalculate(lines)
                val newTotal = recalculated.lastOrNull { it.lineType != WorksheetLineType.COMMENT_HEADER }?.runningTotal ?: 0.0
                pushHistory(activeDocument.copy(lines = recalculated, grandTotal = newTotal))
            },
            onPercentage = {
                val lines = activeDocument.lines.toMutableList()
                val newLine = WorksheetLine(
                    lineType = WorksheetLineType.PERCENTAGE,
                    operator = "-",
                    rawValue = "10"
                )
                lines.add(newLine)
                selectedLineIndex = lines.size - 1
                val recalculated = WorksheetTapeEngine.recalculate(lines)
                val newTotal = recalculated.lastOrNull { it.lineType != WorksheetLineType.COMMENT_HEADER }?.runningTotal ?: 0.0
                pushHistory(activeDocument.copy(lines = recalculated, grandTotal = newTotal))
            },
            onSubtotal = {
                val lines = activeDocument.lines.toMutableList()
                // 1. Subtotal line
                val subtotalLine = WorksheetLine(
                    lineType = WorksheetLineType.SUB_TOTAL,
                    operator = "=",
                    hasDividerBefore = true,
                    rawValue = "0"
                )
                lines.add(subtotalLine)

                // 2. Space line for comments
                val commentLine = WorksheetLine(
                    lineType = WorksheetLineType.COMMENT_HEADER,
                    operator = "",
                    rawValue = "",
                    note = ""
                )
                lines.add(commentLine)

                val recalculated = WorksheetTapeEngine.recalculate(lines)
                val newTotal = recalculated.lastOrNull { it.lineType != WorksheetLineType.COMMENT_HEADER }?.runningTotal ?: 0.0
                selectedLineIndex = lines.size - 1
                pushHistory(activeDocument.copy(lines = recalculated, grandTotal = newTotal))
            },
            onBackspace = {
                val lines = activeDocument.lines.toMutableList()
                val targetIdx = if (selectedLineIndex in lines.indices) selectedLineIndex else lines.size - 1
                if (lines.isNotEmpty() && targetIdx in lines.indices) {
                    val curr = lines[targetIdx]
                    if (curr.rawValue.length > 1) {
                        lines[targetIdx] = curr.copy(rawValue = curr.rawValue.dropLast(1))
                    } else {
                        lines[targetIdx] = curr.copy(rawValue = "0")
                    }
                    val recalculated = WorksheetTapeEngine.recalculate(lines)
                    val newTotal = recalculated.lastOrNull { it.lineType != WorksheetLineType.COMMENT_HEADER }?.runningTotal ?: 0.0
                    pushHistory(activeDocument.copy(lines = recalculated, grandTotal = newTotal))
                }
            },
            onClear = {
                val newDoc = activeDocument.copy(
                    lines = listOf(WorksheetLine(operator = "+", rawValue = "0")),
                    grandTotal = 0.0
                )
                selectedLineIndex = 0
                pushHistory(newDoc)
            },
            onMemoryPlus = {
                val targetLine = activeDocument.lines.getOrNull(selectedLineIndex) ?: activeDocument.lines.lastOrNull()
                val amount = targetLine?.evaluatedNumber ?: activeDocument.grandTotal
                memoryValue += amount
                Toast.makeText(context, "M+: $memoryValue", Toast.LENGTH_SHORT).show()
            },
            onMemoryMinus = {
                val targetLine = activeDocument.lines.getOrNull(selectedLineIndex) ?: activeDocument.lines.lastOrNull()
                val amount = targetLine?.evaluatedNumber ?: activeDocument.grandTotal
                memoryValue -= amount
                Toast.makeText(context, "M-: $memoryValue", Toast.LENGTH_SHORT).show()
            },
            onMemoryRecall = {
                val lines = activeDocument.lines.toMutableList()
                lines.add(WorksheetLine(operator = "+", rawValue = memoryValue.toString()))
                selectedLineIndex = lines.size - 1
                val recalculated = WorksheetTapeEngine.recalculate(lines)
                val newTotal = recalculated.lastOrNull { it.lineType != WorksheetLineType.COMMENT_HEADER }?.runningTotal ?: 0.0
                pushHistory(activeDocument.copy(lines = recalculated, grandTotal = newTotal))
            },
            onMemoryClear = {
                memoryValue = 0.0
                Toast.makeText(context, "Memory cleared", Toast.LENGTH_SHORT).show()
            },
            onCustomKey = {
                val lines = activeDocument.lines.toMutableList()
                val isHelpText = settings.customKeyType == "HELP_TEXT" ||
                        settings.customKeyType == "Customise Button" ||
                        settings.customKeyLabel.contains("Customise", ignoreCase = true)

                if (isHelpText) {
                    lines.add(
                        WorksheetLine(
                            lineType = WorksheetLineType.COMMENT_HEADER,
                            operator = "",
                            rawValue = "",
                            note = "This and other keys can be customized via \"Edit keyboard\" in the menu [:] at the top right."
                        )
                    )
                } else if (settings.customKeyType.startsWith("+") || settings.customKeyType.startsWith("-") || settings.customKeyType == "GST") {
                    val rate = settings.customKeyRate.toDoubleOrNull() ?: 18.0
                    val isTax = !settings.customKeyType.startsWith("-")
                    val label = if (settings.customKeyType.contains("GST")) "GST" else if (isTax) "Tax" else "Reduced"
                    val newLine = WorksheetLine(
                        lineType = WorksheetLineType.PERCENTAGE,
                        operator = if (isTax) "+" else "-",
                        rawValue = rate.toString(),
                        note = label
                    )
                    lines.add(newLine)
                } else if (settings.customKeyType.startsWith("SYSTEM_")) {
                    val sysKey = settings.customKeyType.removePrefix("SYSTEM_")
                    if (sysKey == "AC") {
                        val newDoc = activeDocument.copy(
                            lines = listOf(WorksheetLine(operator = "+", rawValue = "0")),
                            grandTotal = 0.0
                        )
                        selectedLineIndex = 0
                        pushHistory(newDoc)
                        return@WorksheetKeypadView
                    } else {
                        lines.add(WorksheetLine(operator = "+", rawValue = sysKey))
                    }
                } else {
                    val rate = settings.customKeyRate.toDoubleOrNull()
                    if (rate != null) {
                        lines.add(
                            WorksheetLine(
                                lineType = WorksheetLineType.PERCENTAGE,
                                operator = "+",
                                rawValue = rate.toString(),
                                note = settings.customKeyLabel.ifBlank { "Custom" }
                            )
                        )
                    } else {
                        lines.add(
                            WorksheetLine(
                                lineType = WorksheetLineType.COMMENT_HEADER,
                                operator = "",
                                rawValue = "",
                                note = settings.customKeyLabel.ifBlank { "Note" }
                            )
                        )
                    }
                }
                selectedLineIndex = lines.size - 1
                val recalculated = WorksheetTapeEngine.recalculate(lines)
                val newTotal = recalculated.lastOrNull { it.lineType != WorksheetLineType.COMMENT_HEADER }?.runningTotal ?: 0.0
                pushHistory(activeDocument.copy(lines = recalculated, grandTotal = newTotal))
            },
            onOpenCustomKeyDialog = {
                keyNameToCustomize = settings.customKeyLabel.ifBlank { "Customise Button" }
            },
            onOpenVariables = { showVariablesSheet = true },
            onUndo = { applyUndo() },
            onRedo = { applyRedo() },
            onToggleKeyboard = {
                if (inputMode == KeypadInputMode.TEXT_ABC) {
                    inputMode = KeypadInputMode.NUMERIC_K1
                    keyboardController?.hide()
                } else {
                    isKeyboardVisible = !isKeyboardVisible
                }
            },
            onInsertQuickVariable = { varVal ->
                val lines = activeDocument.lines.toMutableList()
                lines.add(WorksheetLine(operator = "+", rawValue = varVal.toString()))
                selectedLineIndex = lines.size - 1
                val recalculated = WorksheetTapeEngine.recalculate(lines)
                val newTotal = recalculated.lastOrNull { it.lineType != WorksheetLineType.COMMENT_HEADER }?.runningTotal ?: 0.0
                pushHistory(activeDocument.copy(lines = recalculated, grandTotal = newTotal))
            },
            modifier = kMod
        )
    }

    if (isLandscape) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(paperBg)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left Side: Display & Paper Tape
            Column(
                modifier = Modifier
                    .weight(0.46f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, if (isLightCanvas) Color(0xFFCBD5E1) else Color(0xFF334155), RoundedCornerShape(12.dp))
            ) {
                tapePaneContent(Modifier.fillMaxWidth().weight(1f))
            }

            // Right Side: Keypad Buttons
            Box(
                modifier = Modifier
                    .weight(0.54f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1F242B))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            ) {
                keypadPaneContent(Modifier.fillMaxSize())
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(paperBg)
        ) {
            if (isKeyboardVisible) {
                if (inputMode == KeypadInputMode.NUMERIC_K1) {
                    tapePaneContent(Modifier.fillMaxWidth().weight(tapeWeight))

                    // RESIZABLE SPLIT DRAG HANDLE
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .background(if (isLightCanvas) Color(0xFFE2E8F0) else Color(0xFF1B202A))
                            .pointerInput(Unit) {
                                detectVerticalDragGestures { _, dragAmount ->
                                    val delta = dragAmount / 1200f
                                    tapeWeight = (tapeWeight + delta).coerceIn(0.25f, 0.72f)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Resize split",
                            tint = subtextColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    keypadPaneContent(Modifier.fillMaxWidth().weight(1f - tapeWeight))
                } else {
                    // In TEXT_ABC mode, tape pane takes top area and the dock sits right above system keyboard
                    tapePaneContent(Modifier.fillMaxWidth().weight(1f))
                    keypadPaneContent(Modifier.fillMaxWidth().wrapContentHeight().imePadding())
                }
            } else {
                tapePaneContent(Modifier.fillMaxWidth().weight(1f))
            }
        }
    }

    // --- SHEETS & DIALOGS ---

    // Documents Drawer
    if (showDrawer) {
        ModalBottomSheet(onDismissRequest = { showDrawer = false }) {
            DocumentsDrawerContent(
                documents = documents,
                activeId = activeDocument.id,
                theme = theme,
                onSelect = { onSelectDocument(it) },
                onNew = {
                    showDrawer = false
                    showCreateDocDialog = true
                },
                onRename = { docToRename ->
                    showDrawer = false
                    documentBeingRenamed = docToRename
                    showRenameDialog = true
                },
                onSharePhoto = { docToShare ->
                    showDrawer = false
                    WorksheetPhotoExporter.shareAsPhoto(context, docToShare, settings)
                },
                onDelete = { onDeleteDocument(it) },
                onDuplicate = { onDuplicateDocument(it) },
                onOpenSettings = {
                    showDrawer = false
                    showSettingsSheet = true
                },
                onOpenInfo = {
                    showDrawer = false
                    showInfoDialog = true
                },
                onOpenTemplates = {
                    showDrawer = false
                    showTemplatesSheet = true
                },
                onOpenBackup = {
                    showDrawer = false
                    showCreateBackupDialog = true
                },
                onOpenRestore = {
                    showDrawer = false
                    showRestoreBackupDialog = true
                },
                onDismiss = { showDrawer = false }
            )
        }
    }

    // Create New Document Dialog
    if (showCreateDocDialog) {
        CreateNewDocDialog(
            suggestedTitle = "Calculation(${documents.size + 1})",
            theme = theme,
            onConfirm = {
                onNewDocument(it)
                showCreateDocDialog = false
            },
            onDismiss = { showCreateDocDialog = false }
        )
    }

    // Rename Document Dialog
    if (showRenameDialog) {
        val targetDoc = documentBeingRenamed ?: activeDocument
        var newTitle by remember(targetDoc.id, targetDoc.title) { mutableStateOf(targetDoc.title) }
        AlertDialog(
            onDismissRequest = {
                showRenameDialog = false
                documentBeingRenamed = null
            },
            title = { Text("Rename Worksheet", color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Worksheet Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val renamedTitle = newTitle.ifBlank { "Calculation" }
                        if (targetDoc.id == activeDocument.id) {
                            pushHistory(activeDocument.copy(title = renamedTitle))
                        } else {
                            onSaveDocument(targetDoc.copy(title = renamedTitle))
                        }
                        documentBeingRenamed = null
                        showRenameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        documentBeingRenamed = null
                        showRenameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("Cancel", color = textColor)
                }
            }
        )
    }

    // Top Header Edit Dialog
    if (showHeaderEditDialog) {
        var headerInput by remember { mutableStateOf(activeDocument.headerNote.ifBlank { "Total - 70,000/-" }) }
        AlertDialog(
            onDismissRequest = { showHeaderEditDialog = false },
            title = { Text("Edit Worksheet Header", color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = headerInput,
                    onValueChange = { headerInput = it },
                    label = { Text("Header note (e.g. Total - 70,000/-)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pushHistory(activeDocument.copy(headerNote = headerInput))
                        showHeaderEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showHeaderEditDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("Cancel", color = textColor)
                }
            }
        )
    }

    // Settings Screen / Sheet
    if (showSettingsSheet) {
        ModalBottomSheet(onDismissRequest = { showSettingsSheet = false }) {
            WorksheetSettingsSheet(
                settings = settings,
                theme = theme,
                onUpdate = { onUpdateSettings(it) },
                onDismiss = { showSettingsSheet = false }
            )
        }
    }

    // Info Dialog
    if (showInfoDialog) {
        WorksheetInfoDialog(theme = theme, onDismiss = { showInfoDialog = false })
    }

    // Upgrade to Pro Dialog
    if (showUpgradeDialog) {
        UpgradeToProDialog(theme = theme, onDismiss = { showUpgradeDialog = false })
    }

    // Templates Sheet
    if (showTemplatesSheet) {
        ModalBottomSheet(onDismissRequest = { showTemplatesSheet = false }) {
            TemplatesSheetContent(
                theme = theme,
                onSelect = {
                    onApplyTemplate(it)
                    showTemplatesSheet = false
                },
                onDismiss = { showTemplatesSheet = false }
            )
        }
    }

    // Share Sheet
    if (showShareSheet) {
        ModalBottomSheet(onDismissRequest = { showShareSheet = false }) {
            ShareSheetContent(
                document = activeDocument,
                settings = settings,
                theme = theme,
                onDismiss = { showShareSheet = false }
            )
        }
    }

    // Variables & Quick Insert Sheet
    if (showVariablesSheet) {
        val existingVars = remember(activeDocument) {
            listOf(
                WorksheetVariable("Quantity", 25.0),
                WorksheetVariable("Price", 525.0),
                WorksheetVariable("chai", 20.0),
                WorksheetVariable("Tax", 18.0)
            )
        }
        ModalBottomSheet(onDismissRequest = { showVariablesSheet = false }) {
            VariablesSheetContent(
                existingVariables = existingVars,
                theme = theme,
                onInsertVariable = { v ->
                    val lines = activeDocument.lines.toMutableList()
                    lines.add(WorksheetLine(operator = "+", rawValue = v.value.toString(), note = v.name))
                    selectedLineIndex = lines.size - 1
                    val recalculated = WorksheetTapeEngine.recalculate(lines)
                    val newTotal = recalculated.lastOrNull()?.runningTotal ?: 0.0
                    pushHistory(activeDocument.copy(lines = recalculated, grandTotal = newTotal))
                },
                onAddNewVariable = { name, value ->
                    val lines = activeDocument.lines.toMutableList()
                    lines.add(WorksheetLine(operator = "+", rawValue = value.toString(), note = name))
                    val recalculated = WorksheetTapeEngine.recalculate(lines)
                    val newTotal = recalculated.lastOrNull()?.runningTotal ?: 0.0
                    pushHistory(activeDocument.copy(lines = recalculated, grandTotal = newTotal))
                },
                onDismiss = { showVariablesSheet = false }
            )
        }
    }

    // Create Backup Dialog
    if (showCreateBackupDialog) {
        CreateBackupDialog(documents = documents, theme = theme, onDismiss = { showCreateBackupDialog = false })
    }

    // Restore Backup Dialog
    if (showRestoreBackupDialog) {
        RestoreBackupDialog(
            theme = theme,
            onRestore = { onRestoreDocuments(it) },
            onDismiss = { showRestoreBackupDialog = false }
        )
    }

    // Button Customization Dialog (CalcTape Pro video)
    if (keyNameToCustomize != null) {
        ButtonCustomizationDialog(
            keyName = keyNameToCustomize!!,
            currentCustomType = settings.customKeyType,
            currentCustomRate = settings.customKeyRate,
            theme = theme,
            onSave = { label, actionType, rate ->
                onUpdateSettings(
                    settings.copy(
                        customKeyLabel = label,
                        customKeyType = actionType,
                        customKeyRate = rate
                    )
                )
                keyNameToCustomize = null
            },
            onDismiss = { keyNameToCustomize = null }
        )
    }
}

/**
 * Single Row on the Paper Tape Canvas
 */
@Composable
private fun TapeLineRow(
    line: WorksheetLine,
    index: Int,
    isSelected: Boolean,
    isTextMode: Boolean,
    settings: WorksheetSettings,
    textColor: Color,
    subtextColor: Color,
    deductionColor: Color,
    accentColor: Color,
    onSelect: () -> Unit,
    onOpenKeyboard: () -> Unit,
    onUpdateNote: (String) -> Unit,
    onDelete: () -> Unit
) {
    val isDeduction = line.operator == "-" || line.evaluatedNumber < 0
    val numColor = if (isDeduction) deductionColor else textColor
    val numFormatted = WorksheetTapeEngine.formatNumber(line.evaluatedNumber, settings.indianDigitGrouping, settings.decimals)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onSelect()
                if (line.lineType == WorksheetLineType.COMMENT_HEADER) {
                    onOpenKeyboard()
                }
            }
            .background(if (isSelected) accentColor.copy(alpha = 0.12f) else Color.Transparent, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        // Subtotal separator underline
        if (line.hasDividerBefore || line.lineType == WorksheetLineType.SUB_TOTAL || line.lineType == WorksheetLineType.GRAND_TOTAL) {
            HorizontalDivider(
                color = textColor.copy(alpha = 0.45f),
                thickness = if (line.lineType == WorksheetLineType.GRAND_TOTAL) 2.dp else 1.dp,
                modifier = Modifier.padding(vertical = 3.dp)
            )
        }

        if (line.lineType == WorksheetLineType.COMMENT_HEADER) {
            // Pure Comment Line (space left when pressing = or typing remarks)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelected && isTextMode) {
                    val focusRequester = remember { FocusRequester() }
                    LaunchedEffect(isSelected, isTextMode) {
                        try {
                            focusRequester.requestFocus()
                        } catch (_: Exception) {}
                    }
                    BasicTextField(
                        value = line.note,
                        onValueChange = onUpdateNote,
                        textStyle = TextStyle(
                            color = textColor,
                            fontSize = settings.fontSize.sp,
                            fontFamily = FontFamily.Default
                        ),
                        cursorBrush = SolidColor(accentColor),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (line.note.isEmpty()) {
                                    Text(
                                        text = "|",
                                        color = accentColor.copy(alpha = 0.8f),
                                        fontSize = settings.fontSize.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                } else {
                    Text(
                        text = if (line.note.isNotBlank()) line.note else if (isSelected) "|" else "",
                        color = if (line.note.isNotBlank()) textColor else accentColor.copy(alpha = 0.7f),
                        fontSize = settings.fontSize.sp,
                        fontFamily = FontFamily.Default,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isSelected) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = deductionColor, modifier = Modifier.size(15.dp))
                    }
                }
            }
        } else {
            // Calculation / Subtotal / Operator line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Operator
                    Text(
                        text = line.operator,
                        color = textColor,
                        fontSize = settings.fontSize.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(18.dp)
                    )

                    // Number
                    Text(
                        text = numFormatted,
                        color = numColor,
                        fontSize = settings.fontSize.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Inline Remark / Note (editable in text mode)
                    if (isSelected && isTextMode) {
                        val noteFocusRequester = remember { FocusRequester() }
                        LaunchedEffect(isSelected, isTextMode) {
                            try {
                                noteFocusRequester.requestFocus()
                            } catch (_: Exception) {}
                        }
                        BasicTextField(
                            value = line.note,
                            onValueChange = onUpdateNote,
                            textStyle = TextStyle(
                                color = subtextColor,
                                fontSize = (settings.fontSize - 1).sp,
                                fontFamily = FontFamily.Default
                            ),
                            cursorBrush = SolidColor(accentColor),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(noteFocusRequester),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (line.note.isEmpty()) {
                                        Text(
                                            text = "note...",
                                            color = subtextColor.copy(alpha = 0.4f),
                                            fontSize = (settings.fontSize - 1).sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    } else if (line.note.isNotBlank()) {
                        Text(
                            text = line.note,
                            color = subtextColor,
                            fontSize = (settings.fontSize - 2).sp,
                            fontFamily = FontFamily.Default,
                            modifier = Modifier.clickable {
                                onSelect()
                                onOpenKeyboard()
                            }
                        )
                    }

                    // Variable assignment if any
                    if (!line.variableName.isNullOrBlank()) {
                        Text(
                            text = "= ${line.variableName}",
                            color = accentColor,
                            fontSize = (settings.fontSize - 2).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // If selected, show subtle action icons
                if (isSelected) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = deductionColor, modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Pre-made Templates Bottom Sheet
 */
@Composable
fun TemplatesSheetContent(
    theme: ThemePalette,
    onSelect: (WorksheetTemplate) -> Unit,
    onDismiss: () -> Unit
) {
    val templates = remember { WorksheetTapeEngine.getDefaultTemplates() }
    val isLightCanvas = theme.backgroundColor.luminance() > 0.45f
    val textColor = if (isLightCanvas) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val subtextColor = if (isLightCanvas) Color(0xFF64748B) else Color(0xFF94A3B8)

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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = theme.accentColor)
                Text(text = "Pre-made Templates", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = textColor)
            }
        }

        Text(
            text = "Tap a template to instantly load a complete calculation worksheet:",
            color = subtextColor,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(templates.size) { idx ->
                val template = templates[idx]
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = theme.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(template) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = template.title, color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Surface(shape = CircleShape, color = theme.accentColor.copy(alpha = 0.15f)) {
                                Text(
                                    text = "${template.lines.size} steps",
                                    color = theme.accentColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = template.description, color = subtextColor, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * Share & Export Bottom Sheet
 */
@Composable
fun ShareSheetContent(
    document: WorksheetDocument,
    settings: WorksheetSettings = WorksheetSettings(),
    theme: ThemePalette,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current

    val plainText = remember(document) { WorksheetTapeEngine.exportToPlainText(document) }
    val markdownText = remember(document) { WorksheetTapeEngine.exportToMarkdown(document) }
    val isLightCanvas = theme.backgroundColor.luminance() > 0.45f
    val textColor = if (isLightCanvas) Color(0xFF1E293B) else Color(0xFFF1F5F9)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.90f)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = theme.accentColor)
                Text(text = "Share & Export Worksheet", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = textColor)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Receipt Preview Box
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isLightCanvas) Color(0xFFF8FAFC) else Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.25f)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 4.dp)
        ) {
            LazyColumn(modifier = Modifier.padding(12.dp)) {
                item {
                    Text(
                        text = plainText,
                        color = textColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Share as Photo Button (Highest priority for photo sharing)
            Button(
                onClick = {
                    WorksheetPhotoExporter.shareAsPhoto(context, document, settings)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Share as Photo (PNG Image)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            // Save Photo to Gallery
            Button(
                onClick = {
                    WorksheetPhotoExporter.savePhotoToGallery(context, document, settings)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Save Photo to Gallery / Photos", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            // Plain text share
            Button(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, plainText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share ${document.title}"))
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Share via WhatsApp / Text", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(plainText))
                        haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.cardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.accentColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Text(text = "Copy Plain Text", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(markdownText))
                        haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.cardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.accentColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Text(text = "Copy Markdown", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/**
 * Custom Key Configuration Dialog
 */
@Composable
fun CustomKeyDialog(
    currentRate: String,
    currentType: String,
    theme: ThemePalette,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val isLightCanvas = theme.backgroundColor.luminance() > 0.45f
    val textColor = if (isLightCanvas) Color(0xFF1E293B) else Color(0xFFF1F5F9)

    var rateInput by remember { mutableStateOf(currentRate) }
    var selectedType by remember { mutableStateOf(currentType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Customize Quick Key", color = textColor, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select key type & percentage rate:", color = textColor, fontSize = 12.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("GST" to "+ Tax / GST", "DISC" to "− Discount").forEach { (typeKey, label) ->
                        val isSelected = selectedType == typeKey
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) theme.accentColor else theme.cardBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f).clickable { selectedType = typeKey }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                // Presets
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("5", "10", "12", "15", "18", "28").forEach { preset ->
                        Surface(
                            shape = CircleShape,
                            color = if (rateInput == preset) theme.accentColor.copy(alpha = 0.2f) else theme.cardBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.accentColor),
                            modifier = Modifier.clickable { rateInput = preset }
                        ) {
                            Text(
                                text = "$preset%",
                                color = theme.accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = rateInput,
                    onValueChange = { rateInput = it },
                    label = { Text("Percentage Rate %") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(rateInput.ifBlank { "18" }, selectedType) },
                colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)
            ) {
                Text("Set Key", color = Color.White)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                Text("Cancel", color = textColor)
            }
        }
    )
}

package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.unit.dp
import com.example.model.CalculationHistory
import com.example.model.ThemePalette
import com.example.ui.viewmodel.CalculatorUiState
import com.example.ui.viewmodel.CalculatorViewModel

@Composable
fun LandscapeDualPaneView(
    uiState: CalculatorUiState,
    theme: ThemePalette,
    historyList: List<CalculationHistory>,
    viewModel: CalculatorViewModel,
    haptics: HapticFeedback,
    modifier: Modifier = Modifier
) {
    val isScientific = uiState.mode == com.example.model.CalculatorMode.SCIENTIFIC

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Left Pane: Display & Utilities Dock (MC, MR, M+, M-, History, Notation, Copy)
        LandscapeDisplayPane(
            expression = uiState.expression,
            result = uiState.result,
            previewResult = uiState.previewResult,
            theme = theme,
            angleMode = uiState.angleMode,
            hasMemory = uiState.hasMemory,
            mode = uiState.mode,
            displayConfig = uiState.displayConfig,
            historyCount = historyList.size,
            cursorPosition = uiState.cursorPosition,
            onCursorChange = { viewModel.setCursorPosition(it) },
            isEvaluated = uiState.lastEvaluated,
            onToggleAngleMode = { viewModel.toggleAngleMode() },
            onOpenHistory = { viewModel.setShowHistorySheet(true) },
            onOpenDecimalConverter = { viewModel.setShowDecimalConverterSheet(true) },
            onToggleNotation = { viewModel.toggleDisplayNotation(haptics) },
            onMemoryAdd = { viewModel.onMemoryAdd(haptics) },
            onMemorySubtract = { viewModel.onMemorySubtract(haptics) },
            onMemoryRecall = { viewModel.onMemoryRecall(haptics) },
            onMemoryClear = { viewModel.onMemoryClear(haptics) },
            modifier = Modifier
                .weight(if (isScientific) 0.32f else 0.38f)
                .fillMaxHeight()
        )

        // Right Pane: Scientific or Standard Keypad depending on mode
        if (isScientific) {
            LandscapeScientificKeypad(
                theme = theme,
                angleMode = uiState.angleMode,
                isSecondFunction = uiState.isSecondFunction,
                onToggleAngleMode = { viewModel.toggleAngleMode() },
                onToggleSecondFunction = { viewModel.toggleSecondFunction() },
                onInput = { viewModel.onInput(it, haptics) },
                onFunction = { viewModel.onFunction(it, haptics) },
                onConstant = { viewModel.onConstant(it, haptics) },
                onClear = { viewModel.onClear(haptics) },
                onBackspace = { viewModel.onBackspace(haptics) },
                onNegate = { viewModel.onNegate(haptics) },
                onEquals = { viewModel.onEquals(haptics) },
                onMemoryAdd = { viewModel.onMemoryAdd(haptics) },
                onMemorySubtract = { viewModel.onMemorySubtract(haptics) },
                onMemoryRecall = { viewModel.onMemoryRecall(haptics) },
                onMemoryClear = { viewModel.onMemoryClear(haptics) },
                onEng = { viewModel.onEngKey(haptics) },
                onAns = { viewModel.onAnsKey(haptics) },
                onDecimalConverter = { viewModel.setShowDecimalConverterSheet(true) },
                modifier = Modifier
                    .weight(0.68f)
                    .fillMaxHeight()
            )
        } else {
            LandscapeStandardKeypad(
                theme = theme,
                onInput = { viewModel.onInput(it, haptics) },
                onClear = { viewModel.onClear(haptics) },
                onBackspace = { viewModel.onBackspace(haptics) },
                onNegate = { viewModel.onNegate(haptics) },
                onEquals = { viewModel.onEquals(haptics) },
                modifier = Modifier
                    .weight(0.62f)
                    .fillMaxHeight()
            )
        }
    }
}

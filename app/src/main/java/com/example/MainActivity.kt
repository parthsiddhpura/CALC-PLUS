package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.CalculatorMode
import com.example.ui.CalculatorScreen
import com.example.ui.theme.CalculatorThemes
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.widget.CalculatorAppWidgetProvider

class MainActivity : ComponentActivity() {
    private var pendingMode: String? = null
    private var pendingInitialExpr: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.billing.PlayBillingManager.initialize(this)
        pendingMode = intent?.getStringExtra(CalculatorAppWidgetProvider.EXTRA_MODE)
        pendingInitialExpr = intent?.getStringExtra(com.example.widget.WidgetActionReceiver.EXTRA_INITIAL_EXPR)

        setContent {
            val calculatorViewModel: CalculatorViewModel = viewModel()
            val uiState by calculatorViewModel.uiState.collectAsStateWithLifecycle()
            val theme = remember(
                uiState.currentThemeId,
                uiState.customAccentColor,
                uiState.customShapeType,
                uiState.customDisplayFont,
                uiState.displayConfig.showScanlinesOverride
            ) {
                calculatorViewModel.getEffectiveTheme(uiState)
            }

            LaunchedEffect(pendingMode, pendingInitialExpr) {
                pendingInitialExpr?.let { expr ->
                    calculatorViewModel.setExpressionFromWidget(expr)
                    pendingInitialExpr = null
                }

                pendingMode?.let { modeStr ->
                    when (modeStr) {
                        "GST_CALCULATOR" -> calculatorViewModel.setMode(CalculatorMode.GST_CALCULATOR)
                        "CALCULATION_CHAINS" -> calculatorViewModel.setMode(CalculatorMode.CALCULATION_CHAINS)
                        "AI_COPILOT" -> calculatorViewModel.setMode(CalculatorMode.AI_COPILOT)
                        "CUSTOM_BUILDER" -> calculatorViewModel.setMode(CalculatorMode.CUSTOM_BUILDER)
                        "CURRENCY_CONVERTER" -> calculatorViewModel.setMode(CalculatorMode.CURRENCY_CONVERTER)
                        "UNIT_CONVERTER" -> calculatorViewModel.setMode(CalculatorMode.UNIT_CONVERTER)
                        "EMI_LOAN" -> calculatorViewModel.setMode(CalculatorMode.EMI_LOAN)
                        "BMI_CALCULATOR" -> calculatorViewModel.setMode(CalculatorMode.BMI_CALCULATOR)
                        "AGE_CALCULATOR" -> calculatorViewModel.setMode(CalculatorMode.AGE_CALCULATOR)
                        "PROGRAMMER" -> calculatorViewModel.setMode(CalculatorMode.PROGRAMMER)
                        "TIP_SPLIT" -> calculatorViewModel.setMode(CalculatorMode.TIP_SPLIT)
                        "ENGINEERING" -> calculatorViewModel.setMode(CalculatorMode.ENGINEERING)
                        "SCIENTIFIC" -> calculatorViewModel.setMode(CalculatorMode.SCIENTIFIC)
                        else -> calculatorViewModel.setMode(CalculatorMode.STANDARD)
                    }
                    pendingMode = null
                }
            }

            MyApplicationTheme(
                darkTheme = theme.isDark,
                dynamicColor = false
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = theme.backgroundColor
                ) {
                    CalculatorScreen(viewModel = calculatorViewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val modeStr = intent.getStringExtra(CalculatorAppWidgetProvider.EXTRA_MODE)
        if (modeStr != null) {
            pendingMode = modeStr
        }
        val initialExpr = intent.getStringExtra(com.example.widget.WidgetActionReceiver.EXTRA_INITIAL_EXPR)
        if (initialExpr != null) {
            pendingInitialExpr = initialExpr
        }
    }
}


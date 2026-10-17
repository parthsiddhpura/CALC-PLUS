package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.AiCopilotResponse
import com.example.domain.AiMathCopilotEngine
import com.example.model.CalculatorMode
import com.example.model.ThemePalette
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiMathCopilotView(
    theme: ThemePalette,
    onNavigateMode: (CalculatorMode) -> Unit,
    onLoadToExpression: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var queryText by remember { mutableStateOf("18% GST on ₹45,000 plus 10% discount") }
    var copilotResult by remember {
        mutableStateOf<AiCopilotResponse?>(
            AiMathCopilotEngine.solveQuery("18% GST on ₹45,000 plus 10% discount")
        )
    }
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current
    var copiedNotice by remember { mutableStateOf(false) }

    val sampleQueries = listOf(
        "18% GST on ₹45,000 plus 10% discount",
        "What's $5,000 in INR invested for 10 years at 12%",
        "Solve 2x² + 5x - 3 = 0",
        "25% tip on ₹4,800 bill split among 5 people",
        "Compound growth of ₹10,000 monthly SIP for 15 years at 14%"
    )

    // Beta Version Note with "Don't ask again" option
    BetaFeatureNoticeDialog(
        featureKey = "ai_math_copilot",
        featureTitle = "AI Math Copilot",
        theme = theme,
        noteText = "AI Math & Logic Copilot solves natural language expressions, step-by-step algebra, currency/tax calculations, and financial formulas. Please verify numerical output for high-stakes or critical decisions as we continuously enhance accuracy."
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = theme.accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = theme.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "AI Math & Logic Copilot",
                        color = theme.screenTextColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Natural language math + Step-by-Step Solver",
                        color = theme.screenExpressionColor,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Query Input Field
        OutlinedTextField(
            value = queryText,
            onValueChange = { queryText = it },
            placeholder = {
                Text(
                    "Ask any calculation, equation, tax or financial question...",
                    fontSize = 12.sp,
                    color = theme.screenExpressionColor.copy(alpha = 0.6f)
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (queryText.isNotBlank()) {
                        IconButton(onClick = { queryText = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = theme.screenExpressionColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            if (queryText.isNotBlank()) {
                                copilotResult = AiMathCopilotEngine.solveQuery(queryText)
                                focusManager.clearFocus()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Solve Query",
                            tint = theme.accentColor
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (queryText.isNotBlank()) {
                    copilotResult = AiMathCopilotEngine.solveQuery(queryText)
                    focusManager.clearFocus()
                }
            }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = theme.accentColor,
                unfocusedBorderColor = theme.screenBorderColor.copy(alpha = 0.4f),
                focusedTextColor = theme.screenTextColor,
                unfocusedTextColor = theme.screenTextColor,
                focusedContainerColor = theme.surfaceColor,
                unfocusedContainerColor = theme.surfaceColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp)
        )

        // Sample Query Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            sampleQueries.forEach { sample ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = theme.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.screenBorderColor.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            queryText = sample
                            copilotResult = AiMathCopilotEngine.solveQuery(sample)
                            focusManager.clearFocus()
                        }
                ) {
                    Text(
                        text = sample,
                        color = theme.screenTextColor,
                        fontSize = 10.sp,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Output & Step-by-Step Reasoner
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (copilotResult != null) {
                val res = copilotResult!!

                // Final Answer Banner
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                        border = androidx.compose.foundation.BorderStroke(2.dp, theme.accentColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = theme.accentColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = res.category.uppercase(Locale.ROOT),
                                        color = theme.accentColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(res.finalAnswer))
                                        copiedNotice = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy result",
                                        tint = theme.screenExpressionColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            // Dynamic font size adapting automatically to result length
                            val answerFontSize = when {
                                res.finalAnswer.length > 35 -> 18.sp
                                res.finalAnswer.length > 22 -> 22.sp
                                res.finalAnswer.length > 14 -> 25.sp
                                else -> 28.sp
                            }
                            Text(
                                text = res.finalAnswer,
                                color = theme.accentColor,
                                fontSize = answerFontSize,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = res.summary,
                                color = theme.screenTextColor,
                                fontSize = if (res.summary.length > 120) 11.5.sp else 12.5.sp,
                                lineHeight = if (res.summary.length > 120) 16.sp else 18.sp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Quick Action Button
                            if (res.actionSuggestion.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = theme.cardBackground,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.accentColor),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            when {
                                                res.actionSuggestion.contains("GST") -> onNavigateMode(CalculatorMode.GST_CALCULATOR)
                                                res.actionSuggestion.contains("Chains") -> onNavigateMode(CalculatorMode.CALCULATION_CHAINS)
                                                res.actionSuggestion.contains("Scientific") -> onNavigateMode(CalculatorMode.SCIENTIFIC)
                                                else -> onNavigateMode(CalculatorMode.STANDARD)
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint = theme.accentColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = res.actionSuggestion,
                                            color = theme.accentColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Step-by-Step Mathematical Solver Section
                item {
                    Text(
                        text = "AI STEP-BY-STEP MATHEMATICAL PROOF",
                        color = theme.accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                items(res.steps) { step ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.screenBorderColor.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
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
                                        color = theme.accentColor,
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${step.stepNumber}",
                                                color = theme.backgroundColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Text(
                                        text = step.title,
                                        color = theme.screenTextColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = theme.cardBackground
                                ) {
                                    Text(
                                        text = step.intermediateResult,
                                        color = theme.accentColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = step.formula,
                                color = theme.screenTextColor,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = step.explanation,
                                color = theme.screenExpressionColor,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

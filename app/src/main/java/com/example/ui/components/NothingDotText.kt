package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Authentic 5x7 Dot-Matrix Character Bitmaps matching the Nothing OS NDot typography.
 * Each character is defined as 7 rows of 5 boolean bits.
 */
object NDotFontMatrix {

    // 5 wide x 7 high binary matrices
    val GLYPHS: Map<Char, List<String>> = mapOf(
        '0' to listOf(
            ".###.",
            "#...#",
            "#..##",
            "#.#.#",
            "##..#",
            "#...#",
            ".###."
        ),
        '1' to listOf(
            "..#..",
            ".##..",
            "..#..",
            "..#..",
            "..#..",
            "..#..",
            ".###."
        ),
        '2' to listOf(
            ".###.",
            "#...#",
            "....#",
            "..##.",
            ".#...",
            "#....",
            "#####"
        ),
        '3' to listOf(
            ".###.",
            "#...#",
            "....#",
            "..##.",
            "....#",
            "#...#",
            ".###."
        ),
        '4' to listOf(
            "#...#",
            "#...#",
            "#...#",
            "#####",
            "....#",
            "....#",
            "....#"
        ),
        '5' to listOf(
            "#####",
            "#....",
            "#....",
            "####.",
            "....#",
            "#...#",
            ".###."
        ),
        '6' to listOf(
            ".###.",
            "#....",
            "#....",
            "####.",
            "#...#",
            "#...#",
            ".###."
        ),
        '7' to listOf(
            "#####",
            "....#",
            "...#.",
            "..#..",
            ".#...",
            ".#...",
            ".#..."
        ),
        '8' to listOf(
            ".###.",
            "#...#",
            "#...#",
            ".###.",
            "#...#",
            "#...#",
            ".###."
        ),
        '9' to listOf(
            ".###.",
            "#...#",
            "#...#",
            ".####",
            "....#",
            "....#",
            ".###."
        ),
        '.' to listOf(
            ".....",
            ".....",
            ".....",
            ".....",
            ".....",
            "..##.",
            "..##."
        ),
        ',' to listOf(
            ".....",
            ".....",
            ".....",
            ".....",
            "..##.",
            "..##.",
            ".#..."
        ),
        '+' to listOf(
            ".....",
            "..#..",
            "..#..",
            "#####",
            "..#..",
            "..#..",
            "....."
        ),
        '-' to listOf(
            ".....",
            ".....",
            ".....",
            "#####",
            ".....",
            ".....",
            "....."
        ),
        '×' to listOf(
            ".....",
            "#...#",
            ".#.#.",
            "..#..",
            ".#.#.",
            "#...#",
            "....."
        ),
        '*' to listOf(
            ".....",
            "#...#",
            ".#.#.",
            "..#..",
            ".#.#.",
            "#...#",
            "....."
        ),
        '÷' to listOf(
            ".....",
            "..#..",
            ".....",
            "#####",
            ".....",
            "..#..",
            "....."
        ),
        '/' to listOf(
            "....#",
            "...#.",
            "...#.",
            "..#..",
            ".#...",
            ".#...",
            "#...."
        ),
        '=' to listOf(
            ".....",
            ".....",
            "#####",
            ".....",
            "#####",
            ".....",
            "....."
        ),
        '%' to listOf(
            "#...#",
            "#..#.",
            "..#..",
            ".#...",
            ".#..#",
            "#...#",
            "....."
        ),
        '(' to listOf(
            "..##.",
            ".#...",
            "#....",
            "#....",
            "#....",
            ".#...",
            "..##."
        ),
        ')' to listOf(
            ".##..",
            "...#.",
            "....#",
            "....#",
            "....#",
            "...#.",
            ".##.."
        ),
        '₹' to listOf(
            "#####",
            "..#..",
            "####.",
            "..#.#",
            "####.",
            "..#.#",
            "..#.#"
        ),
        '$' to listOf(
            "..#..",
            ".####",
            "#.#..",
            ".###.",
            "..#.#",
            "####.",
            "..#.."
        ),
        ' ' to listOf(
            ".....",
            ".....",
            ".....",
            ".....",
            ".....",
            ".....",
            "....."
        )
    )
}

/**
 * Animated NDot dot-matrix character rendered cleanly on Compose Canvas.
 */
@Composable
fun NothingDotChar(
    char: Char,
    dotColor: Color,
    dimDotColor: Color = dotColor.copy(alpha = 0.08f),
    charHeight: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val matrix = NDotFontMatrix.GLYPHS[char] ?: NDotFontMatrix.GLYPHS[' ']!!

    val charWidth = charHeight * (5f / 7f)

    Canvas(
        modifier = modifier.size(width = charWidth, height = charHeight)
    ) {
        val rows = 7
        val cols = 5
        val cellW = size.width / cols
        val cellH = size.height / rows
        val dotRadius = minOf(cellW, cellH) * 0.38f

        for (r in 0 until rows) {
            val rowStr = matrix[r]
            for (c in 0 until cols) {
                val isLit = rowStr.getOrNull(c) == '#'
                val cx = c * cellW + cellW / 2f
                val cy = r * cellH + cellH / 2f

                drawCircle(
                    color = if (isLit) dotColor else dimDotColor,
                    radius = if (isLit) dotRadius else dotRadius * 0.45f,
                    center = Offset(cx, cy)
                )
            }
        }
    }
}

/**
 * Full line of Nothing NDot dot-matrix text for calculator display expressions and results.
 */
@Composable
fun NothingDotText(
    text: String,
    dotColor: Color,
    dimDotColor: Color = dotColor.copy(alpha = 0.08f),
    charHeight: Dp = 42.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        text.forEach { char ->
            NothingDotChar(
                char = char,
                dotColor = dotColor,
                dimDotColor = dimDotColor,
                charHeight = charHeight
            )
        }
    }
}

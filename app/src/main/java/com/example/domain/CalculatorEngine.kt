package com.example.domain

import com.example.model.AngleMode
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.Stack
import kotlin.math.*

object CalculatorEngine {

    private val mathContext = MathContext(15, RoundingMode.HALF_UP)
    val PHI: Double = (1.0 + sqrt(5.0)) / 2.0

    private val US_SYMBOLS = DecimalFormatSymbols(Locale.US)
    private val IMPLICIT_REGEX = Regex("([0-9πeφPIEPHI)]+)\\s*([a-zA-Z(√∛]+)")
    private val NUM_CONST_REGEX = Regex("(\\d+)(PI|E|PHI)")

    /**
     * Evaluates a mathematical expression string.
     * Returns a formatted result string or throws an exception/returns "Error".
     */
    fun evaluate(expression: String, angleMode: AngleMode): String {
        if (expression.isBlank()) return ""
        try {
            val cleanExpr = prepareExpression(expression)
            val tokens = tokenize(cleanExpr)
            val rpn = shuntingYard(tokens)
            val result = evaluateRpn(rpn, angleMode)
            return formatResult(result)
        } catch (e: Exception) {
            return "Error"
        }
    }

    /**
     * Safe live preview evaluation that does not show "Error" if user is midway typing.
     */
    fun evaluatePreview(expression: String, angleMode: AngleMode): String? {
        if (expression.isBlank()) return null
        val cleanPreview = expression.trim().replace(",", "").replace(Regex("(?<=\\d)\\s+(?=\\d)"), "")
        // If it's just a single number, no need for preview
        if (cleanPreview.toDoubleOrNull() != null) return null

        // Try balancing open parentheses
        var balanced = expression.trim()
        val openCount = balanced.count { it == '(' }
        val closeCount = balanced.count { it == ')' }
        if (openCount > closeCount) {
            balanced += ")".repeat(openCount - closeCount)
        }

        // Remove trailing operator if user just typed it (excluding % which is a complete postfix operator)
        while (balanced.isNotEmpty() && "+−-×*÷/^".contains(balanced.last())) {
            balanced = balanced.dropLast(1).trim()
        }
        if (balanced.isBlank()) return null

        return try {
            val cleanExpr = prepareExpression(balanced)
            val tokens = tokenize(cleanExpr)
            val rpn = shuntingYard(tokens)
            val result = evaluateRpn(rpn, angleMode)
            if (result.isNaN() || result.isInfinite()) null else formatResult(result)
        } catch (e: Exception) {
            null
        }
    }

    private fun prepareExpression(expr: String): String {
        // 1. Strip commas (thousands grouping separators) and formatting spaces between digits
        var s = expr
            .replace(",", "")
            .replace(Regex("(?<=\\d)\\s+(?=\\d)"), "")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "PI")
            .replace("e", "E")
            .replace("φ", "PHI")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .replace("asin", "arcsin")
            .replace("acos", "arccos")
            .replace("atan", "arctan")
            .replace("asinh", "arcsinh")
            .replace("acosh", "arccosh")
            .replace("atanh", "arctanh")

        // Handle letter 'x' or 'X' as multiplication (e.g. 7x10% -> 7*10%)
        s = s.replace(Regex("(?<=[0-9)πeφ%])\\s*[xX]\\s*(?=[0-9(πeφ])"), "*")

        // Handle implicit multiplication using precompiled regexes
        s = s.replace(IMPLICIT_REGEX, "$1*$2")
        s = s.replace(NUM_CONST_REGEX, "$1*$2")
        s = s.replace(Regex("\\)\\s*([0-9πeφ])"), ")*$1")
        s = s.replace(Regex("(PI|E|PHI)\\s*([0-9])"), "$1*$2")

        // Implicit multiplication after percent (e.g. 10%7 -> 10%*7, 10%(5) -> 10%*(5))
        s = s.replace(Regex("%\\s*([0-9(PIEPHIsqrtcbrtsincostan])"), "%*$1")

        // Handle percentage addition and subtraction markup/discount:
        // A + B% -> (A + (A * (B%)))
        // A - B% -> (A - (A * (B%)))
        val markupRegex = Regex("(?<=^|[+\\-*/^(])\\s*([0-9.]+|\\([^()]+\\))\\s*([+-])\\s*([0-9.]+|\\([^()]+\\))%")
        var prev = ""
        while (prev != s && markupRegex.containsMatchIn(s)) {
            prev = s
            s = s.replace(markupRegex) { mr ->
                val a = mr.groupValues[1]
                val op = mr.groupValues[2]
                val b = mr.groupValues[3]
                "($a $op ($a * ($b%)))"
            }
        }

        return s
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = ArrayList<String>(expr.length / 2 + 4)
        var i = 0
        val len = expr.length

        while (i < len) {
            val c = expr[i]
            if (c.isWhitespace()) {
                i++
                continue
            }

            if (c.isDigit() || c == '.') {
                val sb = StringBuilder()
                while (i < len && (expr[i].isDigit() || expr[i] == '.' || expr[i] == 'E' && i + 1 < len && (expr[i+1].isDigit() || expr[i+1] == '-' || expr[i+1] == '+'))) {
                    sb.append(expr[i])
                    i++
                }
                tokens.add(sb.toString())
                continue
            }

            if (c.isLetter()) {
                val sb = StringBuilder()
                while (i < len && expr[i].isLetter()) {
                    sb.append(expr[i])
                    i++
                }
                val word = sb.toString()
                tokens.add(word)
                continue
            }

            // Check for negative numbers / unary minus
            if (c == '-') {
                val prev = tokens.lastOrNull()
                val isUnary = prev == null || prev == "(" || isOperator(prev)
                if (isUnary) {
                    tokens.add("NEG")
                    i++
                    continue
                }
            }

            // Check for unary plus (e.g. +5 or 5*+2) - skip harmlessly
            if (c == '+') {
                val prev = tokens.lastOrNull()
                val isUnary = prev == null || prev == "(" || isOperator(prev)
                if (isUnary) {
                    i++
                    continue
                }
            }

            if ("+*/^%!()".contains(c) || c == '-') {
                tokens.add(c.toString())
                i++
                continue
            }

            i++
        }
        return tokens
    }

    private fun isOperator(token: String): Boolean {
        return token in listOf("+", "-", "*", "/", "^", "NEG")
    }

    private fun isFunction(token: String): Boolean {
        return token in listOf(
            "sin", "cos", "tan", "arcsin", "arccos", "arctan",
            "sinh", "cosh", "tanh", "arcsinh", "arccosh", "arctanh",
            "ln", "log", "log10", "log2", "sqrt", "cbrt", "abs", "exp", "floor", "ceil", "round"
        )
    }

    private fun precedence(op: String): Int {
        return when (op) {
            "NEG" -> 5
            "^" -> 4
            "*", "/" -> 3
            "+", "-" -> 2
            else -> 0
        }
    }

    private fun shuntingYard(tokens: List<String>): List<String> {
        val output = ArrayList<String>(tokens.size)
        val stack = Stack<String>()

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null || token == "PI" || token == "E" || token == "PHI" -> {
                    output.add(token)
                }
                token == "!" || token == "%" -> {
                    // Unary postfix operators apply directly to the preceding operand in RPN
                    output.add(token)
                }
                isFunction(token) -> {
                    stack.push(token)
                }
                token == "(" -> {
                    stack.push(token)
                }
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.peek() != "(") {
                        output.add(stack.pop())
                    }
                    if (stack.isNotEmpty() && stack.peek() == "(") {
                        stack.pop()
                    }
                    if (stack.isNotEmpty() && isFunction(stack.peek())) {
                        output.add(stack.pop())
                    }
                }
                isOperator(token) -> {
                    while (stack.isNotEmpty() && isOperator(stack.peek()) &&
                        (precedence(stack.peek()) > precedence(token) ||
                                (precedence(stack.peek()) == precedence(token) && token != "^" && token != "NEG"))
                    ) {
                        output.add(stack.pop())
                    }
                    stack.push(token)
                }
            }
        }

        while (stack.isNotEmpty()) {
            output.add(stack.pop())
        }

        return output
    }

    private fun evaluateRpn(rpn: List<String>, angleMode: AngleMode): Double {
        val stack = Stack<Double>()

        for (token in rpn) {
            when {
                token == "PI" -> stack.push(Math.PI)
                token == "E" -> stack.push(Math.E)
                token == "PHI" -> stack.push(PHI)
                token.toDoubleOrNull() != null -> stack.push(token.toDouble())
                token == "NEG" -> {
                    val a = if (stack.isNotEmpty()) stack.pop() else 0.0
                    stack.push(-a)
                }
                token == "!" -> {
                    val a = if (stack.isNotEmpty()) stack.pop() else 0.0
                    stack.push(factorial(a))
                }
                token == "%" -> {
                    val a = if (stack.isNotEmpty()) stack.pop() else 0.0
                    stack.push(a / 100.0)
                }
                isFunction(token) -> {
                    val a = if (stack.isNotEmpty()) stack.pop() else 0.0
                    val res = when (token) {
                        "sin" -> if (angleMode == AngleMode.DEG) sin(Math.toRadians(a)) else sin(a)
                        "cos" -> if (angleMode == AngleMode.DEG) cos(Math.toRadians(a)) else cos(a)
                        "tan" -> if (angleMode == AngleMode.DEG) tan(Math.toRadians(a)) else tan(a)
                        "arcsin" -> {
                            val v = asin(a)
                            if (angleMode == AngleMode.DEG) Math.toDegrees(v) else v
                        }
                        "arccos" -> {
                            val v = acos(a)
                            if (angleMode == AngleMode.DEG) Math.toDegrees(v) else v
                        }
                        "arctan" -> {
                            val v = atan(a)
                            if (angleMode == AngleMode.DEG) Math.toDegrees(v) else v
                        }
                        "sinh" -> sinh(a)
                        "cosh" -> cosh(a)
                        "tanh" -> tanh(a)
                        "arcsinh" -> ln(a + sqrt(a * a + 1.0))
                        "arccosh" -> if (a < 1.0) throw ArithmeticException("Domain error") else ln(a + sqrt(a * a - 1.0))
                        "arctanh" -> if (abs(a) >= 1.0) throw ArithmeticException("Domain error") else 0.5 * ln((1.0 + a) / (1.0 - a))
                        "ln" -> ln(a)
                        "log", "log10" -> log10(a)
                        "log2" -> ln(a) / ln(2.0)
                        "sqrt" -> sqrt(a)
                        "cbrt" -> cbrt(a)
                        "abs" -> abs(a)
                        "exp" -> exp(a)
                        "floor" -> floor(a)
                        "ceil" -> ceil(a)
                        "round" -> kotlin.math.round(a)
                        else -> a
                    }
                    stack.push(res)
                }
                isOperator(token) -> {
                    val b = if (stack.isNotEmpty()) stack.pop() else 0.0
                    val a = if (stack.isNotEmpty()) stack.pop() else 0.0
                    val res = when (token) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> {
                            if (b == 0.0) throw ArithmeticException("Division by zero")
                            a / b
                        }
                        "^" -> a.pow(b)
                        else -> 0.0
                    }
                    stack.push(res)
                }
            }
        }

        if (stack.size > 1) return Double.NaN
        return if (stack.isNotEmpty()) stack.pop() else 0.0
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n)) return Double.NaN
        if (n > 170) return Double.POSITIVE_INFINITY
        var result = 1.0
        val count = n.toLong()
        for (i in 2..count) {
            result *= i
        }
        return result
    }

    fun formatResult(value: Double): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"

        // Avoid -0.0
        val v = if (abs(value) < 1e-13) 0.0 else value

        if (abs(v) >= 1e12 || (abs(v) > 0 && abs(v) < 1e-6)) {
            val df = DecimalFormat("0.######E0", US_SYMBOLS)
            return df.format(v)
        }

        // Standard clean decimal format
        val df = DecimalFormat("#,##0.##########", US_SYMBOLS)
        return df.format(v)
    }

    fun formatWithoutCommas(value: Double): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"
        val v = if (abs(value) < 1e-13) 0.0 else value
        val df = DecimalFormat("0.##########", US_SYMBOLS)
        return df.format(v)
    }
}


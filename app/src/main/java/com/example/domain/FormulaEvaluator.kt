package com.example.domain

import java.util.Locale
import kotlin.math.*

object FormulaEvaluator {

    private val SUPPORTED_FUNCTIONS = setOf(
        "sqrt", "cbrt", "abs", "round", "ceil", "floor",
        "log", "ln", "log10", "exp",
        "sin", "cos", "tan", "asin", "acos", "atan",
        "min", "max", "pow"
    )

    private val SUPPORTED_CONSTANTS = mapOf(
        "pi" to Math.PI,
        "e" to Math.E
    )

    /**
     * Evaluates a mathematical expression string with given variable values.
     * Supports standard operators (+, -, *, /, ^, %), implicit multiplication,
     * scientific/financial functions, and previous-output chaining.
     */
    fun evaluate(expression: String, variables: Map<String, Double>): Double {
        if (expression.isBlank()) return 0.0
        try {
            val processed = substituteVariables(expression, variables)
            val result = parseExpression(processed)
            return if (result.isNaN() || result.isInfinite()) 0.0 else result
        } catch (e: Exception) {
            return 0.0
        }
    }

    /**
     * Strictly verifies that a formula is syntactically valid, references only known variables
     * or supported mathematical functions/constants, and evaluates to a finite number.
     */
    fun validateFormula(expression: String, variables: Map<String, Double>): Boolean {
        if (expression.isBlank()) return false
        return try {
            val normalizedVars = variables.keys.map { it.lowercase(Locale.ROOT).trim() }.toSet()
            val cleanExpr = normalizeOperators(expression.trim().lowercase(Locale.ROOT))
                .replace("{prev}", "prev")

            // Verify all identifier tokens are known variables, functions, or constants
            val rawTokens = tokenizeRaw(cleanExpr)
            for (token in rawTokens) {
                val first = token.firstOrNull() ?: continue
                if (first.isLetter() || first == '_') {
                    if (token !in normalizedVars &&
                        token !in SUPPORTED_FUNCTIONS &&
                        token !in SUPPORTED_CONSTANTS &&
                        token != "prev"
                    ) {
                        return false
                    }
                }
            }

            // Verify balanced parentheses
            var depth = 0
            for (ch in cleanExpr) {
                if (ch == '(') depth++
                else if (ch == ')') {
                    depth--
                    if (depth < 0) return false
                }
            }
            if (depth != 0) return false

            val processed = substituteVariables(expression, variables)
            val result = parseExpression(processed)
            !result.isNaN() && !result.isInfinite()
        } catch (e: Exception) {
            false
        }
    }

    private fun normalizeOperators(expr: String): String {
        return expr
            .replace("**", "^")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("–", "-")
            .replace("π", "pi")
    }

    private fun substituteVariables(expression: String, variables: Map<String, Double>): String {
        var processed = normalizeOperators(expression.trim().lowercase(Locale.ROOT))

        // Support {prev} placeholder first
        if (variables.containsKey("prev")) {
            val prevVal = variables["prev"] ?: 0.0
            val formattedPrev = if (prevVal.isNaN() || prevVal.isInfinite()) "0.0" else prevVal.toString()
            processed = processed.replace("{prev}", "($formattedPrev)")
        }

        // Sort variables by length descending so longer variable names are replaced first
        val sortedVars = variables.entries.sortedByDescending { it.key.length }
        for ((key, value) in sortedVars) {
            val cleanKey = key.lowercase(Locale.ROOT).trim()
            if (cleanKey.isNotEmpty()) {
                val regex = Regex("\\b${Regex.escape(cleanKey)}\\b")
                val formattedVal = if (value.isNaN() || value.isInfinite()) "0.0" else value.toString()
                processed = processed.replace(regex, "($formattedVal)")
            }
        }

        return processed
    }

    private fun parseExpression(expr: String): Double {
        val rawTokens = tokenizeRaw(expr)
        val tokens = insertImplicitMultiplication(rawTokens)
        val parser = ExpressionParser(tokens)
        return parser.parse()
    }

    private fun tokenizeRaw(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val clean = expr.replace(" ", "")

        while (i < clean.length) {
            val c = clean[i]
            when {
                c in "+-*/^%()," -> {
                    tokens.add(c.toString())
                    i++
                }
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < clean.length && (clean[i].isDigit() || clean[i] == '.')) {
                        sb.append(clean[i])
                        i++
                    }
                    // Handle scientific notation e.g. 1.5e-4 or 1e6
                    if (i < clean.length && (clean[i] == 'e' || clean[i] == 'E')) {
                        val nextIdx = i + 1
                        val hasSciExponent = if (nextIdx < clean.length && (clean[nextIdx] == '+' || clean[nextIdx] == '-')) {
                            nextIdx + 1 < clean.length && clean[nextIdx + 1].isDigit()
                        } else {
                            nextIdx < clean.length && clean[nextIdx].isDigit()
                        }
                        if (hasSciExponent) {
                            sb.append(clean[i])
                            i++
                            if (clean[i] == '+' || clean[i] == '-') {
                                sb.append(clean[i])
                                i++
                            }
                            while (i < clean.length && clean[i].isDigit()) {
                                sb.append(clean[i])
                                i++
                            }
                        }
                    }
                    tokens.add(sb.toString())
                }
                c.isLetter() || c == '_' -> {
                    val sb = StringBuilder()
                    while (i < clean.length && (clean[i].isLetterOrDigit() || clean[i] == '_')) {
                        sb.append(clean[i])
                        i++
                    }
                    tokens.add(sb.toString())
                }
                else -> i++
            }
        }
        return tokens
    }

    private fun isNumberToken(token: String): Boolean {
        return token.firstOrNull()?.let { it.isDigit() || it == '.' } == true
    }

    private fun insertImplicitMultiplication(tokens: List<String>): List<String> {
        if (tokens.size < 2) return tokens
        val result = mutableListOf<String>()
        for (i in tokens.indices) {
            val curr = tokens[i]
            result.add(curr)
            if (i + 1 < tokens.size) {
                val next = tokens[i + 1]
                val currIsValueOrClose = isNumberToken(curr) || curr == ")" || curr in SUPPORTED_CONSTANTS
                val nextIsOpenOrIdent = next == "(" || next in SUPPORTED_FUNCTIONS || next in SUPPORTED_CONSTANTS || isNumberToken(next) && curr == ")"
                if (currIsValueOrClose && nextIsOpenOrIdent) {
                    result.add("*")
                }
            }
        }
        return result
    }

    private class ExpressionParser(private val tokens: List<String>) {
        private var pos = 0

        fun parse(): Double {
            if (tokens.isEmpty()) return 0.0
            val res = parseAdditive()
            return if (res.isNaN() || res.isInfinite()) 0.0 else res
        }

        private fun peek(): String? = if (pos < tokens.size) tokens[pos] else null
        private fun consume(): String = tokens[pos++]

        private fun parseAdditive(): Double {
            var left = parseMultiplicative()
            while (peek() == "+" || peek() == "-") {
                val op = consume()
                val right = parseMultiplicative()
                left = if (op == "+") left + right else left - right
            }
            return left
        }

        private fun parseMultiplicative(): Double {
            var left = parseUnary()
            while (peek() == "*" || peek() == "/" || peek() == "%") {
                val op = consume()
                val right = parseUnary()
                left = when (op) {
                    "*" -> left * right
                    "/" -> if (abs(right) > 1e-15) left / right else 0.0
                    "%" -> if (abs(right) > 1e-15) left % right else 0.0
                    else -> left
                }
            }
            return left
        }

        private fun parseUnary(): Double {
            if (peek() == "+") {
                consume()
                return parseUnary()
            }
            if (peek() == "-") {
                consume()
                return -parseUnary()
            }
            return parseExponential()
        }

        private fun parseExponential(): Double {
            var left = parsePrimary()
            if (peek() == "^") {
                consume()
                val right = parseUnary() // Right-associative & allows 2^-3
                left = left.pow(right)
            }
            return left
        }

        private fun parsePrimary(): Double {
            val token = peek() ?: return 0.0

            if (token == "(") {
                consume() // (
                val expr = parseAdditive()
                if (peek() == ")") consume() // )
                return expr
            }

            if (token in SUPPORTED_CONSTANTS) {
                consume()
                return SUPPORTED_CONSTANTS[token] ?: 0.0
            }

            // Function calls
            if (token in SUPPORTED_FUNCTIONS) {
                val funcName = consume()
                if (peek() == "(") {
                    consume() // (
                    val args = mutableListOf<Double>()
                    if (peek() != ")") {
                        args.add(parseAdditive())
                        while (peek() == ",") {
                            consume()
                            args.add(parseAdditive())
                        }
                    }
                    if (peek() == ")") consume() // )

                    val arg1 = args.getOrElse(0) { 0.0 }
                    val arg2 = args.getOrElse(1) { 0.0 }

                    return when (funcName) {
                        "sqrt" -> if (arg1 >= 0.0) sqrt(arg1) else 0.0
                        "cbrt" -> cbrt(arg1)
                        "abs" -> abs(arg1)
                        "round" -> round(arg1)
                        "ceil" -> ceil(arg1)
                        "floor" -> floor(arg1)
                        "log", "ln" -> if (arg1 > 0.0) ln(arg1) else 0.0
                        "log10" -> if (arg1 > 0.0) log10(arg1) else 0.0
                        "exp" -> exp(arg1)
                        "sin" -> sin(arg1)
                        "cos" -> cos(arg1)
                        "tan" -> tan(arg1)
                        "asin" -> if (arg1 in -1.0..1.0) asin(arg1) else 0.0
                        "acos" -> if (arg1 in -1.0..1.0) acos(arg1) else 0.0
                        "atan" -> atan(arg1)
                        "min" -> if (args.size >= 2) args.minOrNull() ?: arg1 else arg1
                        "max" -> if (args.size >= 2) args.maxOrNull() ?: arg1 else arg1
                        "pow" -> arg1.pow(arg2)
                        else -> arg1
                    }
                }
            }

            // Number
            consume()
            return token.toDoubleOrNull() ?: 0.0
        }
    }
}

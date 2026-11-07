package codestitcher.calcpro.calculation

import java.util.Locale
import kotlin.math.*

object ExpressionEvaluator {

    sealed class EvalResult {
        data class Success(val value: Double, val formatted: String) : EvalResult()
        data class Error(val message: String) : EvalResult()
    }

    fun evaluate(expression: String, isDegreeMode: Boolean = true, lastAnswer: Double = 0.0): EvalResult {
        var trimmed = expression.trim()
        if (trimmed.isEmpty()) return EvalResult.Success(0.0, "0")

        // Normalize Casio fx-991EX symbols
        trimmed = trimmed
            .replace("sin⁻¹", "asin")
            .replace("cos⁻¹", "acos")
            .replace("tan⁻¹", "atan")
            .replace("ANS", "($lastAnswer)")
            .replace("ans", "($lastAnswer)")
            .replace("Ans", "($lastAnswer)")

        return try {
            val parser = Parser(trimmed, isDegreeMode)
            val result = parser.parse()
            if (result.isNaN()) {
                EvalResult.Error("Undefined")
            } else if (result.isInfinite()) {
                EvalResult.Error("Division by zero")
            } else {
                EvalResult.Success(result, formatNumber(result))
            }
        } catch (e: ArithmeticException) {
            EvalResult.Error(e.message ?: "Math error")
        } catch (_: Exception) {
            EvalResult.Error("Invalid syntax")
        }
    }

    fun formatNumber(value: Double): String {
        if (value.isNaN()) return "Undefined"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"

        // If it's effectively an integer
        if (abs(value - value.roundToLong()) < 1e-11) {
            val longVal = value.roundToLong()
            return if (abs(longVal) < 1e14) {
                longVal.toString()
            } else {
                String.format(Locale.US, "%.6e", value).replace("e+", "e")
            }
        }

        // Clean floating point rounding
        val formatted = String.format(Locale.US, "%.10f", value)
            .trimEnd('0')
            .trimEnd('.')

        return if (formatted.length > 14 || (abs(value) < 1e-5 && abs(value) > 0)) {
            String.format(Locale.US, "%.6g", value).replace("e+", "e")
        } else {
            formatted
        }
    }

    private class Parser(val input: String, val isDegrees: Boolean) {
        var pos = -1
        var ch = ' '

        fun nextChar() {
            pos++
            ch = if (pos < input.length) input[pos] else '\u0000'
        }

        fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            while (ch == ' ') nextChar()
            if (pos < input.length) throw IllegalArgumentException("Unexpected char: $ch")
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+') -> x += parseTerm()
                    eat('-') -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*') || eat('×') || eat('x') -> x *= parseFactor()
                    eat('/') || eat('÷') -> {
                        val divisor = parseFactor()
                        if (abs(divisor) < 1e-15) throw ArithmeticException("Division by zero")
                        x /= divisor
                    }
                    eat('P') -> {
                        // Casio nPr (Permutations)
                        val r = parseFactor()
                        x = permutations(x, r)
                    }
                    eat('C') -> {
                        // Casio nCr (Combinations)
                        val r = parseFactor()
                        x = combinations(x, r)
                    }
                    isImplicitMultiplication() -> {
                        x *= parseFactor()
                    }
                    else -> return x
                }
            }
        }

        fun isImplicitMultiplication(): Boolean {
            while (ch == ' ') nextChar()
            return ch == '(' || ch == 'π' || ch == '√' || ch == '∛' || (ch in 'a'..'z') || (ch in 'A'..'Z') || (ch in '0'..'9')
        }

        fun parseFactor(): Double {
            while (ch == ' ') nextChar()
            if (eat('+')) return parseFactor()
            if (eat('-')) return -parseFactor()

            var x: Double
            val startPos = pos

            if (eat('(')) {
                x = parseExpression()
                eat(')')
            } else if ((ch in '0'..'9') || ch == '.') {
                while ((ch in '0'..'9') || ch == '.') nextChar()
                val numStr = input.substring(startPos, pos)
                x = numStr.toDouble()
            } else if ((ch in 'a'..'z') || (ch in 'A'..'Z') || ch == 'π' || ch == '√' || ch == '∛') {
                while ((ch in 'a'..'z') || (ch in 'A'..'Z') || ch == 'π' || ch == '√' || ch == '∛') nextChar()
                val func = input.substring(startPos, pos).lowercase()
                when (func) {
                    "pi", "π" -> x = Math.PI
                    "e" -> x = Math.E
                    else -> {
                        val hasParen = eat('(')
                        val arg = parseExpression()
                        if (hasParen) eat(')')
                        x = when (func) {
                            "sqrt", "√" -> {
                                if (arg < 0) throw ArithmeticException("Domain error")
                                sqrt(arg)
                            }
                            "cbrt", "∛" -> cbrt(arg)
                            "sin" -> {
                                val rad = if (isDegrees) Math.toRadians(arg) else arg
                                sin(rad)
                            }
                            "cos" -> {
                                val rad = if (isDegrees) Math.toRadians(arg) else arg
                                cos(rad)
                            }
                            "tan" -> {
                                val rad = if (isDegrees) Math.toRadians(arg) else arg
                                val c = cos(rad)
                                if (abs(c) < 1e-12) throw ArithmeticException("Undefined")
                                tan(rad)
                            }
                            "asin" -> {
                                if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error")
                                val rad = asin(arg)
                                if (isDegrees) Math.toDegrees(rad) else rad
                            }
                            "acos" -> {
                                if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error")
                                val rad = acos(arg)
                                if (isDegrees) Math.toDegrees(rad) else rad
                            }
                            "atan" -> {
                                val rad = atan(arg)
                                if (isDegrees) Math.toDegrees(rad) else rad
                            }
                            "sinh" -> sinh(arg)
                            "cosh" -> cosh(arg)
                            "tanh" -> tanh(arg)
                            "log" -> {
                                if (arg <= 0) throw ArithmeticException("Domain error")
                                log10(arg)
                            }
                            "ln" -> {
                                if (arg <= 0) throw ArithmeticException("Domain error")
                                ln(arg)
                            }
                            "exp" -> exp(arg)
                            "abs" -> abs(arg)
                            else -> throw IllegalArgumentException("Unknown function: $func")
                        }
                    }
                }
            } else {
                throw IllegalArgumentException("Unexpected: $ch")
            }

            // Postfix operators: `%`, `!`, `²`, `³`, `⁻¹`, `^`
            while (true) {
                when {
                    eat('%') -> x *= 0.01
                    eat('!') -> x = factorial(x)
                    eat('²') -> x = x * x
                    eat('³') -> x = x * x * x
                    eat('⁻') && eat('¹') -> {
                        if (abs(x) < 1e-15) throw ArithmeticException("Division by zero")
                        x = 1.0 / x
                    }
                    eat('^') -> {
                        val exponent = parseFactor()
                        x = x.pow(exponent)
                    }
                    else -> break
                }
            }

            return x
        }

        private fun factorial(n: Double): Double {
            if (n < 0 || abs(n - n.roundToLong()) > 1e-9) {
                throw ArithmeticException("Factorial requires non-negative integer")
            }
            val intN = n.roundToLong()
            if (intN > 170) throw ArithmeticException("Overflow")
            var res = 1.0
            for (i in 2..intN) {
                res *= i
            }
            return res
        }

        private fun permutations(n: Double, r: Double): Double {
            if (n < 0 || r < 0 || r > n || abs(n - n.roundToLong()) > 1e-9 || abs(r - r.roundToLong()) > 1e-9) {
                throw ArithmeticException("nPr requires 0 <= r <= n integers")
            }
            val nLong = n.roundToLong()
            val rLong = r.roundToLong()
            var res = 1.0
            for (i in (nLong - rLong + 1)..nLong) {
                res *= i
            }
            return res
        }

        private fun combinations(n: Double, r: Double): Double {
            if (n < 0 || r < 0 || r > n || abs(n - n.roundToLong()) > 1e-9 || abs(r - r.roundToLong()) > 1e-9) {
                throw ArithmeticException("nCr requires 0 <= r <= n integers")
            }
            val nLong = n.roundToLong()
            var rLong = r.roundToLong()
            if (rLong > nLong - rLong) rLong = nLong - rLong
            var res = 1.0
            for (i in 1..rLong) {
                res = res * (nLong - i + 1) / i
            }
            return res
        }
    }
}

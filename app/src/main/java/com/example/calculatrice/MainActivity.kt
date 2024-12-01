package com.example.calculatrice

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    private lateinit var display: TextView
    private var currentExpression: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        display = findViewById(R.id.display)

        // Boutons numériques
        val numberButtons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )

        numberButtons.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                appendToExpression((it as Button).text.toString())
            }
        }

        // Opérations de base
        findViewById<Button>(R.id.btnPlus).setOnClickListener { appendToExpression("+") }
        findViewById<Button>(R.id.btnMinus).setOnClickListener { appendToExpression("-") }
        findViewById<Button>(R.id.btnMultiply).setOnClickListener { appendToExpression("*") }
        findViewById<Button>(R.id.btnDivide).setOnClickListener { appendToExpression("/") }
        findViewById<Button>(R.id.btnModulo).setOnClickListener { appendToExpression("%") }
        findViewById<Button>(R.id.btnDot).setOnClickListener { appendToExpression(".") }

        // Fonctions scientifiques
        findViewById<Button>(R.id.btnSin).setOnClickListener { applyScientificFunction("sin") }
        findViewById<Button>(R.id.btnCos).setOnClickListener { applyScientificFunction("cos") }
        findViewById<Button>(R.id.btnLog).setOnClickListener { applyScientificFunction("log") }
        findViewById<Button>(R.id.btnSqrt).setOnClickListener { applyScientificFunction("sqrt") }

        // Calcul et nettoyage
        findViewById<Button>(R.id.btnEquals).setOnClickListener { calculateResult() }
        findViewById<Button>(R.id.btnClear).setOnClickListener { clearExpression() }
    }

    private fun appendToExpression(value: String) {
        currentExpression += value
        display.text = currentExpression
    }

    private fun clearExpression() {
        currentExpression = ""
        display.text = "0"
    }

    private fun applyScientificFunction(function: String) {
        try {
            val result = when (function) {
                "sin" -> sin(currentExpression.toDouble())
                "cos" -> cos(currentExpression.toDouble())
                "log" -> log10(currentExpression.toDouble())
                "sqrt" -> sqrt(currentExpression.toDouble())
                else -> throw IllegalArgumentException("Fonction inconnue")
            }
            display.text = result.toString()
            currentExpression = result.toString()
        } catch (e: Exception) {
            display.text = "Erreur"
        }
    }

    private fun calculateResult() {
        try {
            val result = eval(currentExpression)
            display.text = result.toString()
            currentExpression = result.toString()
        } catch (e: Exception) {
            display.text = "Erreur"
        }
    }

    private fun eval(expression: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos].code else -1
            }

            fun eat(charToEat: Char): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat.code) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: " + ch.toChar())
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
                        eat('*') -> x *= parseFactor()
                        eat('/') -> x /= parseFactor()
                        eat('%') -> x %= parseFactor() // Gestion de l'opérateur Modulo
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+')) return parseFactor()
                if (eat('-')) return -parseFactor()

                var x: Double
                val startPos = pos
                if (eat('(')) {
                    x = parseExpression()
                    eat(')')
                } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
                    while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                    x = expression.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }

                return x
            }
        }.parse()
    }

}

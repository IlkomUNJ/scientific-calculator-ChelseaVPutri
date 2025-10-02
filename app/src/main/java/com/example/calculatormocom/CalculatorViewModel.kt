package com.example.calculatormocom

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable

class CalculatorViewModel : ViewModel() {
    var state by mutableStateOf(CalculatorState())
        private set

    fun onAction(action: CalculatorAction) {
        when(action) {
            is CalculatorAction.Number -> enterNumber(action.number.toString())
            is CalculatorAction.Decimal -> enterDecimal()
            is CalculatorAction.Clear -> state = CalculatorState()
            is CalculatorAction.Operation -> enterOperation(action.operation)
            is CalculatorAction.Calculate -> performCalculation()
            is CalculatorAction.Delete -> performDeletion()
            is CalculatorAction.ParenthesesOpen -> enterParenthesis("(")
            is CalculatorAction.ParenthesesClose -> enterParenthesis(")")
        }
    }

    private fun enterNumber(number: String) {
        state = state.copy(equation = state.equation + number)
    }

    private fun enterDecimal() {
        val lastNumber = state.equation.split(Regex("[+\\-×÷]")).last()
        if (state.equation.isNotBlank() && !lastNumber.contains(".")) {
            state = state.copy(equation = state.equation + ".")
        }
    }

    private fun enterOperation(operation: CalculatorOperation) {
        when (operation) {
            is CalculatorOperation.SquareRoot -> {
                state = state.copy(equation = state.equation + "Math.sqrt(")
            }
            // Add cases for sin, cos, and tan
            is CalculatorOperation.Sin -> {
                state = state.copy(equation = state.equation + "Math.sin(Math.PI/180*")
            }
            is CalculatorOperation.Cos -> {
                state = state.copy(equation = state.equation + "Math.cos(Math.PI/180*")
            }
            is CalculatorOperation.Tan -> {
                state = state.copy(equation = state.equation + "Math.tan(Math.PI/180*")
            }
            else -> {
                if (state.equation.isNotBlank() && (state.equation.last().isDigit() || state.equation.last() == ')')) {
                    state = state.copy(equation = state.equation + operation.button)
                }
            }
        }
    }

    private fun performCalculation() {
        if (state.equation.isBlank()) {
            return
        }

        try {
            val expression = state.equation
                .replace('×', '*')
                .replace('÷', '/')

            val rhinoContext: Context = Context.enter()
            rhinoContext.optimizationLevel = -1
            val scriptable: Scriptable = rhinoContext.initStandardObjects()

            val result = rhinoContext.evaluateString(scriptable, expression, "javascript", 1, null)
            val finalResult = result.toString().toBigDecimal().stripTrailingZeros().toPlainString()

            state = state.copy(result = finalResult)

        } catch (e: Exception) {
            state = state.copy(result = "Error")
        } finally {
            Context.exit()
        }
    }

    private fun performDeletion() {
        if (state.equation.isNotBlank()) {
            state = state.copy(equation = state.equation.dropLast(1))
        }
    }

    private fun enterParenthesis(parenthesis: String) {
        state = state.copy(equation = state.equation + parenthesis)
    }
}
package app.sadhan.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.sadhan.core.ExpressionEvaluator
import app.sadhan.core.number
import app.sadhan.ui.CopyButton
import app.sadhan.ui.ErrorText
import app.sadhan.ui.Input
import app.sadhan.ui.ResultCard
import app.sadhan.ui.ToolColumn

@Composable
fun CalculatorScreen() {
    var expression by remember { mutableStateOf("") }
    var history by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    fun calculate() {
        try {
            val result = number(ExpressionEvaluator.evaluate(expression))
            history = "$expression = $result"
            expression = result
            error = null
        } catch (e: IllegalArgumentException) { error = e.message }
    }
    ToolColumn {
        Input("Expression", expression, { expression = it.take(512); error = null })
        Text("Percent divides by 100. Example: 200 × (1 + 10%)")
        ErrorText(error)
        if (history.isNotEmpty()) ResultCard("This session only", history)
        listOf(
            listOf("(", ")", "%", "C"),
            listOf("7", "8", "9", "÷"),
            listOf("4", "5", "6", "×"),
            listOf("1", "2", "3", "−"),
            listOf("0", ".", "⌫", "+"),
        ).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    OutlinedButton(modifier = Modifier.weight(1f), onClick = {
                        error = null
                        when (key) {
                            "C" -> { expression = ""; history = "" }
                            "⌫" -> expression = expression.dropLast(1)
                            else -> if (expression.length < 512) expression += key
                        }
                    }) { Text(key) }
                }
            }
        }
        Button(onClick = ::calculate, modifier = Modifier.fillMaxWidth()) { Text("Calculate") }
        if (expression.isNotBlank()) CopyButton(expression)
    }
}

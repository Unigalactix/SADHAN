package app.sadhan.ui.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import app.sadhan.core.ExpressionEvaluator
import app.sadhan.core.inputNumber
import app.sadhan.core.normalizeExpressionInput
import app.sadhan.ui.ErrorText
import app.sadhan.ui.ResultCard
import app.sadhan.ui.Input
import app.sadhan.ui.copy
import app.sadhan.ui.ReportTabWork

@Composable
fun CalculatorScreen() {
    val context = LocalContext.current
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val decimal = java.text.DecimalFormatSymbols.getInstance(locale).decimalSeparator.toString()
    var expression by remember { mutableStateOf("") }
    var history by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    ReportTabWork("calculator", if (expression.isNotBlank()) "Your calculation will be erased." else null)
    fun calculate() {
        try {
            val result = inputNumber(ExpressionEvaluator.evaluate(normalizeExpressionInput(expression, locale)), locale)
            history = "$expression = $result"
            expression = result
            error = null
        } catch (e: IllegalArgumentException) { error = e.message }
        focus.clearFocus(force = true)
        keyboard?.hide()
    }
    fun key(value: String) {
        error = null
        when (value) {
            "C" -> { expression = ""; history = "" }
            "⌫" -> expression = expression.dropLast(1)
            else -> if (expression.length < 512) expression += value
        }
    }
    val numbers = listOf(listOf("7", "8", "9", "÷"), listOf("4", "5", "6", "×"), listOf("1", "2", "3", "−"), listOf("0", decimal, "⌫", "+"))
    val auxiliary = listOf("(", ")", "%", "C")
    val display: @Composable () -> Unit = {
        Input("Expression", expression, { expression = it.take(512); error = null })
        ErrorText(error)
        Box(Modifier.fillMaxWidth().heightIn(max = 132.dp).verticalScroll(rememberScrollState())) {
            ResultCard("This session only", history.ifBlank { "Ready when you are" })
        }
    }
    val action: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = ::calculate, modifier = Modifier.weight(1f)) { Text("Calculate") }
            IconButton(enabled = expression.isNotBlank(), onClick = { copy(context, expression) }) { Icon(Icons.Default.ContentCopy, "Copy result") }
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().padding(16.dp)) {
        if (maxHeight < 420.dp && maxWidth >= 560.dp) {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    display()
                    CalculatorKeys(listOf(auxiliary), true, ::key)
                    Text("Percent divides by 100.", style = MaterialTheme.typography.labelSmall)
                }
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.weight(1f).verticalScroll(rememberScrollState())) { CalculatorKeys(numbers, true, ::key) }
                    action()
                }
            }
        } else {
            Column(Modifier.widthIn(max = 760.dp).fillMaxWidth().fillMaxHeight().align(Alignment.TopCenter), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                display()
                Box(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                    CalculatorKeys(listOf(auxiliary) + numbers, false, ::key)
                }
                action()
                Text("Percent divides by 100. Open tabs stay in RAM only.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CalculatorKeys(rows: List<List<String>>, compact: Boolean, click: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { key ->
                    val operator = key in listOf("+", "−", "×", "÷")
                    FilledTonalButton(
                        onClick = { click(key) }, modifier = Modifier.weight(1f).height(if (compact) 36.dp else 48.dp),
                        shape = MaterialTheme.shapes.medium, contentPadding = PaddingValues(4.dp),
                        colors = if (operator) ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                            else ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = .8f), contentColor = MaterialTheme.colorScheme.onSurface),
                    ) { Text(key, style = MaterialTheme.typography.titleLarge) }
                }
            }
        }
    }
}

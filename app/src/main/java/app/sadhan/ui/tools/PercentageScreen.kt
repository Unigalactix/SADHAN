package app.sadhan.ui.tools

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import app.sadhan.core.PercentageMode
import app.sadhan.core.number
import app.sadhan.core.percentage
import app.sadhan.ui.*

@Composable
fun PercentageScreen() {
    var mode by remember { mutableStateOf(PercentageMode.OF) }
    var first by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }
    ToolColumn {
        Choice("Calculation", mode.label, PercentageMode.entries.map { it.label }) { label -> mode = PercentageMode.entries.first { it.label == label } }
        Input(when (mode) { PercentageMode.OF -> "Percentage"; PercentageMode.SHARE -> "Part"; else -> "Original value" }, first, { first = it }, true)
        Input(when (mode) { PercentageMode.OF -> "Amount"; PercentageMode.SHARE -> "Whole"; else -> "New value" }, second, { second = it }, true)
        if (first.isNotBlank() && second.isNotBlank()) {
            var error: String? = null
            val result = try {
                percentage(mode, first.toDoubleOrNull() ?: throw IllegalArgumentException("Enter a valid first number"), second.toDoubleOrNull() ?: throw IllegalArgumentException("Enter a valid second number"))
            } catch (e: IllegalArgumentException) { error = e.message; null }
            ErrorText(error)
            result?.let { ResultCard("Result", number(it) + if (mode == PercentageMode.OF) "" else "%") }
        }
        Text("Percentage change uses the absolute original value as its denominator.")
    }
}

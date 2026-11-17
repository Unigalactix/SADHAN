package app.sadhan.ui.tools

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import app.sadhan.core.randomInteger
import app.sadhan.core.toInputIntOrNull
import app.sadhan.ui.*

@Composable
fun RandomPickerScreen() {
    var mode by remember { mutableStateOf("List picker") }
    var choices by remember { mutableStateOf("") }
    var minimum by remember { mutableStateOf("1") }
    var maximum by remember { mutableStateOf("100") }
    var result by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    ToolColumn {
        Choice("Mode", mode, listOf("List picker", "Number", "Dice")) { mode = it; result = null; error = null }
        when (mode) {
            "List picker" -> Input("One option per line", choices, { choices = it }, singleLine = false)
            "Number" -> {
                Input("Minimum (whole number)", minimum, { minimum = it }, true)
                Input("Maximum (whole number)", maximum, { maximum = it }, true)
            }
            else -> Text("Roll a six-sided die.")
        }
        Button(onClick = {
            try {
                error = null
                result = when (mode) {
                    "List picker" -> {
                        val items = choices.lines().map(String::trim).filter(String::isNotEmpty)
                        require(items.isNotEmpty()) { "Enter at least one option" }
                        items[randomInteger(0, items.lastIndex)]
                    }
                    "Number" -> randomInteger(minimum.toInputIntOrNull() ?: throw IllegalArgumentException("Invalid minimum"), maximum.toInputIntOrNull() ?: throw IllegalArgumentException("Invalid maximum")).toString()
                    else -> randomInteger(1, 6).toString()
                }
            } catch (e: IllegalArgumentException) { error = e.message }
        }) { Text(if (mode == "Dice") "Roll dice" else "Pick randomly") }
        ErrorText(error)
        result?.let { ResultCard("Your pick", it) }
    }
}

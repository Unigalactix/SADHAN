package app.sadhan.ui.tools

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.sadhan.core.convert
import app.sadhan.core.inputNumber
import app.sadhan.core.toInputDoubleOrNull
import app.sadhan.core.unitCategories
import app.sadhan.ui.Choice
import app.sadhan.ui.ErrorText
import app.sadhan.ui.Input
import app.sadhan.ui.ResultCard
import app.sadhan.ui.ToolColumn

@Composable
fun UnitConverterScreen() {
    var category by remember { mutableStateOf(unitCategories.first()) }
    var from by remember { mutableStateOf(category.factors.keys.first()) }
    var to by remember { mutableStateOf(category.factors.keys.elementAt(1)) }
    var input by remember { mutableStateOf("1") }
    ToolColumn {
        Choice("Category", category.name, unitCategories.map { it.name }) { name ->
            category = unitCategories.first { it.name == name }
            from = category.factors.keys.first()
            to = category.factors.keys.elementAt(1)
        }
        Input("Value", input, { input = it }, true)
        Choice("From", from, category.factors.keys.toList()) { from = it }
        OutlinedButton(onClick = { val previous = from; from = to; to = previous }) { Text("Swap units") }
        Choice("To", to, category.factors.keys.toList()) { to = it }
        if (input.isNotBlank()) {
            var failure: String? = null
            val result = try {
                val value = input.toInputDoubleOrNull() ?: throw IllegalArgumentException("Enter a valid number")
                inputNumber(convert(value, category, from, to))
            } catch (e: IllegalArgumentException) { failure = e.message; null }
            ErrorText(failure)
            if (result != null) ResultCard(to, result)
        }
    }
}

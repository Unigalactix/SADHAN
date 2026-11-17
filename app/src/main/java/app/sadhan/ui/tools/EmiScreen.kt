package app.sadhan.ui.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.sadhan.core.loan
import app.sadhan.core.money
import app.sadhan.ui.ErrorText
import app.sadhan.ui.Input
import app.sadhan.ui.ResultCard
import app.sadhan.ui.ToolColumn

@Composable
fun EmiScreen() {
    var principal by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("8") }
    var months by remember { mutableStateOf("12") }
    ToolColumn {
        Input("Principal", principal, { principal = it }, true)
        Input("Annual interest rate (%)", rate, { rate = it }, true)
        Input("Months (1-1200)", months, { months = it }, true)
        if (principal.isNotBlank()) {
            var failure: String? = null
            val result = try {
                loan(
                    principal.toDoubleOrNull() ?: throw IllegalArgumentException("Enter a valid principal"),
                    rate.toDoubleOrNull() ?: throw IllegalArgumentException("Enter a valid rate"),
                    months.toIntOrNull() ?: throw IllegalArgumentException("Enter whole months"),
                )
            } catch (e: IllegalArgumentException) {
                failure = e.message
                null
            }
            ErrorText(failure)
            if (result != null) {
                ResultCard("Monthly EMI", money(result.monthly))
                ResultCard("Total interest", money(result.interest))
                ResultCard("Total payable", money(result.total))
            }
        }
    }
}

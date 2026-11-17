package app.sadhan.ui.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.sadhan.core.money
import app.sadhan.core.toInputDoubleOrNull
import app.sadhan.core.toInputIntOrNull
import app.sadhan.ui.ErrorText
import app.sadhan.ui.Input
import app.sadhan.ui.ResultCard
import app.sadhan.ui.ToolColumn

@Composable
fun TipSplitScreen() {
    var bill by remember { mutableStateOf("") }
    var tip by remember { mutableStateOf("10") }
    var people by remember { mutableStateOf("2") }
    val amount = bill.toInputDoubleOrNull()
    val percent = tip.toInputDoubleOrNull()
    val count = people.toInputIntOrNull()
    val valid = amount != null && amount.isFinite() && amount >= 0 &&
        percent != null && percent.isFinite() && percent >= 0 && count != null && count > 0
    ToolColumn {
        Input("Bill amount", bill, { bill = it }, true)
        Input("Tip (%)", tip, { tip = it }, true)
        Input("People", people, { people = it }, true)
        if (valid) {
            val gratuity = amount * percent / 100
            val total = amount + gratuity
            if (total.isFinite()) {
                ResultCard("Per person", money(total / count))
                ResultCard("Tip total", money(gratuity))
                ResultCard("Bill with tip", money(total))
            } else ErrorText("Amount is too large")
        } else if (bill.isNotBlank()) ErrorText("Enter non-negative amounts and a positive whole number of people")
    }
}

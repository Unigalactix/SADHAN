package app.sadhan.ui.tools

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import app.sadhan.core.discount
import app.sadhan.core.money
import app.sadhan.ui.*

@Composable
fun DiscountScreen() {
    var price by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("10") }
    var tax by remember { mutableStateOf("0") }
    ToolColumn {
        Input("Original price", price, { price = it }, true)
        Input("Discount (%)", rate, { rate = it }, true)
        Input("Tax after discount (%)", tax, { tax = it }, true)
        if (price.isNotBlank()) {
            var error: String? = null
            val result = try {
                discount(price.toDoubleOrNull() ?: throw IllegalArgumentException("Enter a valid price"), rate.toDoubleOrNull() ?: throw IllegalArgumentException("Enter a valid discount"), tax.toDoubleOrNull() ?: throw IllegalArgumentException("Enter a valid tax rate"))
            } catch (e: IllegalArgumentException) { error = e.message; null }
            ErrorText(error)
            result?.let {
                ResultCard("You pay", money(it.total))
                ResultCard("Savings", money(it.savings))
                ResultCard("Discounted subtotal / tax", "${money(it.subtotal)} / ${money(it.tax)}")
            }
        }
        Text("Tax is applied after the discount. Estimates only; local tax and rounding rules may differ.")
    }
}

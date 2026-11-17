package app.sadhan.ui.tools

import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import app.sadhan.core.currencies
import app.sadhan.core.currencyRate
import app.sadhan.core.money
import app.sadhan.data.CurrencyRepository
import app.sadhan.data.ExchangeRates
import app.sadhan.ui.Choice
import app.sadhan.ui.ErrorText
import app.sadhan.ui.Input
import app.sadhan.ui.ResultCard
import app.sadhan.ui.ToolColumn
import kotlinx.coroutines.launch
import org.json.JSONException
import java.io.IOException

@Composable
fun CurrencyScreen() {
    var amount by remember { mutableStateOf("1") }
    var from by remember { mutableStateOf("USD") }
    var to by remember { mutableStateOf("INR") }
    var rates by remember { mutableStateOf<ExchangeRates?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    suspend fun refresh() {
        busy = true
        error = null
        rates = null
        try { rates = CurrencyRepository.latest() }
        catch (e: IOException) { error = "Rates unavailable: ${e.message}" }
        catch (e: JSONException) { error = "Invalid rate response: ${e.message}" }
        finally { busy = false }
    }
    LaunchedEffect(Unit) { refresh() }
    ToolColumn {
        Input("Amount", amount, { amount = it }, true)
        Choice("From", from, currencies) { from = it }
        OutlinedButton(onClick = { val previous = from; from = to; to = previous }) { Text("Swap currencies") }
        Choice("To", to, currencies) { to = it }
        if (busy) CircularProgressIndicator()
        ErrorText(error)
        rates?.let { table ->
            var failure: String? = null
            val converted = try {
                val value = amount.toDoubleOrNull() ?: throw IllegalArgumentException("Enter a valid amount")
                require(value.isFinite() && value >= 0) { "Enter a non-negative finite amount" }
                val result = value * currencyRate(table.usdRates, from, to)
                require(result.isFinite()) { "Amount is too large" }
                money(result)
            } catch (e: IllegalArgumentException) { failure = e.message; null }
            ErrorText(failure)
            if (converted != null) ResultCard(to, converted)
            Text("Reference rates dated ${table.date}. Indicative only, not bank or live trading rates.")
        }
        if (from == "AED" || to == "AED") Text("AED estimate: fixed USD peg of 1 USD = 3.6725 AED. Frankfurter does not supply an AED market rate.")
        Button(enabled = !busy, onClick = { scope.launch { refresh() } }) { Text("Refresh rates") }
        Text("Rates: Frankfurter / European Central Bank. Kept only in memory.")
    }
}

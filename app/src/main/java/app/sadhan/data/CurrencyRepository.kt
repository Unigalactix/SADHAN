package app.sadhan.data

import app.sadhan.core.Network

data class ExchangeRates(val date: String, val usdRates: Map<String, Double>)

object CurrencyRepository {
    suspend fun latest(): ExchangeRates {
        val data = Network.json("https://api.frankfurter.app/latest?from=USD")
        val rates = data.getJSONObject("rates")
        return ExchangeRates(data.getString("date"), rates.keys().asSequence().associateWith { rates.getDouble(it) })
    }
}

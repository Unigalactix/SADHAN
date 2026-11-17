package app.sadhan.core

import java.math.BigDecimal
import java.math.MathContext
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.expm1
import kotlin.math.ln1p

object ExpressionEvaluator {
    fun evaluate(expression: String): Double {
        require(expression.length <= 512) { "Expression is too long" }
        val source = expression.replace('×', '*').replace('÷', '/').replace('−', '-')
        var index = 0
        fun spaces() { while (index < source.length && source[index].isWhitespace()) index++ }
        fun take(char: Char): Boolean {
            spaces()
            return if (index < source.length && source[index] == char) { index++; true } else false
        }
        lateinit var sum: () -> Double
        lateinit var factor: () -> Double
        factor = {
            spaces()
            var value = when {
                take('+') -> factor()
                take('-') -> -factor()
                take('(') -> sum().also { require(take(')')) { "Missing closing parenthesis" } }
                else -> {
                    spaces()
                    val start = index
                    while (index < source.length && (source[index].isDigit() || source[index] == '.')) index++
                    require(index > start) { "Expected a number" }
                    source.substring(start, index).toDoubleOrNull()
                        ?: throw IllegalArgumentException("Invalid number")
                }
            }
            while (take('%')) value /= 100
            value
        }
        fun product(): Double {
            var value = factor()
            while (true) {
                value = when {
                    take('*') -> value * factor()
                    take('/') -> {
                        val divisor = factor()
                        require(divisor != 0.0) { "Cannot divide by zero" }
                        value / divisor
                    }
                    else -> return value
                }
            }
        }
        sum = {
            var value = product()
            var running = true
            while (running) {
                when {
                    take('+') -> value += product()
                    take('-') -> value -= product()
                    else -> running = false
                }
            }
            value
        }
        val result = sum()
        spaces()
        require(index == source.length) { "Unexpected character at ${index + 1}" }
        require(result.isFinite()) { "Result is too large" }
        return result
    }
}

fun number(value: Double): String {
    require(value.isFinite()) { "Result is too large" }
    return BigDecimal.valueOf(if (value == 0.0) 0.0 else value)
        .round(MathContext(12)).stripTrailingZeros().toPlainString()
}

fun money(value: Double): String = String.format(Locale.getDefault(), "%,.2f", value)

data class LoanResult(val monthly: Double, val interest: Double, val total: Double)

fun loan(principal: Double, annualRate: Double, months: Int): LoanResult {
    require(principal.isFinite() && principal > 0) { "Principal must be positive" }
    require(annualRate.isFinite() && annualRate >= 0) { "Rate cannot be negative" }
    require(months in 1..1200) { "Months must be between 1 and 1200" }
    val rate = annualRate / 1200
    val payment = if (rate == 0.0) principal / months
        else principal * rate / -expm1(-months * ln1p(rate))
    val total = payment * months
    require(total.isFinite()) { "Amount is too large" }
    return LoanResult(payment, (total - principal).coerceAtLeast(0.0), total)
}

fun age(start: LocalDate, end: LocalDate): Period {
    require(!start.isAfter(end)) { "Start date must not be after end date" }
    return Period.between(start, end)
}

fun daysBetween(start: LocalDate, end: LocalDate): Long = ChronoUnit.DAYS.between(start, end)

data class UnitCategory(val name: String, val factors: Map<String, Double>)

val unitCategories = listOf(
    UnitCategory("Length", linkedMapOf("Metres" to 1.0, "Kilometres" to 1000.0, "Centimetres" to .01, "Millimetres" to .001, "Miles" to 1609.344, "Yards" to .9144, "Feet" to .3048, "Inches" to .0254)),
    UnitCategory("Weight", linkedMapOf("Kilograms" to 1.0, "Grams" to .001, "Milligrams" to .000001, "Tonnes" to 1000.0, "Pounds" to .45359237, "Ounces" to .028349523125)),
    UnitCategory("Temperature", linkedMapOf("Celsius" to 1.0, "Fahrenheit" to 1.0, "Kelvin" to 1.0)),
    UnitCategory("Data", linkedMapOf("Bytes" to 1.0, "Bits" to .125, "KB" to 1000.0, "MB" to 1e6, "GB" to 1e9, "TB" to 1e12, "KiB" to 1024.0, "MiB" to 1048576.0, "GiB" to 1073741824.0)),
    UnitCategory("Area", linkedMapOf("Square metres" to 1.0, "Square kilometres" to 1e6, "Square feet" to .09290304, "Acres" to 4046.8564224, "Hectares" to 10000.0)),
    UnitCategory("Speed", linkedMapOf("Metres/second" to 1.0, "Kilometres/hour" to 1 / 3.6, "Miles/hour" to .44704, "Knots" to .514444444444)),
)

fun convert(value: Double, category: UnitCategory, from: String, to: String): Double {
    require(value.isFinite()) { "Enter a finite number" }
    require(from in category.factors && to in category.factors) { "Unknown unit" }
    val result = if (category.name == "Temperature") {
        val celsius = when (from) {
            "Fahrenheit" -> (value - 32) * 5 / 9
            "Kelvin" -> value - 273.15
            else -> value
        }
        require(celsius >= -273.1500001) { "Temperature is below absolute zero" }
        when (to) {
            "Fahrenheit" -> celsius * 9 / 5 + 32
            "Kelvin" -> celsius + 273.15
            else -> celsius
        }
    } else value * category.factors.getValue(from) / category.factors.getValue(to)
    require(result.isFinite()) { "Result is too large" }
    return result
}

val currencies = listOf("USD", "EUR", "GBP", "INR", "AED", "JPY", "AUD", "CAD", "SGD", "CHF", "CNY", "NZD", "ZAR", "BRL", "KRW")

fun currencyRate(usdRates: Map<String, Double>, from: String, to: String): Double {
    fun rate(code: String): Double = when (code) {
        "USD" -> 1.0
        "AED" -> 3.6725
        else -> usdRates[code]?.takeIf { it.isFinite() && it > 0 }
            ?: throw IllegalArgumentException("Rate unavailable for $code")
    }
    return rate(to) / rate(from)
}

fun youtubeId(input: String): String? {
    val text = input.trim()
    if (text.matches(Regex("[A-Za-z0-9_-]{11}"))) return text
    val uri = try { java.net.URI(text) } catch (_: java.net.URISyntaxException) { return null }
    if (uri.scheme != "https" && uri.scheme != "http") return null
    val host = uri.host?.lowercase(Locale.ROOT) ?: return null
    val id = when (host) {
        "youtu.be" -> uri.path.trim('/').substringBefore('/')
        "youtube.com", "www.youtube.com", "m.youtube.com", "youtube-nocookie.com", "www.youtube-nocookie.com" -> {
            if (uri.path == "/watch") uri.rawQuery?.split('&')?.firstOrNull { it.startsWith("v=") }?.removePrefix("v=")
            else uri.path.split('/').let { parts ->
                if (parts.size >= 3 && parts[1] in listOf("embed", "shorts", "live")) parts[2] else null
            }
        }
        else -> null
    }
    return id?.takeIf { it.matches(Regex("[A-Za-z0-9_-]{11}")) }
}

package app.sadhan.core

import java.math.BigInteger
import java.net.URI
import java.net.URLEncoder
import java.security.SecureRandom

enum class PercentageMode(val label: String) { OF("Percentage of amount"), SHARE("Part as percentage"), CHANGE("Percentage change") }

fun percentage(mode: PercentageMode, first: Double, second: Double): Double {
    require(first.isFinite() && second.isFinite()) { "Enter finite numbers" }
    val result = when (mode) {
        PercentageMode.OF -> first / 100 * second
        PercentageMode.SHARE -> { require(second != 0.0) { "Whole cannot be zero" }; first / second * 100 }
        PercentageMode.CHANGE -> { require(first != 0.0) { "Original cannot be zero" }; (second - first) / kotlin.math.abs(first) * 100 }
    }
    require(result.isFinite()) { "Result is too large" }
    return result
}

data class DiscountResult(val savings: Double, val subtotal: Double, val tax: Double, val total: Double)

fun discount(price: Double, percent: Double, taxRate: Double): DiscountResult {
    require(price.isFinite() && price >= 0 && percent.isFinite() && percent in 0.0..100.0 && taxRate.isFinite() && taxRate >= 0) { "Use a non-negative price/tax and a discount from 0 to 100%" }
    val savings = price * (percent / 100)
    val subtotal = price - savings
    val tax = subtotal * (taxRate / 100)
    require((subtotal + tax).isFinite()) { "Amount is too large" }
    return DiscountResult(savings, subtotal, tax, subtotal + tax)
}

private val secureRandom = SecureRandom()

fun randomInteger(minimum: Int, maximum: Int): Int {
    require(minimum <= maximum) { "Minimum must not exceed maximum" }
    val bound = BigInteger.valueOf(maximum.toLong() - minimum + 1)
    var value: BigInteger
    do { value = BigInteger(bound.bitLength(), secureRandom) } while (value >= bound)
    return (value.toLong() + minimum).toInt()
}

fun securePassword(length: Int, upper: Boolean, lower: Boolean, digits: Boolean, symbols: Boolean): String {
    require(length in 12..64) { "Choose a length from 12 to 64" }
    val groups = buildList {
        if (upper) add("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
        if (lower) add("abcdefghijklmnopqrstuvwxyz")
        if (digits) add("0123456789")
        if (symbols) add("!@#$%^&*()-_=+[]{}:,.?")
    }
    require(groups.isNotEmpty()) { "Select at least one character type" }
    val alphabet = groups.joinToString("")
    val output = groups.map { it[secureRandom.nextInt(it.length)] }.toMutableList()
    while (output.size < length) output.add(alphabet[secureRandom.nextInt(alphabet.length)])
    for (index in output.lastIndex downTo 1) {
        val other = secureRandom.nextInt(index + 1)
        val previous = output[index]; output[index] = output[other]; output[other] = previous
    }
    return output.joinToString("")
}

fun browserDestination(input: String): String {
    val text = input.trim()
    require(text.isNotEmpty() && text.length <= 4096) { "Enter a web address or search query (up to 4096 characters)" }
    if (text.startsWith("https://", true)) {
        val uri = try { URI(text) } catch (_: java.net.URISyntaxException) { throw IllegalArgumentException("Invalid web address") }
        require(!uri.host.isNullOrBlank() && uri.userInfo == null) { "Enter a valid HTTPS address without embedded credentials" }
        return uri.toASCIIString()
    }
    require(!Regex("^[A-Za-z][A-Za-z0-9+.-]*:").containsMatchIn(text)) { "Only secure HTTPS web addresses are supported" }
    if (Regex("^[A-Za-z0-9][A-Za-z0-9.-]+\\.[A-Za-z]{2,}(?::[0-9]+)?(?:[/#?].*)?$").matches(text)) return browserDestination("https://$text")
    return "https://duckduckgo.com/?q=${URLEncoder.encode(text, "UTF-8")}"
}

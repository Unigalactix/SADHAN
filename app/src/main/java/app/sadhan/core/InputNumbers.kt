package app.sadhan.core

import java.math.BigDecimal
import java.text.DecimalFormatSymbols
import java.util.Locale

private fun normalizedInput(value: String, locale: Locale): String? {
    val symbols = DecimalFormatSymbols.getInstance(locale)
    val text = value.trim().replace('−', '-')
    if (text.isEmpty()) return null
    val ascii = buildString {
        text.forEach { char -> append(if (Character.digit(char, 10) >= 0) ('0'.code + Character.digit(char, 10)).toChar() else char) }
    }
    val exponent = ascii.indexOfFirst { it == 'e' || it == 'E' }
    val mantissa = if (exponent >= 0) ascii.take(exponent) else ascii
    val suffix = if (exponent >= 0) ascii.substring(exponent) else ""
    if (suffix.isNotEmpty() && !Regex("[eE][+-]?[0-9]+").matches(suffix)) return null
    val sign = mantissa.takeWhile { it == '+' || it == '-' }
    if (sign.length > 1) return null
    val unsigned = mantissa.removePrefix(sign)
    val parts = unsigned.split(symbols.decimalSeparator)
    if (parts.size > 2) return null
    val whole = parts[0]
    val fraction = parts.getOrNull(1)
    if (fraction != null && fraction.any { it !in '0'..'9' }) return null
    val groups = whole.split(symbols.groupingSeparator)
    if (groups.any { group -> group.any { it !in '0'..'9' } }) return null
    if (groups.size > 1) {
        val western = groups[0].length in 1..3 && groups.drop(1).all { it.length == 3 }
        val indian = locale.country == "IN" && groups.last().length == 3 &&
            groups.first().length in 1..2 && groups.drop(1).dropLast(1).all { it.length == 2 }
        if (!western && !indian) return null
    }
    if (whole.isEmpty() && fraction.isNullOrEmpty()) return null
    val digits = groups.joinToString("")
    return sign + digits.ifEmpty { "0" } + if (fraction != null) ".$fraction$suffix" else suffix
}

fun String.toInputDoubleOrNull(locale: Locale = Locale.getDefault()): Double? =
    normalizedInput(this, locale)?.toDoubleOrNull()?.takeIf(Double::isFinite)

fun String.toInputIntOrNull(locale: Locale = Locale.getDefault()): Int? {
    val normalized = normalizedInput(this, locale) ?: return null
    return try { BigDecimal(normalized).intValueExact() }
    catch (_: NumberFormatException) { null }
    catch (_: ArithmeticException) { null }
}

fun inputNumber(value: Double, locale: Locale = Locale.getDefault()): String {
    val symbols = DecimalFormatSymbols.getInstance(locale)
    return number(value).map { char ->
        when {
            char == '.' -> symbols.decimalSeparator
            char in '0'..'9' -> (symbols.zeroDigit.code + char.code - '0'.code).toChar()
            else -> char
        }
    }.joinToString("")
}

fun normalizeExpressionInput(value: String, locale: Locale = Locale.getDefault()): String {
    val symbols = DecimalFormatSymbols.getInstance(locale)
    val output = StringBuilder()
    var index = 0
    while (index < value.length) {
        val char = value[index]
        if (Character.digit(char, 10) >= 0 || char == symbols.decimalSeparator) {
            val start = index++
            while (index < value.length && (Character.digit(value[index], 10) >= 0 || value[index] == symbols.decimalSeparator || value[index] == symbols.groupingSeparator)) index++
            output.append(normalizedInput(value.substring(start, index), locale)
                ?: throw IllegalArgumentException("Invalid number for ${locale.displayName}"))
        } else {
            require(char != '.' || symbols.decimalSeparator == '.') { "Use '${symbols.decimalSeparator}' as the decimal separator" }
            output.append(char)
            index++
        }
    }
    return output.toString()
}

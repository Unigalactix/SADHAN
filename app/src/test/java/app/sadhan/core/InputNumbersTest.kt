package app.sadhan.core

import org.junit.Assert.*
import org.junit.Test
import java.text.DecimalFormatSymbols
import java.util.Locale

class InputNumbersTest {
    @Test fun localizedDecimalsAndGroupingAreStrictAndComplete() {
        assertEquals(1234.5, "1,234.5".toInputDoubleOrNull(Locale.US)!!, 0.0)
        assertEquals(1234.5, "1.234,5".toInputDoubleOrNull(Locale.GERMANY)!!, 0.0)
        val group = DecimalFormatSymbols.getInstance(Locale.FRANCE).groupingSeparator
        assertEquals(1234.5, "1${group}234,5".toInputDoubleOrNull(Locale.FRANCE)!!, 0.0)
        assertEquals(1234567.5, "12,34,567.5".toInputDoubleOrNull(Locale("en", "IN"))!!, 0.0)
        listOf("1,5", "1,23,4", "1,000x", "NaN", "Infinity", "", "--1", "1e").forEach { assertNull(it, it.toInputDoubleOrNull(Locale.US)) }
    }

    @Test fun integersNeverRoundAndSupportLocalizedDigits() {
        assertEquals(1200, "1.200".toInputIntOrNull(Locale.GERMANY))
        assertEquals(12, "١٢".toInputIntOrNull(Locale("ar", "EG")))
        assertNull("1,5".toInputIntOrNull(Locale.GERMANY))
        assertNull("2147483648".toInputIntOrNull(Locale.US))
    }

    @Test fun calculatorUsesLocaleNumbersAndKeepsOperators() {
        val normalized = normalizeExpressionInput("1,5 × (2 + 0,5)", Locale.GERMANY)
        assertEquals(3.75, ExpressionEvaluator.evaluate(normalized), .00001)
        assertEquals("3,75", inputNumber(3.75, Locale.GERMANY))
        assertEquals(-1.5, "−1,5".toInputDoubleOrNull(Locale.GERMANY)!!, 0.0)
        assertThrows(IllegalArgumentException::class.java) { normalizeExpressionInput("1,23 + 4", Locale.US) }
    }
}

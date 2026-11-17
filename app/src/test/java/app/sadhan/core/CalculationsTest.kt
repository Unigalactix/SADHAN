package app.sadhan.core

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class CalculationsTest {
    @Test fun expressionsRespectPrecedenceAndPercent() {
        assertEquals(14.0, ExpressionEvaluator.evaluate("2 + 3 × 4"), 0.000001)
        assertEquals(220.0, ExpressionEvaluator.evaluate("200 × (1 + 10%)"), 0.000001)
        assertEquals(-1.5, ExpressionEvaluator.evaluate("−(2 + 1) ÷ 2"), 0.000001)
        assertEquals(0.75, ExpressionEvaluator.evaluate(".5 + .25"), 0.000001)
    }

    @Test fun invalidExpressionsFailExplicitly() {
        listOf("", "1/0", "2..2", "1 +", "(2+3", "2abc", "1e5", "(".repeat(513)).forEach {
            assertThrows("Expected invalid expression: $it", IllegalArgumentException::class.java) { ExpressionEvaluator.evaluate(it) }
        }
    }

    @Test fun loanSupportsZeroAndTypicalInterest() {
        assertEquals(1000.0, loan(12000.0, 0.0, 12).monthly, 0.000001)
        val result = loan(100000.0, 12.0, 12)
        assertEquals(8884.8789, result.monthly, 0.001)
        assertEquals(result.monthly * 12 - 100000, result.interest, 0.000001)
        assertThrows(IllegalArgumentException::class.java) { loan(100.0, -1.0, 12) }
        assertThrows(IllegalArgumentException::class.java) { loan(100.0, 1.0, 0) }
    }

    @Test fun conversionHandlesOffsetsBinaryUnitsAndInvalidTemperatures() {
        val temp = unitCategories.first { it.name == "Temperature" }
        assertEquals(32.0, convert(0.0, temp, "Celsius", "Fahrenheit"), 0.000001)
        assertEquals(0.0, convert(-273.15, temp, "Celsius", "Kelvin"), 0.000001)
        assertThrows(IllegalArgumentException::class.java) { convert(-1.0, temp, "Kelvin", "Celsius") }
        val data = unitCategories.first { it.name == "Data" }
        assertEquals(1048576.0, convert(1.0, data, "MiB", "Bytes"), 0.0)
        assertEquals(0.125, convert(1.0, data, "Bits", "Bytes"), 0.0)
    }

    @Test fun datesHandleLeapYearsAndOrdering() {
        val start = LocalDate.of(2000, 2, 29)
        val end = LocalDate.of(2024, 2, 29)
        assertEquals(24, age(start, end).years)
        assertEquals(2L, daysBetween(LocalDate.of(2024, 2, 28), LocalDate.of(2024, 3, 1)))
        assertThrows(IllegalArgumentException::class.java) { age(end, start) }
    }

    @Test fun currenciesUseCrossRatesAndClearlyDefinedPeg() {
        assertEquals(90.0, currencyRate(mapOf("EUR" to 0.9, "INR" to 81.0), "EUR", "INR"), 0.000001)
        assertEquals(3.6725, currencyRate(emptyMap(), "USD", "AED"), 0.000001)
        assertEquals(1.0, currencyRate(emptyMap(), "AED", "AED"), 0.0)
        assertThrows(IllegalArgumentException::class.java) { currencyRate(emptyMap(), "USD", "INR") }
    }

    @Test fun youtubeIdsValidateHostsAndFormats() {
        val id = "dQw4w9WgXcQ"
        listOf(id, "https://youtu.be/$id?t=10", "https://www.youtube.com/watch?v=$id&x=1", "https://m.youtube.com/shorts/$id").forEach {
            assertEquals(id, youtubeId(it))
        }
        listOf("https://evil.example/watch?v=$id", "https://youtube.com.evil.example/watch?v=$id", "javascript:alert(1)", "short").forEach { assertNull(youtubeId(it)) }
    }
}

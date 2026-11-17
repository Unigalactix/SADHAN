package app.sadhan.core

import org.junit.Assert.*
import org.junit.Test

class UtilitiesTest {
    @Test fun percentagesAndDiscountsHandleLimitsAndZeroBases() {
        assertEquals(25.0, percentage(PercentageMode.OF, 10.0, 250.0), .00001)
        assertEquals(25.0, percentage(PercentageMode.SHARE, 1.0, 4.0), .00001)
        assertEquals(-20.0, percentage(PercentageMode.CHANGE, 100.0, 80.0), .00001)
        assertThrows(IllegalArgumentException::class.java) { percentage(PercentageMode.CHANGE, 0.0, 10.0) }
        val sale = discount(100.0, 20.0, 10.0)
        assertEquals(88.0, sale.total, .00001)
        assertEquals(20.0, sale.savings, .00001)
        assertThrows(IllegalArgumentException::class.java) { discount(1.0, 101.0, 0.0) }
    }

    @Test fun passwordsIncludeEveryRequestedClassAndRespectSelections() {
        repeat(30) {
            val all = securePassword(20, true, true, true, true)
            assertEquals(20, all.length)
            assertTrue(all.any(Char::isUpperCase) && all.any(Char::isLowerCase) && all.any(Char::isDigit) && all.any { !it.isLetterOrDigit() })
            assertTrue(securePassword(12, false, false, true, false).all(Char::isDigit))
        }
        assertThrows(IllegalArgumentException::class.java) { securePassword(12, false, false, false, false) }
    }

    @Test fun randomIntegersSupportFullSignedRangeAndEqualEndpoints() {
        assertEquals(-5, randomInteger(-5, -5))
        repeat(50) { assertTrue(randomInteger(1, 6) in 1..6); randomInteger(Int.MIN_VALUE, Int.MAX_VALUE) }
        assertThrows(IllegalArgumentException::class.java) { randomInteger(9, 1) }
    }

    @Test fun privateBrowserRejectsUnsafeSchemesAndBuildsEncodedSearches() {
        assertEquals("https://example.com/path", browserDestination("example.com/path"))
        assertEquals("https://duckduckgo.com/?q=hello+world", browserDestination("hello world"))
        listOf("javascript:alert(1)", "file:///data/data/app.sadhan", "intent://example", "http://example.com", "https://user:pass@example.com").forEach {
            assertThrows(IllegalArgumentException::class.java) { browserDestination(it) }
        }
    }
}

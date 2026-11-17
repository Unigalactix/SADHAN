package app.sadhan.update

import org.junit.Assert.*
import org.junit.Test

class VersionTest {
    @Test fun sameReleaseNeverOffersAnUpdate() {
        assertEquals(0, Version.parse("v1.0.0").compareTo(Version.parse("1.0.0")))
        assertEquals(0, Version.parse("1.0.0+build.2").compareTo(Version.parse("1.0.0")))
    }

    @Test fun orderingIsNumericAndSemverCompliant() {
        val ordered = listOf("1.0.0-alpha", "1.0.0-alpha.2", "1.0.0-alpha.10", "1.0.0-beta", "1.0.0", "1.0.1", "1.2.0", "1.10.0", "2.0.0")
        ordered.zipWithNext().forEach { (left, right) -> assertTrue("$left < $right", Version.parse(left) < Version.parse(right)) }
    }

    @Test fun malformedVersionsAreRejected() {
        listOf("1.0", "01.0.0", "1.0.0-01", "latest", "1.0.-1").forEach {
            assertThrows(IllegalArgumentException::class.java) { Version.parse(it) }
        }
    }
}

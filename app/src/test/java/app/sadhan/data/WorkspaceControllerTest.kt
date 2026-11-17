package app.sadhan.data

import org.junit.Assert.*
import org.junit.Test

class WorkspaceControllerTest {
    @Test fun reopeningDoesNotDuplicateSessionsAndLimitsDoNotEvictWork() {
        val controller = WorkspaceController(2)
        assertEquals(OpenSessionResult.OPENED, controller.open("scratchpad"))
        val first = controller.sessions.single()
        first.warnings["note"] = "Temporary work"
        assertEquals(OpenSessionResult.EXISTING, controller.open("scratchpad"))
        assertSame(first, controller.sessions.single())
        controller.open("calculator")
        assertEquals(OpenSessionResult.LIMIT, controller.open("video"))
        assertEquals("Temporary work", first.closeWarning)
        assertEquals(2, controller.sessions.size)
    }

    @Test fun confirmedCloseErasesWarningsAndReopenCreatesFreshSession() {
        val controller = WorkspaceController()
        controller.open("scratchpad")
        val old = controller.sessions.single()
        old.warnings["note"] = "Temporary work"
        controller.close(setOf("scratchpad"))
        assertNull(old.closeWarning)
        assertTrue(controller.sessions.isEmpty())
        controller.open("scratchpad")
        assertNotSame(old, controller.sessions.single())
        assertThrows(IllegalArgumentException::class.java) { controller.close(setOf("missing")) }
    }
}

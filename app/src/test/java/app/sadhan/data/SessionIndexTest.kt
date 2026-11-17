package app.sadhan.data

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.Assert.*
import org.junit.Test

class SessionIndexTest {
    @Test fun completedIndexIsReusedUntilInvalidated() = runBlocking {
        val index = SessionIndex<String>()
        var scans = 0
        val source = flow {
            scans++
            emit(IndexSnapshot(listOf("first"), 1, 2))
            emit(IndexSnapshot(listOf("first", "second"), 2, 2, true))
        }
        index.refresh(source)
        index.refresh(source)
        assertEquals(1, scans)
        assertEquals(2, index.snapshot.items.size)
        index.invalidate()
        index.refresh(source)
        assertEquals(2, scans)
    }

    @Test fun cancellingPartialScanReleasesLoadingAndAllowsRetry() = runBlocking {
        val index = SessionIndex<String>()
        val seen = CompletableDeferred<Unit>()
        val job = launch {
            index.refresh(flow {
                emit(IndexSnapshot(listOf("partial"), 1))
                seen.complete(Unit)
                awaitCancellation()
            })
        }
        seen.await()
        assertEquals(listOf("partial"), index.snapshot.items)
        job.cancelAndJoin()
        assertFalse(index.loading)
        assertTrue(index.needsScan)
        index.refresh(flowOf(IndexSnapshot(listOf("complete"), 1, 1, true)))
        assertFalse(index.needsScan)
        index.accessDenied()
        assertTrue(index.snapshot.items.isEmpty())
        assertNotNull(index.error)
    }
}

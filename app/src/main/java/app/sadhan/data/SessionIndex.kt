package app.sadhan.data

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import java.io.IOException

data class IndexSnapshot<T>(val items: List<T> = emptyList(), val inspected: Int = 0, val total: Int? = null, val complete: Boolean = false)

class SessionIndex<T> {
    var snapshot by mutableStateOf(IndexSnapshot<T>())
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var generation by mutableIntStateOf(0)
        private set
    private var loadedGeneration = -1
    val needsScan get() = loadedGeneration != generation

    fun invalidate() { generation++ }
    fun clear() { snapshot = IndexSnapshot(); error = null; loadedGeneration = -1; generation++ }
    fun accessDenied() { clear(); error = "Folder access was revoked. Choose the folder again." }

    suspend fun refresh(source: Flow<IndexSnapshot<T>>) {
        if (!needsScan) return
        val requested = generation
        loading = true
        error = null
        try {
            source.collect { snapshot = it }
            if (snapshot.complete) loadedGeneration = requested
            else error = "The library scan ended before completion. Rescan to retry."
        } catch (failure: SecurityException) {
            snapshot = IndexSnapshot()
            error = "Access was revoked. Choose the folder or allow media access again."
        } catch (failure: IOException) { error = "Library scan failed: ${failure.message}" }
        catch (failure: IllegalArgumentException) { error = "Invalid library source: ${failure.message}" }
        finally { loading = false }
    }
}

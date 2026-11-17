package app.sadhan.data

import androidx.compose.runtime.*

@Stable
class WorkspaceSession(val route: String) {
    internal val warnings = mutableStateMapOf<String, String>()
    val closeWarning: String? get() = warnings.values.distinct().takeIf { it.isNotEmpty() }?.joinToString("\n")
}

enum class OpenSessionResult { OPENED, EXISTING, LIMIT }

@Stable
class WorkspaceController(val limit: Int = 6) {
    private val open = mutableStateListOf<WorkspaceSession>()
    val sessions: List<WorkspaceSession> get() = open
    init { require(limit > 0) }

    fun open(route: String): OpenSessionResult {
        require(route.isNotBlank()) { "A tool route is required" }
        if (open.any { it.route == route }) return OpenSessionResult.EXISTING
        if (open.size == limit) return OpenSessionResult.LIMIT
        open.add(WorkspaceSession(route))
        return OpenSessionResult.OPENED
    }

    fun close(routes: Set<String>) {
        require(routes.isNotEmpty() && routes.all { route -> open.any { it.route == route } }) { "Choose an open tool tab" }
        open.filter { it.route in routes }.forEach { it.warnings.clear() }
        open.removeAll { it.route in routes }
    }
}

package app.sadhan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.sadhan.data.WorkspaceController
import app.sadhan.data.WorkspaceSession

val LocalWorkspaceSession = staticCompositionLocalOf<WorkspaceSession?> { null }

@Composable
fun ReportTabWork(source: String, warning: String?) {
    val session = LocalWorkspaceSession.current
    SideEffect {
        if (warning == null) session?.warnings?.remove(source)
        else session?.warnings?.set(source, warning)
    }
    DisposableEffect(session, source) { onDispose { session?.warnings?.remove(source) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceOverview(controller: WorkspaceController, active: String, select: (String) -> Unit, close: (Set<String>) -> Unit, dismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = dismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Your workspace", style = MaterialTheme.typography.headlineSmall)
            Text("${controller.sessions.size} of ${controller.limit} tabs · RAM only", color = MaterialTheme.colorScheme.secondary)
            Text("Switch without losing work. Closing a tab erases it; nothing is restored after the process ends.")
            if (controller.sessions.isEmpty()) Text("No tools are open yet. Choose one from Tools, Media or Games.")
            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(controller.sessions, key = { it.route }) { session ->
                    val tool = toolCatalog.first { it.route == session.route }
                    Surface(color = if (active == tool.route) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow, shape = MaterialTheme.shapes.medium) {
                        Row(Modifier.fillMaxWidth().padding(8.dp)) {
                            TextButton(onClick = { select(tool.route); dismiss() }, modifier = Modifier.weight(1f)) {
                                Icon(tool.icon, null)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(tool.title)
                                    if (session.closeWarning != null) Text("Contains session work", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            IconButton(onClick = { close(setOf(tool.route)) }) { Icon(Icons.Default.Close, "Close ${tool.title} tab") }
                        }
                    }
                }
            }
            if (controller.sessions.isNotEmpty()) OutlinedButton(onClick = { close(controller.sessions.map { it.route }.toSet()) }) { Text("Close all tabs") }
            TextButton(onClick = dismiss) { Text("Done") }
        }
    }
}

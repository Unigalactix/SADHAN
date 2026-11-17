package app.sadhan.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.sadhan.update.UpdateState

@Composable
fun HomeScreen(updatesEnabled: Boolean, updates: UpdateState, navigate: (String) -> Unit, destination: AppDestination = AppDestination.TOOLS) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }
    val catalog = toolCatalog.filter { it.parentDestination() == destination.route }
    val filtered = catalog.filter {
        (category == "All" || it.section == category) && it.matchesSearch(query)
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxWidth in 380.dp..599.dp && LocalConfiguration.current.fontScale <= 1.2f
        val columns = if (compact) 3 else (maxWidth.value / 200).toInt().coerceIn(2, 5)
        LazyColumn(Modifier.testTag("tool-list").padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Column(Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(when (destination) { AppDestination.MEDIA -> "Your files. Your player."; AppDestination.GAMES -> "A little time to play."; else -> "Everything, at hand." }, style = MaterialTheme.typography.headlineSmall)
                    Text("${catalog.size} ${if (destination == AppDestination.GAMES) "original games" else "tools"} · Session-only workspace", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(query, { query = it }, label = { Text("Find a tool") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (listOf("All") + catalog.map { it.section }.distinct()).forEach { section ->
                            item { FilterChip(selected = category == section, onClick = { category = section }, label = { Text(section) }) }
                        }
                    }
                }
            }
            if (updatesEnabled && destination == AppDestination.TOOLS) {
                updates.update?.let { update -> item { UpdateCard(update) } }
                if (updates.checking) item { Text("Checking for updates...", style = MaterialTheme.typography.labelSmall) }
                updates.message?.takeIf { !it.startsWith("You're up to date") }?.let { message ->
                    item { Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            if (filtered.isEmpty()) item { Text("No matching tools. Try a different name or category.") }
            filtered.groupBy { it.section }.forEach { (section, sectionTools) ->
                item(key = section) {
                    Text(section.uppercase(), Modifier.padding(top = 12.dp, bottom = 2.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                }
                sectionTools.chunked(columns).forEach { row ->
                    item(key = row.first().route) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { tool ->
                                Card(
                                    onClick = { navigate(tool.route) }, modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = .73f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .6f)),
                                ) {
                                    Column(Modifier.fillMaxWidth().heightIn(min = if (compact) 112.dp else 144.dp).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Icon(tool.icon, null, tint = MaterialTheme.colorScheme.primary)
                                            if (tool.beta) BetaChip()
                                        }
                                        Text(tool.title, style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium)
                                        if (!compact) Text(tool.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
            item {
                TextButton(onClick = { navigate("developer") }) { Text("Made by Rajesh Kodaganti · About developer") }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

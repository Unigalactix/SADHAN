package app.sadhan.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.sadhan.update.UpdateState

@Composable
fun HomeScreen(updatesEnabled: Boolean, updates: UpdateState, navigate: (String) -> Unit) {
    LazyColumn(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column(Modifier.padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Small tasks.\nOne fine toolbox.", style = MaterialTheme.typography.headlineLarge)
                Text("${toolCatalog.size} tools. No history. Yours to use.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (updatesEnabled) {
            updates.update?.let { update -> item { UpdateCard(update) } }
            if (updates.checking) item { Text("Checking for updates...", style = MaterialTheme.typography.bodySmall) }
            updates.message?.takeIf { !it.startsWith("You're up to date") }?.let { message ->
                item { Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        toolCatalog.groupBy { it.section }.forEach { (section, sectionTools) ->
            item(key = section) {
                Text(section.uppercase(), Modifier.padding(top = 16.dp, bottom = 4.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            sectionTools.chunked(2).forEach { row ->
                item(key = row.first().route) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { tool ->
                            Card(onClick = { navigate(tool.route) }, modifier = Modifier.weight(1f)) {
                                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Icon(tool.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        if (tool.beta) BetaChip()
                                    }
                                    Text(tool.title, style = MaterialTheme.typography.titleMedium)
                                    Text(tool.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
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

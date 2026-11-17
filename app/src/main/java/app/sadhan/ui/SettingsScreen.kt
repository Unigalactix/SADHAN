package app.sadhan.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.sadhan.data.SettingsStore
import app.sadhan.data.ThemeMode

@Composable
fun SettingsScreen(store: SettingsStore) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settings = store.value
    ToolColumn {
        Text("Make it yours", style = MaterialTheme.typography.headlineLarge)
        Text("Appearance", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(selected = settings.theme == mode, onClick = { store.update(settings.copy(theme = mode)) }, label = { Text(mode.label) })
            }
        }
        Text("System follows your device. Navy glass or frosted light, with orange controls and cyan readouts.")
        Text("Internet & privacy", style = MaterialTheme.typography.titleMedium)
        Text("Only these settings are saved. Turning Internet off overrides every feature below.")
        SettingsToggle("Internet", settings.internet) { store.update(settings.copy(internet = it)) }
        SettingsToggle("Weather", settings.weather) { store.update(settings.copy(weather = it)) }
        SettingsToggle("Currency", settings.currency) { store.update(settings.copy(currency = it)) }
        SettingsToggle("YouTube (Beta)", settings.youtube) { store.update(settings.copy(youtube = it)) }
        SettingsToggle("Private Browser", settings.browser) { store.update(settings.copy(browser = it)) }
        SettingsToggle("Update checks", settings.updates) { store.update(settings.copy(updates = it)) }
        Text("Storage libraries", style = MaterialTheme.typography.titleMedium)
        Text("Music/video access is optional and requested inside each player. Document folders are chosen with Android's folder picker. Lists are scanned in memory, never saved.")
        if (settings.documentFolder != null) {
            androidx.compose.material3.OutlinedButton(onClick = {
                val uri = android.net.Uri.parse(settings.documentFolder)
                try { context.contentResolver.releasePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                catch (e: SecurityException) { android.util.Log.w("SADHAN", "Document folder permission was already unavailable", e) }
                store.update(settings.copy(documentFolder = null))
                notify(context, "Document folder forgotten")
            }) { Text("Forget document folder") }
        }
    }
}

@Composable
private fun SettingsToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = MaterialTheme.shapes.medium) {
    Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(label, style = MaterialTheme.typography.titleMedium) }
        Switch(checked = checked, onCheckedChange = onChange, modifier = Modifier.semantics { contentDescription = label })
    }
    }
}

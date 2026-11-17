package app.sadhan.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.sadhan.data.SettingsStore

@Composable
fun SettingsScreen(store: SettingsStore) {
    val settings = store.value
    ToolColumn {
        Text("Only these settings are saved. Turning Internet off overrides every feature below.")
        SettingsToggle("Internet", settings.internet) { store.update(settings.copy(internet = it)) }
        SettingsToggle("Weather", settings.weather) { store.update(settings.copy(weather = it)) }
        SettingsToggle("Currency", settings.currency) { store.update(settings.copy(currency = it)) }
        SettingsToggle("YouTube (Beta)", settings.youtube) { store.update(settings.copy(youtube = it)) }
        SettingsToggle("Update checks", settings.updates) { store.update(settings.copy(updates = it)) }
    }
}

@Composable
private fun SettingsToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) { Text(label, style = MaterialTheme.typography.titleMedium) }
        Switch(checked = checked, onCheckedChange = onChange, modifier = Modifier.semantics { contentDescription = label })
    }
}

package app.sadhan.ui.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.sadhan.ui.ResultCard
import app.sadhan.ui.ToolColumn
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.sadhan.ui.InstrumentDial

@Composable
fun WorldClockScreen() {
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) { while (true) { now = Instant.now(); delay(1000) } }
    val cities = remember {
        linkedMapOf("Kolkata" to "Asia/Kolkata", "Dubai" to "Asia/Dubai", "London" to "Europe/London", "New York" to "America/New_York", "Los Angeles" to "America/Los_Angeles", "Singapore" to "Asia/Singapore", "Tokyo" to "Asia/Tokyo", "Sydney" to "Australia/Sydney", "Berlin" to "Europe/Berlin", "Toronto" to "America/Toronto")
    }
    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }
    val date = remember { DateTimeFormatter.ofPattern("EEE, d MMM · z") }
    ToolColumn {
        cities.forEach { (city, zone) ->
            val local = now.atZone(ZoneId.of(zone))
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = .73f), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .6f))) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(city, style = MaterialTheme.typography.titleMedium)
                        Text(date.format(local), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    InstrumentDial(local.minute + local.second / 60f, Modifier.size(64.dp), local.hour % 12 + local.minute / 60f)
                    Text(formatter.format(local), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

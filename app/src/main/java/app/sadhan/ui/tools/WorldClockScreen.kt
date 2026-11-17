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

@Composable
fun WorldClockScreen() {
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) { while (true) { now = Instant.now(); delay(1000) } }
    val cities = remember {
        linkedMapOf("Kolkata" to "Asia/Kolkata", "Dubai" to "Asia/Dubai", "London" to "Europe/London", "New York" to "America/New_York", "Los Angeles" to "America/Los_Angeles", "Singapore" to "Asia/Singapore", "Tokyo" to "Asia/Tokyo", "Sydney" to "Australia/Sydney", "Berlin" to "Europe/Berlin", "Toronto" to "America/Toronto")
    }
    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm:ss  •  EEE, d MMM") }
    ToolColumn {
        cities.forEach { (city, zone) -> ResultCard(city, formatter.format(now.atZone(ZoneId.of(zone)))) }
    }
}

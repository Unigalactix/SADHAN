package app.sadhan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.sadhan.data.AppSettings
import app.sadhan.ui.tools.*

val companionRoutes = setOf("calculator", "tip", "emi", "dates", "units", "percentage", "discount", "text", "scratchpad", "stopwatch", "timer", "clock", "weather", "compass", "level", "random")

@Composable
fun CompanionToolPane(primary: String, settings: AppSettings) {
    val initial = when (primary) {
        "calculator", "tip", "emi", "percentage", "discount" -> "units"
        "timer" -> "stopwatch"
        "stopwatch" -> "timer"
        "compass" -> "level"
        "level" -> "compass"
        "weather" -> "scratchpad"
        "clock" -> "dates"
        else -> "calculator"
    }
    var selected by remember(primary) { mutableStateOf(initial) }
    val options = toolCatalog.filter { it.route in companionRoutes && it.route != primary }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("COMPANION WORKSPACE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            Choice("Alongside your tool", options.first { it.route == selected }.title, options.map { it.title }) { title -> selected = options.first { it.title == title }.route }
        }
        Box(Modifier.weight(1f)) {
            key(selected) {
                when (selected) {
                    "calculator" -> CalculatorScreen()
                    "tip" -> TipSplitScreen()
                    "emi" -> EmiScreen()
                    "dates" -> AgeDatesScreen()
                    "units" -> UnitConverterScreen()
                    "percentage" -> PercentageScreen()
                    "discount" -> DiscountScreen()
                    "text" -> TextToolsScreen()
                    "scratchpad" -> ScratchpadScreen()
                    "stopwatch" -> StopwatchScreen()
                    "timer" -> TimerScreen()
                    "clock" -> WorldClockScreen()
                    "weather" -> InternetGate(settings.internet && settings.weather) { WeatherScreen() }
                    "compass" -> CompassScreen()
                    "level" -> LevelScreen()
                    "random" -> RandomPickerScreen()
                }
            }
        }
    }
}

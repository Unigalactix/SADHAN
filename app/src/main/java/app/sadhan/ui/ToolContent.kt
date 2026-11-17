package app.sadhan.ui

import androidx.compose.runtime.Composable
import app.sadhan.data.SettingsStore
import app.sadhan.ui.tools.*
import app.sadhan.ui.games.*

@Composable
fun ToolContent(route: String, store: SettingsStore, fullscreen: Boolean, onFullscreen: (Boolean) -> Unit) {
    val settings = store.value
    when (route) {
        "calculator" -> CalculatorScreen()
        "tip" -> TipSplitScreen()
        "emi" -> EmiScreen()
        "dates" -> AgeDatesScreen()
        "units" -> UnitConverterScreen()
        "qr-maker" -> QrMakerScreen()
        "qr-scanner" -> QrScannerScreen()
        "text" -> TextToolsScreen()
        "scratchpad" -> ScratchpadScreen()
        "images" -> ImageCompressorScreen()
        "stopwatch" -> StopwatchScreen()
        "timer" -> TimerScreen()
        "clock" -> WorldClockScreen()
        "weather" -> InternetGate(settings.internet && settings.weather) { WeatherScreen() }
        "currency" -> InternetGate(settings.internet && settings.currency) { CurrencyScreen() }
        "youtube" -> InternetGate(settings.internet && settings.youtube) { YouTubeScreen() }
        "browser" -> InternetGate(settings.internet && settings.browser) { BrowserScreen() }
        "flashlight" -> FlashlightScreen()
        "compass" -> CompassScreen()
        "level" -> LevelScreen()
        "music" -> MusicPlayerScreen()
        "video" -> VideoPlayerScreen(fullscreen, onFullscreen)
        "pdf" -> PdfReaderScreen(store)
        "percentage" -> PercentageScreen()
        "discount" -> DiscountScreen()
        "random" -> RandomPickerScreen()
        "password" -> PasswordGeneratorScreen()
        "snake" -> SnakeScreen()
        "solitaire" -> SolitaireScreen()
        "chess" -> ChessScreen()
        "runner" -> PixelRunnerScreen()
        "race" -> LaneRacerScreen()
        "rhythm" -> RhythmTilesScreen()
        "blocks" -> BlockDropScreen()
    }
}

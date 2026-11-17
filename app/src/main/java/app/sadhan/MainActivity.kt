package app.sadhan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.sadhan.data.SettingsStore
import app.sadhan.ui.AboutScreen
import app.sadhan.ui.DeveloperScreen
import app.sadhan.ui.HomeScreen
import app.sadhan.ui.InternetGate
import app.sadhan.ui.LicensesScreen
import app.sadhan.ui.SettingsScreen
import app.sadhan.ui.theme.SadhanTheme
import app.sadhan.ui.tools.*
import app.sadhan.ui.toolCatalog
import app.sadhan.update.UpdateState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { SadhanTheme { SadhanApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SadhanApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val store = remember { SettingsStore(context.applicationContext) }
    val settings = store.value
    val updates = remember { UpdateState() }
    val updatesEnabled = settings.internet && settings.updates
    LaunchedEffect(updatesEnabled) { if (updatesEnabled) updates.check() }
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "home"
    val title = when (route) {
        "home" -> "SADHAN"
        "settings" -> "Settings"
        "about" -> "About"
        "developer" -> "About developer"
        "licenses" -> "Open-source licenses"
        else -> toolCatalog.firstOrNull { it.route == route }?.title ?: "SADHAN"
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    if (route != "home") IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (route == "home") {
                        IconButton(onClick = { nav.navigate("about") }) { Icon(Icons.Default.Info, "About") }
                        IconButton(onClick = { nav.navigate("settings") }) { Icon(Icons.Default.Settings, "Settings") }
                    }
                },
            )
        },
    ) { padding ->
        NavHost(
            navController = nav, startDestination = "home", modifier = Modifier.padding(padding),
            enterTransition = { EnterTransition.None }, exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None }, popExitTransition = { ExitTransition.None },
        ) {
            composable("home") { HomeScreen(updatesEnabled, updates) { nav.navigate(it) } }
            composable("settings") { SettingsScreen(store) }
            composable("about") {
                AboutScreen(updatesEnabled, updates, onDeveloper = { nav.navigate("developer") }, onLicenses = { nav.navigate("licenses") })
            }
            composable("developer") { DeveloperScreen() }
            composable("licenses") { LicensesScreen() }
            composable("calculator") { CalculatorScreen() }
            composable("tip") { TipSplitScreen() }
            composable("emi") { EmiScreen() }
            composable("dates") { AgeDatesScreen() }
            composable("units") { UnitConverterScreen() }
            composable("qr-maker") { QrMakerScreen() }
            composable("qr-scanner") { QrScannerScreen() }
            composable("text") { TextToolsScreen() }
            composable("scratchpad") { ScratchpadScreen() }
            composable("images") { ImageCompressorScreen() }
            composable("stopwatch") { StopwatchScreen() }
            composable("timer") { TimerScreen() }
            composable("clock") { WorldClockScreen() }
            composable("weather") { InternetGate(settings.internet && settings.weather) { WeatherScreen() } }
            composable("currency") { InternetGate(settings.internet && settings.currency) { CurrencyScreen() } }
            composable("youtube") { InternetGate(settings.internet && settings.youtube) { YouTubeScreen() } }
            composable("flashlight") { FlashlightScreen() }
            composable("compass") { CompassScreen() }
            composable("level") { LevelScreen() }
            composable("music") { MusicPlayerScreen() }
            composable("video") { VideoPlayerScreen() }
            composable("pdf") { PdfReaderScreen() }
        }
    }
}

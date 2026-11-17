package app.sadhan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import app.sadhan.data.SettingsStore
import app.sadhan.ui.*
import app.sadhan.ui.theme.SadhanTheme
import app.sadhan.update.UpdateState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val store = remember { SettingsStore(applicationContext) }
            SadhanTheme(store.value.theme) { SadhanApp(store) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SadhanApp(store: SettingsStore) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val focus = LocalFocusManager.current
    val keyboard = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val settings = store.value
    val updates = remember { UpdateState() }
    val updatesEnabled = settings.internet && settings.updates
    LaunchedEffect(updatesEnabled) { if (updatesEnabled) updates.check() }
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "home"
    val tabs = remember { mutableStateListOf<String>() }
    val tabScroll = rememberLazyListState()
    var fullscreen by remember { mutableStateOf(false) }
    fun navigate(destination: String) {
        if (destination == route) return
        if (toolCatalog.any { it.route == destination } && destination !in tabs) {
            if (tabs.size == 6) { notify(context, "Six tool tabs are open. Close a tab before adding another."); return }
            tabs.add(destination)
        }
        nav.navigate(destination) { popUpTo("home"); launchSingleTop = true }
    }
    LaunchedEffect(route) {
        focus.clearFocus(force = true)
        keyboard?.hide()
        if (toolCatalog.any { it.route == route } && route !in tabs) nav.popBackStack("home", false)
    }
    LaunchedEffect(route, tabs.size) { tabs.indexOf(route).takeIf { it >= 0 }?.let { tabScroll.animateScrollToItem(it) } }
    val tool = toolCatalog.firstOrNull { it.route == route }
    val title = tool?.title ?: when (route) {
        "settings" -> "Settings"
        "about" -> "About"
        "developer" -> "About developer"
        "licenses" -> "Open-source licenses"
        else -> "SADHAN"
    }
    BackHandler(tool != null && !fullscreen) { navigate("home") }
    GlassBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = {
                if (!fullscreen) Column {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (route == "home") Image(painterResource(R.drawable.launcher_foreground), null, Modifier.size(40.dp))
                                Text(title, style = MaterialTheme.typography.titleLarge)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .76f)),
                        navigationIcon = {
                            if (route != "home") IconButton(onClick = { if (tool != null) navigate("home") else nav.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                        },
                        actions = {
                            if (route == "home" && androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp < 840) {
                                IconButton(onClick = { navigate("about") }) { Icon(Icons.Default.Info, "About") }
                                IconButton(onClick = { navigate("settings") }) { Icon(Icons.Default.Settings, "Settings") }
                            }
                        },
                    )
                    if (tabs.isNotEmpty()) LazyRow(Modifier.padding(horizontal = 12.dp), state = tabScroll, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(tabs, key = { it }) { tab ->
                            val label = toolCatalog.first { it.route == tab }.title
                            InputChip(
                                selected = route == tab, onClick = { navigate(tab) }, label = { Text(label) },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        val wasActive = route == tab
                                        tabs.remove(tab)
                                        if (wasActive) navigate("home")
                                    }) { Icon(Icons.Default.Close, "Close $label tab") }
                                },
                            )
                        }
                        item { IconButton(onClick = { navigate("home") }) { Icon(Icons.Default.Add, "Open another tool") } }
                    }
                }
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding().clipToBounds()) {
                NavHost(nav, "home", enterTransition = { fadeIn(tween(180)) }, exitTransition = { fadeOut(tween(120)) }) {
                    composable("home") {
                        AdaptiveWorkspace("home", padding.calculateTopPadding(), false, ::navigate, companion = {}) {
                            HomeScreen(updatesEnabled, updates, ::navigate)
                        }
                    }
                    composable("settings") { AdaptiveWorkspace("settings", padding.calculateTopPadding(), false, ::navigate, companion = {}) { SettingsScreen(store) } }
                    composable("about") {
                        AdaptiveWorkspace("about", padding.calculateTopPadding(), false, ::navigate, companion = {}) {
                            AboutScreen(updatesEnabled, updates, onDeveloper = { nav.navigate("developer") }, onLicenses = { nav.navigate("licenses") })
                        }
                    }
                    composable("developer") { DeveloperScreen() }
                    composable("licenses") { LicensesScreen() }
                    toolCatalog.forEach { descriptor -> composable(descriptor.route) {} }
                }
                tabs.forEach { tab ->
                    key(tab) {
                        ToolTabHost(active = route == tab) {
                            AdaptiveWorkspace(tab, padding.calculateTopPadding(), fullscreen && route == tab, ::navigate, companion = { CompanionToolPane(tab, settings) }) {
                                ToolContent(tab, store, fullscreen && route == tab) { fullscreen = it }
                            }
                        }
                    }
                }
            }
        }
    }
}

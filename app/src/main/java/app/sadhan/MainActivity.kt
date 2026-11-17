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
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import app.sadhan.data.*
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
    val expanded = LocalConfiguration.current.screenWidthDp >= 840
    val settings = store.value
    val updates = remember { UpdateState() }
    val updatesEnabled = settings.internet && settings.updates
    LaunchedEffect(updatesEnabled) { if (updatesEnabled) updates.check() }
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "home"
    val workspace = remember { WorkspaceController() }
    val tabScroll = rememberLazyListState()
    var overview by remember { mutableStateOf(false) }
    var pendingClose by remember { mutableStateOf<Set<String>?>(null) }
    var fullscreen by remember { mutableStateOf(false) }
    val tool = toolCatalog.firstOrNull { it.route == route }
    val primary = AppDestination.entries.firstOrNull { it.route == route }

    fun navigate(destination: String) {
        if (destination == route) return
        if (toolCatalog.any { it.route == destination } && workspace.open(destination) == OpenSessionResult.LIMIT) {
            overview = true
            notify(context, "Six tool tabs are open. Close a tab to add another.")
            return
        }
        nav.navigate(destination) { popUpTo("home"); launchSingleTop = true }
    }
    fun closeNow(routes: Set<String>) {
        val destination = toolCatalog.firstOrNull { it.route == route }?.parentDestination() ?: "home"
        workspace.close(routes)
        pendingClose = null
        if (route in routes) { fullscreen = false; navigate(destination) }
        if (workspace.sessions.isEmpty()) overview = false
    }
    fun requestClose(routes: Set<String>) {
        if (routes.size > 1 || workspace.sessions.any { it.route in routes && it.closeWarning != null }) pendingClose = routes
        else closeNow(routes)
    }
    LaunchedEffect(route) {
        focus.clearFocus(force = true)
        keyboard?.hide()
        if (tool != null && workspace.sessions.none { it.route == route }) nav.popBackStack("home", false)
    }
    LaunchedEffect(route, workspace.sessions.size) {
        workspace.sessions.indexOfFirst { it.route == route }.takeIf { it >= 0 }?.let { tabScroll.animateScrollToItem(it) }
    }
    BackHandler(tool != null && !fullscreen) { navigate(requireNotNull(tool).parentDestination()) }
    val title = tool?.title ?: primary?.label?.takeUnless { primary == AppDestination.TOOLS } ?: when (route) {
        "about" -> "About"
        "developer" -> "About developer"
        "licenses" -> "Open-source licenses"
        else -> "SADHAN"
    }
    CompositionLocalProvider(LocalSettingsAction provides { navigate("settings") }) {
    GlassBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = {
                if (!fullscreen) Column {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (route == "home") Image(painterResource(R.drawable.launcher_foreground), null, Modifier.size(36.dp))
                                Text(title, style = MaterialTheme.typography.titleLarge)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .8f)),
                        navigationIcon = {
                            if (tool != null || primary == null && route != "home" || route == "settings") IconButton(onClick = {
                                if (tool != null) navigate(tool.parentDestination()) else nav.popBackStack()
                            }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                        },
                        actions = {
                            IconButton(onClick = { overview = true }) {
                                BadgedBox(badge = { if (workspace.sessions.isNotEmpty()) Badge { Text(workspace.sessions.size.toString()) } }) { Icon(Icons.Default.Tab, "Workspace") }
                            }
                            if (primary != null) IconButton(onClick = { nav.navigate("about") }) { Icon(Icons.Default.Info, "About") }
                        },
                    )
                    if (expanded && workspace.sessions.isNotEmpty()) LazyRow(Modifier.padding(horizontal = 12.dp), state = tabScroll, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(workspace.sessions, key = { it.route }) { session ->
                            val label = toolCatalog.first { it.route == session.route }.title
                            InputChip(selected = route == session.route, onClick = { navigate(session.route) }, label = { Text(label) }, trailingIcon = {
                                IconButton(onClick = { requestClose(setOf(session.route)) }) { Icon(Icons.Default.Close, "Close $label tab") }
                            })
                        }
                        item { IconButton(onClick = { navigate("home") }) { Icon(Icons.Default.Add, "Open another tool") } }
                    }
                }
            },
            bottomBar = {
                if (!fullscreen && !expanded && primary != null) NavigationBar(tonalElevation = 0.dp) {
                    AppDestination.entries.forEach { destination ->
                        NavigationBarItem(selected = route == destination.route, onClick = { navigate(destination.route) },
                            icon = { Icon(destination.icon, destination.label) }, label = { Text(destination.label) })
                    }
                }
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding().clipToBounds()) {
                NavHost(nav, "home", enterTransition = { fadeIn(tween(180)) }, exitTransition = { fadeOut(tween(120)) }) {
                    AppDestination.entries.forEach { destination ->
                        composable(destination.route) {
                            AdaptiveWorkspace(destination.route, padding.calculateTopPadding(), false, ::navigate, companion = {}) {
                                if (destination == AppDestination.SETTINGS) SettingsScreen(store)
                                else HomeScreen(updatesEnabled, updates, ::navigate, destination)
                            }
                        }
                    }
                    composable("about") { AboutScreen(updatesEnabled, updates, onDeveloper = { nav.navigate("developer") }, onLicenses = { nav.navigate("licenses") }) }
                    composable("developer") { DeveloperScreen() }
                    composable("licenses") { LicensesScreen() }
                    toolCatalog.forEach { descriptor -> composable(descriptor.route) {} }
                }
                workspace.sessions.forEach { session ->
                    key(session.route) {
                        CompositionLocalProvider(LocalWorkspaceSession provides session) {
                            ToolTabHost(active = route == session.route) {
                                AdaptiveWorkspace(session.route, padding.calculateTopPadding(), fullscreen && route == session.route, ::navigate,
                                    companion = { CompanionToolPane(session.route, settings) }) {
                                    ToolContent(session.route, store, fullscreen && route == session.route) { fullscreen = it }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (overview) WorkspaceOverview(workspace, route, ::navigate, ::requestClose) { overview = false }
        pendingClose?.let { routes ->
            AlertDialog(onDismissRequest = { pendingClose = null }, title = { Text(if (routes.size == 1) "Close this tab?" else "Close ${routes.size} tabs?") },
                text = { Text((workspace.sessions.filter { it.route in routes }.mapNotNull { it.closeWarning }.distinct() +
                    "This erases the selected tabs' in-memory work immediately. There is no undo and nothing is saved to disk.").joinToString("\n\n")) },
                confirmButton = { TextButton(onClick = { closeNow(routes) }) { Text("Close tab${if (routes.size > 1) "s" else ""}") } },
                dismissButton = { TextButton(onClick = { pendingClose = null }) { Text("Keep open") } })
        }
    }
    }
}

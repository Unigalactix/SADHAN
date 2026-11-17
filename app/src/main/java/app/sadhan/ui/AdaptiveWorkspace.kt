package app.sadhan.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import kotlinx.coroutines.flow.map

@Composable
fun GlassBackground(content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(Modifier.fillMaxSize().background(colors.background).background(
        Brush.radialGradient(listOf(colors.primary.copy(alpha = .10f), colors.background.copy(alpha = 0f)), radius = 1200f),
    )) { content() }
}

@Composable
fun AdaptiveWorkspace(route: String, topPadding: androidx.compose.ui.unit.Dp, fullscreen: Boolean, navigate: (String) -> Unit, companion: @Composable () -> Unit, content: @Composable () -> Unit) {
    val activity = LocalContext.current as Activity
    val density = LocalDensity.current
    val info by remember(activity) {
        WindowInfoTracker.getOrCreate(activity).windowLayoutInfo(activity).map<WindowLayoutInfo, WindowLayoutInfo?> { it }
    }.collectAsState(initial = null)
    val fold = info?.displayFeatures.orEmpty().filterIsInstance<FoldingFeature>().firstOrNull {
        it.isSeparating || it.occlusionType == FoldingFeature.OcclusionType.FULL
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val vertical = fold?.takeIf { it.orientation == FoldingFeature.Orientation.VERTICAL }
        val split = vertical?.let { with(density) { it.bounds.left.toDp() } }?.takeIf { it in 180.dp..(maxWidth - 180.dp) }
        val paired = !fullscreen && route in companionRoutes && maxWidth >= 640.dp && maxHeight >= 480.dp
        val rail = !fullscreen && split == null && maxWidth >= 840.dp && !paired
        val leftWidth = split ?: if (paired) (maxWidth - 24.dp) * .46f else if (rail) 88.dp else 0.dp
        val gap = if (split != null) with(density) { vertical!!.bounds.width().toDp() }.coerceAtLeast(16.dp) else if (paired) 24.dp else 0.dp
        val horizontal = fold?.takeIf { it.orientation == FoldingFeature.Orientation.HORIZONTAL }
        val before = horizontal?.let { (with(density) { it.bounds.top.toDp() } - topPadding - 12.dp).coerceIn(0.dp, maxHeight) }
        val after = horizontal?.let { (with(density) { it.bounds.bottom.toDp() } - topPadding + 12.dp).coerceIn(0.dp, maxHeight) }
        val pane = if (before != null && after != null) {
            if (before >= maxHeight - after) Modifier.height(before) else Modifier.padding(top = after)
        } else Modifier
        Row(Modifier.fillMaxSize().then(pane)) {
            Box(Modifier.width(leftWidth).fillMaxHeight()) {
                if (!fullscreen && (paired || (split != null && route in companionRoutes))) {
                    key(route) { companion() }
                } else if (split != null && !fullscreen) {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("YOUR TOOLBOX", Modifier.padding(12.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        NavigationDrawerItem(label = { Text("All tools") }, selected = route == "home", onClick = { navigate("home") })
                        toolCatalog.forEach { tool ->
                            NavigationDrawerItem(label = { Text(tool.title) }, icon = { Icon(tool.icon, null) }, selected = route == tool.route, onClick = { navigate(tool.route) })
                        }
                        NavigationDrawerItem(label = { Text("Settings") }, selected = route == "settings", onClick = { navigate("settings") })
                        NavigationDrawerItem(label = { Text("About") }, selected = route == "about", onClick = { navigate("about") })
                    }
                } else if (rail) {
                    NavigationRail(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .65f)) {
                        Spacer(Modifier.height(20.dp))
                        NavigationRailItem(selected = route == "home", onClick = { navigate("home") }, icon = { Icon(Icons.Default.GridView, "All tools") }, label = { Text("Tools") })
                        Spacer(Modifier.weight(1f))
                        NavigationRailItem(selected = route == "settings", onClick = { navigate("settings") }, icon = { Icon(Icons.Default.Settings, "Settings") }, label = { Text("Settings") })
                        NavigationRailItem(selected = route == "about", onClick = { navigate("about") }, icon = { Icon(Icons.Default.Info, "About") }, label = { Text("About") })
                    }
                }
            }
            Spacer(Modifier.width(gap))
            Box(Modifier.weight(1f).fillMaxHeight()) { content() }
        }
    }
}

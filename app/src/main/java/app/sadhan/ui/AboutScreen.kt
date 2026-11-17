package app.sadhan.ui

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import app.sadhan.BuildConfig
import app.sadhan.update.UpdateChecker
import app.sadhan.update.UpdateState
import kotlinx.coroutines.launch

@Composable
fun AboutScreen(updatesEnabled: Boolean, updates: UpdateState, onDeveloper: () -> Unit, onLicenses: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    ToolColumn {
        ResultCard("SADHAN", "v${BuildConfig.VERSION_NAME}")
        Text("SADHAN adds no ads, trackers or accounts. Open tabs live in memory; no tool history is written.")
        Text("27 tools, seven original games, and a workspace that is yours to use.")
        if (updatesEnabled) {
            updates.update?.let { UpdateCard(it) }
            Button(enabled = !updates.checking, onClick = { scope.launch { updates.check() } }) {
                Text(if (updates.checking) "Checking..." else "Check for updates")
            }
            updates.message?.let { Text(it) }
        } else Text("Turned off in Settings")
        OutlinedButton(onClick = onDeveloper) { Text("About developer") }
        OutlinedButton(onClick = { openUrl(context, UpdateChecker.REPOSITORY) }) { Text("Source code on GitHub") }
        OutlinedButton(onClick = { openUrl(context, "${UpdateChecker.REPOSITORY}/issues/new/choose") }) { Text("Report an issue") }
        Text("Reporting opens GitHub in your browser. No diagnostics, inputs or screenshots are attached automatically.")
        Text("MIT License · Copyright 2026 Rajesh Kodaganti")
        OutlinedButton(onClick = { openUrl(context, "${UpdateChecker.REPOSITORY}/blob/main/LICENSE") }) { Text("Read MIT license") }
        Text("Built with AndroidX / Jetpack Compose, CameraX, Media3, WindowManager, Kotlin and ZXing (Apache 2.0).")
        OutlinedButton(onClick = onLicenses) { Text("Open-source licenses") }
        Text("Weather: Open-Meteo (CC BY 4.0). Rates: Frankfurter / ECB. Websites and YouTube have their own ads, terms and privacy policies. Private browsing is not anonymity.")
        OutlinedButton(onClick = { openUrl(context, "${UpdateChecker.REPOSITORY}/blob/main/THIRD_PARTY_NOTICES.md") }) { Text("Third-party notices") }
    }
}

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
        Text("No ads. No tracking. No accounts. Nothing you type in a tool is saved.")
        Text("A daily toolbox, built to do small things properly.")
        if (updatesEnabled) {
            updates.update?.let { UpdateCard(it) }
            Button(enabled = !updates.checking, onClick = { scope.launch { updates.check() } }) {
                Text(if (updates.checking) "Checking..." else "Check for updates")
            }
            updates.message?.let { Text(it) }
        } else Text("Turned off in Settings")
        OutlinedButton(onClick = onDeveloper) { Text("About developer") }
        OutlinedButton(onClick = { openUrl(context, UpdateChecker.REPOSITORY) }) { Text("Source code on GitHub") }
        Text("MIT License · Copyright 2026 Rajesh Kodaganti")
        OutlinedButton(onClick = { openUrl(context, "${UpdateChecker.REPOSITORY}/blob/main/LICENSE") }) { Text("Read MIT license") }
        Text("Built with AndroidX / Jetpack Compose and CameraX (Apache 2.0), Kotlin (Apache 2.0) and ZXing (Apache 2.0).")
        OutlinedButton(onClick = onLicenses) { Text("Open-source licenses") }
        Text("Weather: Open-Meteo (CC BY 4.0). Currency reference data: Frankfurter / ECB. YouTube is a third-party service with its own ads, terms and privacy policy.")
        OutlinedButton(onClick = { openUrl(context, "${UpdateChecker.REPOSITORY}/blob/main/THIRD_PARTY_NOTICES.md") }) { Text("Third-party notices") }
    }
}

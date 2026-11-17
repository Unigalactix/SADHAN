package app.sadhan.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun LicensesScreen() {
    val context = LocalContext.current
    val mit = remember { context.assets.open("mit-license.txt").bufferedReader().use { it.readText() } }
    val apache = remember { context.assets.open("apache-2.0.txt").bufferedReader().use { it.readText() } }
    ToolColumn {
        Text("Open-source licenses", style = MaterialTheme.typography.headlineSmall)
        Text(mit, style = MaterialTheme.typography.bodySmall)
        Text("AndroidX (Core, Activity, Lifecycle, Compose, Navigation, CameraX, Startup, ProfileInstaller), Material icons, Kotlin, coroutines and ZXing: Apache License 2.0. Copyright The Android Open Source Project, Google, JetBrains s.r.o., Kotlin contributors and ZXing authors respectively.")
        Text(apache, style = MaterialTheme.typography.bodySmall)
    }
}

package app.sadhan.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.sadhan.update.AppUpdate
import app.sadhan.update.UpdateDownloader
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

@Composable
fun UpdateCard(update: AppUpdate) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var downloaded by remember { mutableStateOf<File?>(null) }
    fun install(file: File) {
        try { UpdateDownloader.install(context, file) }
        catch (_: ActivityNotFoundException) { error = "Android package installer is unavailable" }
        catch (e: SecurityException) { error = "Installation blocked: ${e.message}" }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (context.packageManager.canRequestPackageInstalls()) downloaded?.let(::install)
        else error = "Install permission was not granted. Tap Download and install to try again."
    }
    fun requestInstall(file: File) {
        if (context.packageManager.canRequestPackageInstalls()) install(file)
        else {
            try { permission.launch(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))) }
            catch (_: ActivityNotFoundException) { error = "Android install-permission settings are unavailable" }
        }
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Update available: v${update.version}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
            Button(enabled = !busy, onClick = {
                val file = downloaded
                if (file != null && file.exists()) requestInstall(file)
                else scope.launch {
                    busy = true
                    error = null
                    try {
                        val apk = UpdateDownloader.download(context, update) { bytes, total ->
                            progress = if (total > 0) "${bytes * 100 / total}%" else "${bytes / 1024} KB"
                        }
                        downloaded = apk
                        requestInstall(apk)
                    } catch (e: IOException) { error = "Update download failed: ${e.message}" }
                    catch (e: SecurityException) { error = "Update access denied: ${e.message}" }
                    finally { busy = false }
                }
            }) { Text(if (busy) "Downloading $progress" else "Download and install") }
            OutlinedButton(onClick = { openUrl(context, update.releaseUrl) }) { Text("Release notes") }
            ErrorText(error)
        }
    }
}

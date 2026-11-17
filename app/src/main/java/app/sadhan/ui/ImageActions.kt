package app.sadhan.ui

import android.content.ActivityNotFoundException
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.sadhan.core.ImageFiles
import kotlinx.coroutines.launch
import java.io.IOException

@Composable
fun ImageActions(bytes: ByteArray, mime: String, extension: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    fun export(save: Boolean) {
        scope.launch {
            busy = true
            error = null
            try {
                if (save) {
                    ImageFiles.save(context, bytes, mime, extension)
                    notify(context, "Saved to Pictures/SADHAN")
                } else ImageFiles.share(context, bytes, mime, extension)
            } catch (e: IOException) { error = "Image export failed: ${e.message}" }
            catch (e: SecurityException) { error = "Image access denied: ${e.message}" }
            catch (_: ActivityNotFoundException) { error = "No app is available to share this image" }
            finally { busy = false }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { export(true) }, enabled = !busy) { Text("Save to Pictures") }
        OutlinedButton(onClick = { export(false) }, enabled = !busy) { Text("Share") }
    }
    ErrorText(error)
}

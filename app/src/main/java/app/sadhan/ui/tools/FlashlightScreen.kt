package app.sadhan.ui.tools

import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.sadhan.ui.CameraPermission
import app.sadhan.ui.ErrorText
import app.sadhan.ui.ResultCard
import app.sadhan.ui.ToolColumn

@Composable
fun FlashlightScreen() {
    CameraPermission {
        val context = LocalContext.current
        val owner = LocalLifecycleOwner.current
        val manager = remember { context.getSystemService(CameraManager::class.java) }
        var on by remember { mutableStateOf(false) }
        var available by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }
        val cameraId = remember {
            try {
                manager.cameraIdList.firstOrNull {
                    val info = manager.getCameraCharacteristics(it)
                    info.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                        info.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
                }
            } catch (e: CameraAccessException) { error = "Cannot access camera: ${e.message}"; null }
        }
        fun setTorch(enabled: Boolean) {
            if (cameraId != null) {
                try { manager.setTorchMode(cameraId, enabled); on = enabled; error = null }
                catch (e: CameraAccessException) { error = "Flashlight unavailable: ${e.message}" }
                catch (e: SecurityException) { error = "Camera permission is needed: ${e.message}" }
                catch (e: IllegalArgumentException) { error = "Flashlight unavailable: ${e.message}" }
            }
        }
        DisposableEffect(cameraId, owner) {
            val callback = object : CameraManager.TorchCallback() {
                override fun onTorchModeChanged(id: String, enabled: Boolean) {
                    if (id == cameraId) { on = enabled; available = true }
                }
                override fun onTorchModeUnavailable(id: String) {
                    if (id == cameraId) { on = false; available = false }
                }
            }
            manager.registerTorchCallback(ContextCompat.getMainExecutor(context), callback)
            val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP && on) setTorch(false) }
            owner.lifecycle.addObserver(observer)
            onDispose {
                owner.lifecycle.removeObserver(observer)
                manager.unregisterTorchCallback(callback)
                if (on) setTorch(false)
            }
        }
        ToolColumn {
            ResultCard("Flashlight", if (cameraId == null) "No rear flashlight on this device" else if (on) "ON" else "OFF")
            Button(enabled = cameraId != null && available, onClick = { setTorch(!on) }) { Text(if (on) "Turn off" else "Turn on") }
            Text("Turns off when you leave this tool or put the app in the background.")
            if (!available) ErrorText("Camera is in use or the flashlight is unavailable")
            ErrorText(error)
        }
    }
}

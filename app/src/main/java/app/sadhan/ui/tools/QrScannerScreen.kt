package app.sadhan.ui.tools

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.sadhan.ui.CameraPermission
import app.sadhan.ui.CopyButton
import app.sadhan.ui.ErrorText
import app.sadhan.ui.ResultCard
import app.sadhan.ui.ToolColumn
import app.sadhan.ui.openUrl
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

@Composable
fun QrScannerScreen() {
    CameraPermission {
        val context = LocalContext.current
        var result by remember { mutableStateOf<String?>(null) }
        ToolColumn {
            Text("Point the camera at a QR code. Nothing is saved or sent.")
            if (result == null) CameraPreview(onResult = { result = it })
            result?.let { text ->
                ResultCard("Scanned content", text)
                CopyButton(text)
                if (Uri.parse(text).scheme in listOf("https", "http")) {
                    OutlinedButton(onClick = { openUrl(context, text) }) { Text("Open URL") }
                }
                Button(onClick = { result = null }) { Text("Scan another") }
            }
        }
    }
}

@Composable
private fun CameraPreview(onResult: (String) -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    var error by remember { mutableStateOf<String?>(null) }
    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxWidth().height(360.dp))
    ErrorText(error)
    DisposableEffect(owner, previewView) {
        val executor = Executors.newSingleThreadExecutor()
        val main = ContextCompat.getMainExecutor(context)
        val future = ProcessCameraProvider.getInstance(context)
        val preview = Preview.Builder().build()
        val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
        val reader = MultiFormatReader()
        var disposed = false
        var delivered = false
        future.addListener({
            if (!disposed) {
                try {
                    val provider = future.get()
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    analysis.setAnalyzer(executor) { image ->
                        try {
                            val text = decodeFrame(image, reader)
                            if (text != null) main.execute {
                                if (!disposed && !delivered) { delivered = true; onResult(text) }
                            }
                        } finally { image.close() }
                    }
                    provider.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                } catch (e: ExecutionException) { error = "Cannot start camera: ${e.cause?.message}" }
                catch (e: IllegalArgumentException) { error = "Camera unavailable: ${e.message}" }
                catch (e: IllegalStateException) { error = "Camera unavailable: ${e.message}" }
                catch (e: SecurityException) { error = "Camera permission is unavailable: ${e.message}" }
            }
        }, main)
        onDispose {
            disposed = true
            analysis.clearAnalyzer()
            if (future.isDone && !future.isCancelled) {
                try { future.get().unbind(preview, analysis) }
                catch (e: ExecutionException) { android.util.Log.w("SADHAN", "Camera cleanup failed", e.cause) }
            }
            executor.shutdown()
        }
    }
}

private fun decodeFrame(image: ImageProxy, reader: MultiFormatReader): String? {
    val plane = image.planes[0]
    val buffer = plane.buffer
    val start = buffer.position()
    val pixels = ByteArray(image.width * image.height)
    for (y in 0 until image.height) for (x in 0 until image.width) {
        pixels[y * image.width + x] = buffer.get(start + y * plane.rowStride + x * plane.pixelStride)
    }
    var source = PlanarYUVLuminanceSource(pixels, image.width, image.height, 0, 0, image.width, image.height, false)
    return try {
        reader.decodeWithState(BinaryBitmap(HybridBinarizer(source))).text
    } catch (_: ReaderException) {
        try {
            // Rotating the luminance data also lets one-dimensional codes scan in portrait.
            val rotated = ByteArray(pixels.size)
            for (y in 0 until image.height) for (x in 0 until image.width) {
                rotated[x * image.height + image.height - y - 1] = pixels[y * image.width + x]
            }
            source = PlanarYUVLuminanceSource(rotated, image.height, image.width, 0, 0, image.height, image.width, false)
            reader.decodeWithState(BinaryBitmap(HybridBinarizer(source))).text
        } catch (_: ReaderException) { null }
    } finally { reader.reset() }
}

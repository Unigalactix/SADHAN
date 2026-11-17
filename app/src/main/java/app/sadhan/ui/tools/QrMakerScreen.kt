package app.sadhan.ui.tools

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import app.sadhan.core.ImageFiles
import app.sadhan.ui.ErrorText
import app.sadhan.ui.ImageActions
import app.sadhan.ui.Input
import app.sadhan.ui.ToolColumn
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
fun QrMakerScreen() {
    var text by remember { mutableStateOf("") }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var bytes by remember { mutableStateOf<ByteArray?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    ToolColumn {
        Input("QR content", text, { text = it; bitmap = null; bytes = null }, singleLine = false)
        Text("Made entirely on your phone. Maximum 2000 characters.")
        Button(enabled = text.isNotBlank() && !busy, onClick = {
            val content = text
            scope.launch {
                busy = true
                error = null
                try {
                    require(content.length <= 2000) { "Please use 2000 characters or fewer" }
                    val result = withContext(Dispatchers.Default) {
                        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 1024, 1024, mapOf(EncodeHintType.CHARACTER_SET to "UTF-8"))
                        val pixels = IntArray(matrix.width * matrix.height) { index ->
                            if (matrix[index % matrix.width, index / matrix.width]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                        }
                        val image = Bitmap.createBitmap(pixels, matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
                        image to ImageFiles.encode(image, Bitmap.CompressFormat.PNG, 100)
                    }
                    if (text == content) { bitmap = result.first; bytes = result.second }
                } catch (e: WriterException) { error = "Cannot encode this content: ${e.message}" }
                catch (e: IllegalArgumentException) { error = e.message }
                catch (e: IOException) { error = e.message }
                finally { busy = false }
            }
        }) { Text(if (busy) "Making QR..." else "Make QR") }
        ErrorText(error)
        bitmap?.let {
            Image(it.asImageBitmap(), "Generated QR code", Modifier.fillMaxWidth().height(300.dp).background(Color.White).padding(8.dp))
        }
        bytes?.let { ImageActions(it, "image/png", "png") }
    }
}

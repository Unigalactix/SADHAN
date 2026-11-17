package app.sadhan.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.Closeable
import java.io.IOException

class PdfDocument private constructor(private val descriptor: ParcelFileDescriptor, private val renderer: PdfRenderer) : Closeable {
    private var closed = false
    val pageCount: Int = renderer.pageCount

    @Synchronized
    fun render(index: Int): Bitmap {
        check(!closed) { "Document is closed" }
        return renderer.openPage(index).use { page ->
            val scale = minOf(1536.0 / page.width, 2048.0 / page.height)
            val bitmap = Bitmap.createBitmap((page.width * scale).toInt().coerceAtLeast(1), (page.height * scale).toInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmap
        }
    }

    @Synchronized
    override fun close() {
        if (!closed) {
            closed = true
            try { renderer.close() } finally { descriptor.close() }
        }
    }

    companion object {
        fun open(context: Context, uri: Uri): PdfDocument {
            val descriptor = context.contentResolver.openFileDescriptor(uri, "r")
                ?: throw IOException("Cannot open PDF")
            try { return PdfDocument(descriptor, PdfRenderer(descriptor)) }
            catch (e: Exception) { descriptor.close(); throw e }
        }
    }
}

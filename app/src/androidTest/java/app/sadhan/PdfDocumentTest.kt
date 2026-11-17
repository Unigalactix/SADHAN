package app.sadhan

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sadhan.media.PdfDocument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class PdfDocumentTest {
    @Test fun localPdfRendersDistinctPagesWithinMemoryBound() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = File(context.cacheDir, "reader-test.pdf")
        try {
            val source = android.graphics.pdf.PdfDocument()
            try {
                listOf(Color.RED, Color.BLUE).forEachIndexed { index, color ->
                    val page = source.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(300, 400, index + 1).create())
                    page.canvas.drawRect(20f, 20f, 200f, 200f, Paint().apply { this.color = color })
                    source.finishPage(page)
                }
                file.outputStream().use { source.writeTo(it) }
            } finally { source.close() }
            PdfDocument.open(context, Uri.fromFile(file)).use { pdf ->
                assertEquals(2, pdf.pageCount)
                listOf(Color.RED, Color.BLUE).forEachIndexed { index, color ->
                    val image = pdf.render(index)
                    assertTrue(image.width <= 1536 && image.height <= 2048)
                    assertEquals(color, image.getPixel(image.width / 3, image.height / 3))
                    assertEquals(Color.WHITE, image.getPixel(0, 0))
                    image.recycle()
                }
            }
        } finally { file.delete() }
    }
}

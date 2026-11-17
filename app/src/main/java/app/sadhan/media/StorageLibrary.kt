package app.sadhan.media

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.coroutines.coroutineContext

object StorageLibrary {
    suspend fun media(context: Context, video: Boolean): List<LibraryItem> = withContext(Dispatchers.IO) {
        val collection = if (video) MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            else MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.DURATION, MediaStore.MediaColumns.MIME_TYPE) +
            if (video) arrayOf(MediaStore.MediaColumns.WIDTH, MediaStore.MediaColumns.HEIGHT)
            else arrayOf(MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM)
        val cursor = context.contentResolver.query(collection, projection, "${MediaStore.MediaColumns.IS_PENDING}=0", null, "${MediaStore.MediaColumns.DISPLAY_NAME} ASC")
            ?: throw IOException("The device media library could not be read")
        cursor.use {
            buildList {
                while (it.moveToNext()) {
                    coroutineContext.ensureActive()
                    val title = if (video) it.getString(1) else it.getString(4)?.takeIf(String::isNotBlank) ?: it.getString(1)
                    val detail = if (video) {
                        if (it.getInt(4) > 0) "${it.getInt(4)} × ${it.getInt(5)}" else ""
                    } else listOfNotNull(it.getString(5), it.getString(6)).filter { value -> value.isNotBlank() && value != "<unknown>" }.distinct().joinToString(" · ")
                    add(LibraryItem(ContentUris.withAppendedId(collection, it.getLong(0)), title ?: "Untitled file", detail, it.getLong(2), it.getString(3).orEmpty()))
                }
            }
        }
    }

    suspend fun documents(context: Context, tree: Uri): List<LibraryItem> = withContext(Dispatchers.IO) {
        val root = DocumentsContract.getTreeDocumentId(tree)
        val pending = ArrayDeque<String>().apply { add(root) }
        val visited = mutableSetOf<String>()
        val items = mutableListOf<LibraryItem>()
        while (pending.isNotEmpty()) {
            coroutineContext.ensureActive()
            val parent = pending.removeFirst()
            if (!visited.add(parent)) continue
            val children = DocumentsContract.buildChildDocumentsUriUsingTree(tree, parent)
            val cursor = context.contentResolver.query(children, arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE,
            ), null, null, null) ?: throw IOException("Cannot read the selected folder")
            cursor.use {
                while (it.moveToNext()) {
                    coroutineContext.ensureActive()
                    val id = it.getString(0)
                    val mime = it.getString(2).orEmpty()
                    if (mime == DocumentsContract.Document.MIME_TYPE_DIR) pending.add(id)
                    else {
                        val name = it.getString(1) ?: "Untitled document"
                        if (mime == "application/pdf" || mime.startsWith("text/") || name.substringAfterLast('.', "").lowercase() in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "rtf", "epub")) {
                            items.add(LibraryItem(DocumentsContract.buildDocumentUriUsingTree(tree, id), name, mime, mime = mime))
                        }
                    }
                }
            }
        }
        items.distinctBy { it.uri }.sortedBy { it.title.lowercase() }
    }
}

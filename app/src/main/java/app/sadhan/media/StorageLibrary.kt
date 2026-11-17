package app.sadhan.media

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import app.sadhan.data.IndexSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.last
import java.io.IOException

object StorageLibrary {
    suspend fun media(context: Context, video: Boolean): List<LibraryItem> = mediaUpdates(context, video).last().items
    suspend fun documents(context: Context, tree: Uri): List<LibraryItem> = documentUpdates(context, tree).last().items

    fun mediaUri(video: Boolean): Uri = if (video) MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

    fun mediaUpdates(context: Context, video: Boolean): Flow<IndexSnapshot<LibraryItem>> = flow {
        val collection = mediaUri(video)
        val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.DURATION, MediaStore.MediaColumns.MIME_TYPE) +
            if (video) arrayOf(MediaStore.MediaColumns.WIDTH, MediaStore.MediaColumns.HEIGHT)
            else arrayOf(MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM)
        val cursor = context.contentResolver.query(collection, projection, "${MediaStore.MediaColumns.IS_PENDING}=0", null, "${MediaStore.MediaColumns.DISPLAY_NAME} ASC")
            ?: throw IOException("The device media library could not be read")
        val items = mutableListOf<LibraryItem>()
        cursor.use {
            val total = it.count
            while (it.moveToNext()) {
                currentCoroutineContext().ensureActive()
                val title = if (video) it.getString(1) else it.getString(4)?.takeIf(String::isNotBlank) ?: it.getString(1)
                val detail = if (video) {
                    if (it.getInt(4) > 0) "${it.getInt(4)} × ${it.getInt(5)}" else ""
                } else listOfNotNull(it.getString(5), it.getString(6)).filter { value -> value.isNotBlank() && value != "<unknown>" }.distinct().joinToString(" · ")
                items.add(LibraryItem(ContentUris.withAppendedId(collection, it.getLong(0)), title ?: "Untitled file", detail, it.getLong(2), it.getString(3).orEmpty()))
                if (items.size == 1 || items.size % 128 == 0) emit(IndexSnapshot(items.toList(), items.size, total))
            }
            emit(IndexSnapshot(items.toList(), items.size, total, complete = true))
        }
    }.flowOn(Dispatchers.IO)

    fun documentUpdates(context: Context, tree: Uri): Flow<IndexSnapshot<LibraryItem>> = flow {
        val pending = ArrayDeque<String>().apply { add(DocumentsContract.getTreeDocumentId(tree)) }
        val visited = mutableSetOf<String>()
        val items = linkedMapOf<Uri, LibraryItem>()
        var inspected = 0
        while (pending.isNotEmpty()) {
            currentCoroutineContext().ensureActive()
            val parent = pending.removeFirst()
            if (!visited.add(parent)) continue
            val children = DocumentsContract.buildChildDocumentsUriUsingTree(tree, parent)
            val cursor = context.contentResolver.query(children, arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE,
            ), null, null, null) ?: throw IOException("Cannot read the selected folder")
            cursor.use {
                while (it.moveToNext()) {
                    currentCoroutineContext().ensureActive()
                    inspected++
                    val id = it.getString(0)
                    val mime = it.getString(2).orEmpty()
                    if (mime == DocumentsContract.Document.MIME_TYPE_DIR) pending.add(id)
                    else {
                        val name = it.getString(1) ?: "Untitled document"
                        if (mime == "application/pdf" || mime.startsWith("text/") || name.substringAfterLast('.', "").lowercase() in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "rtf", "epub")) {
                            val uri = DocumentsContract.buildDocumentUriUsingTree(tree, id)
                            items[uri] = LibraryItem(uri, name, mime, mime = mime)
                        }
                    }
                    if (inspected == 1 || inspected % 128 == 0) emit(IndexSnapshot(items.values.toList(), inspected))
                }
            }
        }
        emit(IndexSnapshot(items.values.sortedBy { it.title.lowercase() }, inspected, complete = true))
    }.flowOn(Dispatchers.IO)
}

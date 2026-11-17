package app.sadhan.media

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

fun documentName(context: Context, uri: Uri): String {
    return context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
        if (it.moveToFirst() && !it.isNull(0)) it.getString(0) else null
    } ?: "Selected file"
}

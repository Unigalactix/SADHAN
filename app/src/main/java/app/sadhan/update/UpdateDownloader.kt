package app.sadhan.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.FileProvider
import app.sadhan.BuildConfig
import app.sadhan.core.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import kotlin.coroutines.coroutineContext

object UpdateDownloader {
    suspend fun download(context: Context, update: AppUpdate, progress: (Long, Long) -> Unit): File =
        withContext(Dispatchers.IO) {
            val directory = File(context.cacheDir, "updates")
            if (!directory.exists() && !directory.mkdirs()) throw IOException("Cannot create update directory")
            val part = File(directory, "update.part")
            val destination = File(directory, "SADHAN-v${update.version}.apk")
            val connection = Network.open(update.apkUrl)
            connection.setRequestProperty("Accept", "application/octet-stream")
            try {
                if (connection.responseCode !in 200..299) throw IOException("Download returned HTTP ${connection.responseCode}")
                val length = connection.contentLengthLong
                val limit = 150L * 1024 * 1024
                if (length > limit) throw IOException("Update is too large")
                val digest = MessageDigest.getInstance("SHA-256")
                connection.inputStream.use { input ->
                    part.outputStream().use { output ->
                        val buffer = ByteArray(32 * 1024)
                        var total = 0L
                        while (true) {
                            coroutineContext.ensureActive()
                            val count = input.read(buffer)
                            if (count < 0) break
                            total += count
                            if (total > limit) throw IOException("Update is too large")
                            digest.update(buffer, 0, count)
                            output.write(buffer, 0, count)
                            progress(total, length)
                        }
                        if (length >= 0 && length != total) throw IOException("Incomplete APK download")
                    }
                }
                val hash = digest.digest().joinToString("") { "%02x".format(it) }
                if (update.digest != null && !hash.equals(update.digest, ignoreCase = true)) throw IOException("APK checksum does not match the release")
                verifyPackage(context, part)
                if (destination.exists() && !destination.delete()) throw IOException("Cannot replace old download")
                if (!part.renameTo(destination)) throw IOException("Cannot finalize APK download")
                destination
            } finally {
                connection.disconnect()
                if (part.exists() && !part.delete()) android.util.Log.w("SADHAN", "Could not remove partial update")
            }
        }

    @Suppress("DEPRECATION")
    private fun verifyPackage(context: Context, file: File) {
        val manager = context.packageManager
        val archive = manager.getPackageArchiveInfo(file.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES)
            ?: throw IOException("Downloaded file is not an APK")
        val installed = manager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        if (archive.packageName != context.packageName || archive.longVersionCode <= BuildConfig.VERSION_CODE) throw IOException("APK is not a newer SADHAN build")
        val expected = installed.signingInfo?.apkContentsSigners?.map { it.toCharsString() }?.toSet()
            ?: throw IOException("Cannot read installed signing certificate")
        val actual = archive.signingInfo?.apkContentsSigners?.map { it.toCharsString() }?.toSet()
        if (actual != expected) throw IOException("APK signing certificate does not match SADHAN")
    }

    fun install(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}

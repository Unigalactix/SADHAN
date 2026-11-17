package app.sadhan

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.sadhan.media.StorageLibrary
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer
import java.nio.ByteOrder

@RunWith(AndroidJUnit4::class)
class StorageLibraryTest {
    @Test fun optedInMusicLibraryFindsARealStoredAudioFile() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
        automation.grantRuntimePermission(context.packageName, permission)
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "SADHAN-library-test.wav")
            put(MediaStore.Audio.Media.TITLE, "SADHAN library test")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/SADHANTests")
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }
        val uri = requireNotNull(resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values))
        try {
            val samples = 16000
            val audio = ByteBuffer.allocate(44 + samples * 2).order(ByteOrder.LITTLE_ENDIAN).apply {
                put("RIFF".toByteArray()); putInt(36 + samples * 2); put("WAVEfmt ".toByteArray())
                putInt(16); putShort(1); putShort(1); putInt(16000); putInt(32000)
                putShort(2); putShort(16); put("data".toByteArray()); putInt(samples * 2)
            }.array()
            requireNotNull(resolver.openOutputStream(uri)).use { it.write(audio) }
            resolver.update(uri, ContentValues().apply { put(MediaStore.Audio.Media.IS_PENDING, 0) }, null, null)
            val library = StorageLibrary.media(context, false)
            assertTrue(library.any { android.content.ContentUris.parseId(it.uri) == android.content.ContentUris.parseId(uri) })
        } finally { resolver.delete(uri, null, null) }
    }
}

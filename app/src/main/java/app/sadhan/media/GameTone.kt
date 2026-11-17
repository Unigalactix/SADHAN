package app.sadhan.media

import android.content.Context
import android.media.*
import android.os.Handler
import android.os.Looper
import kotlin.math.*

class GameTone(context: Context) {
    private val manager = context.applicationContext.getSystemService(AudioManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private var track: AudioTrack? = null
    private var focused = false
    private val release = Runnable { stop() }
    private val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
    private val focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK).setAudioAttributes(attributes)
        .setOnAudioFocusChangeListener { change -> if (change < 0) stop() }.build()

    fun play(column: Int) {
        require(column in 0..3) { "Choose one of four notes" }
        handler.removeCallbacks(release)
        if (!focused) {
            check(manager.requestAudioFocus(focus) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) { "Audio is in use. Mute game sound or try again." }
            focused = true
        }
        track?.release()
        track = null
        val frequency = listOf(261.63, 293.66, 329.63, 392.0)[column]
        val samples = ShortArray(4000) { sample ->
            val t = sample / 16000.0
            val envelope = min(1.0, t / .005) * (1 - t / .25).coerceAtLeast(0.0).pow(2) * exp(-6 * t)
            ((sin(2 * PI * frequency * t) + .25 * sin(4 * PI * frequency * t)) * envelope * 7000).toInt().toShort()
        }
        val audio = AudioTrack.Builder().setAudioAttributes(attributes)
            .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(16000).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setTransferMode(AudioTrack.MODE_STATIC).setBufferSizeInBytes(samples.size * 2).build()
        track = audio
        check(audio.write(samples, 0, samples.size) == samples.size) { "Audio output rejected the generated note" }
        audio.play()
        handler.postDelayed(release, 300)
    }

    fun stop() {
        handler.removeCallbacks(release)
        track?.release()
        track = null
        if (focused) { focused = false; manager.abandonAudioFocusRequest(focus) }
    }
}

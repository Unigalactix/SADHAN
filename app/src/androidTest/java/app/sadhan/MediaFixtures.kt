package app.sadhan

import android.media.*
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun writeTestAudio(file: File, seconds: Int = 4) {
    val samples = 16000 * seconds
    val bytes = ByteBuffer.allocate(44 + samples * 2).order(ByteOrder.LITTLE_ENDIAN).apply {
        put("RIFF".toByteArray()); putInt(36 + samples * 2); put("WAVEfmt ".toByteArray())
        putInt(16); putShort(1); putShort(1); putInt(16000); putInt(32000)
        putShort(2); putShort(16); put("data".toByteArray()); putInt(samples * 2)
    }.array()
    file.writeBytes(bytes)
}

fun writeTestVideo(file: File) {
    val encoder = MediaCodec.createEncoderByType("video/avc")
    val muxer = MediaMuxer(file.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    var muxing = false
    try {
        val format = MediaFormat.createVideoFormat("video/avc", 64, 64).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
            setInteger(MediaFormat.KEY_BIT_RATE, 64_000)
            setInteger(MediaFormat.KEY_FRAME_RATE, 15)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()
        var frame = 0
        var track = -1
        val info = MediaCodec.BufferInfo()
        val deadline = System.currentTimeMillis() + 20_000
        var done = false
        while (!done && System.currentTimeMillis() < deadline) {
            if (frame <= 30) {
                val input = encoder.dequeueInputBuffer(10_000)
                if (input >= 0) {
                    if (frame == 30) encoder.queueInputBuffer(input, 0, 0, frame * 1_000_000L / 15, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    else {
                        val pixels = ByteArray(64 * 64 * 3 / 2) { if (it < 64 * 64) (50 + (it % 64 + frame * 2) % 120).toByte() else 128.toByte() }
                        requireNotNull(encoder.getInputBuffer(input)).put(pixels)
                        encoder.queueInputBuffer(input, 0, pixels.size, frame * 1_000_000L / 15, 0)
                    }
                    frame++
                }
            }
            val output = encoder.dequeueOutputBuffer(info, 10_000)
            if (output == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                track = muxer.addTrack(encoder.outputFormat)
                muxer.start()
                muxing = true
            } else if (output >= 0) {
                if (info.size > 0 && info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0) muxer.writeSampleData(track, requireNotNull(encoder.getOutputBuffer(output)), info)
                done = info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
                encoder.releaseOutputBuffer(output, false)
            }
        }
        check(done) { "Synthetic test video encoding timed out" }
    } finally { encoder.release(); if (muxing) muxer.stop(); muxer.release() }
}

package app.sadhan.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object Network {
    suspend fun json(address: String): JSONObject = withContext(Dispatchers.IO) {
        open(address).let { connection ->
            try {
                if (connection.responseCode !in 200..299) throw IOException("Server returned HTTP ${connection.responseCode}")
                val body = connection.inputStream.bufferedReader().use { reader ->
                    val text = StringBuilder()
                    val buffer = CharArray(4096)
                    while (true) {
                        val count = reader.read(buffer)
                        if (count < 0) break
                        text.append(buffer, 0, count)
                        if (text.length > 2_000_000) throw IOException("Server response is too large")
                    }
                    text.toString()
                }
                JSONObject(body)
            } finally { connection.disconnect() }
        }
    }

    fun open(address: String): HttpURLConnection {
        require(URL(address).protocol == "https") { "Only secure HTTPS connections are allowed" }
        return (URL(address).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 20_000
            setRequestProperty("User-Agent", "SADHAN")
            setRequestProperty("Accept", "application/json")
        }
    }
}

package app.sadhan.update

import app.sadhan.BuildConfig
import app.sadhan.core.Network
import java.io.IOException

data class AppUpdate(val version: String, val releaseUrl: String, val apkUrl: String, val digest: String?)

object UpdateChecker {
    const val REPOSITORY = "https://github.com/Unigalactix/SADHAN"

    suspend fun check(): AppUpdate? {
        val data = Network.json("https://api.github.com/repos/Unigalactix/SADHAN/releases/latest")
        val tag = data.getString("tag_name")
        val version = tag.removePrefix("v")
        if (Version.parse(version) <= Version.parse(BuildConfig.VERSION_NAME)) return null
        val assets = data.getJSONArray("assets")
        for (index in 0 until assets.length()) {
            val asset = assets.getJSONObject(index)
            if (asset.getString("name") == "SADHAN-v$version.apk") {
                val url = asset.getString("browser_download_url")
                if (!url.startsWith("$REPOSITORY/releases/download/")) throw IOException("Unexpected APK download address")
                val digest = asset.optString("digest").takeIf { it.startsWith("sha256:") }?.removePrefix("sha256:")
                return AppUpdate(version, "$REPOSITORY/releases/tag/$tag", url, digest)
            }
        }
        throw IOException("The latest release has no matching SADHAN APK")
    }
}

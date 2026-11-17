package app.sadhan.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestination(val route: String, val label: String, val icon: ImageVector) {
    TOOLS("home", "Tools", Icons.Default.GridView),
    MEDIA("media", "Media", Icons.Default.PermMedia),
    GAMES("games", "Games", Icons.Default.SportsEsports),
    SETTINGS("settings", "Settings", Icons.Default.Settings),
}

fun Tool.parentDestination(): String = when (section) { "Media" -> "media"; "Games" -> "games"; else -> "home" }

fun Tool.matchesSearch(query: String): Boolean {
    val aliases = when (route) {
        "calculator" -> "math arithmetic expression"
        "tip" -> "split bill dinner restaurant"
        "emi" -> "loan mortgage monthly payment interest"
        "units" -> "convert length weight temperature speed area data kilogram meter"
        "qr-maker" -> "qr generate barcode code"
        "qr-scanner" -> "qr scan barcode camera code"
        "images" -> "photo picture compress resize jpeg"
        "music" -> "songs audio mp3 flac playlist"
        "video" -> "movie mp4 mkv subtitles player"
        "pdf" -> "documents files book reader folder"
        "browser" -> "web internet incognito private"
        "random" -> "dice choose list number"
        "race" -> "car driving racing 2d"
        else -> ""
    }
    val searchable = "$title $subtitle $section $aliases"
    return query.trim().split(Regex("\\s+")).all { it.isEmpty() || searchable.contains(it, true) }
}

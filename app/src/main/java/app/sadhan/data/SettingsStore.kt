package app.sadhan.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class AppSettings(
    val internet: Boolean = true,
    val weather: Boolean = true,
    val currency: Boolean = true,
    val youtube: Boolean = true,
    val updates: Boolean = true,
)

class SettingsStore(context: Context) {
    private val preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    var value by mutableStateOf(
        AppSettings(
            preferences.getBoolean("internet", true),
            preferences.getBoolean("weather", true),
            preferences.getBoolean("currency", true),
            preferences.getBoolean("youtube", true),
            preferences.getBoolean("updates", true),
        )
    )
        private set

    fun update(settings: AppSettings) {
        preferences.edit()
            .putBoolean("internet", settings.internet)
            .putBoolean("weather", settings.weather)
            .putBoolean("currency", settings.currency)
            .putBoolean("youtube", settings.youtube)
            .putBoolean("updates", settings.updates)
            .apply()
        value = settings
    }
}

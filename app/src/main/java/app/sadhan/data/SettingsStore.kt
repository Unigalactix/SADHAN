package app.sadhan.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ThemeMode(val label: String) { SYSTEM("System"), LIGHT("Light"), DARK("Dark") }

data class AppSettings(
    val internet: Boolean = true,
    val weather: Boolean = true,
    val currency: Boolean = true,
    val youtube: Boolean = true,
    val updates: Boolean = true,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val browser: Boolean = true,
    val documentFolder: String? = null,
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
            ThemeMode.entries.firstOrNull { it.name == preferences.getString("theme", ThemeMode.SYSTEM.name) }
                ?: ThemeMode.SYSTEM.also { android.util.Log.w("SADHAN", "Invalid theme preference; using System") },
            preferences.getBoolean("browser", true),
            preferences.getString("documentFolder", null),
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
            .putString("theme", settings.theme.name)
            .putBoolean("browser", settings.browser)
            .putString("documentFolder", settings.documentFolder)
            .apply()
        value = settings
    }
}

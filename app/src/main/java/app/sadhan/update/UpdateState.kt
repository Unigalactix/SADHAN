package app.sadhan.update

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONException
import java.io.IOException

class UpdateState {
    var update by mutableStateOf<AppUpdate?>(null)
        private set
    var checking by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    suspend fun check() {
        if (checking) return
        checking = true
        message = null
        try {
            update = UpdateChecker.check()
            message = if (update == null) "You're up to date (v${app.sadhan.BuildConfig.VERSION_NAME})" else null
        } catch (e: IOException) { message = "Could not check for updates: ${e.message}" }
        catch (e: JSONException) { message = "Invalid release response: ${e.message}" }
        catch (e: IllegalArgumentException) { message = "Invalid release version: ${e.message}" }
        finally { checking = false }
    }
}

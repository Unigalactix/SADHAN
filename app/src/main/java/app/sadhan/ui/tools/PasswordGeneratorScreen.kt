package app.sadhan.ui.tools

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import app.sadhan.core.securePassword
import app.sadhan.ui.*
import kotlin.math.roundToInt

@Composable
fun PasswordGeneratorScreen() {
    val context = LocalContext.current
    var length by remember { mutableFloatStateOf(20f) }
    var upper by remember { mutableStateOf(true) }
    var lower by remember { mutableStateOf(true) }
    var digits by remember { mutableStateOf(true) }
    var symbols by remember { mutableStateOf(true) }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    ReportTabWork("password", if (password.isNotEmpty()) "The generated password will be erased from this tab." else null)
    ToolColumn {
        Text("Cryptographically secure randomness. Passwords are never saved or sent anywhere.")
        Text("Length: ${length.roundToInt()}")
        Slider(length, { length = it }, valueRange = 12f..64f, steps = 51)
        listOf(Triple("Uppercase", upper, { value: Boolean -> upper = value }), Triple("Lowercase", lower, { value: Boolean -> lower = value }), Triple("Numbers", digits, { value: Boolean -> digits = value }), Triple("Symbols", symbols, { value: Boolean -> symbols = value })).forEach { (label, checked, change) ->
            Row { Text(label, Modifier.weight(1f)); Switch(checked, change) }
        }
        Button(onClick = {
            try { password = securePassword(length.roundToInt(), upper, lower, digits, symbols); error = null }
            catch (e: IllegalArgumentException) { error = e.message }
        }) { Text("Generate password") }
        ErrorText(error)
        if (password.isNotEmpty()) {
            OutlinedTextField(password, {}, readOnly = true, label = { Text("Generated password") }, modifier = Modifier.fillMaxWidth())
            OutlinedButton(onClick = { copy(context, password, sensitive = true) }) { Text("Copy password") }
        }
        Text("Copying places the password on Android's clipboard. Store it in a trusted password manager, not in this toolbox.")
    }
}

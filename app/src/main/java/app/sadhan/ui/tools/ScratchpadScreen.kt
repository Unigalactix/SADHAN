package app.sadhan.ui.tools

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.sadhan.ui.CopyButton
import app.sadhan.ui.Input
import app.sadhan.ui.ToolColumn

@Composable
fun ScratchpadScreen() {
    var text by remember { mutableStateOf("") }
    ToolColumn {
        Text("Kept only in this open tab. Closing the tab erases it; nothing is saved to disk.")
        Input("Temporary note", text, { text = it }, singleLine = false)
        OutlinedButton(onClick = { text = "" }) { Text("Erase now") }
        if (text.isNotEmpty()) CopyButton(text)
    }
}

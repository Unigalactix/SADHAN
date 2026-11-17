package app.sadhan.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import app.sadhan.ui.CopyButton
import app.sadhan.ui.Input
import app.sadhan.ui.ResultCard
import app.sadhan.ui.ToolColumn
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextToolsScreen() {
    var text by remember { mutableStateOf("") }
    val words = remember(text) { Regex("\\S+").findAll(text).count() }
    ToolColumn {
        Input("Your text", text, { text = it }, singleLine = false)
        ResultCard("Live counts", "$words words | ${text.codePointCount(0, text.length)} characters | ${if (text.isEmpty()) 0 else text.lines().size} lines")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("UPPERCASE", "lowercase", "Title Case", "Dedupe lines", "Trim", "Clear").forEach { action ->
                OutlinedButton(onClick = {
                    text = when (action) {
                        "UPPERCASE" -> text.uppercase(Locale.getDefault())
                        "lowercase" -> text.lowercase(Locale.getDefault())
                        "Title Case" -> Regex("\\S+").replace(text.lowercase(Locale.getDefault())) { it.value.replaceFirstChar { c -> c.titlecase(Locale.getDefault()) } }
                        "Dedupe lines" -> text.lines().distinct().joinToString("\n")
                        "Trim" -> text.lines().joinToString("\n") { it.trim() }.trim()
                        else -> ""
                    }
                }) { Text(action) }
            }
        }
        if (text.isNotEmpty()) CopyButton(text)
    }
}

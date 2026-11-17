package app.sadhan.ui

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun DeveloperScreen() {
    val context = LocalContext.current
    ToolColumn {
        ResultCard("About developer", "Rajesh Kodaganti (Rugved)")
        Text("Creator of SADHAN", style = MaterialTheme.typography.titleLarge)
        Text("AI Software Engineer at Microsoft & Quadrant Technologies | MS Grad in Computer Engineering")
        Text("SADHAN is open source under the MIT license. Explore my work or connect using the links below.")
        Button(onClick = { openUrl(context, "https://rajeshkodaganti.com") }) { Text("Visit portfolio") }
        OutlinedButton(onClick = { openUrl(context, "https://www.linkedin.com/in/rajesh-kodaganti-323118215/") }) { Text("Connect on LinkedIn") }
        OutlinedButton(onClick = { openUrl(context, "https://github.com/Unigalactix") }) { Text("GitHub profile") }
    }
}

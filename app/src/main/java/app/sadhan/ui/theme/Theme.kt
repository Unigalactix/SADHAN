package app.sadhan.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val Amber = Color(0xFFFFB300)

@Composable
fun SadhanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Amber, onPrimary = Color.Black,
            secondary = Amber, onSecondary = Color.Black,
            background = Color.Black, onBackground = Color.White,
            surface = Color(0xFF151515), onSurface = Color.White,
            surfaceVariant = Color(0xFF222222), onSurfaceVariant = Color(0xFFBBBBBB),
        ),
        shapes = Shapes(medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(20.dp)),
        content = content,
    )
}

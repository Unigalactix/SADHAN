package app.sadhan.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import app.sadhan.data.ThemeMode

val Amber = Color(0xFFFF851B)

@Composable
fun SadhanTheme(mode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val activity = LocalContext.current as ComponentActivity
    SideEffect {
        val bars = if (dark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        activity.enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
    }
    val colors = if (dark) darkColorScheme(
        primary = Amber, onPrimary = Color.Black,
        primaryContainer = Color(0xFF493018), onPrimaryContainer = Color(0xFFFFDEC3),
        secondary = Color(0xFF26BADA), onSecondary = Color.Black,
        secondaryContainer = Color(0xFF123C4B), onSecondaryContainer = Color(0xFF9CE4F5),
        background = Color(0xFF06101F), onBackground = Color(0xFFF1F5F8),
        surface = Color(0xFF0D1D30), onSurface = Color(0xFFF1F5F8),
        surfaceVariant = Color(0xFF203449), onSurfaceVariant = Color(0xFFB2C4D0),
        surfaceContainerLow = Color(0xFF11263A), surfaceContainer = Color(0xFF172D43),
        surfaceContainerHigh = Color(0xFF20394F), outlineVariant = Color(0xFF345066),
    ) else lightColorScheme(
        primary = Color(0xFFA84700), onPrimary = Color.White,
        primaryContainer = Color(0xFFFFDBC3), onPrimaryContainer = Color(0xFF351000),
        secondary = Color(0xFF00677D), onSecondary = Color.White,
        secondaryContainer = Color(0xFFD4EDF4), onSecondaryContainer = Color(0xFF003B49),
        background = Color(0xFFEDF4F7), onBackground = Color(0xFF102135),
        surface = Color(0xFFF9FCFE), onSurface = Color(0xFF102135),
        surfaceVariant = Color(0xFFDCE7EE), onSurfaceVariant = Color(0xFF4A6070),
        surfaceContainerLow = Color(0xFFF7FBFE), surfaceContainer = Color(0xFFE4EEF4),
        surfaceContainerHigh = Color(0xFFD9E7F0), outlineVariant = Color(0xFFC5D5E0),
    )
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(
            headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-1).sp),
            headlineSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 25.sp, lineHeight = 32.sp),
            titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 23.sp, lineHeight = 30.sp),
            titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
        ),
        shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(24.dp), extraLarge = RoundedCornerShape(28.dp)),
        content = content,
    )
}

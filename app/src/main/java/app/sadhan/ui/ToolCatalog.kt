package app.sadhan.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Piano
import androidx.compose.ui.graphics.vector.ImageVector

data class Tool(val route: String, val title: String, val subtitle: String, val section: String, val icon: ImageVector, val beta: Boolean = false)

val toolCatalog = listOf(
    Tool("calculator", "Calculator", "Expressions, no fuss", "Everyday", Icons.Default.Calculate),
    Tool("tip", "Tip & Split", "Share a bill fairly", "Everyday", Icons.Default.Percent),
    Tool("emi", "EMI Calculator", "Plan your payments", "Everyday", Icons.Default.Savings),
    Tool("dates", "Age & Dates", "Count the days", "Everyday", Icons.Default.Today),
    Tool("units", "Unit Converter", "Six kinds of measure", "Everyday", Icons.Default.Straighten),
    Tool("percentage", "Percentage", "Shares and changes", "Everyday", Icons.Default.Percent),
    Tool("discount", "Discount & Tax", "Know the final price", "Everyday", Icons.Default.LocalOffer),
    Tool("qr-maker", "QR Maker", "Create, save, share", "Pocket", Icons.Default.QrCode),
    Tool("qr-scanner", "QR Scanner", "Point and decode", "Pocket", Icons.Default.QrCodeScanner),
    Tool("text", "Text Tools", "Count and transform", "Pocket", Icons.Default.TextFields),
    Tool("scratchpad", "Scratchpad", "Write it, then forget it", "Pocket", Icons.Default.EditNote),
    Tool("images", "Image Compressor", "Smaller photos, locally", "Pocket", Icons.Default.Compress),
    Tool("random", "Random Picker", "Lists, numbers and dice", "Pocket", Icons.Default.Casino),
    Tool("password", "Password Generator", "Secure, on-device secrets", "Pocket", Icons.Default.Password),
    Tool("stopwatch", "Stopwatch", "Every split second", "Time", Icons.Default.Timer),
    Tool("timer", "Timer", "Turn the dial", "Time", Icons.Default.Schedule),
    Tool("clock", "World Clock", "Ten cities, right now", "Time", Icons.Default.Language),
    Tool("weather", "Weather", "A look outside", "Internet", Icons.Default.WbSunny),
    Tool("currency", "Currency", "Reference exchange rates", "Internet", Icons.Default.Payments),
    Tool("youtube", "YouTube", "Official embed player", "Internet", Icons.Default.PlayCircle, beta = true),
    Tool("browser", "Private Browser", "Session-only private tabs", "Internet", Icons.Default.Public, beta = true),
    Tool("flashlight", "Flashlight", "A little light", "Sensors", Icons.Default.FlashlightOn),
    Tool("compass", "Compass", "Find magnetic north", "Sensors", Icons.Default.Explore),
    Tool("level", "Bubble Level", "Find your balance", "Sensors", Icons.Default.WorkspacePremium),
    Tool("music", "Music Player", "Your local audio", "Media", Icons.Default.MusicNote),
    Tool("video", "Video Player", "Watch a local file", "Media", Icons.Default.VideoLibrary),
    Tool("pdf", "PDF Reader", "Read, zoom, turn pages", "Media", Icons.Default.PictureAsPdf),
    Tool("snake", "Snake", "A pocket arcade classic", "Games", Icons.Default.SportsEsports),
    Tool("solitaire", "Solitaire", "Draw-one Klondike", "Games", Icons.Default.Style),
    Tool("chess", "Chess", "Local two-player strategy", "Games", Icons.Default.Extension),
    Tool("runner", "Pixel Runner", "An original jump-and-run", "Games", Icons.Default.DirectionsRun),
    Tool("race", "Lane Racer", "A three-lane 2D racer", "Games", Icons.Default.DirectionsCar),
    Tool("rhythm", "Rhythm Tiles", "Tap to procedural piano", "Games", Icons.Default.Piano),
    Tool("blocks", "Block Drop", "Original falling-block puzzle", "Games", Icons.Default.Extension),
)

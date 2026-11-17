package app.sadhan.ui.games

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.sadhan.games.*
import app.sadhan.ui.ErrorText
import app.sadhan.ui.ReportTabWork

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SolitaireScreen() {
    var game by remember { mutableStateOf(SolitaireGame.new()) }
    val history = remember { mutableStateListOf<SolitaireGame>() }
    ReportTabWork("solitaire", if (game.moves > 0) "Your solitaire game and undo history will be erased." else null)
    var selected by remember { mutableStateOf<CardSource?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    fun change(action: () -> SolitaireGame) {
        try {
            val next = action()
            if (next != game) { history.add(game); game = next }
            selected = null; error = null
        } catch (e: IllegalArgumentException) { error = e.message }
    }
    GamePage(
        help = "Draw-one Klondike with unlimited redeals. Select a face-up card or sequence, then a numbered tableau pile or foundation. Build downward in alternating colors; empty piles require Kings. Foundations build Ace to King by suit. The tableau scrolls sideways on narrow panes.",
        status = {
        Text(if (game.won) "All foundations complete!" else "Klondike Solitaire", style = MaterialTheme.typography.titleLarge)
        Text("${game.moves} moves · Stock ${game.stock.size} · Foundations ${game.foundations.sumOf { it.size }}/52")
        },
        board = { modifier -> Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { change { game.draw() } }, enabled = !game.won && (game.stock.isNotEmpty() || game.waste.isNotEmpty())) { Text(if (game.stock.isEmpty()) "Recycle waste" else "Draw one") }
            game.waste.lastOrNull()?.let { card -> PlayingCardView(card, selected?.let { it.column == null && it.foundation == null } == true) { selected = CardSource(); error = null } }
        }
        Text("Foundations — Ace to King", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) { foundation ->
                PlayingCardView(game.foundations[foundation].lastOrNull(), selected?.foundation == foundation) {
                    val source = selected
                    if (source != null) change { game.moveToFoundation(source, foundation) }
                    else if (game.foundations[foundation].isNotEmpty()) selected = CardSource(foundation = foundation)
                    else error = "Select an Ace or a matching next card first"
                }
            }
        }
        Text("Tableau — alternating colors, descending ranks", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            game.columns.forEachIndexed { column, pile ->
                Column(Modifier.width(52.dp), verticalArrangement = Arrangement.spacedBy(5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedButton(onClick = {
                        selected?.let { source -> change { game.moveToColumn(source, column) } }
                            ?: run { error = "Select a face-up card or sequence first" }
                    }, contentPadding = PaddingValues(4.dp)) { Text("${column + 1}") }
                    Text("${pile.count { !it.faceUp }} down", style = MaterialTheme.typography.labelSmall)
                    pile.forEachIndexed { index, card ->
                        if (card.faceUp) PlayingCardView(card.card, selected == CardSource(column, index)) { selected = CardSource(column, index); error = null }
                    }
                    if (pile.isEmpty()) Text("King", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        } },
        controls = { ErrorText(error) },
        footer = {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(enabled = history.isNotEmpty(), onClick = { game = history.removeAt(history.lastIndex); selected = null; error = null }) { Text("Undo") }
            OutlinedButton(onClick = { game = SolitaireGame.new(); history.clear(); selected = null; error = null }) { Text("New game") }
        }
        },
    )
}

@Composable
private fun PlayingCardView(card: PlayingCard?, selected: Boolean, click: () -> Unit) {
    Surface(
        Modifier.width(52.dp).height(64.dp).clickable(onClick = click),
        color = if (selected) Color(0xFFFFD6A9) else Color(0xFFF9F7EE),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else Color(0xFFA8B5BD)),
    ) {
        Box(contentAlignment = Alignment.Center) { Text(card?.label ?: "A", color = if (card?.suit?.red == true) Color(0xFFB5203F) else Color(0xFF102333), style = MaterialTheme.typography.titleMedium) }
    }
}

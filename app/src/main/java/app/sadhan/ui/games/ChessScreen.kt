package app.sadhan.ui.games

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sadhan.games.*
import app.sadhan.ui.ErrorText
import app.sadhan.ui.ToolColumn

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChessScreen() {
    var game by remember { mutableStateOf(ChessGame.new()) }
    val history = remember { mutableStateListOf<ChessGame>() }
    var selected by remember { mutableStateOf<Int?>(null) }
    var promotion by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var finished by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val legal = remember(game, selected) { selected?.let(game::legalMoves) ?: emptyList() }
    val hasMove = remember(game) { game.hasLegalMove }
    val check = remember(game) { game.inCheck(game.turn) }
    val automatic = when {
        !hasMove -> if (check) "Checkmate — ${game.turn.other().name.lowercase()} wins" else "Draw — stalemate"
        game.insufficientMaterial -> "Draw — insufficient material"
        game.halfmoves >= 150 || game.repetitions.getOrDefault(game.positionKey(), 0) >= 5 -> "Draw — repetition / move rule"
        else -> null
    }
    val ended = finished ?: automatic
    fun play(from: Int, to: Int, kind: ChessKind = ChessKind.QUEEN) {
        try {
            val next = game.move(from, to, kind)
            history.add(game); game = next; selected = null; promotion = null; error = null
        } catch (e: IllegalArgumentException) { error = e.message }
    }
    ToolColumn {
        Text(ended ?: "${game.turn.name.lowercase().replaceFirstChar(Char::uppercase)} to move${if (check) " — check" else ""}", style = MaterialTheme.typography.titleLarge)
        BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val boardSize = maxWidth.coerceAtMost(480.dp)
            val square = boardSize / 8
            Column(Modifier.size(boardSize)) {
                repeat(8) { row ->
                    Row {
                        repeat(8) { column ->
                            val index = row * 8 + column
                            val piece = game.board[index]
                            Surface(
                                Modifier.size(square).semantics { contentDescription = "${('a'.code + column).toChar()}${8 - row} ${piece?.let { "${it.side.name} ${it.kind.name}" } ?: "empty"}" }
                                    .clickable(enabled = ended == null) {
                                        val from = selected
                                        if (piece?.side == game.turn) { selected = index; error = null }
                                        else if (from != null && index in legal) {
                                            if (game.board[from]?.kind == ChessKind.PAWN && row in listOf(0, 7)) promotion = from to index
                                            else play(from, index)
                                        } else error = "Select your piece, then a highlighted legal square"
                                    },
                                color = when { selected == index -> Color(0xFFFFB46A); index in legal -> Color(0xFF75C8D1); (row + column) % 2 == 0 -> Color(0xFFDAE5EA); else -> Color(0xFF718C9F) },
                                border = if (index in legal) BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (piece != null) Text(piece.glyph, fontSize = (square.value * .7).sp, color = if (piece.side == ChessSide.WHITE) Color.White else Color(0xFF091728),
                                        style = MaterialTheme.typography.bodyLarge.copy(shadow = androidx.compose.ui.graphics.Shadow(Color.Black, blurRadius = 2f)))
                                    else if (index in legal) Text("•", color = Color(0xFF094E66))
                                }
                            }
                        }
                    }
                }
            }
        }
        ErrorText(error)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(enabled = history.isNotEmpty(), onClick = { game = history.removeAt(history.lastIndex); selected = null; finished = null }) { Text("Undo") }
            OutlinedButton(enabled = ended == null && game.canClaimDraw, onClick = { finished = "Draw claimed" }) { Text("Claim draw") }
            OutlinedButton(onClick = { game = ChessGame.new(); history.clear(); selected = null; finished = null; error = null }) { Text("New game") }
        }
        Text("Local two-player chess. Legal-move highlighting, castling, en passant, promotion, checkmate, stalemate and draw rules. No engine, accounts or saved games.", style = MaterialTheme.typography.bodySmall)
    }
    promotion?.let { (from, to) ->
        AlertDialog(onDismissRequest = { promotion = null }, title = { Text("Promote pawn") }, text = {
            Column { listOf(ChessKind.QUEEN, ChessKind.ROOK, ChessKind.BISHOP, ChessKind.KNIGHT).forEach { kind ->
                TextButton(onClick = { play(from, to, kind) }) { Text(kind.name.lowercase().replaceFirstChar(Char::uppercase)) }
            } }
        }, confirmButton = {})
    }
}

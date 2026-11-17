package app.sadhan.games

import kotlin.math.abs

enum class ChessSide { WHITE, BLACK; fun other() = if (this == WHITE) BLACK else WHITE }
enum class ChessKind { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }
data class ChessPiece(val kind: ChessKind, val side: ChessSide) {
    val glyph: String get() = (if (side == ChessSide.WHITE) "♔♕♖♗♘♙" else "♚♛♜♝♞♟")[kind.ordinal].toString()
}

data class ChessGame(
    val board: List<ChessPiece?>,
    val turn: ChessSide = ChessSide.WHITE,
    val castling: String = "KQkq",
    val enPassant: Int? = null,
    val halfmoves: Int = 0,
    val repetitions: Map<String, Int> = emptyMap(),
) {
    private fun inside(x: Int, y: Int) = x in 0..7 && y in 0..7
    fun inCheck(side: ChessSide): Boolean {
        val king = board.indexOfFirst { it?.kind == ChessKind.KING && it.side == side }
        return king < 0 || attacked(king, side.other())
    }

    private fun attacked(square: Int, by: ChessSide): Boolean {
        val x = square % 8; val y = square / 8
        return board.indices.any { from ->
            val piece = board[from]
            if (piece == null || piece.side != by) false else {
                val dx = x - from % 8; val dy = y - from / 8
                when (piece.kind) {
                    ChessKind.PAWN -> abs(dx) == 1 && dy == if (by == ChessSide.WHITE) -1 else 1
                    ChessKind.KNIGHT -> abs(dx) * abs(dy) == 2
                    ChessKind.KING -> maxOf(abs(dx), abs(dy)) == 1
                    else -> {
                        val diagonal = abs(dx) == abs(dy) && dx != 0
                        val straight = (dx == 0) != (dy == 0)
                        val shape = when (piece.kind) { ChessKind.BISHOP -> diagonal; ChessKind.ROOK -> straight; else -> diagonal || straight }
                        if (!shape) false else {
                            val sx = dx.compareTo(0); val sy = dy.compareTo(0)
                            (1 until maxOf(abs(dx), abs(dy))).all { step -> board[(from / 8 + sy * step) * 8 + from % 8 + sx * step] == null }
                        }
                    }
                }
            }
        }
    }

    fun legalMoves(from: Int): List<Int> {
        val piece = board.getOrNull(from) ?: return emptyList()
        if (piece.side != turn) return emptyList()
        val x = from % 8; val y = from / 8
        val candidates = mutableListOf<Int>()
        fun add(nx: Int, ny: Int) {
            if (inside(nx, ny)) {
                val target = board[ny * 8 + nx]
                if (target?.side != turn && target?.kind != ChessKind.KING) candidates.add(ny * 8 + nx)
            }
        }
        when (piece.kind) {
            ChessKind.PAWN -> {
                val step = if (turn == ChessSide.WHITE) -1 else 1
                if (inside(x, y + step) && board[(y + step) * 8 + x] == null) {
                    add(x, y + step)
                    if (y == if (turn == ChessSide.WHITE) 6 else 1) {
                        if (board[(y + step * 2) * 8 + x] == null) add(x, y + step * 2)
                    }
                }
                listOf(-1, 1).forEach { dx ->
                    if (inside(x + dx, y + step)) {
                        val target = (y + step) * 8 + x + dx
                        if (board[target]?.side == turn.other() || target == enPassant) add(x + dx, y + step)
                    }
                }
            }
            ChessKind.KNIGHT -> listOf(-2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1).forEach { (dx, dy) -> add(x + dx, y + dy) }
            ChessKind.KING -> {
                for (dx in -1..1) for (dy in -1..1) if (dx != 0 || dy != 0) add(x + dx, y + dy)
                val home = if (turn == ChessSide.WHITE) 60 else 4
                if (from == home && !inCheck(turn)) {
                    listOf(2 to if (turn == ChessSide.WHITE) 'K' else 'k', -2 to if (turn == ChessSide.WHITE) 'Q' else 'q').forEach { (offset, right) ->
                        val rook = home + if (offset > 0) 3 else -4
                        val path = if (offset > 0) (home + 1)..(home + 2) else (home - 3)..(home - 1)
                        val step = offset.compareTo(0)
                        if (right in castling && board[rook] == ChessPiece(ChessKind.ROOK, turn) && path.all { board[it] == null } &&
                            !attacked(home + step, turn.other()) && !attacked(home + offset, turn.other())) candidates.add(home + offset)
                    }
                }
            }
            else -> {
                val directions = when (piece.kind) {
                    ChessKind.ROOK -> listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
                    ChessKind.BISHOP -> listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
                    else -> listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1, -1 to -1, -1 to 1, 1 to -1, 1 to 1)
                }
                directions.forEach { (dx, dy) ->
                    var nx = x + dx; var ny = y + dy
                    while (inside(nx, ny)) {
                        add(nx, ny)
                        if (board[ny * 8 + nx] != null) break
                        nx += dx; ny += dy
                    }
                }
            }
        }
        return candidates.filter { !apply(from, it, ChessKind.QUEEN).inCheck(turn) }
    }

    private fun apply(from: Int, to: Int, promotion: ChessKind): ChessGame {
        val piece = requireNotNull(board[from])
        val next = board.toMutableList()
        val capture = next[to] != null || piece.kind == ChessKind.PAWN && to == enPassant
        next[from] = null
        if (piece.kind == ChessKind.PAWN && to == enPassant && from % 8 != to % 8) next[to + if (turn == ChessSide.WHITE) 8 else -8] = null
        next[to] = if (piece.kind == ChessKind.PAWN && to / 8 in listOf(0, 7)) piece.copy(kind = promotion) else piece
        if (piece.kind == ChessKind.KING && abs(to - from) == 2) {
            val rook = if (to > from) from + 3 else from - 4
            next[if (to > from) from + 1 else from - 1] = next[rook]; next[rook] = null
        }
        var rights = castling
        if (piece.kind == ChessKind.KING) rights = rights.filterNot { it in if (turn == ChessSide.WHITE) "KQ" else "kq" }
        listOf(0 to 'q', 7 to 'k', 56 to 'Q', 63 to 'K').forEach { (square, right) -> if (from == square || to == square) rights = rights.replace(right.toString(), "") }
        return copy(board = next, turn = turn.other(), castling = rights,
            enPassant = if (piece.kind == ChessKind.PAWN && abs(to - from) == 16) (from + to) / 2 else null,
            halfmoves = if (piece.kind == ChessKind.PAWN || capture) 0 else halfmoves + 1)
    }

    fun move(from: Int, to: Int, promotion: ChessKind = ChessKind.QUEEN): ChessGame {
        require(to in legalMoves(from)) { "That move is not legal" }
        require(promotion in listOf(ChessKind.QUEEN, ChessKind.ROOK, ChessKind.BISHOP, ChessKind.KNIGHT)) { "Choose a valid promotion piece" }
        val next = apply(from, to, promotion)
        val key = next.positionKey()
        return next.copy(repetitions = repetitions + (key to (repetitions.getOrDefault(key, 0) + 1)))
    }

    fun positionKey(): String {
        val ep = enPassant?.takeIf { target ->
            board.indices.any { from -> board[from] == ChessPiece(ChessKind.PAWN, turn) && abs(from % 8 - target % 8) == 1 &&
                target / 8 - from / 8 == (if (turn == ChessSide.WHITE) -1 else 1) &&
                !apply(from, target, ChessKind.QUEEN).inCheck(turn) }
        }
        return board.joinToString("") { it?.let { piece -> "${piece.side.ordinal}${piece.kind.ordinal}" } ?: ".." } + "$turn$castling$ep"
    }

    val hasLegalMove get() = board.indices.any { board[it]?.side == turn && legalMoves(it).isNotEmpty() }
    val canClaimDraw get() = halfmoves >= 100 || repetitions.getOrDefault(positionKey(), 0) >= 3
    val insufficientMaterial: Boolean get() {
        val pieces = board.withIndex().filter { it.value != null && it.value!!.kind != ChessKind.KING }
        return pieces.isEmpty() || pieces.size == 1 && pieces[0].value!!.kind in listOf(ChessKind.KNIGHT, ChessKind.BISHOP) ||
            pieces.all { it.value!!.kind == ChessKind.BISHOP } && pieces.map { (it.index % 8 + it.index / 8) % 2 }.distinct().size == 1
    }

    companion object {
        fun new(): ChessGame {
            val row = listOf(ChessKind.ROOK, ChessKind.KNIGHT, ChessKind.BISHOP, ChessKind.QUEEN, ChessKind.KING, ChessKind.BISHOP, ChessKind.KNIGHT, ChessKind.ROOK)
            val game = ChessGame(List(64) { index ->
                when (index / 8) {
                    0 -> ChessPiece(row[index % 8], ChessSide.BLACK)
                    1 -> ChessPiece(ChessKind.PAWN, ChessSide.BLACK)
                    6 -> ChessPiece(ChessKind.PAWN, ChessSide.WHITE)
                    7 -> ChessPiece(row[index % 8], ChessSide.WHITE)
                    else -> null
                }
            })
            return game.copy(repetitions = mapOf(game.positionKey() to 1))
        }
    }
}

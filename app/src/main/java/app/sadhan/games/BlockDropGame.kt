package app.sadhan.games

import kotlin.random.Random

private val blockShapes = listOf(
    listOf(Cell(0, 1), Cell(1, 1), Cell(2, 1), Cell(3, 1)),
    listOf(Cell(1, 0), Cell(2, 0), Cell(1, 1), Cell(2, 1)),
    listOf(Cell(1, 0), Cell(0, 1), Cell(1, 1), Cell(2, 1)),
    listOf(Cell(1, 0), Cell(2, 0), Cell(0, 1), Cell(1, 1)),
    listOf(Cell(0, 0), Cell(1, 0), Cell(1, 1), Cell(2, 1)),
    listOf(Cell(0, 0), Cell(0, 1), Cell(1, 1), Cell(2, 1)),
    listOf(Cell(2, 0), Cell(0, 1), Cell(1, 1), Cell(2, 1)),
)

data class FallingPiece(val type: Int, val cells: List<Cell> = blockShapes[type], val x: Int = 3, val y: Int = 0)
data class BlockDropGame(
    val board: List<Int> = List(180) { 0 },
    val piece: FallingPiece = FallingPiece(2),
    val next: Int = 0,
    val bag: List<Int> = emptyList(),
    val score: Int = 0,
    val lines: Int = 0,
    val ended: Boolean = false,
) {
    val level get() = lines / 10 + 1
    fun fits(candidate: FallingPiece): Boolean = candidate.cells.all {
        val x = candidate.x + it.x; val y = candidate.y + it.y
        x in 0..9 && y in 0..17 && board[y * 10 + x] == 0
    }
    fun move(dx: Int): BlockDropGame {
        val candidate = piece.copy(x = piece.x + dx)
        return if (!ended && fits(candidate)) copy(piece = candidate) else this
    }
    fun rotate(): BlockDropGame {
        if (ended || piece.type == 1) return this
        val size = if (piece.type == 0) 4 else 3
        val cells = piece.cells.map { Cell(size - 1 - it.y, it.x) }
        val candidate = listOf(0, -1, 1, -2, 2).map { offset -> piece.copy(cells = cells, x = piece.x + offset) }.firstOrNull(::fits)
        return candidate?.let { copy(piece = it) } ?: this
    }
    fun step(random: Random = Random.Default): BlockDropGame {
        if (ended) return this
        val down = piece.copy(y = piece.y + 1)
        if (fits(down)) return copy(piece = down)
        val occupied = board.toMutableList()
        piece.cells.forEach { occupied[(piece.y + it.y) * 10 + piece.x + it.x] = piece.type + 1 }
        val remaining = occupied.chunked(10).filter { row -> row.any { it == 0 } }
        val cleared = 18 - remaining.size
        val newBoard = List(cleared * 10) { 0 } + remaining.flatten()
        val order = if (bag.isEmpty()) (0..6).shuffled(random) else bag
        val result = copy(board = newBoard, piece = FallingPiece(next), next = order.first(), bag = order.drop(1),
            score = score + listOf(0, 100, 300, 500, 800)[cleared] * level, lines = lines + cleared)
        return result.copy(ended = !result.fits(result.piece))
    }
    fun drop(random: Random = Random.Default): BlockDropGame {
        if (ended) return this
        var falling = this
        var distance = 0
        while (falling.fits(falling.piece.copy(y = falling.piece.y + 1))) {
            falling = falling.copy(piece = falling.piece.copy(y = falling.piece.y + 1)); distance++
        }
        return falling.copy(score = falling.score + distance * 2).step(random)
    }
    companion object {
        fun new(random: Random = Random.Default): BlockDropGame {
            val bag = (0..6).shuffled(random)
            return BlockDropGame(piece = FallingPiece(bag[0]), next = bag[1], bag = bag.drop(2))
        }
    }
}

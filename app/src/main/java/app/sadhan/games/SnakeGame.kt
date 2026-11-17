package app.sadhan.games

import kotlin.random.Random

data class Cell(val x: Int, val y: Int)
enum class Direction(val dx: Int, val dy: Int) { UP(0, -1), RIGHT(1, 0), DOWN(0, 1), LEFT(-1, 0) }

data class SnakeGame(
    val body: List<Cell> = listOf(Cell(9, 10), Cell(8, 10), Cell(7, 10)),
    val direction: Direction = Direction.RIGHT,
    val queued: Direction = direction,
    val food: Cell = Cell(14, 10),
    val score: Int = 0,
    val ended: Boolean = false,
) {
    fun turn(next: Direction): SnakeGame =
        if (next.dx + direction.dx == 0 && next.dy + direction.dy == 0) this else copy(queued = next)

    fun step(random: Random = Random.Default): SnakeGame {
        if (ended) return this
        val head = Cell(body[0].x + queued.dx, body[0].y + queued.dy)
        val growing = head == food
        val occupied = if (growing) body else body.dropLast(1)
        if (head.x !in 0..19 || head.y !in 0..19 || head in occupied) return copy(ended = true)
        val next = listOf(head) + if (growing) body else body.dropLast(1)
        val empty = if (growing) buildList { for (y in 0..19) for (x in 0..19) if (Cell(x, y) !in next) add(Cell(x, y)) } else emptyList()
        return copy(body = next, direction = queued, score = score + if (growing) 10 else 0,
            food = if (growing && empty.isNotEmpty()) empty[random.nextInt(empty.size)] else food,
            ended = growing && empty.isEmpty())
    }
}

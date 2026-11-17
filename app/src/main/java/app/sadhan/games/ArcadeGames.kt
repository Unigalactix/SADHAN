package app.sadhan.games

import kotlin.math.abs
import kotlin.random.Random

data class RoadCar(val lane: Int, val y: Float)
data class LaneRacerGame(val lane: Int = 1, val cars: List<RoadCar> = emptyList(), val distance: Float = 0f, val spawn: Float = 0f, val ended: Boolean = false) {
    fun steer(direction: Int) = if (ended) this else copy(lane = (lane + direction).coerceIn(0, 2))
    fun step(seconds: Float, random: Random = Random.Default): LaneRacerGame {
        if (ended) return this
        val speed = 5f + (distance / 50).coerceAtMost(9f)
        val moved = cars.map { it.copy(y = it.y + speed * seconds) }.filter { it.y < 22 }
        val crash = moved.any { it.lane == lane && abs(it.y - 17f) < 1.5f }
        val elapsed = spawn + seconds
        val spawnNow = elapsed >= (1.3f - distance / 2000).coerceAtLeast(.65f)
        return copy(cars = moved + if (spawnNow) listOf(RoadCar(random.nextInt(3), -2f)) else emptyList(),
            distance = distance + speed * seconds, spawn = if (spawnNow) 0f else elapsed, ended = crash)
    }
}

data class Platform(val x: Float, val y: Float, val width: Float)
val runnerPlatforms = listOf(Platform(0f, 9f, 18f), Platform(21f, 9f, 14f), Platform(38f, 9f, 24f),
    Platform(7f, 7f, 4f), Platform(14f, 5f, 4f), Platform(27f, 7f, 5f), Platform(42f, 6f, 5f))
val runnerStars = listOf(Cell(9, 6), Cell(16, 4), Cell(29, 6), Cell(44, 5), Cell(53, 8))

data class PixelRunnerGame(
    val x: Float = 2f, val y: Float = 8.1f, val velocityY: Float = 0f, val grounded: Boolean = true,
    val lives: Int = 3, val stars: Set<Int> = emptySet(), val won: Boolean = false, val ended: Boolean = false,
) {
    fun step(seconds: Float, direction: Int, jump: Boolean): PixelRunnerGame {
        if (ended || won) return this
        val dt = seconds.coerceIn(0f, .05f)
        val nx = (x + direction.coerceIn(-1, 1) * 5f * dt).coerceIn(0f, 60f)
        var vy = if (jump && grounded) -9.5f else velocityY
        vy += 20f * dt
        var ny = y + vy * dt
        var landed = false
        if (vy >= 0) runnerPlatforms.sortedBy { it.y }.firstOrNull {
            nx + .7f > it.x && nx < it.x + it.width && y + .9f <= it.y + .03f && ny + .9f >= it.y
        }?.let { ny = it.y - .9f; vy = 0f; landed = true }
        if (ny > 12f) return copy(x = 2f, y = 8.1f, velocityY = 0f, grounded = true, lives = lives - 1, ended = lives <= 1)
        val collected = stars + runnerStars.indices.filter { abs(nx + .35f - runnerStars[it].x) < .8f && abs(ny + .45f - runnerStars[it].y) < 1f }
        return copy(x = nx, y = ny, velocityY = vy, grounded = landed, stars = collected, won = nx >= 58f)
    }
}

data class RhythmTilesGame(val rows: List<Int> = List(4) { Random.nextInt(4) }, val score: Int = 0, val remaining: Float = 30f, val ended: Boolean = false) {
    fun tap(column: Int, row: Int, random: Random = Random.Default): RhythmTilesGame {
        if (ended) return this
        return if (row == 3 && column == rows.last()) copy(rows = listOf(random.nextInt(4)) + rows.dropLast(1), score = score + 1)
            else copy(ended = true)
    }
    fun step(seconds: Float): RhythmTilesGame {
        val time = (remaining - seconds).coerceAtLeast(0f)
        return if (ended) this else copy(remaining = time, ended = time == 0f)
    }
}

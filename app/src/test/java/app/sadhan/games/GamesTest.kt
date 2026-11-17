package app.sadhan.games

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class GamesTest {
    @Test fun snakeRejectsReverseGrowsAndDetectsWall() {
        val initial = SnakeGame()
        assertEquals(Direction.RIGHT, initial.turn(Direction.LEFT).queued)
        val food = initial.copy(food = Cell(10, 10)).step(Random(1))
        assertEquals(4, food.body.size)
        assertEquals(10, food.score)
        assertFalse(food.food in food.body)
        assertTrue(initial.copy(body = listOf(Cell(19, 10))).step().ended)
    }

    @Test fun snakeCanMoveIntoVacatedTailButNotItsBody() {
        val snake = SnakeGame(body = listOf(Cell(1, 1), Cell(1, 2), Cell(0, 2), Cell(0, 1)), direction = Direction.UP, queued = Direction.LEFT)
        assertFalse(snake.step().ended)
        assertTrue(snake.copy(queued = Direction.DOWN).step().ended)
    }

    @Test fun solitaireDealsUniqueCardsAndDrawsRecycleInOrder() {
        val game = SolitaireGame.new(Random(2))
        assertEquals(24, game.stock.size)
        val cards = game.stock + game.columns.flatten().map { it.card }
        assertEquals(52, cards.toSet().size)
        assertEquals((1..7).toList(), game.columns.map { it.size })
        assertTrue(game.columns.all { pile -> pile.count { it.faceUp } == 1 })
        var drawn = game
        repeat(24) { drawn = drawn.draw() }
        assertEquals(game.stock.last(), drawn.draw().draw().waste.last())
    }

    @Test fun solitaireEnforcesAlternationFoundationsAndKingEmptyPile() {
        val ace = PlayingCard(Suit.HEARTS, 1)
        val king = PlayingCard(Suit.SPADES, 13)
        val queen = PlayingCard(Suit.DIAMONDS, 12)
        val game = SolitaireGame(stock = emptyList(), waste = listOf(ace), foundations = List(4) { emptyList() },
            columns = listOf(listOf(TableauCard(king, true)), listOf(TableauCard(queen, true))) + List(5) { emptyList() })
        val foundation = game.moveToFoundation(CardSource(), 0)
        assertEquals(listOf(ace), foundation.foundations[0])
        assertEquals(listOf(king, queen), game.moveToColumn(CardSource(1, 0), 0).columns[0].map { it.card })
        assertThrows(IllegalArgumentException::class.java) { game.moveToColumn(CardSource(1, 0), 2) }
        assertThrows(IllegalArgumentException::class.java) { game.moveToFoundation(CardSource(0, 0), 0) }
    }

    @Test fun chessInitialPositionHasTwentyMovesAndFoolsMateIsMate() {
        val start = ChessGame.new()
        assertEquals(20, start.board.indices.sumOf { start.legalMoves(it).size })
        val mate = start.move(53, 45).move(12, 28).move(54, 38).move(3, 39)
        assertTrue(mate.inCheck(ChessSide.WHITE))
        assertFalse(mate.hasLegalMove)
    }

    private fun position(vararg pieces: Pair<Int, ChessPiece>, turn: ChessSide = ChessSide.WHITE, rights: String = "") =
        ChessGame(List(64) { index -> pieces.firstOrNull { it.first == index }?.second }, turn = turn, castling = rights)

    @Test fun chessCastlingRequiresClearSafePathAndMovesRook() {
        val game = position(60 to ChessPiece(ChessKind.KING, ChessSide.WHITE), 63 to ChessPiece(ChessKind.ROOK, ChessSide.WHITE), 4 to ChessPiece(ChessKind.KING, ChessSide.BLACK), rights = "K")
        val castle = game.move(60, 62)
        assertEquals(ChessKind.ROOK, castle.board[61]?.kind)
        assertNull(castle.board[63])
        val attacked = game.copy(board = game.board.mapIndexed { i, p -> if (i == 5) ChessPiece(ChessKind.ROOK, ChessSide.BLACK) else p })
        assertFalse(62 in attacked.legalMoves(60))
    }

    @Test fun chessHandlesEnPassantPromotionAndPinnedPieces() {
        val ep = ChessGame.new().move(52, 36).move(8, 16).move(36, 28).move(11, 27).move(28, 19)
        assertNull(ep.board[27])
        assertEquals(ChessPiece(ChessKind.PAWN, ChessSide.WHITE), ep.board[19])
        val promotion = position(60 to ChessPiece(ChessKind.KING, ChessSide.WHITE), 4 to ChessPiece(ChessKind.KING, ChessSide.BLACK), 8 to ChessPiece(ChessKind.PAWN, ChessSide.WHITE)).move(8, 0, ChessKind.KNIGHT)
        assertEquals(ChessKind.KNIGHT, promotion.board[0]?.kind)
        val pinned = position(60 to ChessPiece(ChessKind.KING, ChessSide.WHITE), 52 to ChessPiece(ChessKind.ROOK, ChessSide.WHITE),
            4 to ChessPiece(ChessKind.ROOK, ChessSide.BLACK), 0 to ChessPiece(ChessKind.KING, ChessSide.BLACK))
        assertFalse(51 in pinned.legalMoves(52))
    }

    @Test fun chessRepetitionCanBeClaimedAndInsufficientMaterialDetected() {
        var game = ChessGame.new()
        repeat(2) { game = game.move(62, 45).move(6, 21).move(45, 62).move(21, 6) }
        assertTrue(game.canClaimDraw)
        assertTrue(position(60 to ChessPiece(ChessKind.KING, ChessSide.WHITE), 4 to ChessPiece(ChessKind.KING, ChessSide.BLACK)).insufficientMaterial)
    }

    @Test fun blocksClearRowsRotateAtWallsAndSpawnFairBags() {
        val board = MutableList(180) { 0 }
        for (x in 0..5) board[170 + x] = 1
        val game = BlockDropGame(board = board, piece = FallingPiece(0, x = 6, y = 16))
        val cleared = game.step(Random(1))
        assertEquals(1, cleared.lines)
        assertEquals(100, cleared.score)
        assertTrue(cleared.board.takeLast(10).all { it == 0 })
        val initial = BlockDropGame.new(Random(1))
        assertEquals(7, (listOf(initial.piece.type, initial.next) + initial.bag).toSet().size)
        val rotated = initial.copy(piece = FallingPiece(2, x = 0)).rotate()
        assertTrue(rotated.fits(rotated.piece))
    }

    @Test fun arcadeCollisionsAndRhythmRulesAreReal() {
        assertTrue(LaneRacerGame(cars = listOf(RoadCar(1, 16.9f))).step(.01f).ended)
        assertFalse(LaneRacerGame(cars = listOf(RoadCar(1, 16.9f))).steer(-1).step(.01f).ended)
        val rhythm = RhythmTilesGame(rows = listOf(0, 1, 2, 3))
        assertEquals(1, rhythm.tap(3, 3, Random(1)).score)
        assertTrue(rhythm.tap(2, 3).ended)
        assertTrue(rhythm.step(30f).ended)
        val jumping = PixelRunnerGame().step(.05f, 1, true)
        assertTrue(jumping.y < 8.1f && jumping.x > 2f)
        assertEquals(2, PixelRunnerGame(x = 19f, y = 12.1f).step(.01f, 0, false).lives)
    }
}

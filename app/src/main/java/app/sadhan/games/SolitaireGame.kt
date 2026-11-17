package app.sadhan.games

import kotlin.random.Random

enum class Suit(val symbol: String, val red: Boolean) { CLUBS("♣", false), DIAMONDS("♦", true), HEARTS("♥", true), SPADES("♠", false) }
data class PlayingCard(val suit: Suit, val rank: Int) {
    val label: String get() = "${when (rank) { 1 -> "A"; 11 -> "J"; 12 -> "Q"; 13 -> "K"; else -> rank.toString() }}${suit.symbol}"
}
data class TableauCard(val card: PlayingCard, val faceUp: Boolean)
data class CardSource(val column: Int? = null, val index: Int = 0, val foundation: Int? = null)

data class SolitaireGame(
    val stock: List<PlayingCard>,
    val waste: List<PlayingCard>,
    val foundations: List<List<PlayingCard>>,
    val columns: List<List<TableauCard>>,
    val moves: Int = 0,
) {
    val won get() = foundations.sumOf { it.size } == 52
    fun draw(): SolitaireGame = when {
        stock.isNotEmpty() -> copy(stock = stock.dropLast(1), waste = waste + stock.last(), moves = moves + 1)
        waste.isNotEmpty() -> copy(stock = waste.reversed(), waste = emptyList(), moves = moves + 1)
        else -> this
    }

    fun selected(source: CardSource): List<PlayingCard> {
        if (source.foundation != null) return foundations[source.foundation].lastOrNull()?.let { listOf(it) } ?: emptyList()
        if (source.column == null) return waste.lastOrNull()?.let { listOf(it) } ?: emptyList()
        val pile = columns.getOrNull(source.column) ?: return emptyList()
        if (source.index !in pile.indices || !pile[source.index].faceUp) return emptyList()
        return pile.drop(source.index).map { it.card }
    }

    private fun remove(source: CardSource): SolitaireGame {
        if (source.foundation != null) return copy(foundations = foundations.mapIndexed { index, pile -> if (index == source.foundation) pile.dropLast(1) else pile })
        if (source.column == null) return copy(waste = waste.dropLast(1))
        val remaining = columns[source.column].take(source.index).toMutableList()
        if (remaining.isNotEmpty()) remaining[remaining.lastIndex] = remaining.last().copy(faceUp = true)
        return copy(columns = columns.mapIndexed { index, pile -> if (index == source.column) remaining else pile })
    }

    fun moveToColumn(source: CardSource, destination: Int): SolitaireGame {
        require(destination in 0..6 && source.column != destination) { "Choose a different destination pile" }
        val cards = selected(source)
        require(cards.isNotEmpty()) { "Select a face-up card first" }
        require(cards.zipWithNext().all { (a, b) -> a.rank == b.rank + 1 && a.suit.red != b.suit.red }) { "Selected sequence is not in alternating descending order" }
        val top = columns[destination].lastOrNull()?.card
        require(if (top == null) cards.first().rank == 13 else top.rank == cards.first().rank + 1 && top.suit.red != cards.first().suit.red) { "Build downward in alternating colors; empty piles need a King" }
        val removed = remove(source)
        return removed.copy(columns = removed.columns.mapIndexed { index, pile -> if (index == destination) pile + cards.map { TableauCard(it, true) } else pile }, moves = moves + 1)
    }

    fun moveToFoundation(source: CardSource, destination: Int): SolitaireGame {
        require(destination in 0..3) { "Choose a foundation" }
        require(source.foundation != destination) { "Choose a different foundation" }
        val cards = selected(source)
        require(cards.size == 1) { "Only a single top card can move to a foundation" }
        val card = cards.first()
        val target = foundations[destination]
        require(if (target.isEmpty()) card.rank == 1 else target.last().suit == card.suit && target.last().rank + 1 == card.rank) { "Foundations build from Ace to King in the same suit" }
        val removed = remove(source)
        return removed.copy(foundations = foundations.mapIndexed { index, pile -> if (index == destination) pile + card else pile }, moves = moves + 1)
    }

    companion object {
        fun new(random: Random = Random.Default): SolitaireGame {
            val deck = Suit.entries.flatMap { suit -> (1..13).map { PlayingCard(suit, it) } }.shuffled(random)
            var offset = 0
            val columns = List(7) { column ->
                List(column + 1) { index -> TableauCard(deck[offset++], index == column) }
            }
            return SolitaireGame(deck.drop(offset), emptyList(), List(4) { emptyList() }, columns)
        }
    }
}

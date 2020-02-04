package beveragebandits

import pathfinder.BasicPathfinder
import utils.listComparator

data class MovePriority(val path: List<Position>) : Comparable<MovePriority> {
    override fun compareTo(other: MovePriority): Int {
        return compareBy<MovePriority> { it.path.size }
            .thenBy { it.path.last() }
            .thenBy(listComparator()) { it.path }
            .compare(this, other)
    }
}

fun chooseDestination(
    from: Position,
    enemyPositions: Collection<Position>,
    cavern: Cavern
): List<Position>? {

    if (enemyPositions.any { it.isAdjacentTo(from) }) return null

    val inRange = enemyPositions
        .flatMap { e -> Direction.values().map { e + it } } // adjacent places
        .filter { cavern.emptyAt(it) } // that are empty

    if (inRange.isEmpty()) return null

    val reachable = BasicPathfinder(
        waysOutOp = cavern::waysOut,
        distanceOp = { l -> MovePriority(l) }
    ).findShortest(listOf(from)) { list -> list.last() in inRange }

    return reachable
}

package beveragebandits

import pathfinder.BasicPathfinder

data class MovePriority(val distance: Int, val position: Position?) : Comparable<MovePriority> {
    override fun compareTo(other: MovePriority): Int =
        compareValuesBy(this, other, { it.distance }, { it.position })
}

fun chooseDestination(
    from: Position,
    enemyPositions: Collection<Position>,
    cavern: Cavern
): Position? {

    if (enemyPositions.any { it.isAdjacentTo(from) }) return null

    val inRange = enemyPositions
        .flatMap { e -> Direction.values().map { e + it } } // adjacent places
        .filter { cavern.emptyAt(it) } // that are empty

    if (inRange.isEmpty()) return null

    val reachable = BasicPathfinder(
        waysOutOp = cavern::waysOut,
        distanceOp = { l -> MovePriority(l.size, l.last()) }
    ).findShortest(listOf(from)) { list -> list.last() in inRange }

    return reachable?.last()
}

fun nextStep(
    from: Position,
    destination: Position,
    cavern: Cavern
): Position {

    val path = BasicPathfinder(
        waysOutOp = cavern::waysOut,
        distanceOp = { l -> MovePriority(l.size, if (l.size > 1) l[1] else null) }
    ).findShortest(listOf(from)) { list -> list.last() == destination }!!
    return path[1]
}
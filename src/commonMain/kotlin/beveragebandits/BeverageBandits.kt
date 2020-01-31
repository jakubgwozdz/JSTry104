package beveragebandits

import pathfinder.BFSPathfinder
import pathfinder.BasicPathfinder
import pathfinder.Cache
import utils.replace
import utils.without

enum class MobType(val char: Char) {
    Elf('E'), Goblin('G');

    companion object {
        val chars by lazy { values().map { it.char } }
        fun of(char: Char) = values().single { it.char == char }
    }
}

data class Mob(val position: Position, val type: MobType, val hp: Int = 200) {
    fun isAdjacentTo(other: Mob): Boolean = position.isAdjacentTo(other.position)
}

sealed class FightState(
    open val cavern: Cavern,
    open val elves: List<Mob>,
    open val goblins: List<Mob>,
    open val turnsCompleted: Int
){
    override fun toString(): String {

        val state = cavern.map.mapIndexed { y, l ->
            val mobsOnLine = (elves.filter { it.position.y == y } + goblins.filter { it.position.y == y })
                .sortedBy { it.position }
            "$l   ${mobsOnLine.joinToString(",  ") { "${it.type.char}(${it.hp})" }}".trim()
        }.joinToString("\n")

        return "After $turnsCompleted rounds: \n$state"
    }

    val outcome get() = turnsCompleted * (elves.sumBy { it.hp } + goblins.sumBy { it.hp })

}

data class FightInProgress(
    override val cavern: Cavern,
    override val elves: List<Mob>,
    override val goblins: List<Mob>,
    override val turnsCompleted: Int,
    val mobsToGo: List<Mob>
) : FightState(cavern, elves, goblins, turnsCompleted) {
    override fun toString(): String = super.toString()
}

data class FightEnded(
    override val cavern: Cavern,
    override val elves: List<Mob>,
    override val goblins: List<Mob>,
    override val turnsCompleted: Int
) : FightState(cavern, elves, goblins, turnsCompleted) {

    override fun toString(): String = super.toString()
}

fun newFight(cavern: Cavern): FightInProgress {
    val order = cavern.actionOrder().map { Mob(it.first, it.second) }
    val (elves, goblins) = order.partition { it.type == MobType.Elf }
    return FightInProgress(cavern, elves, goblins, 0, order)
}

fun FightInProgress.fightToEnd(): FightEnded {
    var s:FightState = this
    while (s is FightInProgress) {
        s = s.fullRound()
        println(s)
    }
    return s as FightEnded
}

fun FightInProgress.fullRound(): FightState {

    var s = this
    while (s.mobsToGo.isNotEmpty()) s = s.mobsToGo.first().let {
        val enemies = when (it.type) {
            MobType.Elf -> s.goblins
            MobType.Goblin -> s.elves
        }
        if (enemies.isEmpty()) return FightEnded(s.cavern, s.elves, s.goblins, s.turnsCompleted)
        s.mobTurn(it, enemies)
    }

    return s.copy(
        turnsCompleted = s.turnsCompleted + 1,
        mobsToGo = (s.elves + s.goblins).sortedBy { it.position })
}

fun FightInProgress.mobTurn(mob: Mob, enemies: List<Mob>): FightInProgress {
    val initial = this
    val (afterMove, mobAfterMove) = initial.mobMove(mob, enemies)
    val afterAttack = afterMove.mobAttack(mobAfterMove)

    return afterAttack.copy(mobsToGo = afterAttack.mobsToGo.drop(1))
}

data class MovePriority(val distance: Int, val position: Position) : Comparable<MovePriority> {
    override fun compareTo(other: MovePriority): Int = compareValuesBy(this, other, { it.distance }, { it.position })
}

fun FightInProgress.chooseDestination(mob: Mob, enemies: List<Mob>): Position? {

    if (enemies.any { it.isAdjacentTo(mob) }) return null

    val inRange = enemies
        .flatMap { e -> Direction.values().map { e.position + it } } // adjacent places
        .filter { cavern.canGoTo(it) } // that are empty

    if (inRange.isEmpty()) return null

    val cache = Cache<Position, MovePriority>()

    val reachable = BFSPathfinder<Position, List<Position>, MovePriority>(
        // logWithTime = { println(it()) },
        adderOp = { l, t -> l + t },
        waysOutOp = { l ->
            Direction.values().map { l.last() + it }
                .filter { cavern.canGoTo(it) }
                .filter { it !in l }
        },
        distanceOp = { l -> MovePriority(l.size, if (l.size > 1) l[1] else l.single()) },
        meaningfulOp = { l, d -> cache.isBetterThanPrevious(l.last(), d) }
    ).findShortest(listOf(mob.position)) { list -> list.last() in inRange }

    return reachable?.last()
}

fun FightInProgress.mobMove(mob: Mob, enemies: List<Mob>): Pair<FightInProgress, Mob> {
    val destination = chooseDestination(mob, enemies)

    return if (destination != null) {
        val cache = Cache<Position, MovePriority>()

        val path = BFSPathfinder<Position, List<Position>, MovePriority>(
            adderOp = { l, t -> l + t },
            waysOutOp = { list ->
                Direction.values().map { list.last() + it }.filter { cavern.canGoTo(it) }
            },
            distanceOp = { l -> MovePriority(l.size, l.last()) },
            meaningfulOp = { l, d -> cache.isBetterThanPrevious(l.last(), d) }
        ).findShortest(listOf(mob.position)) { list -> list.last() == destination }!!
        val nextStep = path[1]
        val newMobState = mob.copy(position = nextStep)
        this.copy(
            cavern = cavern.mobMove(mob.position, nextStep),
            elves = elves.replace(mob, newMobState),
            goblins = goblins.replace(mob, newMobState),
            mobsToGo = mobsToGo.replace(mob, newMobState)
        ) to newMobState
    } else {
        this to mob
    }
}

data class AttackPriority(val hp: Int, val position: Position) : Comparable<AttackPriority> {
    override fun compareTo(other: AttackPriority): Int = compareValuesBy(this, other, { it.hp }, { it.position })
}

fun FightInProgress.mobAttack(mob: Mob): FightInProgress {
    val enemy = when (mob.type) {
        MobType.Elf -> goblins
        MobType.Goblin -> elves
    }
        .filter { it.position.isAdjacentTo(mob.position) }
        .minBy { AttackPriority(it.hp, it.position) }
        ?: return this

    val newEnemyState = enemy.copy(hp = enemy.hp - 3)

    return if (newEnemyState.hp > 0) {
        this.copy(
            elves = elves.replace(enemy, newEnemyState),
            goblins = goblins.replace(enemy, newEnemyState),
            mobsToGo = mobsToGo.replace(enemy, newEnemyState)
        )
    } else {
        this.copy(
            cavern = cavern.killUnit(enemy.position),
            elves = elves.without(enemy),
            goblins = goblins.without(enemy),
            mobsToGo = mobsToGo.without(enemy)
        )
    }
}


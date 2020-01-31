package beveragebandits

import pathfinder.BFSPathfinder
import pathfinder.Cache
import utils.replace
import utils.without

class FightRules {

    fun newFight(cavern: Cavern): FightInProgress {
        val mobs = cavern.mobs().map { Mob(it.first, it.second) }
        return FightInProgress(cavern, mobs, 0, mobs.sortedBy { it.position })
    }

    private fun nextTurn(fightState: FightInProgress): FightInProgress {
        return fightState.copy(
            turnsCompleted = fightState.turnsCompleted + 1,
            toGo = fightState.mobs.sortedBy { it.position })
    }

    fun fightToEnd(fightState: FightInProgress): FightEnded {
        var s: FightState = fightState
        while (s is FightInProgress) {
            s = fullRound(s)
        }
        return s as FightEnded
    }

    fun fullRound(fightState: FightInProgress): FightState {

        var s = fightState
        while (s.toGo.isNotEmpty()) {

            if (s.mobs.none { it.type == MobType.Goblin } || s.mobs.none { it.type == MobType.Elf }) {
                return FightEnded(s.cavern, s.mobs, s.turnsCompleted)
            }

            val s2 = nextMobTurn(s)
            s = s2
        }

        return nextTurn(s)
    }

    fun nextMobTurn(fightState: FightInProgress): FightInProgress {

        val afterMove = movePhase(fightState)
        val afterAttack = attackPhase(afterMove)

        return afterAttack.copy(toGo = afterAttack.toGo.drop(1))
    }

    data class MovePriority(val distance: Int, val position: Position?) : Comparable<MovePriority> {
        override fun compareTo(other: MovePriority): Int =
            compareValuesBy(this, other, { it.distance }, { it.position })
    }

    fun chooseDestination(
        mob: Mob,
        enemies: List<Mob>,
        cavern: Cavern
    ): Position? {

        if (enemies.any { it.isAdjacentTo(mob) }) return null

        val inRange = enemies
            .flatMap { e -> Direction.values().map { e.position + it } } // adjacent places
            .filter { cavern.canGoTo(it) } // that are empty

        if (inRange.isEmpty()) return null

        val cache =
            Cache<Position, MovePriority>()

        val reachable = BFSPathfinder<Position, List<Position>, MovePriority>(
            // logWithTime = { println(it()) },
            adderOp = { l, t -> l + t },
            waysOutOp = { l ->
                Direction.values().map { l.last() + it }
                    .filter { cavern.canGoTo(it) }
                    .filter { it !in l }
            },
            distanceOp = { l -> MovePriority(l.size, l.last()) },
            meaningfulOp = { l, d -> cache.isBetterThanPrevious(l.last(), d) }
        ).findShortest(listOf(mob.position)) { list -> list.last() in inRange }

        return reachable?.last()
    }

    fun nextStep(
        from: Position,
        destination: Position,
        cavern: Cavern
    ): Position {
        val cache = Cache<Position, MovePriority>()

        val path = BFSPathfinder<Position, List<Position>, MovePriority>(
            adderOp = { l, t -> l + t },
            waysOutOp = { list ->
                Direction.values().map { list.last() + it }
                    .filter { cavern.canGoTo(it) }
            },
            distanceOp = { l -> MovePriority(l.size, if (l.size > 1) l[1] else null) },
            meaningfulOp = { l, d -> cache.isBetterThanPrevious(l.last(), d) }
        ).findShortest(listOf(from)) { list -> list.last() == destination }!!
        return path[1]
    }

    fun movePhase(fightState: FightInProgress): FightInProgress {

        val mob = fightState.toGo.first()
        val enemies = fightState.mobs.filterNot { it.type == mob.type }

        val destination = chooseDestination(mob, enemies, fightState.cavern)
            ?: return fightState

        val nextStep = nextStep(mob.position, destination, fightState.cavern)
        val newMobState = mob.copy(position = nextStep)
        return fightState.copy(
            cavern = fightState.cavern.mobMove(mob.position, nextStep),
            mobs = fightState.mobs.replace(mob, newMobState),
            toGo = fightState.toGo.replace(mob, newMobState)
        )
    }

    data class AttackPriority(val hp: Int, val position: Position) : Comparable<AttackPriority> {
        override fun compareTo(other: AttackPriority): Int =
            compareValuesBy(this, other, { it.hp }, { it.position })
    }

    fun attackPhase(fightState: FightInProgress): FightInProgress {

        val mob = fightState.toGo.first()
        val enemy = fightState.mobs
            .filterNot { it.type == mob.type }
            .filter { it.position.isAdjacentTo(mob.position) }
            .minBy { AttackPriority(it.hp, it.position) }
            ?: return fightState

        val newEnemyState = enemy.copy(hp = enemy.hp - 3)

        return if (newEnemyState.hp > 0) {
            fightState.copy(
                mobs = fightState.mobs.replace(enemy, newEnemyState),
                toGo = fightState.toGo.replace(enemy, newEnemyState)
            )
        } else {
            fightState.copy(
                cavern = fightState.cavern.without(enemy.position),
                mobs = fightState.mobs.without(enemy),
                toGo = fightState.toGo.without(enemy)
            )
        }
    }

}

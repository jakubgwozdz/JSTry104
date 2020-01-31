package beveragebandits

import beveragebandits.MobType.Elf
import beveragebandits.MobType.Goblin
import pathfinder.BFSPathfinder
import pathfinder.Cache

class FightRules(val elvesAttackPower: Int = 3) {

    fun newFight(cavern: Cavern): Move {
        return Move(CombatState(cavern), 0)
    }

    fun fightToEnd(phase: CombatInProgress): CombatEnded {
        var s: Phase = phase
        while (true) {
            s = when (s) {
                is MobTurn -> fullRound(s)
                is EndOfRound -> nextRound(s)
                is CombatEnded -> return s
            }
        }
    }

    fun nextRound(phase: EndOfRound): Move {
        return Move(phase.state.nextRound(), 0)
    }

    fun fullRound(phase: MobTurn): Phase {

        var s: Phase = phase
        while (s is MobTurn && s.mobIndex in s.state.mobs.indices) {
            s = when {
                s.state.mobs[s.mobIndex].hp <= 0 -> nextMob(s)
                s.state.mobs.none { it.type == Goblin && it.hp > 0 } -> ElvesWins(s.state)
                s.state.mobs.none { it.type == Elf && it.hp > 0 } -> GoblinsWins(s.state)
                else -> mobTurn(s)
            }
        }

        return s
    }

    fun nextMob(phase: MobTurn): Phase {
        return if (phase.mobIndex + 1 in phase.state.mobs.indices)
            Move(phase.state, phase.mobIndex + 1)
        else
            EndOfRound(phase.state)
    }

    fun mobTurn(state: MobTurn): Phase {
        var s: Phase = state
        while (s is MobTurn && s.mobIndex == state.mobIndex) {
            s = mobPhase(s)
        }
        return s
    }

    private fun mobPhase(s: MobTurn): Phase {
        return when (s) {
            is Move -> movePhase(s)
            is Attack -> attackPhase(s)
            is EndTurn -> nextMob(s)
        }
    }

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

        val cache =
            Cache<Position, MovePriority>()

        val reachable = BFSPathfinder<Position, List<Position>, MovePriority>(
            // logWithTime = { println(it()) },
            adderOp = { l, t -> l + t },
            waysOutOp = { l ->
                Direction.values().map { l.last() + it }
                    .filter { cavern.emptyAt(it) }
                    .filter { it !in l }
            },
            distanceOp = { l -> MovePriority(l.size, l.last()) },
            meaningfulOp = { l, d -> cache.isBetterThanPrevious(l.last(), d) }
        ).findShortest(listOf(from)) { list -> list.last() in inRange }

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
                    .filter { cavern.emptyAt(it) }
            },
            distanceOp = { l -> MovePriority(l.size, if (l.size > 1) l[1] else null) },
            meaningfulOp = { l, d -> cache.isBetterThanPrevious(l.last(), d) }
        ).findShortest(listOf(from)) { list -> list.last() == destination }!!
        return path[1]
    }

    fun movePhase(phase: Move): Attack {

        val mob = phase.state.mobs[phase.mobIndex]

        val enemyPositions = phase.state.mobs
            .filterNot { it.type == mob.type }
            .filter { it.hp > 0 }
            .map { it.position }

        val destination = chooseDestination(mob.position, enemyPositions, phase.state.cavern)
            ?: return Attack(phase.state, phase.mobIndex)

        val nextStep = nextStep(mob.position, destination, phase.state.cavern)
        return Attack(
            state = phase.state.moveMob(mob, nextStep),
            mobIndex = phase.mobIndex
        )
    }

    data class AttackPriority(val hp: Int, val position: Position) : Comparable<AttackPriority> {
        override fun compareTo(other: AttackPriority): Int =
            compareValuesBy(this, other, { it.hp }, { it.position })
    }

    fun attackPhase(phase: Attack): EndTurn {

        val mob = phase.state.mobs[phase.mobIndex]

        val enemy = phase.state.mobs
            .filterNot { it.type == mob.type }
            .filter { it.hp > 0 }
            .filter { it.position.isAdjacentTo(mob.position) }
            .minBy { AttackPriority(it.hp, it.position) }

        return EndTurn(
            state = enemy?.let { phase.state.hitMob(it, if (mob.type == Elf) elvesAttackPower else 3) } ?: phase.state,
            mobIndex = phase.mobIndex
        )
    }
}

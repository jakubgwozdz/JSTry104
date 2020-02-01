package beveragebandits

import beveragebandits.MobType.Elf
import beveragebandits.MobType.Goblin

class FightRules(
    val elvesAttackPower: Int = 3,
    val goblinsWin: (CombatState) -> Boolean = { s -> s.mobs.none { it.type == Elf && it.hp > 0 } }
) {

    val elvesWin: (CombatState) -> Boolean = { s -> s.mobs.none { it.type == Goblin && it.hp > 0 } }
    val goblinsAttackPower = 3

    fun newFight(cavern: Cavern): StartOfRound {
        return StartOfRound(CombatState(cavern))
    }

    fun fightToEnd(phase: CombatInProgress): CombatEnded {
        var s: Phase = phase
        while (true) {
            s = when (s) {
                is StartOfRound -> firstMob(s)
                is MobTurn -> fullRound(s)
                is EndOfRound -> nextRound(s)
                is CombatEnded -> return s
            }
        }
    }

    fun firstMob(s: StartOfRound) = StartOfTurn(s.state, 0)

    fun fullRound(phase: MobTurn): Phase {

        var p: Phase = phase
        while (p is MobTurn) {
            p = when {
                p is StartOfTurn && p.state.mobs[p.mobIndex].hp <= 0 -> EndOfTurn(p.state, p.mobIndex)
                goblinsWin(p.state) -> GoblinsWin(p.state)
                elvesWin(p.state) -> ElvesWin(p.state)
                else -> mobTurn(p)
            }
        }

        return p
    }

    fun nextMob(phase: EndOfTurn): Phase {
        return if (phase.mobIndex + 1 in phase.state.mobs.indices)
            StartOfTurn(phase.state, phase.mobIndex + 1)
        else
            EndOfRound(phase.state)
    }

    fun mobTurn(phase: MobTurn): Phase {
        var p: Phase = phase
        while (p is MobTurn && p.mobIndex == phase.mobIndex) {
            p = mobPhase(p)
        }
        return p
    }

    fun nextRound(phase: EndOfRound) = StartOfRound(phase.state.nextRound())

    private fun mobPhase(p: MobTurn): Phase {
        return when (p) {
            is StartOfTurn -> beginTurn(p)
            is Move -> movePhase(p)
            is Attack -> attackPhase(p)
            is EndOfTurn -> nextMob(p)
        }
    }

    fun beginTurn(p: StartOfTurn) = Move(p.state, p.mobIndex)

    fun movePhase(p: Move): Attack {

        val mob = p.state.mobs[p.mobIndex]

        val enemyPositions = p.state.mobs
            .filterNot { it.type == mob.type }
            .filter { it.hp > 0 }
            .map { it.position }

        val destination = chooseDestination(mob.position, enemyPositions, p.state.cavern)
            ?: return Attack(p.state, p.mobIndex)

        val nextStep = nextStep(mob.position, destination, p.state.cavern)
        return Attack(
            state = p.state.moveMob(mob, nextStep),
            mobIndex = p.mobIndex
        )
    }

    fun attackPhase(p: Attack): EndOfTurn {

        val mob = p.state.mobs[p.mobIndex]

        val enemy = chooseEnemy(mob, p.state.mobs)

        val newState = enemy
            ?.let { p.state.hitMob(it, if (mob.type == Elf) elvesAttackPower else goblinsAttackPower) }
            ?: p.state

        return EndOfTurn(
            state = newState,
            mobIndex = p.mobIndex
        )
    }

}


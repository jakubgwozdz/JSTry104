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

    fun fightToEnd(phase: CombatInProgress): EndOfCombat {
        var s: Phase = phase
        while (true) {
            s = when (s) {
                is StartOfRound -> firstMob(s)
                is MobTurn -> fullRound(s)
                is EndOfRound -> nextRound(s)
                is EndOfCombat -> return s
            }
        }
    }

    fun firstMob(s: StartOfRound) = StartOfTurn(s.state, 0)

    fun fullRound(phase: MobTurn): Phase {

        var p: Phase = phase
        while (p is MobTurn) {
            p = mobTurn(p)
        }
        check(p is EndOfRound || p is EndOfCombat)
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
            p = when (p) {
                is StartOfTurn -> beginTurn(p)
                is Move -> movePhase(p)
                is Attack -> attackPhase(p)
                is EndOfTurn -> nextMob(p)
            }
        }
        return p
    }

    fun nextRound(phase: EndOfRound) = StartOfRound(phase.state.nextRound())

    fun beginTurn(p: StartOfTurn) =
        if (p.state.mobs[p.mobIndex].hp > 0) Move(p.state, p.mobIndex)
        else EndOfTurn(p.state, p.mobIndex)

    fun movePhase(p: Move): Phase {

        if (goblinsWin(p.state)) return GoblinsWin(p.state)
        if (elvesWin(p.state)) return ElvesWin(p.state)

        val mob = p.state.mobs[p.mobIndex]

        val enemyPositions = p.state.mobs
            .filterNot { it.type == mob.type }
            .filter { it.hp > 0 }
            .map { it.position }

        val newState = (chooseDestination(mob.position, enemyPositions, p.state.cavern)
            ?.let { destination -> nextStep(mob.position, destination, p.state.cavern) }
            ?.let { nextStep -> p.state.moveMob(mob, nextStep) }
            ?: p.state)

        return Attack(newState, p.mobIndex)
    }

    fun attackPhase(p: Attack): EndOfTurn {

        val mob = p.state.mobs[p.mobIndex]

        val enemy = chooseEnemy(mob, p.state.mobs)

        val newState = enemy
            ?.let { p.state.hitMob(it, if (mob.type == Elf) elvesAttackPower else goblinsAttackPower) }
            ?: p.state

        return EndOfTurn(newState, p.mobIndex)
    }
}


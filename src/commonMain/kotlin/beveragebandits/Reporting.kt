package beveragebandits

interface Reporting {
    fun combatPhase(phase: Phase)
    fun mobAttacks(attacker: Mob, target: Mob, attackPower: Int)
    fun mobMoves(mob: Mob, path: List<Position>)
    fun mobStops(mob: Mob)
}

object NoopReporting : Reporting {

    override fun combatPhase(phase: Phase) {}

    override fun mobAttacks(attacker: Mob, target: Mob, attackPower: Int) {}

    override fun mobMoves(mob: Mob, path: List<Position>) {}

    override fun mobStops(mob: Mob) {}
}

var reporting: Reporting = NoopReporting
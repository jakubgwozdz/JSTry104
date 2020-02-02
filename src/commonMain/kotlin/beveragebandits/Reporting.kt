package beveragebandits

var reportCombatStarted: (CombatState) -> Unit = ::reportCombatStartedNoOp

var reportMobAttacks: (
    prevState: CombatState,
    attacker: Mob,
    target: Mob,
    attackPower: Int,
    nextState: CombatState
) -> Unit = ::reportMobAttacksNoOp

var reportMobMoves: (
    prevState: CombatState,
    mob: Mob,
    path: List<Position>,
    nextState: CombatState
) -> Unit = ::reportMobMovesNoOp

fun reportCombatStartedNoOp(state: CombatState) {}

fun reportMobAttacksNoOp(
    prevState: CombatState,
    attacker: Mob,
    target: Mob,
    attackPower: Int,
    nextState: CombatState
) {
}

fun reportMobMovesNoOp(
    prevState: CombatState,
    mob: Mob,
    path: List<Position>,
    nextState: CombatState
) {
}

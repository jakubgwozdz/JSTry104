package beveragebandits

var reportCombatStarted: (state: CombatState) -> Unit = {}

var reportMobAttacks: (
    prevState: CombatState,
    attacker: Mob,
    target: Mob,
    attackPower: Int,
    nextState: CombatState
) -> Unit = { _, _, _, _, _ -> }

var reportMobMoves: (
    prevState: CombatState,
    mob: Mob,
    path: List<Position>,
    CombatState
) -> Unit = { _, _, _, _ -> }

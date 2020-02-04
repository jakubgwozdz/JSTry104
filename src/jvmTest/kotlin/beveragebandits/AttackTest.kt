package beveragebandits

import kotlin.test.Test
import kotlin.test.expect

class AttackTest {
    @Test
    fun example2Round34() {
        val input = """
            #######
            #GE.#E#
            #E#...#
            #GE##.#
            #E..#E#
            #.....#
            ####### 
            """.trimIndent()

        val elf1 = Mob(1, 1 by 2, MobType.Elf, 200)
        val elf2 = Mob(2, 1 by 5, MobType.Elf, 200)
        val elf3 = Mob(4, 2 by 1, MobType.Elf, 2)
        val elf4 = Mob(5,3 by 2, MobType.Elf, 200)
        val elf5 = Mob(6, 4 by 1, MobType.Elf, 200)
        val elf6 = Mob(7, 4 by 5, MobType.Elf, 200)
        val gob1 = Mob(0, 1 by 1, MobType.Goblin, 5)
        val gob2 = Mob(3, 3 by 1, MobType.Goblin, 32)
        val state33 = Move(
            CombatState(
                cavern = Cavern(input.lines()),
                mobs = listOf(gob1, elf1, elf2, gob2, elf3, elf4, elf5, elf6),
                roundsCompleted = 33
            ),
            mobIndex = 0
        )

        val text33 = """
            After 33 rounds:
            #######
            #GE.#E#   G(5), E(200), E(200)
            #E#...#   E(2)
            #GE##.#   G(32), E(200)
            #E..#E#   E(200), E(200)
            #.....#
            #######
            """.trimIndent()

        expect(text33) { state33.state.description() }

        val state33a = state33.run {
            FightRules().mobTurn(this)
        }

        val text33a = """
            After 33 rounds:
            #######
            #GE.#E#   G(5), E(200), E(200)
            #.#...#
            #GE##.#   G(32), E(200)
            #E..#E#   E(200), E(200)
            #.....#
            #######
            """.trimIndent()

        expect(text33a) { state33a.state.description() }
        expect(listOf(gob1, elf1, elf2, gob2, elf4, elf5, elf6)) { state33a.state.mobs.filter { it.hp > 0 } }
    }
}

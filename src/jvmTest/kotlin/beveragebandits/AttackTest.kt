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

        val elf1 = Mob(1 by 2, MobType.Elf, 200)
        val elf2 = Mob(1 by 5, MobType.Elf, 200)
        val elf3 = Mob(2 by 1, MobType.Elf, 2)
        val elf4 = Mob(3 by 2, MobType.Elf, 200)
        val elf5 = Mob(4 by 1, MobType.Elf, 200)
        val elf6 = Mob(4 by 5, MobType.Elf, 200)
        val gob1 = Mob(1 by 1, MobType.Goblin, 5)
        val gob2 = Mob(3 by 1, MobType.Goblin, 32)
        val state33 = FightInProgress(
            cavern = Cavern(input.lines()),
            mobs = listOf(gob1, elf1, elf2, gob2, elf3, elf4, elf5, elf6),
            turnsCompleted = 33,
            toGo = listOf(gob1, elf1, elf2, gob2, elf3, elf4, elf5, elf6)
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

        expect(text33) { state33.toString() }

        val state33a = state33.run {
            FightRules().nextMobTurn(this)
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

        expect(text33a) { state33a.toString() }
        expect(listOf(gob1, elf1, elf2, gob2, elf4, elf5, elf6)) { state33a.mobs }
        expect(listOf(elf1, elf2, gob2, elf4, elf5, elf6)) { state33a.toGo }

    }
}

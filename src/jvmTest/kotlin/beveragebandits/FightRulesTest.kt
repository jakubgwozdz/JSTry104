package beveragebandits

import kotlin.test.Test
import kotlin.test.expect

class FightRulesTest {

    val fightRules = FightRules()

    @Test
    fun completeExample1() {
        val input = """
            #######
            #.G...#
            #...EG#
            #.#.#G#
            #..G#E#
            #.....#
            #######
            """.trimIndent()

        expect(27730) {
            val fight = fightRules.newCombat(Cavern(input))
            fightRules.fightToEnd(fight)
                .state
                .outcome
        }
    }

    @Test
    fun completeExample2() {
        val input = """
            #######
            #G..#E#
            #E#E.E#
            #G.##.#
            #...#E#
            #...E.#
            #######
            """.trimIndent()

        expect(36334) {
            val fight = fightRules.newCombat(Cavern(input))
            fightRules.fightToEnd(fight)
                .state
                .outcome
        }
    }

    @Test
    fun completeExample3() {
        val input = """
            #######   
            #E..EG#
            #.#G.E#
            #E.##E#
            #G..#.#
            #..E#.#   
            #######
            """.trimIndent()

        expect(39514) {
            val fight = fightRules.newCombat(Cavern(input))
            fightRules.fightToEnd(fight)
                .state
                .outcome
        }
    }

    @Test
    fun completeExample4() {
        val input = """
            #######   
            #E.G#.#
            #.#G..#
            #G.#.G#   
            #G..#.#
            #...E.#
            #######
            """.trimIndent()
        expect(27755) {
            val fight = fightRules.newCombat(Cavern(input))
            fightRules.fightToEnd(fight)
                .state
                .outcome
        }
    }

    @Test
    fun completeExample5() {
        val input = """
            #######   
            #.E...#   
            #.#..G#
            #.###.#   
            #E#G#G#   
            #...#G#
            #######
            """.trimIndent()
        expect(28944) {
            val fight = fightRules.newCombat(Cavern(input))
            fightRules.fightToEnd(fight)
                .state
                .outcome
        }
    }

    @Test
    fun completeExample6() {
        val input = """
            #########   
            #G......#
            #.E.#...#
            #..##..G#
            #...##..#   
            #...#...#
            #.G...G.#   
            #.....G.#   
            #########
            """.trimIndent()
        expect(18740) {
            val fight = fightRules.newCombat(Cavern(input))
            fightRules.fightToEnd(fight)
                .state
                .outcome
        }
    }

    @Test
    fun completeJG() {
        val input = beveragebandits.jakubgwozdz.cavernInput

        expect(222831) {
            val fight = fightRules.newCombat(Cavern(input))
            fightRules.fightToEnd(fight)
                .state
                .outcome
        }
    }

    @Test
    fun completeJG2() {
        val input = beveragebandits.jakubgwozdz.cavernInput

        expect(54096) {

            var attackPower = 3
            var phase:Phase

            do {
                attackPower++
                val rules = FightRules(attackPower) { s->s.mobs.any { it.type==MobType.Elf && it.hp <= 0 }}
                phase = rules.newCombat(Cavern(input))
                phase = rules.fightToEnd(phase)

                println("AP $attackPower outcome ${phase.state.outcome}")
            } while (phase !is ElvesWin)

            phase.state.outcome
        }
    }
}

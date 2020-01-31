package beveragebandits

import kotlin.test.Test
import kotlin.test.expect

class FightTest {

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

        expect(27730) { newFight(Cavern(input)).fightToEnd().outcome }
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

        expect(36334) { newFight(Cavern(input)).fightToEnd().outcome }
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

        expect(39514) { newFight(Cavern(input)).fightToEnd().outcome }
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
        expect(27755) { newFight(Cavern(input)).fightToEnd().outcome }
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
        expect(28944) { newFight(Cavern(input)).fightToEnd().outcome }
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
        expect(18740) { newFight(Cavern(input)).fightToEnd().outcome }
    }

    @Test
    fun completeJG() {
        val input = beveragebandits.jakubgwozdz.cavernInput

        expect(220321) {
            newFight(Cavern(input)).fightToEnd().also{println(it)}.outcome
        }
    }
}
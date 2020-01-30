package beveragebandits

import kotlin.test.Test

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

        var s = newFight(Cavern(input))

        while (true) {
            s = s.fullRound()
            println(s)
        }
    }

}
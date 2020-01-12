package cryostasis

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

sealed class Output
object Prompt : Output()
data class RoomDescription(val room: Room) : Output()
data class TakeActionDescription(val item: String) : Output()
data class DropActionDescription(val item: String) : Output()
data class RoomWithTeleportDescription(val room: Room, val reason: String) : Output()
data class Success(val code: String) : Output()

val alertRegex =
    Regex("A loud, robotic voice says \"Alert! Droids on this ship are (heavier|lighter) than the detected value!\" and you are ejected back to the checkpoint.")
val proceedRegex =
    Regex("A loud, robotic voice says \"Analysis complete! You may proceed.\" and you enter the cockpit.")
val successRegex =
    Regex("\"Oh, hello! You should be able to get in by typing (.+) on the keypad at the main airlock.\"")
val roomNameRegex = Regex("== (.+) ==")
val takeRegex = Regex("You take the (.+)\\.")
val dropRegex = Regex("You drop the (.+)\\.")


class OutputParser {

    enum class State { START, BUILDING_ROOM, AFTER_TELEPORT, AFTER_TAKE, LISTING_INVENTORY, SUCCESS }

    var state = State.START
    val roomBuilder = RoomBuilder()
    val builtOutputs = mutableListOf<Output>()

    fun clear() {
        roomBuilder.clear()
        builtOutputs.clear()
        state = State.START
    }

    fun accept(line: String) {
        state = when (state) {
            State.START -> when {
                line.isBlank() -> state
                roomNameRegex.matches(line) -> {
                    roomBuilder.accept(line)
                    State.BUILDING_ROOM
                }
                takeRegex.matches(line) -> {
                    builtOutputs.add(TakeActionDescription(takeRegex.matchEntire(line)!!.groupValues[1]))
                    State.AFTER_TAKE
                }
                dropRegex.matches(line) -> {
                    builtOutputs.add(DropActionDescription(dropRegex.matchEntire(line)!!.groupValues[1]))
                    State.AFTER_TAKE
                }
                successRegex.matches(line) -> {
                    builtOutputs.add(Success(successRegex.matchEntire(line)!!.groupValues[1]))
                    State.SUCCESS
                }
                line == "Items in your inventory:" -> State.LISTING_INVENTORY
                else -> state // ignore
            }
            State.AFTER_TELEPORT -> when {
                line.isBlank() -> state
                roomNameRegex.matches(line) -> {
                    roomBuilder.accept(line)
                    State.BUILDING_ROOM
                }
                else -> TODO("$state, $line")
            }
            State.BUILDING_ROOM -> when {
                alertRegex.matches(line) -> {
                    builtOutputs.add(RoomWithTeleportDescription(roomBuilder.build(), line))
                    roomBuilder.clear()
                    State.AFTER_TELEPORT
                }
                proceedRegex.matches(line) -> {
//                    builtOutputs.add(RoomWithTeleportDescription(roomBuilder.build(), line))
                    roomBuilder.clear()
                    State.START
                }
                else -> {
                    roomBuilder.accept(line)
                    state
                }
            }
            State.LISTING_INVENTORY, State.AFTER_TAKE -> state // just ignore
            State.SUCCESS -> TODO()
        }
    }

    fun build(): List<Output> = when (state) {
        State.BUILDING_ROOM -> {
            builtOutputs.add(RoomDescription(roomBuilder.build()))
            builtOutputs.toList().also { clear() }
        }
        State.AFTER_TAKE, State.LISTING_INVENTORY, State.START -> builtOutputs.toList().also { clear() }
        State.SUCCESS -> builtOutputs.toList().also { clear() }
        State.AFTER_TELEPORT -> error("Unexpected end of output in state $state")
    }

}


@Suppress("BlockingMethodInNonBlockingContext")
fun Flow<String>.outputs(): Flow<Output> =
    flow {
        val builder = OutputParser()
        collect { line ->
            when (line) {
                "Command?" -> {
                    builder.build().forEach { emit(it) }
                    builder.clear()
                    emit(Prompt)
                }
                else -> builder.accept(line)
            }
        }
        builder.build().forEach { emit(it) }
    }


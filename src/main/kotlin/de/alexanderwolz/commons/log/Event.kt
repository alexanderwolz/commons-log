package de.alexanderwolz.commons.log

data class Event(
    val level: Level,
    val message: String,
    val logger: Logger,
    val caller: StackTraceElement,
    val error: Throwable?
)

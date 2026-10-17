package de.alexanderwolz.commons.log

import java.time.Instant

data class Event(
    val level: Level,
    val message: String,
    val logger: Logger,
    val caller: StackTraceElement,
    val error: Throwable?,
    val timestamp: Instant = Instant.now(),
    val threadName: String = Thread.currentThread().name
)

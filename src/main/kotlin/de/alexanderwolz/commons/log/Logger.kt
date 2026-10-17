package de.alexanderwolz.commons.log

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass


class Logger(
    private val logger: Logger,
    eventListener: ((event: Event) -> Unit)? = null
) {

    constructor(
        clazz: Class<*>,
        eventListener: ((event: Event) -> Unit)? = null
    ) : this(LoggerFactory.getLogger(clazz), eventListener)

    constructor(
        clazz: KClass<*>,
        eventListener: ((event: Event) -> Unit)? = null
    ) : this(LoggerFactory.getLogger(clazz.java), eventListener)

    constructor(
        name: String,
        eventListener: ((event: Event) -> Unit)? = null
    ) : this(LoggerFactory.getLogger(name), eventListener)

    private class Registration(val minLevel: Level, val listener: (event: Event) -> Unit)

    private val registrations = CopyOnWriteArrayList<Registration>().apply {
        eventListener?.let { add(Registration(Level.TRACE, it)) }
    }

    fun addEventListener(listener: (event: Event) -> Unit) {
        addEventListener(Level.TRACE, listener)
    }

    fun addEventListener(minLevel: Level, listener: (event: Event) -> Unit) {
        registrations.add(Registration(minLevel, listener))
    }

    fun removeEventListener(listener: (event: Event) -> Unit) {
        registrations.removeIf { it.listener == listener }
    }

    val isTraceEnabled get() = logger.isTraceEnabled
    val isDebugEnabled get() = logger.isDebugEnabled
    val isInfoEnabled get() = logger.isInfoEnabled
    val isWarnEnabled get() = logger.isWarnEnabled
    val isErrorEnabled get() = logger.isErrorEnabled
    val name: String = logger.name

    val level: Level
        get() = retrieveLevel()

    private fun retrieveLevel(): Level {
        if (isTraceEnabled) return Level.TRACE
        if (isDebugEnabled) return Level.DEBUG
        if (isInfoEnabled) return Level.INFO
        if (isWarnEnabled) return Level.WARN
        if (isErrorEnabled) return Level.ERROR
        return Level.NONE
    }

    fun trace(message: () -> String) {
        log(Level.TRACE, null, message)
    }

    fun trace(throwable: Throwable?, message: () -> String) {
        log(Level.TRACE, throwable, message)
    }

    fun debug(message: () -> String) {
        log(Level.DEBUG, null, message)
    }

    fun debug(throwable: Throwable?, message: () -> String) {
        log(Level.DEBUG, throwable, message)
    }

    fun info(message: () -> String) {
        log(Level.INFO, null, message)
    }

    fun info(throwable: Throwable?, message: () -> String) {
        log(Level.INFO, throwable, message)
    }

    fun warn(message: () -> String) {
        log(Level.WARN, null, message)
    }

    fun warn(throwable: Throwable) {
        log(Level.WARN, throwable) { describe(throwable) }
    }

    fun warn(throwable: Throwable?, message: () -> String) {
        log(Level.WARN, throwable, message)
    }

    fun error(message: () -> String) {
        log(Level.ERROR, null, message)
    }

    fun error(throwable: Throwable) {
        log(Level.ERROR, throwable) { describe(throwable) }
    }

    fun error(throwable: Throwable?, message: () -> String) {
        log(Level.ERROR, throwable, message)
    }

    private fun describe(throwable: Throwable): String {
        return throwable.message ?: throwable.javaClass.name
    }

    private fun log(level: Level, throwable: Throwable?, message: () -> String) {
        val enabled = isEnabled(level)
        val listeners = registrations.filter { level >= it.minLevel && it.minLevel != Level.NONE }
        if (!enabled && listeners.isEmpty()) return

        val text = message()

        if (listeners.isNotEmpty()) {
            val event = Event(level, text, this, findCaller(), throwable)
            listeners.forEach { notify(it.listener, event) }
        }

        if (enabled) {
            when (level) {
                Level.TRACE -> logger.trace(text, throwable)
                Level.DEBUG -> logger.debug(text, throwable)
                Level.INFO -> logger.info(text, throwable)
                Level.WARN -> logger.warn(text, throwable)
                Level.ERROR -> logger.error(text, throwable)
                Level.NONE -> Unit
            }
        }
    }

    private fun notify(listener: (event: Event) -> Unit, event: Event) {
        try {
            listener(event)
        } catch (e: Exception) {
            logger.warn("Event listener failed for logger '$name'", e)
        }
    }

    private fun isEnabled(level: Level): Boolean {
        return when (level) {
            Level.TRACE -> logger.isTraceEnabled
            Level.DEBUG -> logger.isDebugEnabled
            Level.INFO -> logger.isInfoEnabled
            Level.WARN -> logger.isWarnEnabled
            Level.ERROR -> logger.isErrorEnabled
            Level.NONE -> false
        }
    }

    private fun findCaller(): StackTraceElement {
        return STACK_WALKER.walk { frames ->
            frames.filter { it.className != CLASS_NAME }.findFirst().get().toStackTraceElement()
        }
    }

    private companion object {
        private val STACK_WALKER: StackWalker = StackWalker.getInstance()
        private val CLASS_NAME: String = de.alexanderwolz.commons.log.Logger::class.java.name
    }
}

inline fun <reified T : Any> logger(noinline eventListener: ((event: Event) -> Unit)? = null): de.alexanderwolz.commons.log.Logger {
    return de.alexanderwolz.commons.log.Logger(T::class, eventListener)
}

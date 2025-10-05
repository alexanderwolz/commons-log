package de.alexanderwolz.commons.log

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.jvm.javaClass
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

    private val eventListeners = ArrayList<(event: Event) -> Unit>().apply {
        eventListener?.let { add(it) }
    }

    fun addEventListener(listener: (event: Event) -> Unit) {
        eventListeners.add(listener)
    }

    fun removeEventListener(listener: (event: Event) -> Unit) {
        eventListeners.remove(listener)
    }

    val isTraceEnabled = logger.isTraceEnabled
    val isDebugEnabled = logger.isDebugEnabled
    val isInfoEnabled = logger.isInfoEnabled
    val isWarnEnabled = logger.isWarnEnabled
    val isErrorEnabled = logger.isErrorEnabled
    val name = logger.name

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
        if (eventListeners.isNotEmpty()) {
            val caller = Thread.currentThread().stackTrace[2]
            val event = Event(Level.TRACE, message(), this, caller, null)
            eventListeners.forEach {
                it(event)
            }
        }
        if (logger.isTraceEnabled) {
            logger.trace(message())
        }
    }

    fun debug(message: () -> String) {
        if (eventListeners.isNotEmpty()) {
            val caller = Thread.currentThread().stackTrace[2]
            val event = Event(Level.DEBUG, message(), this, caller, null)
            eventListeners.forEach {
                it(event)
            }
        }
        if (logger.isDebugEnabled) {
            logger.debug(message())
        }
    }

    fun info(message: () -> String) {
        if (eventListeners.isNotEmpty()) {
            val caller = Thread.currentThread().stackTrace[2]
            val event = Event(Level.INFO, message(), this, caller, null)
            eventListeners.forEach {
                it(event)
            }
        }
        if (logger.isInfoEnabled) {
            logger.info(message())
        }
    }


    fun warn(message: () -> String) {
        if (eventListeners.isNotEmpty()) {
            val caller = Thread.currentThread().stackTrace[2]
            val event = Event(Level.WARN, message(), this, caller, null)
            eventListeners.forEach {
                it(event)
            }
        }
        if (logger.isWarnEnabled) {
            logger.warn(message())
        }
    }

    fun error(message: () -> String) {
        if (eventListeners.isNotEmpty()) {
            val caller = Thread.currentThread().stackTrace[2]
            val event = Event(Level.ERROR, message(), this, caller, null)
            eventListeners.forEach {
                it(event)
            }
        }
        if (logger.isErrorEnabled) {
            logger.error(message())
        }
    }

    fun error(throwable: Throwable) {
        error(throwable) {
            throwable.message ?: throwable.javaClass.name
        }
    }

    fun error(throwable: Throwable?, message: () -> String) {
        if (eventListeners.isNotEmpty()) {
            val caller = Thread.currentThread().stackTrace[2]
            val event = Event(Level.ERROR, message(), this, caller, null)
            eventListeners.forEach {
                it(event)
            }
        }
        if (logger.isErrorEnabled) {
            logger.error(message(), throwable)
        }
    }
}
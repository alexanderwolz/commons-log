package de.alexanderwolz.commons.log

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import kotlin.reflect.KClass


class Logger(private val logger: Logger) {

    constructor(clazz: Class<*>) : this(LoggerFactory.getLogger(clazz))
    constructor(clazz: KClass<*>) : this(LoggerFactory.getLogger(clazz.java))
    constructor(name: String) : this(LoggerFactory.getLogger(name))

    enum class LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR, OFF
    }

    val isTraceEnabled = logger.isTraceEnabled
    val isDebugEnabled = logger.isDebugEnabled
    val isInfoEnabled = logger.isInfoEnabled
    val isWarnEnabled = logger.isWarnEnabled
    val isErrorEnabled = logger.isErrorEnabled
    val name = logger.name

    val level: LogLevel
        get() = retrieveLevel()

    private fun retrieveLevel(): LogLevel {
        if (isTraceEnabled) return LogLevel.TRACE
        if (isDebugEnabled) return LogLevel.DEBUG
        if (isInfoEnabled) return LogLevel.INFO
        if (isWarnEnabled) return LogLevel.WARN
        if (isErrorEnabled) return LogLevel.ERROR
        return LogLevel.OFF
    }

    fun trace(message: () -> String) {
        if (logger.isTraceEnabled) {
            logger.trace(message())
        }
    }

    fun debug(message: () -> String) {
        if (logger.isDebugEnabled) {
            logger.debug(message())
        }
    }

    fun info(message: () -> String) {
        if (logger.isInfoEnabled) {
            logger.info(message())
        }
    }


    fun warn(message: () -> String) {
        if (logger.isWarnEnabled) {
            logger.warn(message())
        }
    }

    fun error(message: () -> String) {
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
        if (logger.isErrorEnabled) {
            logger.error(message(), throwable)
        }
    }
}
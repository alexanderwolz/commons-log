package de.alexanderwolz.commons.log

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import kotlin.reflect.KClass


class Logger(private val logger: Logger, private val appendToStdOut: Boolean = false) {

    constructor(clazz: Class<*>, appendToStdOut: Boolean = false)
            : this(LoggerFactory.getLogger(clazz), appendToStdOut)

    constructor(clazz: KClass<*>, appendToStdOut: Boolean = false)
            : this(LoggerFactory.getLogger(clazz.java), appendToStdOut)

    constructor(name: String, appendToStdOut: Boolean = false)
            : this(LoggerFactory.getLogger(name), appendToStdOut)

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
            val message = message()
            if (appendToStdOut) {
                println("[TRACE] $message")
            }
            logger.trace(message)
        }
    }

    fun debug(message: () -> String) {
        if (logger.isDebugEnabled) {
            val message = message()
            if (appendToStdOut) {
                println("[DEBUG] $message")
            }
            logger.debug(message)
        }
    }

    fun info(message: () -> String) {
        if (logger.isInfoEnabled) {
            val message = message()
            if (appendToStdOut) {
                println("[INFO] $message")
            }
            logger.info(message)
        }
    }


    fun warn(message: () -> String) {
        if (logger.isWarnEnabled) {
            val message = message()
            if (appendToStdOut) {
                println("[WARN] $message")
            }
            logger.warn(message)
        }
    }

    fun error(message: () -> String) {
        if (logger.isErrorEnabled) {
            val message = message()
            if (appendToStdOut) {
                println("[ERROR] $message")
            }
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
            val message = message()
            if (appendToStdOut) {
                println("[ERROR] $message")
                throwable?.printStackTrace()
            }
            logger.error(message(), throwable)
        }
    }
}
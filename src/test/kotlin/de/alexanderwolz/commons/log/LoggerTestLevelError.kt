package de.alexanderwolz.commons.log

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.also
import kotlin.collections.filter
import kotlin.collections.joinToString
import kotlin.io.use
import kotlin.jvm.javaClass
import kotlin.test.assertEquals
import kotlin.text.isNotBlank
import kotlin.text.lines
import kotlin.text.startsWith


class LoggerTestLevelError : AbstractLoggerTest() {

    @BeforeEach
    fun setupLogger() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "ERROR")
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false")
    }

    @Test
    fun testLogLinesWithLevelTrace() {

        val logLines = saveLogs {
            val logger = Logger(javaClass)
            logger.trace { "This is a trace log" }
            logger.info { "This is an info log" }
            logger.debug { "This is a debug log" }
            logger.warn { "This is a warning log" }
            logger.error { "This is an error log" }
            logger.error(kotlin.NoSuchElementException("This is an example exception")) { "This is a second error log" }
        }

        assertEquals(3, logLines.size)
        assertEquals("ERROR ${javaClass.name} - This is an error log", logLines[0])
        assertEquals("ERROR ${javaClass.name} - This is a second error log", logLines[1])
        assertEquals(
            "${NoSuchElementException::class.qualifiedName}: This is an example exception",
            logLines[2]
        )
    }


}
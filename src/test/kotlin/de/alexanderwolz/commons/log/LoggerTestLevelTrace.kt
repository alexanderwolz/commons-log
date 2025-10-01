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


class LoggerTestLevelTrace : AbstractLoggerTest() {

    @BeforeEach
    fun setupLogger() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "TRACE")
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

        assertEquals(7, logLines.size)
        assertEquals("TRACE ${javaClass.name} - This is a trace log", logLines[0])
        assertEquals("INFO ${javaClass.name} - This is an info log", logLines[1])
        assertEquals("DEBUG ${javaClass.name} - This is a debug log", logLines[2])
        assertEquals("WARN ${javaClass.name} - This is a warning log", logLines[3])
        assertEquals("ERROR ${javaClass.name} - This is an error log", logLines[4])
        assertEquals("ERROR ${javaClass.name} - This is a second error log", logLines[5])
        assertEquals(
            "${NoSuchElementException::class.qualifiedName}: This is an example exception",
            logLines[6]
        )
    }


}
package de.alexanderwolz.commons.log

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LoggerTest {

    @Test
    fun testInstances() {
        Logger(String::class.java).apply {
            assertNotNull(this)
            assertEquals("java.lang.String", name)
        }

        Logger("Rumpelstilzchen").apply {
            assertNotNull(this)
            assertEquals("Rumpelstilzchen", name)
        }

        Logger(NoSuchElementException::class).apply {
            assertNotNull(this)
            assertEquals("java.util.NoSuchElementException", name)
        }

        Logger("LoggerWithEvents") {}.apply {
            assertNotNull(this)
            assertEquals("LoggerWithEvents", name)
        }

    }

    @Test
    fun testLogging() {

        val logger = Logger(javaClass)
        logger.info { logger.level.name }

        assertEquals(Level.DEBUG, logger.level)
        val logLines = saveLogs {
            val logger = Logger(javaClass)
            logger.trace { "This is a trace log" }
            logger.trace { "This is another trace log" }
            logger.debug { "This is a debug log" }
            logger.info { "This is an info log" }
            logger.warn { "This is a warning log" }
            logger.error { "This is an error log" }
            logger.error(kotlin.NoSuchElementException("This is an example exception")) { "This is a second error log" }
        }

        assertEquals(6, logLines.size)
        assertTrue { logLines[0].endsWith(" - This is a debug log") }
        assertTrue { logLines[1].endsWith(" - This is an info log") }
        assertTrue { logLines[2].endsWith(" - This is a warning log") }
        assertTrue { logLines[3].endsWith(" - This is an error log") }
        assertTrue { logLines[4].endsWith(" - This is a second error log") }
        assertTrue { logLines[5].endsWith("This is an example exception") }
    }

    @Test
    fun testLogLevels() {
        val mockLogger = mockk<org.slf4j.Logger>(relaxed = true)
        val events = ArrayList<Event>()
        val logger = Logger(mockLogger) {
            events.add(it)
        }

        every { mockLogger.isInfoEnabled } returns false
        every { mockLogger.isDebugEnabled } returns false
        every { mockLogger.isInfoEnabled } returns false
        every { mockLogger.isWarnEnabled } returns false
        every { mockLogger.isErrorEnabled } returns false

        logger.trace { "My trace log" }
        logger.debug { "My debug log" }
        logger.info { "My info log" }
        logger.warn { "My warn log" }
        logger.error { "My error log" }

        verify(exactly = 0) { mockLogger.trace(any()) }
        verify(exactly = 0) { mockLogger.debug(any()) }
        verify(exactly = 0) { mockLogger.info(any()) }
        verify(exactly = 0) { mockLogger.warn(any()) }
        verify(exactly = 0) { mockLogger.error(any()) }

        assertEquals(5, events.size)
        assertEquals(Level.TRACE, events[0].level)
        assertEquals("My trace log", events[0].message)
        assertEquals(javaClass.name, events[0].caller.className)
        assertEquals(Level.DEBUG, events[1].level)
        assertEquals("My debug log", events[1].message)
        assertEquals(Level.INFO, events[2].level)
        assertEquals("My info log", events[2].message)
        assertEquals(Level.WARN, events[3].level)
        assertEquals("My warn log", events[3].message)
        assertEquals(Level.ERROR, events[4].level)
        assertEquals("My error log", events[4].message)
    }

    @Test
    fun testLoggingWithAdditionalCallback() {

        Logger(javaClass).also {
            val logLines = saveLogs {
                it.info { "My Log" }
            }
            assertEquals(1, logLines.size)
        }

        val events = ArrayList<Event>()
        Logger(javaClass) {
            events.add(it)
        }.also {
            it.info { "My info Log" }
            it.error { "My error Log" }
            it.error(NoSuchElementException("Nope")) { "My error Log with exception" }
        }
        assertEquals(3, events.size)


        events.clear()
        val callback: (Event) -> Unit = { event ->
            events.add(event)
        }

        Logger(javaClass).apply {

            warn { "Some Warn Log" } //should not end up in the list
            assertEquals(0, events.size)

            addEventListener(callback)
            info { "Some Info Log" }
            assertEquals(1, events.size)

            events.clear()
            removeEventListener(callback)
            trace { "Some Trace Log" }
            assertEquals(0, events.size)

        }

    }

    private fun saveLogs(log: () -> Unit): List<String> {

        val originalOut = System.out
        val originalErr = System.err

        val streamOut = ByteArrayOutputStream()
        val streamError = ByteArrayOutputStream()
        val psOut = PrintStream(streamOut)
        val psError = PrintStream(streamError)


        System.setOut(psOut)
        System.setErr(psError)

        log()

        System.setOut(originalOut)
        System.setErr(originalErr)

        val allLines = ArrayList<String>().apply {
            val lines = streamOut.use { it.toString() }.lines().filter { it.isNotBlank() }.toList()
            addAll(lines)
        }.apply {
            val lines = streamError.use { it.toString() }.lines().filter {
                it.isNotBlank()
                        && !it.startsWith("\tat ")
            }.toList()
            addAll(lines)
        }

        return allLines.also {
            println("*********\n${it.joinToString(separator = "\n")}\n*********")
        }
    }

}
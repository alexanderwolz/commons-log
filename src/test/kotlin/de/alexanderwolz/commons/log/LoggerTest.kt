package de.alexanderwolz.commons.log

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
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

        every { mockLogger.isTraceEnabled } returns false
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

    @Test
    fun testEventContainsThrowable() {
        val events = ArrayList<Event>()
        val logger = Logger(mockk<org.slf4j.Logger>(relaxed = true)) { events.add(it) }
        val exception = NoSuchElementException("Nope")

        logger.error(exception) { "With exception" }
        logger.error(exception)

        assertEquals(2, events.size)
        assertSame(exception, events[0].error)
        assertSame(exception, events[1].error)
        assertEquals("Nope", events[1].message)
    }

    @Test
    fun testCallerIsResolvedForAllOverloads() {
        val events = ArrayList<Event>()
        val logger = Logger(mockk<org.slf4j.Logger>(relaxed = true)) { events.add(it) }

        logger.info { "info" }
        logger.error(NoSuchElementException("Nope"))
        logger.error(NoSuchElementException("Nope")) { "error" }

        assertEquals(3, events.size)
        events.forEach {
            assertEquals(javaClass.name, it.caller.className)
            assertEquals("testCallerIsResolvedForAllOverloads", it.caller.methodName)
        }
    }

    @Test
    fun testMessageIsEvaluatedOnce() {
        val mockLogger = mockk<org.slf4j.Logger>(relaxed = true)
        every { mockLogger.isInfoEnabled } returns true
        val logger = Logger(mockLogger) {}

        var count = 0
        logger.info { count++; "message" }

        assertEquals(1, count)
    }

    @Test
    fun testMessageIsNotEvaluatedIfDisabledAndNoListeners() {
        val mockLogger = mockk<org.slf4j.Logger>(relaxed = true)
        every { mockLogger.isDebugEnabled } returns false
        val logger = Logger(mockLogger)

        var count = 0
        logger.debug { count++; "message" }

        assertEquals(0, count)
    }

    @Test
    fun testLevelReflectsRuntimeChanges() {
        val mockLogger = mockk<org.slf4j.Logger>(relaxed = true)
        val logger = Logger(mockLogger)
        assertEquals(Level.NONE, logger.level)

        every { mockLogger.isInfoEnabled } returns true
        assertTrue(logger.isInfoEnabled)
        assertEquals(Level.INFO, logger.level)
    }

    @Test
    fun testListenerCanRemoveItselfWhileLogging() {
        val logger = Logger(mockk<org.slf4j.Logger>(relaxed = true))
        var count = 0
        lateinit var listener: (Event) -> Unit
        listener = {
            count++
            logger.removeEventListener(listener)
        }
        logger.addEventListener(listener)

        logger.info { "first" }
        logger.info { "second" }

        assertEquals(1, count)
    }

    @Test
    fun testFailingListenerDoesNotBreakLogging() {
        val mockLogger = mockk<org.slf4j.Logger>(relaxed = true)
        every { mockLogger.isInfoEnabled } returns true
        val events = ArrayList<Event>()
        val logger = Logger(mockLogger) { throw IllegalStateException("broken listener") }
        logger.addEventListener { events.add(it) }

        logger.info { "still logged" }

        assertEquals(1, events.size)
        verify(exactly = 1) { mockLogger.info("still logged", null) }
        verify(exactly = 1) { mockLogger.warn(any<String>(), any<IllegalStateException>()) }
    }

    @Test
    fun testThrowableOverloadsForAllLevels() {
        val mockLogger = mockk<org.slf4j.Logger>(relaxed = true)
        every { mockLogger.isTraceEnabled } returns true
        every { mockLogger.isDebugEnabled } returns true
        every { mockLogger.isInfoEnabled } returns true
        every { mockLogger.isWarnEnabled } returns true
        val events = ArrayList<Event>()
        val logger = Logger(mockLogger) { events.add(it) }
        val exception = NoSuchElementException("Nope")

        logger.trace(exception) { "trace" }
        logger.debug(exception) { "debug" }
        logger.info(exception) { "info" }
        logger.warn(exception) { "warn" }
        logger.warn(exception)

        verify { mockLogger.trace("trace", exception) }
        verify { mockLogger.debug("debug", exception) }
        verify { mockLogger.info("info", exception) }
        verify { mockLogger.warn("warn", exception) }
        verify { mockLogger.warn("Nope", exception) }
        assertEquals(5, events.size)
        events.forEach { assertSame(exception, it.error) }
        assertEquals(Level.WARN, events[4].level)
    }

    @Test
    fun testListenerMinLevel() {
        val logger = Logger(mockk<org.slf4j.Logger>(relaxed = true))
        val warnEvents = ArrayList<Event>()
        val noneEvents = ArrayList<Event>()
        logger.addEventListener(Level.WARN) { warnEvents.add(it) }
        logger.addEventListener(Level.NONE) { noneEvents.add(it) }

        logger.trace { "trace" }
        logger.debug { "debug" }
        logger.info { "info" }
        logger.warn { "warn" }
        logger.error { "error" }

        assertEquals(listOf(Level.WARN, Level.ERROR), warnEvents.map { it.level })
        assertEquals(0, noneEvents.size)
    }

    @Test
    fun testMessageIsNotEvaluatedIfNoListenerIsInterested() {
        val logger = Logger(mockk<org.slf4j.Logger>(relaxed = true))
        logger.addEventListener(Level.ERROR) {}

        var count = 0
        logger.debug { count++; "message" }

        assertEquals(0, count)
    }

    @Test
    fun testEventContext() {
        val events = ArrayList<Event>()
        val logger = Logger(mockk<org.slf4j.Logger>(relaxed = true)) { events.add(it) }

        val before = Instant.now()
        val thread = Thread({ logger.info { "from thread" } }, "worker-1")
        thread.start()
        thread.join()

        assertEquals(1, events.size)
        assertEquals("worker-1", events[0].threadName)
        assertTrue(!events[0].timestamp.isBefore(before))
        assertTrue(!events[0].timestamp.isAfter(Instant.now()))
    }

    @Test
    fun testReifiedFactory() {
        val events = ArrayList<Event>()
        val logger = logger<LoggerTest> { events.add(it) }
        assertEquals(LoggerTest::class.java.name, logger.name)

        logger.info { "hello" }
        assertEquals(1, events.size)
        assertEquals("testReifiedFactory", events[0].caller.methodName)
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

        try {
            log()
        } finally {
            System.setOut(originalOut)
            System.setErr(originalErr)
        }

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
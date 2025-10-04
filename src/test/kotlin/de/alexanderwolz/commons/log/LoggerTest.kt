package de.alexanderwolz.commons.log

import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LoggerTest {

    @Test
    fun testInstances() {
        val logger1 = Logger(String::class.java)
        assertNotNull(logger1)
        assertEquals("java.lang.String", logger1.name)

        val logger2 = Logger("Rumpelstilzchen")
        assertNotNull(logger2)
        assertEquals("Rumpelstilzchen", logger2.name)

        val logger3 = Logger(NoSuchElementException::class)
        assertNotNull(logger3)
        assertEquals("java.util.NoSuchElementException", logger3.name)
    }

    @Test
    fun testLogging() {
        val logger = Logger(javaClass)
        logger.info { logger.level.name }

        if (logger.isDebugEnabled && !logger.isTraceEnabled) {
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

    }

    @Test
    fun testLoggingWithStdOut() {

        Logger(javaClass).also {
            val logLines = saveLogs {
                it.info { "My Log" }
            }
            assertEquals(1, logLines.size)
        }

        Logger(javaClass, true).also {
            val logLines = saveLogs {
                it.info { "My other Log" }
            }
            assertEquals(2, logLines.size)
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
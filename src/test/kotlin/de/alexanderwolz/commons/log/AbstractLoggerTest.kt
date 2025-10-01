package de.alexanderwolz.commons.log

import java.io.ByteArrayOutputStream
import java.io.PrintStream

abstract class AbstractLoggerTest {

    protected fun saveLogs(log:()->Unit): List<String> {
        val originalErr = System.err
        val baos = ByteArrayOutputStream()
        val ps = PrintStream(baos)

        System.setErr(ps)

        log()

        System.setErr(originalErr)

        return baos.use { it.toString() }.lines().filter { it.isNotBlank() && !it.startsWith("\tat ") }.also {
            println("*********\n${it.joinToString(separator = "\n")}\n*********")
        }
    }

}
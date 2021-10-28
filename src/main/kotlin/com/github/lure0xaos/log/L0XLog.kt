package com.github.lure0xaos.log

import java.lang.System.Logger.Level
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.stream.Stream

object L0XLog {

    private val local: ThreadLocal<Int> = ThreadLocal.withInitial { 0 }
    private val indent get() = "\t".repeat(local.get())

    @Suppress("MemberVisibilityCanBePrivate")
    fun <R : Any?> log(level: Level = Level.TRACE, message: String? = null, function: () -> R): R {
        val start = LocalDateTime.now()
        val element = stackTraceElement()
        val logger = logger(element)
        if (!logger.isLoggable(level)) return function()
        val indent = indent
        val execution =
            message ?: "${element.className}#${element.methodName}[${element.fileName}:${element.lineNumber}]"
        logger.log(level, ">>> $indent $execution")
        local.set(local.get() + 1)
        try {
            return function()
        } finally {
            local.set(local.get() - 1)
            val diff = ChronoUnit.MILLIS.between(start, LocalDateTime.now())
            logger.log(level, "<<< $indent $execution <${diff}ms>")
        }
    }


    @Suppress("unused")
    fun <R : Any?> (() -> R).logging(level: Level = Level.TRACE, execution: String? = null): R =
        log(level, execution, this)

    fun info(msg: String): Unit? =
        logger().takeIf { it.isLoggable(Level.INFO) }?.log(Level.INFO, indent + msg)

    @Suppress("unused")
    fun debug(msg: String): Unit? =
        logger().takeIf { it.isLoggable(Level.DEBUG) }?.log(Level.DEBUG, indent + msg)

    @Suppress("unused")
    fun trace(msg: String): Unit? =
        logger().takeIf { it.isLoggable(Level.TRACE) }?.log(Level.TRACE, indent + msg)

    private fun logger(element: StackTraceElement) = System.getLogger(element.className)
    private fun logger() = logger(stackTraceElement())
    private fun stackTraceElement(): StackTraceElement {
        val instance = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
        return instance.walk { stream: Stream<StackWalker.StackFrame> ->
            stream.filter { frame ->
                !frame.className.contains('$') && PACKAGES.none(frame.className::startsWith)
            }.findFirst().map { frame -> frame.toStackTraceElement() }
        }.orElseGet { StackTraceElement(instance.callerClass.name, "", "", 0) }
    }

    private val PACKAGES = listOf(
        "java.", "javax.",
        "kotlin.", "kotlinx.",
        "${L0XLog::class.java.packageName}."
    )
}

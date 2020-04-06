package net.sarazan.bismarck.platform

import kotlin.reflect.KClass
import kotlinx.coroutines.CoroutineDispatcher

expect interface Closeable {
    @Throws(Exception::class)
    fun close()
}

expect fun currentTimeMillis(): Long
expect fun currentTimeNano(): Long

expect annotation class Throws(vararg val exceptionClasses: KClass<out Throwable>)

expect object BismarckDispatchers {
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}

fun logError(name: String, message: String, err: Throwable) {
    println("[$name] $message\n$err")
}

package net.sarazan.bismarck.platform

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
fun currentTimeNano(): Long = Clock.System.now().toEpochNanoseconds()

expect object BismarckDispatchers {
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}

fun logError(name: String, message: String, err: Throwable) {
    println("[$name] $message\n$err")
}

fun Instant.toEpochNanoseconds(): Long {
    return epochSeconds * 1_000_000_000 + nanosecondsOfSecond
}

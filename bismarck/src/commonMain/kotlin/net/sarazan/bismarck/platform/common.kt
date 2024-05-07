package net.sarazan.bismarck.platform

import kotlinx.coroutines.CoroutineDispatcher

expect object BismarckDispatchers {
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}

fun logError(name: String, message: String, err: Throwable) {
    println("[$name] $message\n$err")
}
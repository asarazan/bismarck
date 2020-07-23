package net.sarazan.bismarck.mobile

internal actual suspend fun <R> network(block: suspend () -> R): R = block()

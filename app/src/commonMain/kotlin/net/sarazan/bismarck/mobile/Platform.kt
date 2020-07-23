package net.sarazan.bismarck.mobile

internal expect suspend fun <R> network(block: suspend () -> R): R

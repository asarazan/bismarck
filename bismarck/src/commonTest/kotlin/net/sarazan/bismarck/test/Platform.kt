package net.sarazan.bismarck.test

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope

expect fun runBlockingTest(
    context: CoroutineContext = EmptyCoroutineContext,
    testBody: suspend CoroutineScope.() -> Unit
)

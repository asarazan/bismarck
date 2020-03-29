package net.sarazan.bismarck.test

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking

@ExperimentalCoroutinesApi
actual fun runBlockingTest(
    context: CoroutineContext,
    testBody: suspend CoroutineScope.() -> Unit
) {
    runBlocking(context, testBody)
}

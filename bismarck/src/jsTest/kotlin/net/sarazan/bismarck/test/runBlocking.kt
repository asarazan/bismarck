package net.sarazan.bismarck.test

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
actual fun runBlockingTest(
    context: CoroutineContext,
    testBody: suspend CoroutineScope.() -> Unit
) {
    GlobalScope.launch(context, block = testBody)
}

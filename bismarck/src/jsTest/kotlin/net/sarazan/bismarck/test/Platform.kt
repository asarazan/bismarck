package net.sarazan.bismarck.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import net.sarazan.bismarck.platform.BismarckDispatchers

private val scope = CoroutineScope(BismarckDispatchers.default)

@ExperimentalCoroutinesApi
actual fun runBlockingTest(testBody: suspend CoroutineScope.() -> Unit) {
    scope.launch {
        testBody()
    }
}

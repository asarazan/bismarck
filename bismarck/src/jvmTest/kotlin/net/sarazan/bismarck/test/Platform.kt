package net.sarazan.bismarck.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking

@ExperimentalCoroutinesApi
actual fun runBlockingTest(testBody: suspend CoroutineScope.() -> Unit) {
    runBlocking {
        testBody()
    }
}

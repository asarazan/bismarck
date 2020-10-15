package net.sarazan.bismarck.test

import kotlinx.coroutines.CoroutineScope

expect fun runBlockingTest(testBody: suspend CoroutineScope.() -> Unit)

package net.sarazan.bismarck.platform

import kotlinx.coroutines.CoroutineDispatcher

actual interface Closeable {
    @Throws(Exception::class)
    actual fun close()
}

actual object BismarckDispatchers {
    actual val default: CoroutineDispatcher = MainLoopDispatcher
    actual val main: CoroutineDispatcher = MainLoopDispatcher
}

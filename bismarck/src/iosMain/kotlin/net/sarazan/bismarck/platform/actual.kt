package net.sarazan.bismarck.platform

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual interface Closeable {
    @Throws(Exception::class)
    actual fun close()
}

actual object BismarckDispatchers {
    actual val default: CoroutineDispatcher = Dispatchers.Main
    actual val main: CoroutineDispatcher = Dispatchers.Main
}

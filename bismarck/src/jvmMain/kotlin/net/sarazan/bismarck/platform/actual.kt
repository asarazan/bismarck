package net.sarazan.bismarck.platform

import java.io.Closeable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual typealias Closeable = Closeable

actual object BismarckDispatchers {
    actual val default: CoroutineDispatcher = Dispatchers.Default
    actual val main: CoroutineDispatcher = Dispatchers.Main
}

package net.sarazan.bismarck.platform

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual object BismarckDispatchers {
    actual val default: CoroutineDispatcher = Dispatchers.Default
    actual val main: CoroutineDispatcher = Dispatchers.Main
}

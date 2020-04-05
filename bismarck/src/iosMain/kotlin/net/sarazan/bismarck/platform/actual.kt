package net.sarazan.bismarck.platform

import kotlin.native.Throws
import kotlin.system.getTimeMillis
import kotlin.system.getTimeNanos
import kotlinx.coroutines.CoroutineDispatcher
import net.sarazan.bismarck.platform.files.FileLike

actual typealias Throws = Throws

actual fun currentTimeMillis(): Long = getTimeMillis()
actual fun currentTimeNano(): Long = getTimeNanos()

actual interface Closeable {
    @Throws(Exception::class)
    actual fun close()
}

actual fun getFile(root: String, child: String?): FileLike {
    return CocoaFile(root, child)
}

actual object BismarckDispatchers {
    actual val default: CoroutineDispatcher = MainLoopDispatcher
    actual val main: CoroutineDispatcher = MainLoopDispatcher
}

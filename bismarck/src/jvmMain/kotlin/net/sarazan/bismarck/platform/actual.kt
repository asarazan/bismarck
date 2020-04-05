package net.sarazan.bismarck.platform

import java.io.Closeable
import kotlin.jvm.Throws
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import net.sarazan.bismarck.platform.files.FileLike

actual typealias Throws = Throws

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
actual fun currentTimeNano(): Long = System.nanoTime()

actual typealias Closeable = Closeable

actual fun getFile(root: String, child: String?): FileLike {
    return JavaFile(root, child)
}

actual object BismarckDispatchers {
    actual val default: CoroutineDispatcher = Dispatchers.Default
    actual val main: CoroutineDispatcher = Dispatchers.Main
}

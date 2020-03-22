package net.sarazan.bismarck.platform

import kotlin.native.Throws
import kotlin.system.getTimeMillis
import kotlin.system.getTimeNanos

actual typealias Throws = Throws

actual fun currentTimeMillis(): Long = getTimeMillis()
actual fun currentTimeNano(): Long = getTimeNanos()

actual interface Closeable {
    @Throws(Exception::class)
    actual fun close()
}
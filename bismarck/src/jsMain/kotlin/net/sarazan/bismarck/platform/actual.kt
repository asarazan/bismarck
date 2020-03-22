package net.sarazan.bismarck.platform

import kotlin.js.Date
import kotlin.reflect.KClass

actual fun currentTimeMillis(): Long = Date.now().toLong()
actual fun currentTimeNano(): Long = (Date.now() * 1000.0).toLong()

actual annotation class Throws(
    actual vararg val exceptionClasses: KClass<out Throwable>
)

actual interface Closeable {
    @Throws(Exception::class)
    actual fun close()
}
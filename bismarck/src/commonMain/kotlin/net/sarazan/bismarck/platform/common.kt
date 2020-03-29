package net.sarazan.bismarck.platform

import kotlin.reflect.KClass
import net.sarazan.bismarck.platform.files.FileLike

expect interface Closeable {
    @Throws(Exception::class)
    fun close()
}

expect fun currentTimeMillis(): Long
expect fun currentTimeNano(): Long

expect annotation class Throws(vararg val exceptionClasses: KClass<out Throwable>)

expect fun getFile(root: String, child: String? = null): FileLike

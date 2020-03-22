package net.sarazan.bismarck.platform

import java.io.Closeable
import kotlin.jvm.Throws

actual typealias Throws = Throws

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
actual fun currentTimeNano(): Long = System.nanoTime()

actual typealias Closeable = Closeable
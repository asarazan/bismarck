package net.sarazan.bismarck.platform

import platform.CoreFoundation.CFAbsoluteTimeGetCurrent
import platform.UIKit.UIDevice
import kotlin.native.Throws
import kotlin.system.getTimeMillis
import kotlin.system.getTimeNanos

actual typealias Throws = Throws

actual fun platformName(): String {
    return UIDevice.currentDevice.systemName() +
            " " +
            UIDevice.currentDevice.systemVersion
}

actual fun currentTimeMillis(): Long = getTimeMillis()
actual fun currentTimeNano(): Long = getTimeNanos()

actual interface Closeable {
    @Throws(Exception::class)
    actual fun close()
}
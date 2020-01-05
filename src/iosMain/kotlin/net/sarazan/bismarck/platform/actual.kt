package net.sarazan.bismarck.platform

import platform.CoreFoundation.CFAbsoluteTimeGetCurrent
import platform.UIKit.UIDevice
import kotlin.native.Throws

actual typealias Throws = Throws

actual fun platformName(): String {
    return UIDevice.currentDevice.systemName() +
            " " +
            UIDevice.currentDevice.systemVersion
}

actual fun currentTimeMillis(): Long {
    return (CFAbsoluteTimeGetCurrent() * 1000.0).toLong()
}
package com.jetbrains.handson.mpp.mobile

import platform.UIKit.UIDevice
import kotlin.native.Throws

actual typealias Throws = Throws

actual fun platformName(): String {
    return UIDevice.currentDevice.systemName() +
            " " +
            UIDevice.currentDevice.systemVersion
}

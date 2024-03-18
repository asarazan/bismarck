package net.sarazan.bismarck.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*

private val mgr = NSFileManager.defaultManager

@OptIn(ExperimentalForeignApi::class)
internal actual class File(private val url: NSURL) {

    actual constructor(path: String) : this(NSURL.fileURLWithPath(path))

    actual constructor(parent: String, child: String) :
            this(NSURL.fileURLWithPath(child, NSURL.fileURLWithPath(parent)))

    actual constructor(parent: File, child: String) :
            this(NSURL.fileURLWithPath(child, parent.url))

    actual val parentFile: File?
        get() = url.URLByDeletingLastPathComponent?.let(::File)

    actual val exists: Boolean
        get() = mgr.fileExistsAtPath(this.url.path!!)

    actual fun delete(): Boolean {
        return mgr.removeItemAtURL(url, null)
    }

    actual fun mkdirs(): Boolean {
        return mgr.createDirectoryAtURL(url, true, null, null)
    }

    actual fun createNewFile(): Boolean {
        return mgr.createFileAtPath(url.path!!, null, null)
    }

    actual fun readBytes(): ByteArray {
        return NSData.dataWithContentsOfURL(url)!!.toByteArray()
    }

    actual fun writeBytes(bytes: ByteArray) {
        bytes.toNSData().writeToURL(url, true)
    }
}

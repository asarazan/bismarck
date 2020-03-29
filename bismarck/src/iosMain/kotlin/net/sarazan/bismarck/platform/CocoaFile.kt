package net.sarazan.bismarck.platform

import net.sarazan.bismarck.platform.files.FileLike
import platform.Foundation.*

private val mgr = NSFileManager.defaultManager

class CocoaFile(private val url: NSURL) : FileLike {

    constructor(root: String, child: String? = null) : this(NSURL(string = child ?: root, relativeToURL = child?.let { NSURL(string = root) }))

    override val parentFile: FileLike?
        get() = url.URLByDeletingLastPathComponent?.let(::CocoaFile)

    override val exists: Boolean
        get() = mgr.fileExistsAtPath(this.url.path!!)

    override fun delete(): Boolean {
        return mgr.removeItemAtURL(url, null)
    }

    override fun mkdirs(): Boolean {
        return mgr.createDirectoryAtURL(url, true, null, null)
    }

    override fun createNewFile(): Boolean {
        return mgr.createFileAtPath(url.path!!, null, null)
    }

    override fun readBytes(): ByteArray {
        return NSData.dataWithContentsOfURL(url)!!.toByteArray()
    }

    override fun writeBytes(bytes: ByteArray) {
        bytes.toNSData().writeToURL(url, true)
    }
}

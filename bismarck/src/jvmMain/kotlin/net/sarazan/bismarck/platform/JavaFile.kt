package net.sarazan.bismarck.platform

import java.io.File

class JavaFile(private val file: File) : FileLike {

    override val parentFile: FileLike = JavaFile(file.parentFile)
    override val exists: Boolean
        get() = file.exists()

    constructor(root: String, child: String) : this(File(root, child))

    override fun delete() = file.delete()
    override fun mkdirs() = file.mkdirs()
    override fun createNewFile() = file.createNewFile()
    override fun readBytes() = file.readBytes()
    override fun writeBytes(bytes: ByteArray) = file.writeBytes(bytes)
}
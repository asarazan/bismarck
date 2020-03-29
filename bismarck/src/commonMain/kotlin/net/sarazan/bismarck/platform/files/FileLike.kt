package net.sarazan.bismarck.platform.files

interface FileLike {
    val parentFile: FileLike?
    val exists: Boolean

    fun delete(): Boolean
    fun mkdirs(): Boolean
    fun createNewFile(): Boolean

    fun readBytes(): ByteArray
    fun writeBytes(bytes: ByteArray)
}

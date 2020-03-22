package net.sarazan.bismarck.persisters

import net.sarazan.bismarck.Serializer
import net.sarazan.bismarck.platform.FileLike

abstract class FilePersister<T: Any>(
    val root: String,
    val serializer: Serializer<T>
) : MemoryPersister<T>() {
    /**
     * We require this to be dynamically computed because the path is often dependent on userid,
     * and we want to switch over automatically in case of an auth event.
     */
    abstract fun path(): String?
    abstract fun getFile(root: String, path: String): FileLike

    override fun get(): T? {
        val path = path() ?: return null
        val cached = super.get()
        if (cached != null) return cached
        val file = getFile(root, path)
        if (!file.exists) return null
        val loaded = serializer.deserialize(file.readBytes())
        super.put(loaded)
        return loaded
    }

    override fun put(data: T?) {
        val path = path() ?: return
        super.put(data)
        val file = getFile(root, path)
        if (data == null) {
            file.delete()
        } else {
            val parent = file.parentFile
            if (!parent.exists) {
                parent.mkdirs()
            }
            if (!file.exists) {
                file.createNewFile()
            }
            file.writeBytes(serializer.serialize(data))
        }
    }
}
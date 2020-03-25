package net.sarazan.bismarck.persisters

import net.sarazan.bismarck.Serializer
import net.sarazan.bismarck.platform.getFile

class FilePersister<T: Any>(
    val root: String,
    var path: String?,
    val serializer: Serializer<T>
) : MemoryPersister<T>() {

    override fun get(): T? {
        val path = path ?: return null
        val cached = super.get()
        if (cached != null) return cached
        val file = getFile(root, path)
        if (!file.exists) return null
        val loaded = serializer.deserialize(file.readBytes())
        super.put(loaded)
        return loaded
    }

    override fun put(data: T?) {
        val path = path ?: return
        super.put(data)
        val file = getFile(root, path)
        if (data == null) {
            file.delete()
        } else {
            val parent = file.parentFile
            if (parent?.exists == false) {
                parent.mkdirs()
            }
            if (!file.exists) {
                file.createNewFile()
            }
            file.writeBytes(serializer.serialize(data))
        }
    }
}
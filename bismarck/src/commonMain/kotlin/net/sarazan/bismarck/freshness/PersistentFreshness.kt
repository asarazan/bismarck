package net.sarazan.bismarck.freshness

import net.sarazan.bismarck.platform.File
import net.sarazan.bismarck.platform.readString
import net.sarazan.bismarck.platform.writeString
import kotlin.time.Duration

class PersistentFreshness(path: String, duration: Duration) : SimpleFreshness(duration) {

    private val file = File(path)

    init {
        if (file.exists) {
            val split = file.readString().split(":")
            lastRunNanos = split[0].toLong()
            resetNanos = split[1].toLong()
        }
    }

    override fun save() {
        file.writeString("$lastRunNanos:$resetNanos")
    }
}

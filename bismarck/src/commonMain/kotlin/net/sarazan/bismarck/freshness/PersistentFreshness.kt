package net.sarazan.bismarck.freshness

import net.sarazan.bismarck.platform.File
import net.sarazan.bismarck.platform.readString
import net.sarazan.bismarck.platform.writeString
import kotlin.time.Duration
import kotlin.time.TimeSource

internal class PersistentFreshness(
    path: String,
    duration: Duration,
    timeSource: TimeSource.WithComparableMarks
) : SimpleFreshness(duration, timeSource) {

    private val file = File(path)

    init {
        if (file.exists) {
            val split = file.readString().split(":")
            /*lastRunNanos = split[0].toLong()
            resetNanos = split[1].toLong()*/
        }
    }

    override fun save() {
        file.writeString("$lastRunMark:$resetMark")
    }
}

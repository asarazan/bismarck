package net.sarazan.bismarck.util.concurrency

import kotlinx.coroutines.yield

/**
 * This mostly just gets used for tests.
 */
internal class SuspendLock(isLocked: Boolean = false) {

    var isLocked = isLocked
        private set

    suspend fun lock() {
        while (isLocked) yield()
        isLocked = true
    }

    fun unlock() {
        isLocked = false
    }
}
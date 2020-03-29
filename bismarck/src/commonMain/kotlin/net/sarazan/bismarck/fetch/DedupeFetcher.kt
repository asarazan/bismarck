package net.sarazan.bismarck.fetch

import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.withLock
import net.sarazan.bismarck.RateLimiter
import net.sarazan.bismarck.platform.currentTimeNano

class DedupeFetcher<T>(
    fetch: Fetch<T>?,
    private val rateLimiter: RateLimiter?
) : Fetcher<T>(fetch) {

    private val lock = Lock()

    override suspend fun doFetch(): T? = try {
        val ts = currentTimeNano()
        _count.incrementAndGet()
        lock.withLock {
            val lastInvalidate = rateLimiter?.lastReset ?: 0L
            if (lastInvalidate > ts) {
                fetch?.invoke()
            } else {
                null // TODO how to differentiate from error state?
            }
        }
    } finally {
        _count.decrementAndGet()
    }
}

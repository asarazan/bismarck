package net.sarazan.bismarck.fetch

import co.touchlab.stately.concurrency.AtomicInt
import net.sarazan.bismarck.RateLimiter

open class Fetcher<T>(protected val fetch: Fetch<T>?) {
    companion object {
        fun <T : Any> get(fetch: Fetch<T>?, rateLimiter: RateLimiter?, dedupe: Boolean): Fetcher<T>? {
            if (fetch == null) return null
            return if (dedupe) {
                DedupeFetcher(fetch, rateLimiter)
            } else {
                Fetcher(fetch)
            }
        }
    }

    val count get() = _count.get()
    protected val _count = AtomicInt(0)

    open suspend fun doFetch(): T? {
        _count.incrementAndGet()
        return try {
            fetch?.invoke()
        } finally {
            _count.decrementAndGet()
        }
    }
}

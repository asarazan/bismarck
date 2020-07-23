package net.sarazan.bismarck.mobile

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import net.sarazan.bismarck.Bismarck
import net.sarazan.bismarck.platform.BismarckDispatchers
import net.sarazan.bismarck.ratelimit.SimpleFreshness
import kotlin.native.concurrent.ThreadLocal

class FeedViewModel(private val scope: CoroutineScope) {
    constructor() : this(CoroutineScope(BismarckDispatchers.main))

    @ThreadLocal
    val cache = Bismarck.create<Feed> {
        fetcher = {
            println("@@@ fetcher get feed")
            Api.getFeed()
        }
        checkOnLaunch = true
        freshness = SimpleFreshness(60 * 1000)
    }

    fun forceRefresh() {
        cache.invalidate()
    }

    fun refreshIfStale() {
        cache.check()
    }

    fun tearDown() {
        println("Tearing Down")
        scope.cancel()
    }
}
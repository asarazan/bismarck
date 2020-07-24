package net.sarazan.bismarck.mobile

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.sarazan.bismarck.Bismarck
import net.sarazan.bismarck.extensions.fileStorage
import net.sarazan.bismarck.platform.BismarckDispatchers
import net.sarazan.bismarck.ratelimit.SimpleFreshness
import net.sarazan.bismarck.serializers.kotlinx.JsonSerializer

class FeedViewModel(private val scope: CoroutineScope, private val filePath: String) {
    constructor(filePath: String) : this(scope = CoroutineScope(BismarckDispatchers.main), filePath = filePath)

    var onEach: ((Feed) -> Unit)? = null

    val cache = Bismarck.create<Feed> {
        fetcher = {
            print("@@@ getFeed")
            Api.getFeed()
        }
        fileStorage {
            path = "$filePath/feed.json"
            serializer = JsonSerializer(Feed.serializer())
        }
        checkOnLaunch = true
        freshness = SimpleFreshness(60 * 1000)
    }

    init {
        scope.launch {
            cache.eachValue {
                println("@@@ kmp feed eachVal - $it")
                it ?: return@eachValue
                onEach?.invoke(it)
            }
        }
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

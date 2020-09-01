package net.sarazan.bismarck.mobile

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.sarazan.bismarck.Bismarck
import net.sarazan.bismarck.extensions.fileStorage
import net.sarazan.bismarck.platform.BismarckDispatchers
import net.sarazan.bismarck.ratelimit.SimpleFreshness
import net.sarazan.bismarck.serializers.kotlinx.JsonSerializer
import kotlin.native.concurrent.ThreadLocal

class FeedViewModel(private val scope: CoroutineScope, private val filePath: String, private val onEach: ((Feed) -> Unit)? = null)  {
    constructor(filePath: String, onEach: ((Feed) -> Unit)?) : this(scope = CoroutineScope(BismarckDispatchers.main), filePath = filePath, onEach = onEach)

    private val api = Api()
    val cache = Bismarck.create<Feed> {
        fetcher = {
            print("@@@ getFeed")
            api.getFeed()
        }
        fileStorage {
            path = "${filePath}/feed.json"
            serializer = JsonSerializer(Feed.serializer())
        }
        checkOnLaunch = true
        freshness = SimpleFreshness(60 * 1000)
    }

    init {
        this.onEach?.let { onEach ->
            scope.launch {
                cache.eachValue {
                    it ?: return@eachValue
                    onEach(it)
                }
            }
        }

    }
    fun forceRefresh() {
        cache.invalidate()
    }

    fun refreshIfStale() {
        cache.check()
//        scope.launch {
//            val response = api.getFeed()
//            println("@@@ feed - ${response}")
//            cache.insert(response)
//        }
    }

    fun tearDown() {
        println("Tearing Down")
        scope.cancel()
    }
}

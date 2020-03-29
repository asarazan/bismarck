package net.sarazan.bismarck

import co.touchlab.stately.concurrency.AtomicInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.launch
import net.sarazan.bismarck.BismarckState.Stale
import net.sarazan.bismarck.fetch.Fetch
import net.sarazan.bismarck.fetch.Fetcher
import net.sarazan.bismarck.persisters.MemoryPersister
import net.sarazan.bismarck.platform.Closeable

data class BismarckConfig<T : Any>(
    var fetch: Fetch<T>? = null,
    var rateLimiter: RateLimiter? = null,
    var persister: Persister<T> = MemoryPersister(),
    var dedupe: Boolean = true,
    var scope: CoroutineScope = GlobalScope,
    var debug: Boolean = false
)

@ExperimentalCoroutinesApi
class Bismarck<T : Any>(private val config: BismarckConfig<T>) : Closeable {
    constructor(config: BismarckConfig<T>.() -> Unit = {}) : this(BismarckConfig<T>().apply(config))

    val rateLimiter get() = this.config.rateLimiter
    val persister get() = this.config.persister
    val scope get() = this.config.scope
    val fetcher = Fetcher.get(this.config.fetch, rateLimiter, this.config.dedupe)

    var value: T?
        get() = trace("value") { persister.get() }
        private set(value) {
            persister.put(value)
            scope.launch {
                valueChannel.send(persister.get())
            }
        }

    val state: BismarckState get() = trace("state") {
        when {
            fetchCount.get() > 0 -> BismarckState.Fetching
            isFresh -> BismarckState.Fresh
            else -> Stale
        }
    }

    var error: Exception? = null
        private set(value) {
            field = value
            scope.launch {
                errorChannel.send(field)
            }
        }

    val isFresh: Boolean get() = trace("isFresh") {
        rateLimiter?.isFresh() ?: false
    }

    private val fetchCount = AtomicInt(0)

    val valueChannel by lazy { ConflatedBroadcastChannel(value) }
    val stateChannel by lazy { ConflatedBroadcastChannel(state) }
    val errorChannel by lazy { ConflatedBroadcastChannel(error) }

    init {
        trace("init") {
            scope.launch {
                trace("init in scope") {
                    valueChannel.send(value)
                    stateChannel.send(state)
                    errorChannel.send(error)
                }
            }
        }
    }

    fun insert(value: T?) = trace("insert") {
        this.value = value
        rateLimiter?.update()
        updateState()
    }

    fun invalidate() = trace("invalidate") {
        rateLimiter?.reset()
        fetch()
    }

    override fun close() {
        valueChannel.close()
        stateChannel.close()
        errorChannel.close()
    }

    private fun fetch() = trace("fetch") {
        val fetcher = fetcher ?: return@trace
        fetchCount.incrementAndGet()
        updateState()
        scope.launch {
            trace("fetch in scope") {
                try {
                    val fetched = fetcher.doFetch()
                    insert(fetched)
                    fetchCount.decrementAndGet()
                    updateState()
                    error = null
                } catch (e: Exception) {
                    log { "Received error: $e" }
                    error = e
                }
            }
        }
    }

    private fun updateState() = trace("updateState") {
        scope.launch {
            trace("updateState in scope") {
                stateChannel.send(state)
            }
        }
    }

    private var indent = 0
    private inline fun log(msg: () -> String) {
        if (this.config.debug) {
            println(buildString {
                (0 until indent).forEach {
                    append(" ")
                }
                append(msg())
            })
        }
    }

    private inline fun <T> trace(msg: String, fn: () -> T): T {
        indent += 1
        log { msg }
        val retval = fn()
//        log { "$msg = $retval" }
        indent -= 1
        return retval
    }
}

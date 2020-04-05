package net.sarazan.bismarck

import co.touchlab.stately.concurrency.AtomicInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import net.sarazan.bismarck.Bismarck.State.Fresh
import net.sarazan.bismarck.Bismarck.State.Stale
import net.sarazan.bismarck.platform.currentTimeNano

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class DefaultBismarck<T : Any>(private val config: Bismarck.Config<T>) : Bismarck<T> {

    override var value: T?
        get() = storage.get()
        set(value) {
            storage.put(value)
            scope.launch {
                valueChannel.send(storage.get())
            }
        }

    override val state: Bismarck.State get() = when {
        _fetchCount.get() > 0 -> Bismarck.State.Fetching
        freshness?.isFresh() ?: false -> Bismarck.State.Fresh
        else -> Stale
    }

    override var error: Exception? = null
        private set(value) {
            field = value
            scope.launch {
                errorChannel.send(field)
            }
        }

    private val _fetchCount = AtomicInt(0)
    private var fetchJob: Job? = null

    private val valueChannel by lazy { ConflatedBroadcastChannel(value) }
    private val stateChannel by lazy { ConflatedBroadcastChannel(state) }
    private val errorChannel by lazy { ConflatedBroadcastChannel(error) }

    private val fetcher get() = config.fetcher
    private val freshness get() = config.freshness
    private val storage get() = config.storage
    private val scope get() = config.scope

    init {
        scope.launch {
            valueChannel.send(value)
            stateChannel.send(state)
            errorChannel.send(error)
            if (config.checkOnLaunch) {
                check()
            }
        }
    }

    override suspend fun eachValue(fn: (T?) -> Unit) {
        valueChannel.consumeEach(fn)
    }
    override suspend fun eachState(fn: (Bismarck.State?) -> Unit) {
        stateChannel.consumeEach(fn)
    }
    override suspend fun eachError(fn: (Exception?) -> Unit) {
        errorChannel.consumeEach(fn)
    }

    override fun check() {
        if (freshness?.isFresh() != true) {
            fetch()
        }
    }

    override fun insert(value: T?) {
        insert(value, currentTimeNano(), false)
    }

    override fun invalidate() {
        freshness?.reset()
        check()
    }

    override fun reset() {
        insert(null, currentTimeNano(), true)
    }

    override fun close() {
        valueChannel.close()
        stateChannel.close()
        errorChannel.close()
    }

    internal fun fetch() {
        val fetch = fetcher ?: return
        val time = currentTimeNano()
        _fetchCount.incrementAndGet()
        updateState()
        val job = fetchJob
        fetchJob = scope.launch {
            // join call acts as a single-pass gate for deduping requests
            job?.join()
            try {
                if (freshness?.isFresh() != true) {
                    insert(fetch.invoke(), time, false)
                    error = null
                }
            } catch (e: Exception) {
                error = e
            } finally {
                _fetchCount.decrementAndGet()
                updateState()
            }
        }
    }

    private fun insert(value: T?, timestamp: Long, reset: Boolean) {
        this.value = value
        if (reset) {
            freshness?.reset()
        } else {
            freshness?.update(timestamp)
        }
        updateState()
    }

    private fun updateState() {
        scope.launch {
            stateChannel.send(state)
        }
    }
}

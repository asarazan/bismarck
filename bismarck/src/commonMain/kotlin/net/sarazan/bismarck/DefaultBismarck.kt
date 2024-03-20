package net.sarazan.bismarck

import co.touchlab.stately.concurrency.AtomicInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.sarazan.bismarck.Bismarck.State.Stale
import net.sarazan.bismarck.platform.currentTimeNano

class DefaultBismarck<T : Any>(private val config: Bismarck.Config<T>) : Bismarck<T> {

    override var value: T?
        get() = storage.get()
        set(value) {
            storage.put(value)
            scope.launch {
                valueChannel.emit(storage.get())
            }
        }
    override val values: StateFlow<T?>
        get() = valueChannel

    override val state: Bismarck.State get() = when {
        _fetchCount.get() > 0 -> Bismarck.State.Fetching
        freshness?.isFresh() ?: false -> Bismarck.State.Fresh
        else -> Stale
    }
    override val states: StateFlow<Bismarck.State?>
        get() = stateChannel

    override var error: Exception? = null
        private set(value) {
            field = value
            scope.launch {
                errorChannel.emit(field)
            }
        }
    override val errors: StateFlow<Exception?>
        get() = errorChannel

    private val _fetchCount = AtomicInt(0)
    private var fetchJob: Job? = null

    private val valueChannel by lazy { MutableStateFlow(value) }
    private val stateChannel by lazy { MutableStateFlow(state) }
    private val errorChannel by lazy { MutableStateFlow(error) }

    private val fetcher get() = config.fetcher
    private val freshness get() = config.freshness
    private val storage get() = config.storage
    private val scope get() = config.scope

    init {
        scope.launch {
            valueChannel.emit(value)
            stateChannel.emit(state)
            errorChannel.emit(error)
            if (config.checkOnLaunch) {
                check()
            }
        }
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

    override fun clear() {
        insert(null, currentTimeNano(), true)
    }

    override fun close() {
        // nothing.
//        valueChannel.close()
//        stateChannel.close()
//        errorChannel.close()
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
            stateChannel.emit(state)
        }
    }
}

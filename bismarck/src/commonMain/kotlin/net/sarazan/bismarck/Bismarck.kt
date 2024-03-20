package net.sarazan.bismarck

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import net.sarazan.bismarck.platform.BismarckDispatchers
import net.sarazan.bismarck.platform.Closeable
import net.sarazan.bismarck.ratelimit.Freshness
import net.sarazan.bismarck.storage.MemoryStorage
import net.sarazan.bismarck.storage.Storage

@DslMarker
annotation class BismarckConfigMarker

typealias Fetcher<T> = suspend () -> T?

interface Bismarck<T : Any> : Closeable {

    @BismarckConfigMarker
    data class Config<T : Any>(
        var fetcher: Fetcher<T>? = null,
        var freshness: Freshness? = null,
        var storage: Storage<T> = MemoryStorage(),
        var scope: CoroutineScope = CoroutineScope(BismarckDispatchers.default),
        var checkOnLaunch: Boolean = false
    )

    enum class State {
        Fresh,
        Stale,
        Fetching
    }

    val value: T?
    val valueFlow: StateFlow<T?>

    val state: State
    val stateFlow: StateFlow<State?>

    val error: Exception?
    val errorFlow: StateFlow<Exception?>

    suspend fun eachValue(fn: (T?) -> Unit)
    suspend fun eachState(fn: (State?) -> Unit)
    suspend fun eachError(fn: (Exception?) -> Unit)

    fun check()
    fun insert(value: T?)
    fun invalidate()
    fun clear()

    companion object {
        fun <T : Any> create(config: Config<T>): Bismarck<T> {
            return DefaultBismarck(config)
        }
        fun <T : Any> create(config: Config<T>.() -> Unit = {}): Bismarck<T> {
            return create(Config<T>().apply(config))
        }
    }
}

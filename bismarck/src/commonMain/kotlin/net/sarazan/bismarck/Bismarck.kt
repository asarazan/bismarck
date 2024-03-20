package net.sarazan.bismarck

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import net.sarazan.bismarck.platform.BismarckDispatchers
import net.sarazan.bismarck.freshness.Freshness
import net.sarazan.bismarck.storage.MemoryStorage
import net.sarazan.bismarck.storage.Storage

@DslMarker
annotation class BismarckConfigMarker

typealias Fetcher<T> = suspend () -> T?

interface Bismarck<T : Any> {

    @BismarckConfigMarker
    data class Config<T : Any>(
        var fetcher: Fetcher<T>? = null,
        var freshness: Freshness? = null,
        var storage: Storage<T> = MemoryStorage(),
        var dispatcher: CoroutineDispatcher = BismarckDispatchers.default,
        var checkOnLaunch: Boolean = false
    )

    enum class State {
        Fresh,
        Stale,
        Fetching
    }

    val value: T?
    val values: StateFlow<T?>

    val states: StateFlow<State?>

    val error: Exception?
    val errors: StateFlow<Exception?>

    suspend fun insert(value: T?)
    suspend fun invalidate()
    suspend fun check()
    suspend fun clear()

    companion object {
        fun <T : Any> create(config: Config<T>): Bismarck<T> {
            return DefaultBismarck(config)
        }
        fun <T : Any> create(config: Config<T>.() -> Unit = {}): Bismarck<T> {
            return create(Config<T>().apply(config))
        }
    }
}

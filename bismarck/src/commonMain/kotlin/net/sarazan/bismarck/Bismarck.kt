package net.sarazan.bismarck

import kotlinx.coroutines.CoroutineScope
import net.sarazan.bismarck.platform.Closeable

interface Bismarck<T : Any> : Closeable {
    companion object {
        fun <T : Any> create(config: BismarckConfig<T>): Bismarck<T> {
            return DefaultBismarck(config)
        }
        fun <T : Any> create(config: BismarckConfig<T>.() -> Unit): Bismarck<T> {
            return create(BismarckConfig<T>().apply(config))
        }
    }

    val value: T?
    val state: BismarckState
    val error: Exception?

    suspend fun eachValue(fn: (T?) -> Unit)
    suspend fun eachState(fn: (BismarckState?) -> Unit)
    suspend fun eachError(fn: (Exception?) -> Unit)

    fun check()
    fun insert(value: T?)
    fun invalidate()
    fun reset()

    fun rescope(scope: CoroutineScope) = RescopedBismarck(this, scope)
}

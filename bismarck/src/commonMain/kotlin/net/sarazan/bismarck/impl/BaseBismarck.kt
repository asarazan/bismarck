/*
 * Copyright 2019 The Bismarck Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sarazan.bismarck.impl

import co.touchlab.stately.concurrency.AtomicInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.coroutineScope
import net.sarazan.bismarck.BismarckState
import net.sarazan.bismarck.Persister
import net.sarazan.bismarck.RateLimiter
import net.sarazan.bismarck.persisters.MemoryPersister
import net.sarazan.bismarck.platform.Closeable

typealias Fetcher<T> = suspend () -> T?

@ExperimentalCoroutinesApi
class BaseBismarck<T : Any> : Closeable {

    val dataChannel = ConflatedBroadcastChannel<T?>(null)
    val stateChannel = ConflatedBroadcastChannel(BismarckState.Stale)
    val errorChannel = ConflatedBroadcastChannel<Exception?>(null)

    var fetcher: Fetcher<T>? = null
    var persister: Persister<T>?  = MemoryPersister()
    var rateLimiter: RateLimiter? = null

    private var fetchCount = AtomicInt(0)

    suspend fun insert(value: T?) {
        persister?.put(value)
        rateLimiter?.let {
            if (value == null) it.reset()
            else it.update()
        }
        dataChannel.send(value)
        state()
    }

    suspend fun invalidate(andFetch: Boolean = true) {
        rateLimiter?.reset()
        if (andFetch) {
            fetchCount.incrementAndGet()
            state()
            try {
                coroutineScope {
                    fetcher?.invoke()?.apply {
                        errorChannel.send(null)
                        insert(this)
                    }
                }
            } catch (e: Exception) {
                errorChannel.send(e)
            }
            fetchCount.decrementAndGet()
            state()
        }
    }

    suspend fun state(): BismarckState {
        val state = when {
            fetchCount.get() > 0            -> BismarckState.Fetching
            rateLimiter?.isFresh() ?: false -> BismarckState.Fresh
            else                            -> BismarckState.Stale
        }
        if (state != stateChannel.value) {
            stateChannel.send(state)
        }
        return state
    }

    override fun close() {
        dataChannel.close()
        stateChannel.close()
        errorChannel.close()
    }
}
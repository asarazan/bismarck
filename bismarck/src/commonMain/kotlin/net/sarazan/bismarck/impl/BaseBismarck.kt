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

import co.touchlab.stately.collections.IsoMutableList
import co.touchlab.stately.concurrency.AtomicInt
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import net.sarazan.bismarck.*
import net.sarazan.bismarck.persisters.MemoryPersister
import net.sarazan.bismarck.platform.currentTimeMillis
import net.sarazan.bismarck.ratelimit.SimpleRateLimiter

open class BaseBismarck<T : Any> : Bismarck<T> {

    private val listeners                   = IsoMutableList<Listener<T>>()
    private val transforms                  = IsoMutableList<Transform<T>>()
    private val dependents                  = IsoMutableList<Bismarck<*>>()

    private var fetchCount                  = AtomicInt(0)
    private var lastState: BismarckState?   = null
    private var lastError: Throwable?       = null

    internal var fetcher: Fetcher<T>?      = null
    internal var persister: Persister<T>?  = MemoryPersister()
    internal var rateLimiter: RateLimiter? = null
    protected var coroutineScope: CoroutineScope = GlobalScope
        private set

    @ExperimentalCoroutinesApi
    private val dataChannel = ConflatedBroadcastChannel<T?>()

    @ExperimentalCoroutinesApi
    private val stateChannel = ConflatedBroadcastChannel<BismarckState>()

    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    override fun consumeEachData(action: (T?) -> Unit) {
        coroutineScope.launch {
            dataChannel.consumeEach(action)
        }
    }

    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    override fun consumeEachState(action: (BismarckState?) -> Unit) {
        coroutineScope.launch {
            stateChannel.consumeEach(action)
        }
    }

    /**
     * Synchronous fetch logic that will be called to fetch/populate our data.
     * Will be scheduled according to the logic in [executor].
     *
     * May be null if you will only be inserting data manually.
     */
    fun fetcher(fetcher: Fetcher<T>?) = apply { this.fetcher = fetcher }

    /**
     * If you want data to actually, you know, persist; then this is your guy.
     *
     * @default: [MemoryPersister] a simple in-memory persister
     */
    fun persister(persister: Persister<T>?) = apply { this.persister = persister }

    /**
     * Strategy for determining if data is "fresh".
     * Unfresh data will still be presented, but will trigger a fetch.
     *
     * @default: [SimpleRateLimiter] data is fresh for 15 minutes or until manually invalidated.
     */
    fun rateLimiter(rateLimiter: RateLimiter?) = apply { this.rateLimiter = rateLimiter }

    /**
     * TODO docs
     */
    fun coroutineScope(coroutineScope: CoroutineScope) = apply { this.coroutineScope = coroutineScope }

    @ExperimentalCoroutinesApi
    protected fun requestFetch() {
        fetchCount.incrementAndGet()
        updateState()
    }

    @ExperimentalCoroutinesApi
    protected fun releaseFetch() {
        fetchCount.decrementAndGet()
        updateState()
    }

    /**
     * Called just before the synchronous fetch operation begins
     */
    protected open fun onFetchBegin(fetch: Fetch<T>) {}

    /**
     * Called just before insertion time.
     * @return whether data is still valid. False will cancel insertion.
     */
    protected open fun onFetchEnd(fetch: Fetch<T>): Boolean = true

    /**
     * Something messed up.
     */
    protected open fun onFetchError(fetch: Fetch<T>) {}

    @ExperimentalCoroutinesApi
    protected suspend fun performFetch() {
        val fetch = Fetch<T>()
        onFetchBegin(fetch)
        try {
            fetcher?.fetch()?.apply {
                lastError = null
                if (onFetchEnd(fetch.copy(finished = currentTimeMillis(), data = this))) {
                    insert(this)
                }
            }
        } catch (e: Fetcher.BismarckFetchError) {
            onFetchError(fetch.copy(finished = currentTimeMillis(), error = e))
            lastError = e
        }
    }

    @ExperimentalCoroutinesApi
    protected open suspend fun blockingFetch() {
        requestFetch()
        performFetch()
        releaseFetch()
    }

    @ExperimentalCoroutinesApi
    fun asyncFetch() {
        coroutineScope.launch {
            blockingFetch()
        }
    }

    @ExperimentalCoroutinesApi
    override fun insert(data: T?) {
        val old = peek()
        val transformedData = transforms.fold(data) {
            it, transformer ->
            transformer.transform(it)
        }
        persister?.put(transformedData)
        rateLimiter?.let { if (transformedData == null) it.reset() else it.update() }
        listeners.forEach {
            it.onUpdate(transformedData)
        }
        notifyChanged()
        if (old != transformedData) {
            dependents.forEach { it.invalidate() }
        }
    }

    override fun isFresh(): Boolean {
        return rateLimiter?.isFresh() ?: false
    }

    override fun invalidate() {
        rateLimiter?.reset()
        dependents.forEach { it.invalidate() }
    }

    @ExperimentalCoroutinesApi
    override fun refresh() {
        if (!isFresh()) { asyncFetch() }
        dependents.forEach { it.refresh() }
    }

    override fun addListener(listener: Listener<T>) = apply {
        listeners.add(listener)
    }

    override fun removeListener(listener: Listener<T>) = apply {
        listeners.remove(listener)
    }

    override fun addTransform(transform: Transform<T>) = apply {
        transforms.add(transform)
    }

    override fun removeTransform(transform: Transform<T>) = apply {
        transforms.remove(transform)
    }

    override fun addDependent(other: Bismarck<*>) = apply {
        dependents.add(other)
    }

    override fun removeDependent(other: Bismarck<*>) = apply {
        dependents.remove(other)
    }

    @ExperimentalCoroutinesApi
    private fun updateState() {
        val state = peekState()
        if (state == lastState) return
        lastState = state
        coroutineScope.launch {
            stateChannel.send(state)
        }
    }

    override fun peekState(): BismarckState {
        return when {
            fetchCount.get() > 0    -> BismarckState.Fetching
            lastError != null       -> BismarckState.Error
            isFresh()               -> BismarckState.Fresh
            else                    -> BismarckState.Stale
        }
    }

    override fun peek(): T? {
        return persister?.get()
    }

    @ExperimentalCoroutinesApi
    override fun notifyChanged() {
        val data = peek()
        coroutineScope.launch {
            dataChannel.send(data)
        }
    }

    @ExperimentalCoroutinesApi
    override fun close() {
        dataChannel.close()
        stateChannel.close()
    }
}
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

import net.sarazan.bismarck.Listener
import net.sarazan.bismarck.Persister
import net.sarazan.bismarck.*
import net.sarazan.bismarck.persisters.CachingPersister
import net.sarazan.bismarck.ratelimit.SimpleRateLimiter
import rx.Observable
import rx.Subscriber
import rx.subscriptions.Subscriptions
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger


open class BaseBismarck<T : Any>() : Bismarck<T> {

    // Because the [synchronized] calls were breaking and I'm lazy
    private val listeners                   = CopyOnWriteArrayList<Listener<T>>()
    private val transforms = CopyOnWriteArrayList<Transform<T>>()
    private val subscribers                 = CopyOnWriteArrayList<Subscriber<in T>>()
    private val stateSubscribers            = CopyOnWriteArrayList<Subscriber<in BismarckState>>()
    private val dependents                  = CopyOnWriteArrayList<Bismarck<*>>()

    private var fetchCount                  = AtomicInteger(0)
    private var lastState: BismarckState?   = null
    private var lastError: Throwable?       = null

    protected var fetcher: Fetcher<T>?      = null
        private set
    protected var persister: Persister<T>?  =
        CachingPersister()
        private set
    protected var rateLimiter: RateLimiter? =
        SimpleRateLimiter(15 * 60 * 1000L)
        private set
    protected var executor: Executor        = Bismarck.DEFAULT_EXECUTOR
        private set

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
     * @default: [CachingPersister] a simple in-memory persister
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
     * You should probably leave this alone-- but if you want to hand-tune thread allocations, knock yourself out.
     *
     * @default: shared instance of [Executors.newCachedThreadPool]
     */
    fun executor(executor: Executor) = apply { this.executor = executor }

    protected final fun requestFetch() {
        fetchCount.incrementAndGet()
        updateState()
    }

    protected final fun releaseFetch() {
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

    protected final fun performFetch() {
        val fetch = Fetch<T>()
        onFetchBegin(fetch)
        try {
            fetcher?.onFetch()?.apply {
                lastError = null
                if (onFetchEnd(fetch.copy(finished = System.currentTimeMillis(), data = this))) {
                    insert(this)
                }
            }
        } catch (e: Fetcher.BismarckFetchError) {
            onFetchError(fetch.copy(finished = System.currentTimeMillis(), error = e))
            lastError = e
        }
    }

    override fun blockingFetch() {
        requestFetch()
        performFetch()
        releaseFetch()
    }

    protected final fun asyncFetch() {
        executor.execute {
            blockingFetch()
        }
    }

    override fun observe(): Observable<T?> {
        return Observable.create(BismarckOnSubscribe())
    }

    override fun observeState(): Observable<BismarckState> {
        return Observable.create(StateOnSubscribe())
    }

    override fun insert(data: T?) {
        val old = peek()
        val transformedData = transforms.fold(data) {
            data, transformer ->
            transformer.transform(data)
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

    override fun refresh() {
        if (!isFresh()) { asyncFetch() }
        dependents.forEach { it.refresh() }
    }

    override fun listen(listener: Listener<T>) = apply {
        listeners.add(listener)
    }

    override fun unlisten(listener: Listener<T>) = apply {
        listeners.remove(listener)
    }

    override fun transform(transform: Transform<T>) = apply {
        transforms.add(transform)
    }

    override fun removeTransform(transform: Transform<T>) = apply {
        transforms.remove(transform)
    }

    override fun addDependent(other: Bismarck<*>) = apply {
        dependents.add(other)
    }

    protected fun updateState() {
        val state = peekState()
        if (state == lastState) return
        lastState = state
        stateSubscribers.forEach {
            it.onNext(state)
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

    override fun notifyChanged() {
        val data = peek()
        subscribers.forEach {
            it.onNext(data)
        }
    }

    private inner class StateOnSubscribe : Observable.OnSubscribe<BismarckState> {
        override fun call(sub: Subscriber<in BismarckState>) {
            if (sub.isUnsubscribed) return
            stateSubscribers.add(sub)
            sub.add(Subscriptions.create { stateSubscribers.remove(sub) })
            sub.onStart()
            sub.onNext(peekState())
        }
    }

    private inner class BismarckOnSubscribe : Observable.OnSubscribe<T?> {
        override fun call(sub: Subscriber<in T?>) {
            if (sub.isUnsubscribed) return
            subscribers.add(sub)
            sub.add(Subscriptions.create { subscribers.remove(sub) })
            sub.onStart()
            peek().let { sub.onNext(it) }
        }
    }
}
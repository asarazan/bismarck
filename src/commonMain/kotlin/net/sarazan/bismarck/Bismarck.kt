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

package net.sarazan.bismarck

import net.sarazan.bismarck.platform.Closeable

interface Bismarck<T : Any> : Closeable {

    suspend fun consumeEachData(action: (T?) -> Unit)

    suspend fun consumeEachState(action: (BismarckState?) -> Unit)

    /**
     * Manually set the data of the bismarck.
     */
    fun insert(data: T?)

    /**
     * Synchronously grab the latest cached version of the data.
     */
    fun peek(): T?

    /**
     * Synchronously grab the latest version of the bismarck state.
     */
    fun peekState(): BismarckState

    /**
     * The bismarck will usually employ some sort of timer or hash comparison to determine this.
     * Can also call [invalidate] to force this to false.
     */
    fun isFresh(): Boolean

    /**
     * Should cause [isFresh] to return false.
     */
    fun invalidate()

    /**
     * Trigger asyncFetch of this and all dependencies where [isFresh] is false.
     */
    fun refresh()

    /**
     * FIFO executed just after data insertion and before dependent invalidation
     */
    fun addListener(listener: Listener<T>): Bismarck<T>

    /**
     * Remove a previously added listener
     */
    fun removeListener(listener: Listener<T>): Bismarck<T>

    /**
     * FIFO executed just after data insertion and before dependent invalidation
     */
    fun addTransform(transform: Transform<T>): Bismarck<T>

    /**
     * Remove a previously added listener
     */
    fun removeTransform(transform: Transform<T>): Bismarck<T>

    /**
     * Dependency chaining. Does not detect circular references, so be careful.
     */
    fun addDependent(other: Bismarck<*>): Bismarck<T>

    /**
     * Dependency chaining. Does not detect circular references, so be careful.
     */
    fun removeDependent(other: Bismarck<*>): Bismarck<T>

    /**
     * Type-agnostic method for clearing data,
     * since logouts will often cause this to happen in a foreach loop.
     */
    fun clear() = insert(null)

    /**
     * Sometimes you do bad things and the data gets changed without an [insert] call. Shame on you.
     */
    fun notifyChanged()
}

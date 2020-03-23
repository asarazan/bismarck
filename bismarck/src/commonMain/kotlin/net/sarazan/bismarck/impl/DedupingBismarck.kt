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

import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.withLock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.sarazan.bismarck.platform.currentTimeNano


/**
 * This dedupes fetches by forcing sequential execution and checking timestamps.
 *
 * A fetch will be discarded iff [isFresh] returns true
 * and no [invalidate] occurred since the fetch was requested.
 */
open class DedupingBismarck<T : Any> : BaseBismarck<T>() {

    private val lock = Lock()

    @ExperimentalCoroutinesApi
    override suspend fun blockingFetch() {
        val ts = currentTimeNano()
        requestFetch()
        lock.withLock {
            val lastInvalidate = rateLimiter?.lastReset ?: 0L
            if (lastInvalidate > ts || !isFresh()) {
                performFetch()
            }
        }
        releaseFetch()
    }
}
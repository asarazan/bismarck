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

package net.sarazan.bismarck.ratelimit

import net.sarazan.bismarck.RateLimiter
import net.sarazan.bismarck.platform.currentTimeNano

class SimpleRateLimiter(val ms: Long) : RateLimiter {

    /**
     * Uses [System.nanoTime]
     */
    var lastRunNanos: Long = 0
        private set

    /**
     * Uses [System.nanoTime]
     */
    override var resetNanos: Long = 0
        private set

    /**
     *
     */
    override fun update(requestNanos: Long) {
        if (requestNanos >= resetNanos) {
            lastRunNanos = getCurrent()
        }
    }

    override fun reset() {
        lastRunNanos = 0
        resetNanos = getCurrent()
    }

    override fun isFresh(): Boolean {
        return !pass(getCurrent())
    }

    private fun getCurrent(): Long {
        return currentTimeNano()
    }

    private fun pass(current: Long): Boolean {
        val last = lastRunNanos
        val ns = ms * 1_000_000
        return last == 0L || current - last >= ns
    }
}

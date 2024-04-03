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

package net.sarazan.bismarck.freshness

import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.TimeSource

internal open class SimpleFreshness(
    private val duration: Duration,
    private val timeSource: TimeSource.WithComparableMarks
) : Freshness {

    protected var lastRunMark: ComparableTimeMark? = null
    protected var resetMark = timeSource.markNow()

    override fun update(requestTimeMark: ComparableTimeMark) {
        if (requestTimeMark >= resetMark) {
            lastRunMark = timeSource.markNow()
            save()
        }
    }

    override fun reset() {
        lastRunMark = null
        resetMark = timeSource.markNow()
        save()
    }

    override fun isFresh() = lastRunMark.let { lastRunMark ->
        lastRunMark != null && timeSource.markNow() - lastRunMark < duration
    }

    override fun remainingTime(): Duration? {
        return lastRunMark?.let {
            val expires = it + duration
            expires - timeSource.markNow()
        }
    }

    protected open fun save() {}
}

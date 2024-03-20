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

package net.sarazan.bismarck.android.rateLimiters

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.preference.PreferenceManager

import net.sarazan.bismarck.RateLimiter
import java.util.concurrent.TimeUnit

public class AndroidRateLimiter(val c: Context, val ms: Long, val key: String) : RateLimiter {

    /**
     * Uses [System.nanoTime]
     */
    override var lastReset: Long = 0
        private set

    override fun update() {
        val e = editor()
        e.putLong(key, System.nanoTime()).commit()
    }

    override fun reset() {
        editor().remove(key).commit()
        lastReset = System.nanoTime()
    }

    override fun isFresh(): Boolean {
        return !pass(System.nanoTime())
    }

    private fun prefs(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(c)
    }

    private fun editor(): Editor {
        return prefs().edit()
    }

    private fun pass(current: Long = System.nanoTime()): Boolean {
        val last = getLastRun()
        val ns = TimeUnit.MILLISECONDS.toNanos(ms)
        return last == 0L || current - last >= ns
    }

    public fun getLastRun(): Long {
        return prefs().getLong(key, 0L)
    }

}

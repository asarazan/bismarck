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

import net.sarazan.bismarck.impl.BaseBismarck


@Suppress("UNCHECKED_CAST")
fun <T : Any, B : BaseBismarck<T>> B.fetcher(fn: suspend () -> T?): B {
    return fetcher(object: Fetcher<T> {
        override suspend fun fetch(): T? {
            return fn()
        }
    }) as B
}

@Suppress("UNCHECKED_CAST")
fun <T : Any, B : Bismarck<T>> B.addListener(fn: (T?) -> Unit): B {
    return addListener(object : Listener<T> {
        override fun onUpdate(data: T?) {
            fn(data)
        }
    }) as B
}

@Suppress("UNCHECKED_CAST")
fun <T : Any, B : Bismarck<T>> B.addTransform(fn: (T?) -> T?): B {
    return addTransform(object : Transform<T> {
        override fun transform(input: T?): T? {
            return fn(input)
        }
    }) as B
}
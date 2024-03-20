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

package net.sarazan.bismarck.android.persisters

import android.content.Context
import net.sarazan.bismarck.Serializer
import net.sarazan.bismarck.persisters.CachingPersister
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

public abstract class AndroidFilePersister<T: Any>(val context: Context, val serializer: Serializer<T>, val useCacheDir: Boolean = false) : CachingPersister<T>() {

    protected abstract fun path(): String?

    override fun get(): T? {
        val dir = if (useCacheDir) context.cacheDir else context.filesDir
        val path = path() ?: return null
        val cached = super.get()
        if (cached != null) return cached
        val file = File(dir, "/$path")
        if (!file.exists()) return null
        val loaded = FileInputStream(file).use { serializer.readObject(it) }
        super.put(loaded)
        return loaded
    }

    override fun put(data: T?) {
        val path = path() ?: return
        super.put(data)
        val file = File(context.filesDir, "/$path")
        if (data == null) {
            file.delete()
        } else {
            val parent = file.parentFile
            if (!parent.exists()) {
                parent.mkdirs()
            }
            if (!file.exists()) {
                file.createNewFile()
            }
            FileOutputStream(file).use { serializer.writeObject(it, data) }
        }
    }
}

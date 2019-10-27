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

package net.sarazan.bismarck.serializers

import net.sarazan.bismarck.Serializer
import java.io.*


class SerializableSerializer<T : Serializable> : Serializer<T> {

    override fun writeObject(stream: OutputStream, data: T): Boolean {
        val oos = ObjectOutputStream(stream)
        oos.writeObject(data)
        oos.close()
        return true
    }

    @Suppress("UNCHECKED_CAST")
    override fun readObject(stream: InputStream): T? {
        val ois = ObjectInputStream(stream)
        try {
            return ois.readObject() as T
        } catch (e: ClassNotFoundException) {
            throw IOException(e)
        } finally {
            ois.close()
        }
    }
}

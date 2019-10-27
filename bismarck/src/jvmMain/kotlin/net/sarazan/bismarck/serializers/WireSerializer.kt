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
import com.squareup.wire.Message
import com.squareup.wire.ProtoAdapter
import java.io.*

class WireSerializer<T : Message<T, *>>(val cls: Class<T>) : Serializer<T> {

    override fun writeObject(stream: OutputStream, data: T): Boolean {
        stream.write(data.encode())
        return true
    }

    override fun readObject(stream: InputStream): T? {
        return ProtoAdapter.get(cls).decode(stream)
    }

}

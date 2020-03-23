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

package net.sarazan.bismarck.persisters

import net.sarazan.bismarck.Serializer
import net.sarazan.bismarck.platform.files.FileLike
import net.sarazan.bismarck.platform.JavaFile

abstract class JavaFilePersister<T: Any>(
    root: String,
    serializer: Serializer<T>
) : FilePersister<T>(root, serializer) {

    override fun getFile(root: String, path: String): FileLike {
        return JavaFile(root, path)
    }
}
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

import android.accounts.AccountManager
import android.content.Context
import net.sarazan.bismarck.Persister
import net.sarazan.bismarck.Serializer
import java.net.URLEncoder

object AndroidPersisters {

    public fun <T: Any> account(c: Context, key: String, accountType: String, serializer: Serializer<T>, useCacheDir: Boolean = false): Persister<T> {
        return object: AndroidFilePersister<T>(c, serializer, useCacheDir) {
            override fun path(): String? {
                val account = AccountManager.get(c).getAccountsByType(accountType).firstOrNull() ?: return null
                return URLEncoder.encode(account.name, Charsets.UTF_8.name()) + "/$key"
            }
        }
    }

}

package net.sarazan.bismarck.serializers.kotlinx

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.serializer
import net.sarazan.bismarck.serialization.Serializer

class JsonSerializer<T : Any>(
    val serializer: KSerializer<T>,
    val json: Json = Json(JsonConfiguration.Stable.copy(isLenient = true))
) : Serializer<T> {

    @OptIn(ExperimentalStdlibApi::class)
    override fun serialize(data: T): ByteArray {
        return json.stringify(serializer, data).encodeToByteArray()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun deserialize(bytes: ByteArray): T? {
        if (bytes.isEmpty()) return null
        return json.parse(serializer, bytes.decodeToString())
    }
}

package net.sarazan.bismarck.serializers

import net.sarazan.bismarck.Serializer

@ExperimentalStdlibApi
@JsName("JsonSerializer")
class JsonSerializer : Serializer<dynamic> {

    override fun serialize(data: dynamic): ByteArray {
        return JSON.stringify(data).encodeToByteArray()
    }

    override fun deserialize(bytes: ByteArray): dynamic {
        return JSON.parse(bytes.decodeToString())
    }
}

package net.sarazan.bismarck.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import net.sarazan.bismarck.serializers.kotlinx.JsonSerializer

@Serializable
data class Payload(
    val foo: String = "",
    var bar: Int? = null
)

val serializer = JsonSerializer(Payload.serializer())

class JsonTests {

    @Test
    fun testSerializer() {
        val payload = Payload("hi", 2)
        val bytes = serializer.serialize(payload)
        assertEquals(serializer.deserialize(bytes), payload)
    }
}

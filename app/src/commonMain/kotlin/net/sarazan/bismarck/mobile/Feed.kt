package net.sarazan.bismarck.mobile

// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json = Json(JsonConfiguration.Stable)
// val feed = json.parse(Feed.serializer(), jsonString)

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class Feed(
    val version: String,
    val title: String,

    @SerialName("home_page_url")
    val homePageURL: String,

    @SerialName("feed_url")
    val feedURL: String,

    val description: String,

    @SerialName("next_url")
    val nextURL: String,

    val icon: String,
    val favicon: String,
    val author: Author,
    val items: List<Item>
)

@Serializable
data class Author(
    val name: String,
    val url: String,
    val avatar: String
)

@Serializable
data class Item(
    val id: String,
    val title: String,

    @SerialName("content_text")
    val contentText: String,

    @SerialName("content_html")
    val contentHTML: String,

    @SerialName("date_published")
    val datePublished: String,

    val attachments: List<Attachment>,

    @SerialName("date_modified")
    val dateModified: String? = null
)

@Serializable
data class Attachment(
    val url: String,

    @SerialName("mime_type")
    val mimeType: MIMEType,

    @SerialName("size_in_bytes")
    val sizeInBytes: SizeInBytes,

    @SerialName("duration_in_seconds")
    val durationInSeconds: Long
)

@Serializable(with = MIMEType.Companion::class)
enum class MIMEType(val value: String) {
    AudioMPEG("audio/mpeg");

    companion object : KSerializer<MIMEType> {
        override val descriptor: SerialDescriptor
            get() {
            return PrimitiveSerialDescriptor("quicktype.MIMEType", PrimitiveKind.STRING)
        }
        override fun deserialize(decoder: Decoder): MIMEType = when (val value = decoder.decodeString()) {
            "audio/mpeg" -> AudioMPEG
            else -> throw IllegalArgumentException("MIMEType could not parse: $value")
        }
        override fun serialize(encoder: Encoder, value: MIMEType) {
            return encoder.encodeString(value.value)
        }
    }
}

@Serializable
data class SizeInBytes(
    @SerialName("0")
    val the0: String
)

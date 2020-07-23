package net.sarazan.bismarck.mobile

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.logging.SIMPLE
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import kotlinx.serialization.json.Json

object Api {
    private val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json.nonstrict)
        }
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.ALL
        }
    }

    suspend fun getFeed(): Feed
            = get("381444908/feed.json")

    private suspend inline fun <reified Response : Any> get(path: String): Response {
        return network {
            client.get<Response> {
                buildHttp(path)
            }
        }
    }

    private suspend inline fun <Request : Any, reified Response : Any> post(path: String, body: Request): Response {
        return network {
            client.post<Response> {
                buildHttp(path)
                contentType(ContentType.Application.Json)
                this.body = body
            }
        }
    }

    private fun HttpRequestBuilder.buildHttp(path: String) {
        url {
            takeFrom("https://feeds.npr.org/")
            path(path)
        }
    }
}
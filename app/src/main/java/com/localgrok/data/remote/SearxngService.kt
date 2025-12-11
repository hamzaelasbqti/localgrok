package com.localgrok.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Retrofit service interface for SearXNG API
 */
interface SearxngApiService {

    /**
     * Perform a web search
     * @param query The search query
     * @param format Response format (json)
     */
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json"
    ): Response<SearxngSearchResponse>
}

/**
 * Response model for SearXNG search results
 */
@Serializable
data class SearxngSearchResponse(
    val query: String? = null,
    @SerialName("number_of_results")
    val numberOfResults: Long? = null,
    val results: List<SearxngResult> = emptyList(),
    val answers: List<String> = emptyList(),
    val infoboxes: List<SearxngInfobox> = emptyList()
)

@Serializable
data class SearxngResult(
    val title: String? = null,
    val url: String? = null,
    val content: String? = null,
    val engine: String? = null,
    @SerialName("parsed_url")
    val parsedUrl: List<String>? = null,
    val engines: List<String>? = null,
    val positions: List<Int>? = null,
    val score: Double? = null,
    val category: String? = null
)

@Serializable
data class SearxngInfobox(
    val infobox: String? = null,
    val id: String? = null,
    val content: String? = null,
    val urls: List<SearxngInfoboxUrl>? = null
)

@Serializable
data class SearxngInfoboxUrl(
    val title: String? = null,
    val url: String? = null
)

/**
 * Provides configured Retrofit instance for SearXNG API
 */
object SearxngClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.HEADERS
    }

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * Create a new SearxngApiService with the specified base URL
     * @param serverIp The IP address of the SearXNG server
     * @param port The port number (default: 8888)
     */
    fun createService(serverIp: String, port: Int = 8888): SearxngApiService {
        val baseUrl = "http://$serverIp:$port/"

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(createOkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        return retrofit.create(SearxngApiService::class.java)
    }

    /**
     * Format search results into a concise string for the AI
     */
    fun formatSearchResults(response: SearxngSearchResponse, maxResults: Int = 5): String {
        val sb = StringBuilder()

        // Include any direct answers first
        if (response.answers.isNotEmpty()) {
            sb.appendLine("Direct Answer: ${response.answers.first()}")
            sb.appendLine()
        }

        // Include infobox content if available
        response.infoboxes.firstOrNull()?.let { infobox ->
            infobox.content?.let { content ->
                sb.appendLine("Summary: $content")
                sb.appendLine()
            }
        }

        // Include top search results
        val topResults = response.results.take(maxResults)
        if (topResults.isNotEmpty()) {
            sb.appendLine("Search Results:")
            topResults.forEachIndexed { index, result ->
                sb.appendLine("${index + 1}. ${result.title ?: "No title"}")
                result.content?.let { content ->
                    sb.appendLine("   $content")
                }
                sb.appendLine()
            }
        }

        return if (sb.isEmpty()) {
            "No results found."
        } else {
            sb.toString().trim()
        }
    }
}


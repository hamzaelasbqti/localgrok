package com.localgrok.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Provides configured Retrofit instance for Ollama API
 */
object OllamaClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Use HEADERS instead of BODY to avoid buffering streaming responses
        level = HttpLoggingInterceptor.Level.HEADERS
    }

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS) // Long timeout for streaming
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * Create a new OllamaApiService with the specified base URL
     * @param serverIp The IP address of the Ollama server (e.g., "192.168.1.50")
     * @param port The port number (default: 11434)
     */
    fun createService(serverIp: String, port: Int = 11434): OllamaApiService {
        val baseUrl = "http://$serverIp:$port/"

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(createOkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        return retrofit.create(OllamaApiService::class.java)
    }

    /**
     * Get the JSON parser for manual parsing of streaming responses
     */
    fun getJson(): Json = json
}


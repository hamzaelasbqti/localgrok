package com.localgrok.data.remote

import com.localgrok.data.remote.model.OllamaChatRequest
import com.localgrok.data.remote.model.OllamaModelsResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming

/**
 * Retrofit service interface for Ollama API
 * Default endpoint: http://<USER_IP>:11434
 */
interface OllamaApiService {

    /**
     * Send a chat message and receive streaming response
     * Returns ResponseBody for manual streaming handling
     */
    @Streaming
    @POST("api/chat")
    suspend fun chatStream(
        @Body request: OllamaChatRequest
    ): Response<ResponseBody>

    /**
     * List available models on the server
     */
    @GET("api/tags")
    suspend fun listModels(): Response<OllamaModelsResponse>

    /**
     * Check if server is reachable
     * Returns ResponseBody to avoid JSON conversion issues (Ollama returns plain text)
     */
    @GET("/")
    suspend fun healthCheck(): Response<ResponseBody>
}


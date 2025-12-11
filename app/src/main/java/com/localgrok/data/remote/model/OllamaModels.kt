package com.localgrok.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for Ollama /api/chat endpoint
 */
@Serializable
data class OllamaChatRequest(
    val model: String,
    val messages: List<OllamaMessage>,
    val stream: Boolean = true,
    val options: OllamaOptions? = null,
    val think: Boolean = false
)

/**
 * Message format for Ollama API
 */
@Serializable
data class OllamaMessage(
    val role: String, // "user", "assistant", "system"
    val content: String,
    val images: List<String>? = null, // Base64 encoded images for multimodal
    val thinking: String? = null // Thinking content when think: true is enabled
)

/**
 * Optional parameters for generation
 */
@Serializable
data class OllamaOptions(
    val temperature: Float? = null,
    @SerialName("num_predict")
    val numPredict: Int? = null,
    @SerialName("top_p")
    val topP: Float? = null,
    @SerialName("top_k")
    val topK: Int? = null
)

/**
 * Streaming response from Ollama /api/chat endpoint
 * Each line is a JSON object with partial response
 */
@Serializable
data class OllamaChatResponse(
    val model: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    val message: OllamaMessage? = null,
    val done: Boolean = false,
    @SerialName("done_reason")
    val doneReason: String? = null,
    @SerialName("total_duration")
    val totalDuration: Long? = null,
    @SerialName("load_duration")
    val loadDuration: Long? = null,
    @SerialName("prompt_eval_count")
    val promptEvalCount: Int? = null,
    @SerialName("prompt_eval_duration")
    val promptEvalDuration: Long? = null,
    @SerialName("eval_count")
    val evalCount: Int? = null,
    @SerialName("eval_duration")
    val evalDuration: Long? = null
)

/**
 * Response from Ollama /api/tags endpoint (list models)
 */
@Serializable
data class OllamaModelsResponse(
    val models: List<OllamaModelInfo>
)

@Serializable
data class OllamaModelInfo(
    val name: String,
    val model: String? = null,
    @SerialName("modified_at")
    val modifiedAt: String? = null,
    val size: Long? = null,
    val digest: String? = null,
    val details: OllamaModelDetails? = null
)

@Serializable
data class OllamaModelDetails(
    @SerialName("parent_model")
    val parentModel: String? = null,
    val format: String? = null,
    val family: String? = null,
    val families: List<String>? = null,
    @SerialName("parameter_size")
    val parameterSize: String? = null,
    @SerialName("quantization_level")
    val quantizationLevel: String? = null
)


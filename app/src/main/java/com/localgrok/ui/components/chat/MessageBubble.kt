package com.localgrok.ui.components.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localgrok.data.local.entity.MessageEntity
import com.localgrok.ui.theme.InterFont
import com.localgrok.ui.theme.LocalAppColors
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography

/**
 * Response status for AI message bubbles
 */
enum class ResponseStatus {
    THINKING,   // Model is in reasoning/thinking phase
    STREAMING,  // Model is streaming content tokens
    COMPLETE    // Response is complete
}

@Composable
fun UserMessageBubble(
    message: MessageEntity,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 4.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
                .background(colors.userBubble)
                .padding(14.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = InterFont,
                    fontSize = 16.sp
                ),
                color = colors.textPrimary
            )
        }
    }
}

@Composable
fun AssistantMessageBubble(
    message: MessageEntity,
    reasoningContent: String = "",
    isReasoningEnabled: Boolean = true,
    isExecutingTool: Boolean = false,
    toolDisplayName: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current

    // Derive the response status from message state
    val responseStatus by remember(message.isStreaming, message.isThinking, message.content) {
        derivedStateOf {
            when {
                message.isThinking -> ResponseStatus.THINKING
                message.isStreaming -> ResponseStatus.STREAMING
                else -> ResponseStatus.COMPLETE
            }
        }
    }

    // Track previous status to detect transitions for animation
    var previousStatus by remember { mutableStateOf(responseStatus) }
    var showThinkingIndicator by remember { mutableStateOf(responseStatus == ResponseStatus.THINKING) }

    // State for expanding/collapsing reasoning content
    var isReasoningExpanded by remember { mutableStateOf(false) }

    // Handle status transitions
    LaunchedEffect(responseStatus) {
        when {
            // Entering thinking state
            responseStatus == ResponseStatus.THINKING -> {
                showThinkingIndicator = true
            }
            // Transitioning from thinking to streaming/complete
            previousStatus == ResponseStatus.THINKING && responseStatus != ResponseStatus.THINKING -> {
                showThinkingIndicator = false
            }
        }
        previousStatus = responseStatus
    }

    // Close reasoning dropdown when tool execution starts
    // This ensures it stays closed after search completes
    LaunchedEffect(isExecutingTool) {
        if (isExecutingTool) {
            isReasoningExpanded = false
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        // Clean text display - no container, no border, no background
        Column(
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            // Helper function to detect tool artifacts in content
            fun containsToolArtifacts(content: String): Boolean {
                return content.contains("<tool_result>", ignoreCase = true) ||
                        content.contains("</tool_result>", ignoreCase = true) ||
                        content.contains("<tool_call>", ignoreCase = true) ||
                        content.contains("</tool_call>", ignoreCase = true) ||
                        content.contains("Search results:", ignoreCase = true) ||
                        content.contains("Current date and time:", ignoreCase = true)
            }

            val persistedToolUsed = message.toolUsed
            val persistedToolDisplayName = message.toolDisplayName.takeIf { it.isNotBlank() }

            // Detect if a tool was used based on explicit execution flags or message content.
            // We intentionally ignore reasoning content here because models may "think aloud"
            // about possible tool calls without actually executing them, which would otherwise
            // create false positives for the tool status indicator.
            val toolWasUsed = remember(
                message.content,
                toolDisplayName,
                persistedToolUsed,
                persistedToolDisplayName
            ) {
                toolDisplayName != null ||
                        persistedToolUsed ||
                        persistedToolDisplayName != null ||
                        containsToolArtifacts(message.content)
            }

            // Determine which tool was used from content or displayName
            val detectedToolName =
                remember(message.content, toolDisplayName, persistedToolDisplayName) {
                    val displayName = toolDisplayName ?: persistedToolDisplayName
                    when {
                        displayName != null -> displayName
                        toolWasUsed -> "Completed"
                        message.content.contains("Search results:", ignoreCase = true) ||
                                message.content.contains("<tool_result>", ignoreCase = true) &&
                                (message.content.contains("Search", ignoreCase = true) ||
                                        message.content.contains(
                                            "web",
                                            ignoreCase = true
                                        )) -> "Searching..."

                        message.content.contains("Current date and time:", ignoreCase = true) ||
                                (message.content.contains("<tool_result>", ignoreCase = true) &&
                                        (message.content.contains("time", ignoreCase = true) ||
                                                message.content.contains(
                                                    "date",
                                                    ignoreCase = true
                                                ) ||
                                                message.content.contains(
                                                    "calendar",
                                                    ignoreCase = true
                                                ))) -> "Checking time..."

                        else -> null
                    }
                }

            // Tool execution indicator - shown when executing or when completed
            if (isExecutingTool) {
                // Active tool execution
                ToolExecutionIndicator(
                    displayText = toolDisplayName ?: "Working...",
                    isCompleted = false,
                    colors = colors
                )
                // Don't show anything else while tool is executing
                return@Row
            } else if (toolWasUsed && responseStatus == ResponseStatus.COMPLETE && detectedToolName != null) {
                // Completed tool execution - show past tense
                ToolExecutionIndicator(
                    displayText = detectedToolName,
                    isCompleted = true,
                    colors = colors
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Thinking indicator - clickable to expand reasoning
            // Logic flow:
            // 1. If actively thinking, always show "Thinking..." (regardless of tool usage)
            // 2. If complete and tool was used, don't show "Thought" (tool status takes priority)
            // 3. If complete and no tool was used but has reasoning, show "Thought"
            val hasReasoning = reasoningContent.isNotBlank()
            val showThinkingSection = when {
                // Always show when actively thinking
                showThinkingIndicator -> true
                // When complete: only show if no tool was used and there's reasoning content
                responseStatus == ResponseStatus.COMPLETE -> hasReasoning && !toolWasUsed
                // Otherwise don't show
                else -> false
            }

            if (showThinkingSection) {
                CollapsibleThinkingSection(
                    isThinking = showThinkingIndicator,
                    reasoningContent = reasoningContent,
                    messageContent = message.content,
                    isExpanded = isReasoningExpanded,
                    onToggleExpanded = { isReasoningExpanded = !isReasoningExpanded },
                    colors = colors
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Show spinner when waiting for first token (non-thinking mode)
            val showLoadingSpinner = !isReasoningEnabled &&
                    message.content.isBlank() &&
                    responseStatus == ResponseStatus.STREAMING &&
                    !showThinkingIndicator

            if (showLoadingSpinner) {
                CircularProgressIndicator(
                    color = colors.textPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Actual content with streaming text (no cursor)
            AnimatedVisibility(
                visible = !showThinkingIndicator && message.content.isNotBlank(),
                enter = fadeIn(animationSpec = tween(300, delayMillis = 100)) + scaleIn(
                    initialScale = 0.98f,
                    animationSpec = tween(300, delayMillis = 100)
                ),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                Markdown(
                    content = message.content,
                    colors = markdownColor(
                        text = colors.textPrimary,
                        codeText = colors.textPrimary,
                        codeBackground = colors.darkGrey,
                        linkText = colors.accent,
                        inlineCodeText = colors.textPrimary,
                        inlineCodeBackground = colors.darkGrey,
                        dividerColor = colors.borderGrey
                    ),
                    typography = markdownTypography(
                        h1 = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = InterFont,
                            color = colors.textPrimary
                        ),
                        h2 = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = InterFont,
                            color = colors.textPrimary
                        ),
                        h3 = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = InterFont,
                            color = colors.textPrimary
                        ),
                        h4 = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = InterFont,
                            color = colors.textPrimary
                        ),
                        h5 = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = InterFont,
                            color = colors.textPrimary
                        ),
                        h6 = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = InterFont,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        ),
                        text = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = InterFont,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        ),
                        code = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = colors.textPrimary
                        ),
                        quote = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = InterFont,
                            fontStyle = FontStyle.Italic,
                            color = colors.textSecondary
                        ),
                        paragraph = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = InterFont,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        ),
                        ordered = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = InterFont,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        ),
                        bullet = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = InterFont,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        ),
                        list = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = InterFont,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        )
                    )
                )
            }

            // Action toolbar - only show when complete
            if (responseStatus == ResponseStatus.COMPLETE && message.content.isNotBlank()) {
                MessageActionToolbar(
                    messageContent = message.content,
                    context = context,
                    colors = colors,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun MessageActionToolbar(
    messageContent: String,
    context: Context,
    colors: com.localgrok.ui.theme.LocalGrokColors,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Copy button
        IconButton(
            onClick = {
                val clipboardManager =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("AI Response", messageContent)
                clipboardManager.setPrimaryClip(clip)
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = "Copy message",
                tint = colors.textDim,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Share button
        IconButton(
            onClick = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, messageContent)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share response"))
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Share message",
                tint = colors.textDim,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

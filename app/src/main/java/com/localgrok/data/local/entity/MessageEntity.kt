package com.localgrok.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a message within a chat conversation
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["chatId"])]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chatId: Long,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val isThinking: Boolean = false,  // True when model is in thinking/reasoning phase
    @ColumnInfo(defaultValue = "")
    val reasoningContent: String = "",  // Stores the thinking/reasoning text for persistence
    @ColumnInfo(defaultValue = "0")
    val toolUsed: Boolean = false,      // True when the assistant used a tool for this message
    @ColumnInfo(defaultValue = "")
    val toolDisplayName: String = ""    // Human readable tool label, e.g., "Searching..."
)


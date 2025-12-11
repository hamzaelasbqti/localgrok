package com.localgrok.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a chat conversation in the local database
 */
@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val model: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)


package com.kreeda.ankana.data.model

/**
 * A single message in the AI assistant conversation.
 */
data class ChatMessage(
    val id: String = "",
    val role: String = "",  // "user" or "assistant"
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

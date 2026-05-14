package com.kreeda.ankana.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreeda.ankana.BuildConfig
import com.kreeda.ankana.data.model.ChatMessage
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel for the AI Chat Assistant screen.
 * Integrates Google Gemini API for smart sports scheduling assistance.
 * Falls back to preset responses when the API key is not configured.
 */
class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow(
        mutableListOf(
            ChatMessage(
                id = "welcome",
                role = "assistant",
                content = "🏟️ Namaskara! I'm your Kreeda-Ankana AI Assistant.\n\nI can help you with:\n• 📅 Suggesting best match timings\n• 📝 Generating challenge messages\n• 🏏 Sports scheduling queries\n• 💡 Tips for organizing matches\n\nHow can I help you today?"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val isApiConfigured = apiKey != "YOUR_GEMINI_API_KEY_HERE" && apiKey.isNotBlank()

    private val generativeModel: GenerativeModel? = if (isApiConfigured) {
        GenerativeModel(
            modelName = "gemini-1.5-pro",
            apiKey = apiKey
        )
    } else null

    private val systemPrompt = """
        You are Kreeda-Ankana Assistant, a helpful AI for village sports teams in India.
        You specialize in:
        - Suggesting optimal match timings based on weather and player availability
        - Generating exciting challenge messages for cricket, volleyball, kabaddi matches
        - Answering queries about sports scheduling and ground management
        - Providing tips for organizing village-level sports events
        Keep responses concise, friendly, and use relevant sports emojis.
        Support both English and Kannada if the user writes in Kannada.
    """.trimIndent()

    // Preset responses for when Gemini API is not configured
    private val presetResponses = mapOf(
        "timing" to "⏰ Best match timings for village grounds:\n\n🌅 Morning: 6:00 AM - 9:00 AM (ideal for summer)\n🌤️ Evening: 4:00 PM - 7:00 PM (most popular)\n\nAvoid 12 PM - 3 PM due to heat. Weekend mornings see highest turnout!",
        "challenge" to "📝 Here's a challenge message for you:\n\n🏏 \"Attention all village teams! We challenge you to a thrilling cricket match this weekend. 10 overs, unlimited excitement! Are you brave enough to face our bowling attack? Drop your reply below! 💪🔥\"\n\nFeel free to modify the sport and details!",
        "schedule" to "📅 Smart Scheduling Tips:\n\n1. Book grounds 2-3 days in advance\n2. Keep buffer time between matches (30 min)\n3. Morning slots fill fast on weekends\n4. Rainy season? Have a backup date ready\n5. Coordinate with other teams via Challenge Board",
        "kabaddi" to "🤼 Kabaddi Match Tips:\n\n• Best time: Early morning or late evening\n• Ground size: 13m × 10m for men\n• Match duration: 2 halves of 20 min each\n• Minimum 7 players per team\n• Use our booking system to reserve the ground!",
        "help" to "🏟️ Here's what I can help with:\n\n1. 📅 **Match Timings** - Ask me for best timings\n2. 📝 **Challenge Messages** - I'll write exciting challenges\n3. 🗓️ **Scheduling** - Tips for organizing matches\n4. 🏏 **Cricket/Volleyball/Kabaddi** - Sport-specific advice\n\nJust ask me anything about sports management!"
    )

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = "user",
            content = userText
        )

        val currentList = _messages.value.toMutableList()
        currentList.add(userMessage)
        _messages.value = currentList

        viewModelScope.launch {
            _isLoading.value = true

            val responseText = if (isApiConfigured && generativeModel != null) {
                try {
                    val response = generativeModel.generateContent(
                        content {
                            text("$systemPrompt\n\nUser says: $userText")
                        }
                    )
                    response.text ?: "I couldn't process that. Please try again."
                } catch (e: Exception) {
                    "⚠️ Connection error: ${e.message}\n\nTrying offline mode..."
                        .also { getPresetResponse(userText) }
                }
            } else {
                getPresetResponse(userText)
            }

            val assistantMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                role = "assistant",
                content = responseText
            )

            val updatedList = _messages.value.toMutableList()
            updatedList.add(assistantMessage)
            _messages.value = updatedList

            _isLoading.value = false
        }
    }

    private fun getPresetResponse(input: String): String {
        val lower = input.lowercase()
        return when {
            lower.contains("time") || lower.contains("timing") || lower.contains("when") ->
                presetResponses["timing"]!!
            lower.contains("challenge") || lower.contains("message") || lower.contains("write") ->
                presetResponses["challenge"]!!
            lower.contains("schedule") || lower.contains("plan") || lower.contains("organize") ->
                presetResponses["schedule"]!!
            lower.contains("kabaddi") ->
                presetResponses["kabaddi"]!!
            lower.contains("help") || lower.contains("what") || lower.contains("can you") ->
                presetResponses["help"]!!
            lower.contains("cricket") ->
                "🏏 Cricket Match Setup:\n\n• Book a 2-hour slot for a 10-over match\n• Morning (6-8 AM) or Evening (4-6 PM) recommended\n• Need minimum 11 players per team\n• Post a challenge on the Challenge Board to find opponents!\n\nWant me to suggest a challenge message?"
            lower.contains("volleyball") ->
                "🏐 Volleyball Match Setup:\n\n• Book a 1.5-hour slot for best-of-3 sets\n• Indoor or outdoor — evening slots are perfect\n• 6 players per team minimum\n• Use Challenge Board to find opponents!\n\nNeed help with anything else?"
            lower.contains("namaskara") || lower.contains("hello") || lower.contains("hi") ->
                "🙏 Namaskara! Welcome to Kreeda-Ankana!\n\nHow can I help you today? I can suggest match timings, generate challenge messages, or help with scheduling."
            else ->
                "🏟️ Great question! Here are some things I can help with:\n\n• Type **\"timing\"** for best match timings\n• Type **\"challenge\"** for a challenge message template\n• Type **\"schedule\"** for scheduling tips\n• Type **\"help\"** for all available options\n\n(Tip: Configure your Gemini API key in build.gradle for full AI-powered responses!)"
        }
    }
}

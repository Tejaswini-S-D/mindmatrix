package com.kreeda.ankana.data.model

/**
 * Represents a match challenge posted by a team.
 * Other teams can reply/accept challenges.
 */
data class Challenge(
    val challengeId: String = "",
    val challengerTeamName: String = "",
    val sport: String = "",
    val date: String = "",           // YYYY-MM-DD
    val timePreference: String = "", // e.g., "Evening", "Morning"
    val status: String = "OPEN",     // OPEN, ACCEPTED, COMPLETED
    val message: String = "",
    val acceptedByTeamName: String? = null,
    val replies: List<ChallengeReply> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * A reply to a challenge on the Challenge Board.
 */
data class ChallengeReply(
    val replyId: String = "",
    val teamName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

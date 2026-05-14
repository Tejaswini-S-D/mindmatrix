package com.kreeda.ankana.data.model

/**
 * Represents a completed or scheduled match with scores.
 */
data class MatchResult(
    val matchId: String = "",
    val team1Name: String = "",
    val team2Name: String = "",
    val sport: String = "",
    val team1Score: String = "",
    val team2Score: String = "",
    val winnerName: String = "",
    val date: String = "",           // YYYY-MM-DD
    val status: String = "COMPLETED", // SCHEDULED, LIVE, COMPLETED
    val timestamp: Long = System.currentTimeMillis()
)

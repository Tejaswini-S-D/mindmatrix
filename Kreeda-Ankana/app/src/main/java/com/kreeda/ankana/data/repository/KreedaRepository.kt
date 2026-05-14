package com.kreeda.ankana.data.repository

import com.kreeda.ankana.data.model.Booking
import com.kreeda.ankana.data.model.Challenge
import com.kreeda.ankana.data.model.ChallengeReply
import com.kreeda.ankana.data.model.MatchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Central repository managing all data operations.
 * Uses in-memory dummy data as fallback when Firebase is not configured.
 * In production, swap the backing stores with Firestore calls.
 */
class KreedaRepository {

    // ── Time Slots ──────────────────────────────────────────────────────────
    val availableTimeSlots = listOf(
        "06:00 AM - 07:00 AM",
        "07:00 AM - 08:00 AM",
        "08:00 AM - 09:00 AM",
        "09:00 AM - 10:00 AM",
        "10:00 AM - 11:00 AM",
        "11:00 AM - 12:00 PM",
        "12:00 PM - 01:00 PM",
        "01:00 PM - 02:00 PM",
        "02:00 PM - 03:00 PM",
        "03:00 PM - 04:00 PM",
        "04:00 PM - 05:00 PM",
        "05:00 PM - 06:00 PM",
        "06:00 PM - 07:00 PM",
        "07:00 PM - 08:00 PM",
        "08:00 PM - 09:00 PM"
    )

    // ── Bookings ────────────────────────────────────────────────────────────
    private val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    private val _bookings = MutableStateFlow(
        mutableListOf(
            Booking("b1", "Village Main Ground", "Ravi Kumar", "Royal Challengers", "Cricket", today, "08:00 AM - 09:00 AM"),
            Booking("b2", "Village Main Ground", "Suresh", "Tornadoes", "Cricket", today, "04:00 PM - 05:00 PM"),
            Booking("b3", "Village Main Ground", "Anand", "Super Spikers", "Volleyball", today, "06:00 PM - 07:00 PM")
        )
    )
    val bookings: Flow<List<Booking>> = _bookings.asStateFlow()

    fun getBookingsForDate(date: String): Flow<List<Booking>> {
        return _bookings.map { list -> list.filter { it.date == date && it.status == "CONFIRMED" } }
    }

    fun getBookedSlotsForDate(date: String): Flow<Set<String>> {
        return _bookings.map { list ->
            list.filter { it.date == date && it.status == "CONFIRMED" }
                .map { it.timeSlot }
                .toSet()
        }
    }

    fun getTodayBookingCount(): Flow<Int> {
        return _bookings.map { list ->
            list.count { it.date == today && it.status == "CONFIRMED" }
        }
    }

    /**
     * Attempts to book a slot. Returns Result.success if booked,
     * Result.failure if the slot is already taken (double-booking prevention).
     */
    suspend fun bookSlot(
        date: String,
        timeSlot: String,
        bookedBy: String,
        teamName: String,
        sport: String
    ): Result<Booking> {
        val current = _bookings.value
        val alreadyBooked = current.any {
            it.date == date && it.timeSlot == timeSlot && it.status == "CONFIRMED"
        }
        if (alreadyBooked) {
            return Result.failure(Exception("This slot is already booked! Please choose another time."))
        }
        val booking = Booking(
            bookingId = UUID.randomUUID().toString().take(8),
            date = date,
            timeSlot = timeSlot,
            bookedBy = bookedBy,
            teamName = teamName,
            sport = sport
        )
        val updated = current.toMutableList().apply { add(booking) }
        _bookings.value = updated
        return Result.success(booking)
    }

    // ── Challenges ──────────────────────────────────────────────────────────
    private val _challenges = MutableStateFlow(
        mutableListOf(
            Challenge(
                challengeId = "c1",
                challengerTeamName = "Royal Challengers",
                sport = "Cricket",
                date = "2026-05-10",
                timePreference = "Evening",
                message = "🏏 Ready for a thrilling 10-over match this Sunday! Any team brave enough?",
                replies = listOf(
                    ChallengeReply("r1", "Tornadoes", "We're in! See you on the field 💪", System.currentTimeMillis() - 3600000)
                )
            ),
            Challenge(
                challengeId = "c2",
                challengerTeamName = "Super Spikers",
                sport = "Volleyball",
                date = "2026-05-12",
                timePreference = "Morning",
                message = "🏐 Looking for a best-of-3 sets volleyball match. Who's ready?"
            ),
            Challenge(
                challengeId = "c3",
                challengerTeamName = "Tornadoes",
                sport = "Kabaddi",
                date = "2026-05-15",
                timePreference = "Afternoon",
                message = "🤼 Our kabaddi team is ready for any challenge! Bring it on!"
            )
        )
    )
    val challenges: Flow<List<Challenge>> = _challenges.asStateFlow()

    fun getOpenChallenges(): Flow<List<Challenge>> {
        return _challenges.map { list ->
            list.filter { it.status == "OPEN" }.sortedByDescending { it.createdAt }
        }
    }

    fun getActiveChallengeCount(): Flow<Int> {
        return _challenges.map { list -> list.count { it.status == "OPEN" } }
    }

    suspend fun postChallenge(challenge: Challenge): Result<Challenge> {
        val newChallenge = challenge.copy(
            challengeId = UUID.randomUUID().toString().take(8),
            createdAt = System.currentTimeMillis()
        )
        val updated = _challenges.value.toMutableList().apply { add(0, newChallenge) }
        _challenges.value = updated
        return Result.success(newChallenge)
    }

    suspend fun addReplyToChallenge(challengeId: String, reply: ChallengeReply): Result<Unit> {
        val current = _challenges.value.toMutableList()
        val index = current.indexOfFirst { it.challengeId == challengeId }
        if (index == -1) return Result.failure(Exception("Challenge not found"))

        val challenge = current[index]
        val newReply = reply.copy(
            replyId = UUID.randomUUID().toString().take(8),
            timestamp = System.currentTimeMillis()
        )
        current[index] = challenge.copy(replies = challenge.replies + newReply)
        _challenges.value = current
        return Result.success(Unit)
    }

    suspend fun acceptChallenge(challengeId: String, teamName: String): Result<Unit> {
        val current = _challenges.value.toMutableList()
        val index = current.indexOfFirst { it.challengeId == challengeId }
        if (index == -1) return Result.failure(Exception("Challenge not found"))

        current[index] = current[index].copy(status = "ACCEPTED", acceptedByTeamName = teamName)
        _challenges.value = current
        return Result.success(Unit)
    }

    // ── Matches / Score Wall ────────────────────────────────────────────────
    private val _matches = MutableStateFlow(
        mutableListOf(
            MatchResult("m1", "Royal Challengers", "Tornadoes", "Cricket", "120/4", "115/8", "Royal Challengers", "2026-05-01"),
            MatchResult("m2", "Super Spikers", "Royal Challengers", "Volleyball", "25-21, 18-25, 25-20", "21-25, 25-18, 20-25", "Super Spikers", "2026-05-02"),
            MatchResult("m3", "Tornadoes", "Super Spikers", "Kabaddi", "42", "38", "Tornadoes", "2026-05-03"),
            MatchResult("m4", "Royal Challengers", "Super Spikers", "Cricket", "145/6", "140/9", "Royal Challengers", "2026-04-28")
        )
    )
    val matches: Flow<List<MatchResult>> = _matches.asStateFlow()

    fun getRecentMatches(): Flow<List<MatchResult>> {
        return _matches.map { list -> list.sortedByDescending { it.timestamp } }
    }

    fun getRecentMatchCount(): Flow<Int> {
        return _matches.map { it.size }
    }

    suspend fun addMatchResult(match: MatchResult): Result<MatchResult> {
        val newMatch = match.copy(
            matchId = UUID.randomUUID().toString().take(8),
            timestamp = System.currentTimeMillis()
        )
        val updated = _matches.value.toMutableList().apply { add(0, newMatch) }
        _matches.value = updated
        return Result.success(newMatch)
    }
}

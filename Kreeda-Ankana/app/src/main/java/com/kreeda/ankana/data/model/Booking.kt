package com.kreeda.ankana.data.model

/**
 * Represents a ground time-slot booking.
 * Bookings are keyed by date+slot to prevent double-booking.
 */
data class Booking(
    val bookingId: String = "",
    val groundName: String = "Village Main Ground",
    val bookedBy: String = "",       // user or team name
    val teamName: String = "",
    val sport: String = "",
    val date: String = "",           // YYYY-MM-DD
    val timeSlot: String = "",       // e.g. "06:00 AM - 08:00 AM"
    val status: String = "CONFIRMED", // CONFIRMED, CANCELLED
    val createdAt: Long = System.currentTimeMillis()
)

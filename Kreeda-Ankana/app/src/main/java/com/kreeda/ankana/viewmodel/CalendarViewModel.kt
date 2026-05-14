package com.kreeda.ankana.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreeda.ankana.data.model.Booking
import com.kreeda.ankana.data.repository.KreedaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * ViewModel for the Ground Calendar booking screen.
 * Manages date selection, slot availability, and booking logic
 * with double-booking prevention.
 */
class CalendarViewModel(
    private val repository: KreedaRepository = KreedaRepository()
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val timeSlots: List<String> = repository.availableTimeSlots

    val bookedSlots: StateFlow<Set<String>> = _selectedDate
        .flatMapLatest { date ->
            repository.getBookedSlotsForDate(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _selectedSlot = MutableStateFlow<String?>(null)
    val selectedSlot: StateFlow<String?> = _selectedSlot.asStateFlow()

    private val _bookingResult = MutableSharedFlow<Result<Booking>>()
    val bookingResult: SharedFlow<Result<Booking>> = _bookingResult.asSharedFlow()

    private val _isBooking = MutableStateFlow(false)
    val isBooking: StateFlow<Boolean> = _isBooking.asStateFlow()

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _selectedSlot.value = null
    }

    fun selectSlot(slot: String) {
        _selectedSlot.value = slot
    }

    fun bookSelectedSlot(bookedBy: String, teamName: String, sport: String) {
        val slot = _selectedSlot.value ?: return
        val date = _selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)

        viewModelScope.launch {
            _isBooking.value = true
            val result = repository.bookSlot(date, slot, bookedBy, teamName, sport)
            _bookingResult.emit(result)
            if (result.isSuccess) {
                _selectedSlot.value = null
            }
            _isBooking.value = false
        }
    }

    fun getNextSevenDays(): List<LocalDate> {
        val today = LocalDate.now()
        return (0L..6L).map { today.plusDays(it) }
    }
}

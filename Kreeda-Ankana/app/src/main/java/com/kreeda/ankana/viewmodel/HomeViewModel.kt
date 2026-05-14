package com.kreeda.ankana.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreeda.ankana.data.repository.KreedaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the Home screen dashboard stats.
 */
class HomeViewModel(
    private val repository: KreedaRepository = KreedaRepository()
) : ViewModel() {

    val todayBookingCount: StateFlow<Int> = repository.getTodayBookingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeChallengeCount: StateFlow<Int> = repository.getActiveChallengeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentMatchCount: StateFlow<Int> = repository.getRecentMatchCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}

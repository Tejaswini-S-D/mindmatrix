package com.kreeda.ankana.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreeda.ankana.data.model.MatchResult
import com.kreeda.ankana.data.repository.KreedaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the Score Wall screen.
 * Manages match results display and adding new scores.
 */
class ScoreWallViewModel(
    private val repository: KreedaRepository = KreedaRepository()
) : ViewModel() {

    val recentMatches: StateFlow<List<MatchResult>> = repository.getRecentMatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _addResult = MutableSharedFlow<Result<MatchResult>>()
    val addResult: SharedFlow<Result<MatchResult>> = _addResult.asSharedFlow()

    fun toggleAddDialog() {
        _showAddDialog.value = !_showAddDialog.value
    }

    fun addScore(
        team1Name: String,
        team2Name: String,
        sport: String,
        team1Score: String,
        team2Score: String,
        winnerName: String,
        date: String
    ) {
        viewModelScope.launch {
            val match = MatchResult(
                team1Name = team1Name,
                team2Name = team2Name,
                sport = sport,
                team1Score = team1Score,
                team2Score = team2Score,
                winnerName = winnerName,
                date = date
            )
            val result = repository.addMatchResult(match)
            _addResult.emit(result)
            if (result.isSuccess) {
                _showAddDialog.value = false
            }
        }
    }
}

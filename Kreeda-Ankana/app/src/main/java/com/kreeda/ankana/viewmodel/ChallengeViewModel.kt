package com.kreeda.ankana.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreeda.ankana.data.model.Challenge
import com.kreeda.ankana.data.model.ChallengeReply
import com.kreeda.ankana.data.repository.KreedaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the Challenge Board screen.
 * Handles challenge posting, accepting, and replying.
 */
class ChallengeViewModel(
    private val repository: KreedaRepository = KreedaRepository()
) : ViewModel() {

    val openChallenges: StateFlow<List<Challenge>> = repository.getOpenChallenges()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _postResult = MutableSharedFlow<Result<Challenge>>()
    val postResult: SharedFlow<Result<Challenge>> = _postResult.asSharedFlow()

    private val _showPostDialog = MutableStateFlow(false)
    val showPostDialog: StateFlow<Boolean> = _showPostDialog.asStateFlow()

    fun togglePostDialog() {
        _showPostDialog.value = !_showPostDialog.value
    }

    fun postChallenge(
        teamName: String,
        sport: String,
        date: String,
        timePreference: String,
        message: String
    ) {
        viewModelScope.launch {
            val challenge = Challenge(
                challengerTeamName = teamName,
                sport = sport,
                date = date,
                timePreference = timePreference,
                message = message
            )
            val result = repository.postChallenge(challenge)
            _postResult.emit(result)
            if (result.isSuccess) {
                _showPostDialog.value = false
            }
        }
    }

    fun replyToChallenge(challengeId: String, teamName: String, message: String) {
        viewModelScope.launch {
            val reply = ChallengeReply(teamName = teamName, message = message)
            repository.addReplyToChallenge(challengeId, reply)
        }
    }

    fun acceptChallenge(challengeId: String, teamName: String) {
        viewModelScope.launch {
            repository.acceptChallenge(challengeId, teamName)
        }
    }
}

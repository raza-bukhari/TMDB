package com.example.tmdb.feature.person

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.tmdb.core.navigation.PersonRoute
import com.example.tmdb.domain.usecase.GetPersonCreditsUseCase
import com.example.tmdb.domain.usecase.GetPersonDetailUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class PersonViewModel(
    savedStateHandle: SavedStateHandle,
    private val getPersonDetail: GetPersonDetailUseCase,
    private val getPersonCredits: GetPersonCreditsUseCase,
) : ViewModel() {

    private val personId = savedStateHandle.toRoute<PersonRoute>().personId
    private val _uiState = MutableStateFlow(PersonUiState())
    val uiState: StateFlow<PersonUiState> = _uiState

    init {
        load()
    }

    fun onRetryClicked() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val detailRequest = async { getPersonDetail(personId) }
            val creditsRequest = async { getPersonCredits(personId) }
            val person = detailRequest.await().getOrElse {
                _uiState.update { state -> state.copy(isLoading = false, errorMessage = "Unable to load this person.") }
                return@launch
            }
            val credits = creditsRequest.await().getOrNull()
            _uiState.value = PersonUiState(
                isLoading = false,
                person = person.toUi(),
                credits = credits?.toUi() ?: kotlinx.collections.immutable.persistentListOf(),
            )
        }
    }
}

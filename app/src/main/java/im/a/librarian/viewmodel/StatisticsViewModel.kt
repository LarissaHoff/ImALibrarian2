package im.a.librarian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import im.a.librarian.domain.model.Statistics
import im.a.librarian.domain.usecase.GetStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatisticsUiState(
    val statistics: Statistics = Statistics(),
    val isLoading: Boolean = true
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getStatisticsUseCase: GetStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = StatisticsUiState(isLoading = true)
            try {
                val stats = getStatisticsUseCase.getStatistics()
                _uiState.value = StatisticsUiState(statistics = stats, isLoading = false)
            } catch (_: Exception) {
                _uiState.value = StatisticsUiState(isLoading = false)
            }
        }
    }
}
package app.imalibrarian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.imalibrarian.domain.model.Book
import app.imalibrarian.domain.model.ReadStatus
import app.imalibrarian.domain.model.ScanResult
import app.imalibrarian.domain.repository.BookRepository
import app.imalibrarian.domain.repository.WishlistRepository
import app.imalibrarian.domain.usecase.AddBookUseCase
import app.imalibrarian.domain.usecase.SearchBooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val localResults: List<Book> = emptyList(),
    val onlineResults: List<ScanResult.Found> = emptyList(),
    val wishlistResults: List<app.imalibrarian.domain.model.WishlistItem> = emptyList(),
    val isSearching: Boolean = false,
    val searchOnline: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchBooksUseCase: SearchBooksUseCase,
    private val bookRepository: BookRepository,
    private val wishlistRepository: WishlistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun search() {
        val query = _uiState.value.query
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)

            searchBooksUseCase.searchLocalBooks(query).collect { books ->
                _uiState.value = _uiState.value.copy(localResults = books)
            }
        }

        viewModelScope.launch {
            wishlistRepository.searchWishlistItems(_uiState.value.query).collect { items ->
                _uiState.value = _uiState.value.copy(wishlistResults = items)
            }
        }
    }

    fun searchOnline() {
        val query = _uiState.value.query
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, searchOnline = true)
            try {
                val results = searchBooksUseCase.searchOnline(query)
                _uiState.value = _uiState.value.copy(
                    onlineResults = results.filterIsInstance<ScanResult.Found>(),
                    isSearching = false
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isSearching = false)
            }
        }
    }

    fun clearSearch() {
        _uiState.value = SearchUiState()
    }
}
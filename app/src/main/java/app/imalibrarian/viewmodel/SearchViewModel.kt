package app.imalibrarian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.imalibrarian.data.local.db.dao.BookDao
import app.imalibrarian.domain.model.Book
import app.imalibrarian.domain.model.ScanResult
import app.imalibrarian.domain.repository.BookRepository
import app.imalibrarian.domain.repository.WishlistRepository
import app.imalibrarian.domain.usecase.SearchBooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

sealed interface SearchSuggestion {
    data class Title(val id: Long, val title: String, val authors: String) : SearchSuggestion
    data class Author(val name: String) : SearchSuggestion
}

data class SearchUiState(
    val query: String = "",
    val localResults: List<Book> = emptyList(),
    val onlineResults: List<ScanResult.Found> = emptyList(),
    val wishlistResults: List<app.imalibrarian.domain.model.WishlistItem> = emptyList(),
    val isSearching: Boolean = false,
    val isSearchingOnline: Boolean = false,
    val searchOnline: Boolean = false,
    val onlineError: String? = null,
    val suggestions: List<SearchSuggestion> = emptyList()
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchBooksUseCase: SearchBooksUseCase,
    private val bookRepository: BookRepository,
    private val wishlistRepository: WishlistRepository,
    private val bookDao: BookDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")
    private var localSearchJob: Job? = null
    private var suggestionsJob: Job? = null
    private var onlineSearchJob: Job? = null

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(DEBOUNCE_MILLIS)
                .distinctUntilChanged()
                .collect { q ->
                    loadSuggestions(q)
                }
        }
    }

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        queryFlow.value = query
    }

    fun submitSearch() {
        val query = _uiState.value.query
        if (query.isBlank()) return

        localSearchJob?.cancel()
        onlineSearchJob?.cancel()

        _uiState.value = _uiState.value.copy(
            searchOnline = false,
            isSearchingOnline = true,
            onlineResults = emptyList(),
            onlineError = null
        )

        runLocalSearch(query)
        runOnlineSearch(query)
    }

    fun showLocal() {
        _uiState.value = _uiState.value.copy(searchOnline = false)
    }

    fun showOnline() {
        _uiState.value = _uiState.value.copy(searchOnline = true)
    }

    private fun runLocalSearch(query: String) {
        localSearchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                localResults = emptyList(),
                wishlistResults = emptyList(),
                isSearching = false
            )
            return
        }
        localSearchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            try {
                searchBooksUseCase.searchLocalBooksByTitleOrAuthor(query)
                    .catch {
                        _uiState.value = _uiState.value.copy(isSearching = false)
                    }
                    .collect { books ->
                        _uiState.value = _uiState.value.copy(localResults = books)
                    }
            } catch (_: CancellationException) {
            } finally {
                _uiState.value = _uiState.value.copy(isSearching = false)
            }
        }
        viewModelScope.launch {
            wishlistRepository.searchWishlistItems(query)
                .catch { }
                .collect { items ->
                    _uiState.value = _uiState.value.copy(wishlistResults = items)
                }
        }
    }

    private fun runOnlineSearch(query: String) {
        onlineSearchJob = viewModelScope.launch {
            try {
                val results = withTimeout(ONLINE_TIMEOUT_MILLIS) {
                    searchBooksUseCase.searchOnline(query)
                }
                _uiState.value = _uiState.value.copy(
                    onlineResults = results.filterIsInstance<ScanResult.Found>(),
                    isSearchingOnline = false
                )
            } catch (e: TimeoutCancellationException) {
                _uiState.value = _uiState.value.copy(
                    isSearchingOnline = false,
                    onlineError = "Search timed out — check your network"
                )
            } catch (e: CancellationException) {
                _uiState.value = _uiState.value.copy(isSearchingOnline = false)
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearchingOnline = false,
                    onlineError = e.message ?: "Online search failed"
                )
            }
        }
    }

    fun retryOnline() {
        runOnlineSearch(_uiState.value.query)
    }

    private fun loadSuggestions(query: String) {
        suggestionsJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(suggestions = emptyList())
            return
        }
        suggestionsJob = viewModelScope.launch {
            try {
                bookDao.suggestTitlesAndAuthors(query, SUGGESTION_LIMIT)
                    .catch { }
                    .collect { rows ->
                        val titles = rows.map { SearchSuggestion.Title(it.id, it.title, it.authorNames) }
                        val authors = rows.asSequence()
                            .flatMap { it.authorNames.split(',') }
                            .map { it.trim() }
                            .filter { it.isNotEmpty() && it.contains(query, ignoreCase = true) }
                            .distinct()
                            .map { SearchSuggestion.Author(it) }
                            .toList()
                        _uiState.value = _uiState.value.copy(
                            suggestions = (titles + authors).take(SUGGESTION_LIMIT)
                        )
                    }
            } catch (_: CancellationException) {
            }
        }
    }

    fun clearSearch() {
        localSearchJob?.cancel()
        suggestionsJob?.cancel()
        onlineSearchJob?.cancel()
        _uiState.value = SearchUiState()
        queryFlow.value = ""
    }

    companion object {
        private const val DEBOUNCE_MILLIS = 80L
        private const val SUGGESTION_LIMIT = 8
        private const val ONLINE_TIMEOUT_MILLIS = 5_000L
    }
}

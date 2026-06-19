package app.imalibrarian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.imalibrarian.domain.model.Book
import app.imalibrarian.domain.model.ReadStatus
import app.imalibrarian.domain.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val books: List<Book> = emptyList(),
    val searchQuery: String = "",
    val selectedGenre: String? = null,
    val selectedReadStatus: ReadStatus? = null,
    val sortOrder: SortOrder = SortOrder.DATE_ADDED,
    val isLoading: Boolean = false
)

enum class SortOrder {
    TITLE, AUTHOR, DATE_ADDED, PUBLICATION_YEAR, RATING
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedGenre = MutableStateFlow<String?>(null)
    private val _selectedReadStatus = MutableStateFlow<ReadStatus?>(null)
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_ADDED)

    init {
        viewModelScope.launch {
            combine(
                bookRepository.getAllBooks(),
                _searchQuery,
                _selectedGenre,
                _selectedReadStatus,
                _sortOrder
            ) { books, query, genre, status, sort ->
                var filtered = books
                if (query.isNotBlank()) {
                    filtered = filtered.filter {
                        it.title.contains(query, ignoreCase = true) ||
                        it.authorNames.contains(query, ignoreCase = true) ||
                        it.isbn10.contains(query) ||
                        it.isbn13.contains(query)
                    }
                }
                if (genre != null) {
                    filtered = filtered.filter { it.genre == genre }
                }
                if (status != null) {
                    filtered = filtered.filter { it.readStatus == status }
                }
                filtered = when (sort) {
                    SortOrder.TITLE -> filtered.sortedBy { it.title }
                    SortOrder.AUTHOR -> filtered.sortedBy { it.publisher }
                    SortOrder.DATE_ADDED -> filtered.sortedByDescending { it.dateAdded }
                    SortOrder.PUBLICATION_YEAR -> filtered.sortedByDescending { it.originalPublicationYear }
                    SortOrder.RATING -> filtered.sortedByDescending { it.rating }
                }
                LibraryUiState(
                    books = filtered,
                    searchQuery = query,
                    selectedGenre = genre,
                    selectedReadStatus = status,
                    sortOrder = sort,
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }

    fun searchBooks(query: String) {
        _searchQuery.value = query
    }

    fun filterByGenre(genre: String?) {
        _selectedGenre.value = genre
    }

    fun filterByReadStatus(status: ReadStatus?) {
        _selectedReadStatus.value = status
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            bookRepository.deleteBook(book)
        }
    }

    fun toggleFavourite(book: Book) {
        viewModelScope.launch {
            bookRepository.updateBook(book.copy(isFavourite = !book.isFavourite))
        }
    }
}
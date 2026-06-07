package app.imalibrarian.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.imalibrarian.domain.model.Book
import app.imalibrarian.domain.model.ReadStatus
import app.imalibrarian.domain.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookDetailUiState(
    val book: Book? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository
) : ViewModel() {

    private val bookId: Long = savedStateHandle.get<Long>("bookId") ?: -1L

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    init {
        loadBook()
    }

    fun loadBook() {
        viewModelScope.launch {
            val book = bookRepository.getBookById(bookId)
            _uiState.value = BookDetailUiState(book = book, isLoading = false)
        }
    }

    fun updateReadStatus(status: ReadStatus) {
        viewModelScope.launch {
            _uiState.value.book?.let { book ->
                bookRepository.updateBook(book.copy(readStatus = status))
                loadBook()
            }
        }
    }

    fun updateRating(rating: Int) {
        viewModelScope.launch {
            _uiState.value.book?.let { book ->
                bookRepository.updateBook(book.copy(rating = rating))
                loadBook()
            }
        }
    }

    fun toggleFavourite() {
        viewModelScope.launch {
            _uiState.value.book?.let { book ->
                bookRepository.updateBook(book.copy(isFavourite = !book.isFavourite))
                loadBook()
            }
        }
    }

    fun deleteBook() {
        viewModelScope.launch {
            _uiState.value.book?.let { book ->
                bookRepository.deleteBook(book)
            }
        }
    }
}
package app.imalibrarian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.imalibrarian.domain.model.Book
import app.imalibrarian.domain.model.Priority
import app.imalibrarian.domain.model.ReadStatus
import app.imalibrarian.domain.model.WishlistItem
import app.imalibrarian.domain.repository.BookRepository
import app.imalibrarian.domain.repository.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WishlistUiState(
    val items: List<WishlistItem> = emptyList(),
    val searchQuery: String = "",
    val selectedPriority: Priority? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val wishlistRepository: WishlistRepository,
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedPriority = MutableStateFlow<Priority?>(null)

    init {
        viewModelScope.launch {
            combine(
                wishlistRepository.getAllWishlistItems(),
                _searchQuery,
                _selectedPriority
            ) { items, query, priority ->
                var filtered = items
                if (query.isNotBlank()) {
                    filtered = filtered.filter {
                        it.title.contains(query, ignoreCase = true) ||
                        it.isbn10.contains(query) ||
                        it.isbn13.contains(query)
                    }
                }
                if (priority != null) {
                    filtered = filtered.filter { it.priority == priority }
                }
                WishlistUiState(
                    items = filtered,
                    searchQuery = query,
                    selectedPriority = priority,
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }

    fun searchItems(query: String) {
        _searchQuery.value = query
    }

    fun filterByPriority(priority: Priority?) {
        _selectedPriority.value = priority
    }

    fun deleteItem(item: WishlistItem) {
        viewModelScope.launch {
            wishlistRepository.deleteWishlistItem(item)
        }
    }

    fun moveToLibrary(item: WishlistItem) {
        viewModelScope.launch {
            val book = Book(
                title = item.title,
                subtitle = item.subtitle,
                authorNames = item.authorNames,
                isbn10 = item.isbn10,
                isbn13 = item.isbn13,
                publisher = item.publisher,
                genre = item.genre,
                subgenre = item.subgenre,
                coverImagePath = item.coverImagePath,
                readStatus = ReadStatus.UNREAD
            )
            bookRepository.addBook(book)
            wishlistRepository.deleteWishlistItem(item)
        }
    }
}
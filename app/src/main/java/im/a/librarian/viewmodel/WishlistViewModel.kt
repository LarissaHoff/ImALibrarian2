package im.a.librarian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import im.a.librarian.domain.model.Book
import im.a.librarian.domain.model.Priority
import im.a.librarian.domain.model.ReadStatus
import im.a.librarian.domain.model.WishlistItem
import im.a.librarian.domain.repository.BookRepository
import im.a.librarian.domain.repository.WishlistRepository
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

    fun updatePriority(item: WishlistItem, newPriority: Priority) {
        viewModelScope.launch {
            wishlistRepository.updateWishlistItem(item.copy(priority = newPriority))
        }
    }

    suspend fun getShareText(priority: Priority? = null): String {
        val allItems = wishlistRepository.getAllWishlistItems().first()
        val items = if (priority != null) allItems.filter { it.priority == priority } else allItems
        return buildString {
            appendLine("My Wishlist")
            appendLine()
            if (priority != null) {
                items.forEach { item ->
                    val author = if (item.authorNames.isNotBlank()) " by ${item.authorNames}" else ""
                    val isbn = when {
                        item.isbn13.isNotBlank() -> " (ISBN: ${item.isbn13})"
                        item.isbn10.isNotBlank() -> " (ISBN: ${item.isbn10})"
                        else -> ""
                    }
                    appendLine("  - ${item.title}$author$isbn")
                }
            } else {
                Priority.entries.forEach { p ->
                    val priorityItems = items.filter { it.priority == p }
                    if (priorityItems.isNotEmpty()) {
                        appendLine("${p.name} PRIORITY:")
                        priorityItems.forEach { item ->
                            val author = if (item.authorNames.isNotBlank()) " by ${item.authorNames}" else ""
                            val isbn = when {
                                item.isbn13.isNotBlank() -> " (ISBN: ${item.isbn13})"
                                item.isbn10.isNotBlank() -> " (ISBN: ${item.isbn10})"
                                else -> ""
                            }
                            appendLine("  - ${item.title}$author$isbn")
                        }
                        appendLine()
                    }
                }
            }
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
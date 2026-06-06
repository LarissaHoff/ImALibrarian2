package app.imalibrarian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.imalibrarian.domain.model.Priority
import app.imalibrarian.domain.model.WishlistItem
import app.imalibrarian.domain.repository.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddWishlistUiState(
    val title: String = "",
    val authorNames: String = "",
    val isbn10: String = "",
    val isbn13: String = "",
    val publisher: String = "",
    val genre: String = "",
    val subgenre: String = "",
    val priority: Priority = Priority.MEDIUM,
    val notes: String = "",
    val isSaving: Boolean = false,
    val saveComplete: Boolean = false
)

@HiltViewModel
class AddWishlistItemViewModel @Inject constructor(
    private val wishlistRepository: WishlistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddWishlistUiState())
    val uiState: StateFlow<AddWishlistUiState> = _uiState.asStateFlow()

    fun updateTitle(title: String) { _uiState.value = _uiState.value.copy(title = title) }
    fun updateAuthorNames(authors: String) { _uiState.value = _uiState.value.copy(authorNames = authors) }
    fun updateIsbn10(isbn: String) { _uiState.value = _uiState.value.copy(isbn10 = isbn) }
    fun updateIsbn13(isbn: String) { _uiState.value = _uiState.value.copy(isbn13 = isbn) }
    fun updatePublisher(publisher: String) { _uiState.value = _uiState.value.copy(publisher = publisher) }
    fun updateGenre(genre: String) { _uiState.value = _uiState.value.copy(genre = genre) }
    fun updateSubgenre(subgenre: String) { _uiState.value = _uiState.value.copy(subgenre = subgenre) }
    fun updatePriority(priority: Priority) { _uiState.value = _uiState.value.copy(priority = priority) }
    fun updateNotes(notes: String) { _uiState.value = _uiState.value.copy(notes = notes) }

    fun saveItem() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val state = _uiState.value
            val item = WishlistItem(
                title = state.title,
                authorNames = state.authorNames,
                isbn10 = state.isbn10,
                isbn13 = state.isbn13,
                publisher = state.publisher,
                genre = state.genre,
                subgenre = state.subgenre,
                priority = state.priority,
                notes = state.notes
            )
            wishlistRepository.addWishlistItem(item)
            _uiState.value = _uiState.value.copy(isSaving = false, saveComplete = true)
        }
    }
}
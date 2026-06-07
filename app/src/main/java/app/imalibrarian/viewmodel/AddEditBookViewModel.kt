package app.imalibrarian.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.imalibrarian.domain.model.Book
import app.imalibrarian.domain.model.ReadStatus
import app.imalibrarian.domain.model.ScanResult
import app.imalibrarian.domain.usecase.AddBookUseCase
import app.imalibrarian.domain.usecase.ScanBarcodeUseCase
import app.imalibrarian.domain.repository.BookRepository
import app.imalibrarian.ui.theme.LanguageFlags
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditBookUiState(
    val id: Long = 0,
    val title: String = "",
    val subtitle: String = "",
    val isbn10: String = "",
    val isbn13: String = "",
    val authorNames: String = "",
    val publisher: String = "",
    val placeOfPublication: String = "",
    val pageCount: String = "",
    val language: String = "",
    val selectedLanguageCode: String = "",
    val customLanguageText: String = "",
    val showCustomLanguageField: Boolean = false,
    val originalPublicationYear: String = "",
    val editionPublicationYear: String = "",
    val editionNumber: String = "",
    val printingNumber: String = "",
    val genre: String = "",
    val subgenre: String = "",
    val dateAcquired: String = "",
    val purchasePrice: String = "",
    val sourceOfPurchase: String = "",
    val shelfLocation: String = "",
    val readStatus: ReadStatus = ReadStatus.UNREAD,
    val rating: Int = 0,
    val personalNotes: String = "",
    val translator: String = "",
    val isFavourite: Boolean = false,
    val seriesName: String = "",
    val seriesNumber: String = "",
    val coverImagePath: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isLookingUp: Boolean = false,
    val lookupFailed: Boolean = false,
    val isDuplicate: Boolean = false,
    val scanResult: ScanResult.Found? = null,
    val saveComplete: Boolean = false
)

@HiltViewModel
class AddEditBookViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val addBookUseCase: AddBookUseCase,
    private val scanBarcodeUseCase: ScanBarcodeUseCase,
    private val bookRepository: BookRepository
) : ViewModel() {

    private val bookId: Long = savedStateHandle.get<Long>("bookId") ?: -1L
    private val scanIsbn: String = savedStateHandle.get<String>("isbn") ?: ""

    private val _uiState = MutableStateFlow(AddEditBookUiState())
    val uiState: StateFlow<AddEditBookUiState> = _uiState.asStateFlow()

    init {
        if (bookId > 0) {
            loadBook()
        } else if (scanIsbn.isNotBlank()) {
            lookupScannedIsbn(scanIsbn)
        }
    }

    private fun loadBook() {
        viewModelScope.launch {
            val book = bookRepository.getBookById(bookId)
            book?.let {
                _uiState.value = AddEditBookUiState(
                    id = it.id,
                    title = it.title,
                    subtitle = it.subtitle,
                    isbn10 = it.isbn10,
                    isbn13 = it.isbn13,
                    publisher = it.publisher,
                    placeOfPublication = it.placeOfPublication,
                    pageCount = it.pageCount.toString(),
                    language = it.language,
                    originalPublicationYear = it.originalPublicationYear?.toString() ?: "",
                    editionPublicationYear = it.editionPublicationYear?.toString() ?: "",
                    editionNumber = it.editionNumber?.toString() ?: "",
                    printingNumber = it.printingNumber?.toString() ?: "",
                    genre = it.genre,
                    subgenre = it.subgenre,
                    dateAcquired = it.dateAcquired?.toString() ?: "",
                    purchasePrice = it.purchasePrice,
                    sourceOfPurchase = it.sourceOfPurchase,
                    shelfLocation = it.shelfLocation,
                    readStatus = it.readStatus,
                    rating = it.rating,
                    personalNotes = it.personalNotes,
                    translator = it.translator,
                    isFavourite = it.isFavourite,
                    seriesName = it.seriesName,
                    seriesNumber = it.seriesNumber?.toString() ?: "",
                    coverImagePath = it.coverImagePath,
                    selectedLanguageCode = LanguageFlags.toFlagCode(it.language),
                    customLanguageText = if (LanguageFlags.isFlagLanguage(it.language)) "" else it.language,
                    showCustomLanguageField = !LanguageFlags.isFlagLanguage(it.language) && it.language.isNotBlank(),
                    isEditing = true
                )
            }
        }
    }

    private fun lookupScannedIsbn(isbn: String) {
        viewModelScope.launch {
            Log.d("BookLookup", "Looking up ISBN: $isbn")
            _uiState.value = _uiState.value.copy(isbn13 = isbn, isLookingUp = true, lookupFailed = false)
            val result = scanBarcodeUseCase.lookupBarcode(isbn)
            when (result) {
                is ScanResult.Found -> {
                    Log.d("BookLookup", "Found: ${result.title} by ${result.authors}")
                    populateFromScan(result)
                }
                is ScanResult.NotFound -> {
                    Log.d("BookLookup", "No results found for ISBN: $isbn")
                    _uiState.value = _uiState.value.copy(isLookingUp = false, lookupFailed = true)
                }
                is ScanResult.Error -> {
                    Log.e("BookLookup", "Lookup error: ${result.message}")
                    _uiState.value = _uiState.value.copy(isLookingUp = false, lookupFailed = true)
                }
            }
        }
    }

    fun populateFromScan(result: ScanResult.Found) {
        _uiState.value = _uiState.value.copy(
            title = result.title,
            subtitle = result.subtitle,
            authorNames = result.authors.joinToString(", "),
            isbn10 = result.isbn10,
            isbn13 = result.isbn13,
            publisher = result.publisher,
            pageCount = if (result.pageCount > 0) result.pageCount.toString() else "",
            language = result.language,
            genre = result.genre,
            originalPublicationYear = result.originalPublicationYear?.toString() ?: "",
            coverImagePath = result.coverUrl,
            selectedLanguageCode = LanguageFlags.toFlagCode(result.language),
            customLanguageText = if (LanguageFlags.isFlagLanguage(result.language)) "" else result.language,
            showCustomLanguageField = !LanguageFlags.isFlagLanguage(result.language) && result.language.isNotBlank(),
            scanResult = result,
            isLookingUp = false
        )
        checkDuplicate()
    }

    fun updateTitle(title: String) { _uiState.value = _uiState.value.copy(title = title) }
    fun updateSubtitle(subtitle: String) { _uiState.value = _uiState.value.copy(subtitle = subtitle) }
    fun updateIsbn10(isbn: String) { _uiState.value = _uiState.value.copy(isbn10 = isbn) }
    fun updateIsbn13(isbn: String) { _uiState.value = _uiState.value.copy(isbn13 = isbn) }
    fun updateAuthorNames(authors: String) { _uiState.value = _uiState.value.copy(authorNames = authors) }
    fun updatePublisher(publisher: String) { _uiState.value = _uiState.value.copy(publisher = publisher) }
    fun updatePlaceOfPublication(place: String) { _uiState.value = _uiState.value.copy(placeOfPublication = place) }
    fun updatePageCount(count: String) { _uiState.value = _uiState.value.copy(pageCount = count) }
    fun updateLanguage(lang: String) { _uiState.value = _uiState.value.copy(language = lang) }
    fun updateOriginalPublicationYear(year: String) { _uiState.value = _uiState.value.copy(originalPublicationYear = year) }
    fun updateEditionPublicationYear(year: String) { _uiState.value = _uiState.value.copy(editionPublicationYear = year) }
    fun updateEditionNumber(num: String) { _uiState.value = _uiState.value.copy(editionNumber = num) }
    fun updatePrintingNumber(num: String) { _uiState.value = _uiState.value.copy(printingNumber = num) }
    fun updateGenre(genre: String) { _uiState.value = _uiState.value.copy(genre = genre) }
    fun updateSubgenre(subgenre: String) { _uiState.value = _uiState.value.copy(subgenre = subgenre) }
    fun updateDateAcquired(date: String) { _uiState.value = _uiState.value.copy(dateAcquired = date) }
    fun updatePurchasePrice(price: String) { _uiState.value = _uiState.value.copy(purchasePrice = price) }
    fun updateSourceOfPurchase(source: String) { _uiState.value = _uiState.value.copy(sourceOfPurchase = source) }
    fun updateShelfLocation(location: String) { _uiState.value = _uiState.value.copy(shelfLocation = location) }
    fun updateReadStatus(status: ReadStatus) { _uiState.value = _uiState.value.copy(readStatus = status) }
    fun updateRating(rating: Int) { _uiState.value = _uiState.value.copy(rating = rating) }
    fun updatePersonalNotes(notes: String) { _uiState.value = _uiState.value.copy(personalNotes = notes) }
    fun updateTranslator(t: String) { _uiState.value = _uiState.value.copy(translator = t) }
    fun updateIsFavourite(fav: Boolean) { _uiState.value = _uiState.value.copy(isFavourite = fav) }
    fun updateSeriesName(name: String) { _uiState.value = _uiState.value.copy(seriesName = name) }
    fun updateSeriesNumber(num: String) { _uiState.value = _uiState.value.copy(seriesNumber = num) }
    fun updateCoverImagePath(path: String) { _uiState.value = _uiState.value.copy(coverImagePath = path) }
    fun selectLanguageFlag(code: String) {
        _uiState.value = _uiState.value.copy(
            selectedLanguageCode = code,
            language = if (code.isNotEmpty()) code else _uiState.value.customLanguageText,
            customLanguageText = if (code.isNotEmpty()) "" else _uiState.value.customLanguageText,
            showCustomLanguageField = code.isEmpty()
        )
    }
    fun updateCustomLanguage(text: String) {
        _uiState.value = _uiState.value.copy(
            customLanguageText = text,
            language = text,
            selectedLanguageCode = ""
        )
    }

    private fun checkDuplicate() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isbn10.isNotBlank() || state.isbn13.isNotBlank()) {
                val existing = addBookUseCase.checkDuplicate(state.isbn10, state.isbn13)
                _uiState.value = _uiState.value.copy(isDuplicate = existing.isNotEmpty())
            }
        }
    }

    fun saveBook() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val state = _uiState.value
            val book = Book(
                id = if (state.isEditing) state.id else 0,
                title = state.title,
                subtitle = state.subtitle,
                authorNames = state.authorNames,
                isbn10 = state.isbn10,
                isbn13 = state.isbn13,
                publisher = state.publisher,
                placeOfPublication = state.placeOfPublication,
                pageCount = state.pageCount.toIntOrNull() ?: 0,
                language = if (state.selectedLanguageCode.isNotBlank()) state.selectedLanguageCode else state.customLanguageText,
                originalPublicationYear = state.originalPublicationYear.toIntOrNull(),
                editionPublicationYear = state.editionPublicationYear.toIntOrNull(),
                editionNumber = state.editionNumber.toIntOrNull(),
                printingNumber = state.printingNumber.toIntOrNull(),
                genre = state.genre,
                subgenre = state.subgenre,
                dateAcquired = state.dateAcquired.toLongOrNull(),
                purchasePrice = state.purchasePrice,
                sourceOfPurchase = state.sourceOfPurchase,
                shelfLocation = state.shelfLocation,
                readStatus = state.readStatus,
                rating = state.rating,
            personalNotes = state.personalNotes,
            translator = state.translator,
            isFavourite = state.isFavourite,
                seriesName = state.seriesName,
                seriesNumber = state.seriesNumber.toIntOrNull(),
                coverImagePath = state.coverImagePath
            )

            if (state.isEditing) {
                bookRepository.updateBook(book)
            } else {
                addBookUseCase.addBook(book)
            }
            _uiState.value = _uiState.value.copy(isSaving = false, saveComplete = true)
        }
    }

    private fun isFlagLanguage(lang: String): Boolean = LanguageFlags.isFlagLanguage(lang)
}
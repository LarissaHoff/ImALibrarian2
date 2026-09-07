package im.a.librarian.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import im.a.librarian.domain.model.Book
import im.a.librarian.domain.model.ReadStatus
import im.a.librarian.domain.model.ScanResult
import im.a.librarian.domain.usecase.AddBookUseCase
import im.a.librarian.domain.usecase.ScanBarcodeUseCase
import im.a.librarian.domain.repository.BookRepository
import im.a.librarian.domain.util.IsbnNormalizer
import im.a.librarian.data.GenreCatalog
import im.a.librarian.ui.theme.LanguageFlags
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditBookUiState(
    val id: Long = 0,
    val title: String = "",
    val subtitle: String = "",
    val isbn: String = "",
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
    val genreSuggestions: List<String> = emptyList(),
    val subgenreSuggestions: List<String> = emptyList(),
    val authorSuggestions: List<String> = emptyList(),
    val seriesSuggestions: List<String> = emptyList(),
    val publisherSuggestions: List<String> = emptyList(),
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
    val dateAdded: Long = System.currentTimeMillis(),
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

    private var duplicateCheckJob: Job? = null

    private var allGenres: List<String> = GenreCatalog.allTerms
    private var allAuthors: List<String> = emptyList()
    private var allSeries: List<String> = emptyList()
    private var allPublishers: List<String> = emptyList()

    init {
        viewModelScope.launch {
            val dbGenres = bookRepository.getAllGenres()
            allGenres = (GenreCatalog.allTerms + dbGenres).distinct().sorted()
            refreshGenreSuggestions(_uiState.value.genre)
            refreshSubgenreSuggestions(_uiState.value.subgenre)
        }
        viewModelScope.launch {
            // authorNames rows are comma-separated combinations ("A, B");
            // split them into individual author names for autocomplete
            allAuthors = bookRepository.getAllAuthors()
                .flatMap { it.split(",") }
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
            allSeries = bookRepository.getAllSeriesNames()
            allPublishers = bookRepository.getAllPublishers()
        }
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
                    authorNames = it.authorNames,
                    isbn = it.isbn13.ifBlank { it.isbn10 },
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
                    dateAdded = it.dateAdded,
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
            _uiState.value = _uiState.value.copy(isbn = isbn, isLookingUp = true, lookupFailed = false)
            checkDuplicate()
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
            isbn = result.isbn13.ifBlank { result.isbn10 },
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
    fun updateIsbn(isbn: String) {
        _uiState.value = _uiState.value.copy(isbn = isbn)
        checkDuplicate()
    }
    fun updateAuthorNames(authors: String) {
        _uiState.value = _uiState.value.copy(authorNames = authors)
        refreshAuthorSuggestions(authors)
    }
    fun updatePublisher(publisher: String) {
        _uiState.value = _uiState.value.copy(publisher = publisher)
        refreshPublisherSuggestions(publisher)
    }
    fun updatePlaceOfPublication(place: String) { _uiState.value = _uiState.value.copy(placeOfPublication = place) }
    fun updatePageCount(count: String) { _uiState.value = _uiState.value.copy(pageCount = count) }
    fun updateLanguage(lang: String) { _uiState.value = _uiState.value.copy(language = lang) }
    fun updateOriginalPublicationYear(year: String) { _uiState.value = _uiState.value.copy(originalPublicationYear = year) }
    fun updateEditionPublicationYear(year: String) { _uiState.value = _uiState.value.copy(editionPublicationYear = year) }
    fun updateEditionNumber(num: String) { _uiState.value = _uiState.value.copy(editionNumber = num) }
    fun updatePrintingNumber(num: String) { _uiState.value = _uiState.value.copy(printingNumber = num) }
    fun updateGenre(genre: String) {
        _uiState.value = _uiState.value.copy(genre = genre)
        refreshGenreSuggestions(genre)
    }
    fun updateSubgenre(subgenre: String) {
        _uiState.value = _uiState.value.copy(subgenre = subgenre)
        refreshSubgenreSuggestions(subgenre)
    }

    private fun refreshGenreSuggestions(query: String) {
        val filtered = if (query.isBlank()) emptyList()
            else allGenres.filter { it.contains(query, ignoreCase = true) }
        _uiState.value = _uiState.value.copy(genreSuggestions = filtered)
    }

    private fun refreshSubgenreSuggestions(query: String) {
        val filtered = if (query.isBlank()) emptyList()
            else allGenres.filter { it.contains(query, ignoreCase = true) }
        _uiState.value = _uiState.value.copy(subgenreSuggestions = filtered)
    }

    private fun refreshAuthorSuggestions(text: String) {
        // autocomplete applies to the author currently being typed,
        // i.e. the segment after the last comma
        val segment = text.substringAfterLast(',').trim()
        val filtered = if (segment.isBlank()) emptyList()
            else allAuthors.filter {
                it.contains(segment, ignoreCase = true) && !it.equals(segment, ignoreCase = true)
            }
        _uiState.value = _uiState.value.copy(authorSuggestions = filtered)
    }

    fun selectAuthorSuggestion(author: String) {
        val current = _uiState.value.authorNames
        val prefix = current.substringBeforeLast(',', missingDelimiterValue = "")
            .takeIf { it.isNotBlank() }?.let { "$it, " } ?: ""
        _uiState.value = _uiState.value.copy(
            authorNames = prefix + author,
            authorSuggestions = emptyList()
        )
    }

    private fun refreshSeriesSuggestions(query: String) {
        val filtered = if (query.isBlank()) emptyList()
            else allSeries.filter {
                it.contains(query, ignoreCase = true) && !it.equals(query, ignoreCase = true)
            }
        _uiState.value = _uiState.value.copy(seriesSuggestions = filtered)
    }

    fun selectSeriesSuggestion(series: String) {
        _uiState.value = _uiState.value.copy(
            seriesName = series,
            seriesSuggestions = emptyList()
        )
    }

    private fun refreshPublisherSuggestions(query: String) {
        val filtered = if (query.isBlank()) emptyList()
            else allPublishers.filter {
                it.contains(query, ignoreCase = true) && !it.equals(query, ignoreCase = true)
            }
        _uiState.value = _uiState.value.copy(publisherSuggestions = filtered)
    }

    fun selectPublisherSuggestion(publisher: String) {
        _uiState.value = _uiState.value.copy(
            publisher = publisher,
            publisherSuggestions = emptyList()
        )
    }

    fun updateDateAcquired(date: String) { _uiState.value = _uiState.value.copy(dateAcquired = date) }
    fun updatePurchasePrice(price: String) { _uiState.value = _uiState.value.copy(purchasePrice = price) }
    fun updateSourceOfPurchase(source: String) { _uiState.value = _uiState.value.copy(sourceOfPurchase = source) }
    fun updateShelfLocation(location: String) { _uiState.value = _uiState.value.copy(shelfLocation = location) }
    fun updateReadStatus(status: ReadStatus) { _uiState.value = _uiState.value.copy(readStatus = status) }
    fun updateRating(rating: Int) { _uiState.value = _uiState.value.copy(rating = rating) }
    fun updatePersonalNotes(notes: String) { _uiState.value = _uiState.value.copy(personalNotes = notes) }
    fun updateTranslator(t: String) { _uiState.value = _uiState.value.copy(translator = t) }
    fun updateIsFavourite(fav: Boolean) { _uiState.value = _uiState.value.copy(isFavourite = fav) }
    fun updateSeriesName(name: String) {
        _uiState.value = _uiState.value.copy(seriesName = name)
        refreshSeriesSuggestions(name)
    }
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
        duplicateCheckJob?.cancel()
        duplicateCheckJob = viewModelScope.launch {
            val state = _uiState.value
            val sanitized = IsbnNormalizer.sanitize(state.isbn)
            if (sanitized.isBlank()) {
                _uiState.value = _uiState.value.copy(isDuplicate = false)
                return@launch
            }
            val isbn13 = IsbnNormalizer.toIsbn13(sanitized).ifBlank { sanitized }
            val isbn10 = if (sanitized.length == 10) sanitized else IsbnNormalizer.toIsbn10(isbn13)
            val existing = addBookUseCase.checkDuplicate(isbn10, isbn13)
            _uiState.value = _uiState.value.copy(isDuplicate = existing.any { it.id != state.id })
        }
    }

    fun saveBook() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val state = _uiState.value
            val sanitizedIsbn = IsbnNormalizer.sanitize(state.isbn)
            val isbn13 = IsbnNormalizer.toIsbn13(sanitizedIsbn).ifBlank { sanitizedIsbn }
            val isbn10 = if (sanitizedIsbn.length == 10) sanitizedIsbn else IsbnNormalizer.toIsbn10(isbn13)
            val book = Book(
                id = if (state.isEditing) state.id else 0,
                title = state.title,
                subtitle = state.subtitle,
                authorNames = state.authorNames,
                isbn10 = isbn10,
                isbn13 = isbn13,
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
                coverImagePath = state.coverImagePath,
                dateAdded = state.dateAdded
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
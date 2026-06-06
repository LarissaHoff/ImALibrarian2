package app.imalibrarian.domain.usecase

import app.imalibrarian.domain.model.Book
import app.imalibrarian.domain.repository.BookRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ExportBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun exportToJson(): String {
        val books = bookRepository.getAllBooks().first()
        return json.encodeToString(books)
    }

    suspend fun exportToCsv(): String {
        val books = bookRepository.getAllBooks().first()
        val headers = listOf(
            "id", "title", "subtitle", "isbn10", "isbn13", "publisher",
            "placeOfPublication", "pageCount", "language",
            "originalPublicationYear", "editionPublicationYear",
            "editionNumber", "printingNumber", "genre", "subgenre",
            "dateAcquired", "purchasePrice", "sourceOfPurchase",
            "shelfLocation", "readStatus", "rating", "personalNotes",
            "isFavourite", "seriesName", "seriesNumber", "coverImagePath",
            "dateAdded"
        )
        val rows = books.map { book ->
            listOf(
                book.id.toString(),
                book.title.csvEscape(),
                book.subtitle.csvEscape(),
                book.isbn10,
                book.isbn13,
                book.publisher.csvEscape(),
                book.placeOfPublication.csvEscape(),
                book.pageCount.toString(),
                book.language,
                book.originalPublicationYear?.toString() ?: "",
                book.editionPublicationYear?.toString() ?: "",
                book.editionNumber?.toString() ?: "",
                book.printingNumber?.toString() ?: "",
                book.genre.csvEscape(),
                book.subgenre.csvEscape(),
                book.dateAcquired?.toString() ?: "",
                book.purchasePrice.csvEscape(),
                book.sourceOfPurchase.csvEscape(),
                book.shelfLocation.csvEscape(),
                book.readStatus.name,
                book.rating.toString(),
                book.personalNotes.csvEscape(),
                book.isFavourite.toString(),
                book.seriesName.csvEscape(),
                book.seriesNumber?.toString() ?: "",
                book.coverImagePath.csvEscape(),
                book.dateAdded.toString()
            ).joinToString(",")
        }
        return listOf(headers.joinToString(",")) + rows.joinToString("\n")
    }

    private fun String.csvEscape(): String {
        return if (contains(",") || contains("\"") || contains("\n")) {
            "\"${replace("\"", "\"\"")}\""
        } else this
    }
}
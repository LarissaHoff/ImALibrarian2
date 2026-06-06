package app.imalibrarian.domain.usecase

import app.imalibrarian.domain.model.Book
import app.imalibrarian.domain.model.ReadStatus
import app.imalibrarian.domain.repository.BookRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ImportBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun importFromJson(jsonString: String): Int {
        val books = json.decodeFromString<List<Book>>(jsonString)
        var count = 0
        for (book in books) {
            val existing = bookRepository.getBooksByIsbn(book.isbn10, book.isbn13)
            if (existing.isEmpty()) {
                bookRepository.addBook(book.copy(id = 0))
                count++
            }
        }
        return count
    }

    suspend fun importFromCsv(csvString: String): Int {
        val lines = csvString.lines().filter { it.isNotBlank() }
        if (lines.size < 2) return 0

        val headers = lines[0].split(",").map { it.trim() }
        var count = 0

        for (i in 1 until lines.size) {
            val values = parseCsvLine(lines[i])
            if (values.size != headers.size) continue

            val getValue = { header: String ->
                val index = headers.indexOf(header)
                if (index >= 0 && index < values.size) values[index] else ""
            }

            val isbn10 = getValue("isbn10")
            val isbn13 = getValue("isbn13")
            val existing = bookRepository.getBooksByIsbn(isbn10, isbn13)
            if (existing.isNotEmpty()) continue

            val book = Book(
                title = getValue("title"),
                subtitle = getValue("subtitle"),
                authorNames = getValue("authorNames"),
                isbn10 = isbn10,
                isbn13 = isbn13,
                publisher = getValue("publisher"),
                placeOfPublication = getValue("placeOfPublication"),
                pageCount = getValue("pageCount").toIntOrNull() ?: 0,
                language = getValue("language"),
                originalPublicationYear = getValue("originalPublicationYear").toIntOrNull(),
                editionPublicationYear = getValue("editionPublicationYear").toIntOrNull(),
                editionNumber = getValue("editionNumber").toIntOrNull(),
                printingNumber = getValue("printingNumber").toIntOrNull(),
                genre = getValue("genre"),
                subgenre = getValue("subgenre"),
                dateAcquired = getValue("dateAcquired").toLongOrNull(),
                purchasePrice = getValue("purchasePrice"),
                sourceOfPurchase = getValue("sourceOfPurchase"),
                shelfLocation = getValue("shelfLocation"),
                readStatus = try {
                    ReadStatus.valueOf(getValue("readStatus"))
                } catch (_: Exception) { ReadStatus.UNREAD },
                rating = getValue("rating").toIntOrNull() ?: 0,
                personalNotes = getValue("personalNotes"),
                isFavourite = getValue("isFavourite").toBoolean(),
                seriesName = getValue("seriesName"),
                seriesNumber = getValue("seriesNumber").toIntOrNull(),
                coverImagePath = getValue("coverImagePath")
            )
            bookRepository.addBook(book)
            count++
        }
        return count
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' && !inQuotes -> inQuotes = true
                char == '"' && inQuotes -> inQuotes = false
                char == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString().trim())
        return result
    }
}
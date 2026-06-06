package app.imalibrarian.domain.model

data class Statistics(
    val totalBooks: Int = 0,
    val totalWishlistItems: Int = 0,
    val booksRead: Int = 0,
    val booksUnread: Int = 0,
    val booksCurrentlyReading: Int = 0,
    val booksDidNotFinish: Int = 0,
    val booksByGenre: Map<String, Int> = emptyMap(),
    val booksByDecade: Map<String, Int> = emptyMap(),
    val mostCommonAuthors: List<AuthorCount> = emptyList(),
    val averageRating: Float = 0f,
    val readingProgress: Float = 0f
)

data class AuthorCount(
    val author: String,
    val count: Int
)
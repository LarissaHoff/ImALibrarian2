package app.imalibrarian.domain.model

data class WishlistItem(
    val id: Long = 0,
    val title: String = "",
    val subtitle: String = "",
    val isbn10: String = "",
    val isbn13: String = "",
    val authorNames: String = "",
    val publisher: String = "",
    val genre: String = "",
    val subgenre: String = "",
    val priority: Priority = Priority.MEDIUM,
    val notes: String = "",
    val coverImagePath: String = "",
    val dateAdded: Long = System.currentTimeMillis()
)
package app.imalibrarian.domain.model

sealed class ScanResult {
    data class Found(
        val title: String,
        val subtitle: String = "",
        val authors: List<String> = emptyList(),
        val isbn10: String = "",
        val isbn13: String = "",
        val publisher: String = "",
        val pageCount: Int = 0,
        val language: String = "",
        val genre: String = "",
        val originalPublicationYear: Int? = null,
        val coverUrl: String = "",
        val confidence: Float = 1.0f
    ) : ScanResult()

    data object NotFound : ScanResult()
    data class Error(val message: String) : ScanResult()
}
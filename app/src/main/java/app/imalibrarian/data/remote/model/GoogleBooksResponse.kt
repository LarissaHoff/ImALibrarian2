package app.imalibrarian.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class GoogleBooksResponse(
    val totalItems: Int = 0,
    val items: List<GoogleBookItem> = emptyList()
)

@Serializable
data class GoogleBookItem(
    val id: String = "",
    val volumeInfo: GoogleVolumeInfo = GoogleVolumeInfo()
)

@Serializable
data class GoogleVolumeInfo(
    val title: String = "",
    val subtitle: String = "",
    val authors: List<String> = emptyList(),
    val publisher: String = "",
    val publishedDate: String = "",
    val description: String = "",
    val pageCount: Int = 0,
    val language: String = "",
    val industryIdentifiers: List<GoogleIndustryIdentifier> = emptyList(),
    val imageLinks: GoogleImageLinks = GoogleImageLinks(),
    val categories: List<String> = emptyList()
)

@Serializable
data class GoogleIndustryIdentifier(
    val type: String = "",
    val identifier: String = ""
)

@Serializable
data class GoogleImageLinks(
    val smallThumbnail: String = "",
    val thumbnail: String = "",
    val small: String = "",
    val medium: String = "",
    val large: String = ""
)
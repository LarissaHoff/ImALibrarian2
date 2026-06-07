package app.imalibrarian.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class OpenLibraryBookData(
    val url: String = "",
    val title: String = "",
    val subtitle: String = "",
    val authors: List<OpenLibraryAuthor> = emptyList(),
    val publishers: List<OpenLibraryPublisher> = emptyList(),
    val number_of_pages: Int = 0,
    val publish_date: String = "",
    val subjects: List<OpenLibrarySubject> = emptyList(),
    val cover: OpenLibraryCover = OpenLibraryCover(),
    val isbn_10: List<String> = emptyList(),
    val isbn_13: List<String> = emptyList()
)

@Serializable
data class OpenLibraryAuthor(
    val url: String = "",
    val name: String = ""
)

@Serializable
data class OpenLibraryPublisher(
    val name: String = ""
)

@Serializable
data class OpenLibrarySubject(
    val url: String = "",
    val name: String = ""
)

@Serializable
data class OpenLibraryCover(
    val small: String = "",
    val medium: String = "",
    val large: String = ""
)

@Serializable
data class OpenLibrarySearchResponse(
    val numFound: Int = 0,
    val docs: List<OpenLibrarySearchDoc> = emptyList()
)

@Serializable
data class OpenLibrarySearchDoc(
    val key: String = "",
    val title: String = "",
    val subtitle: String? = null,
    val author_name: List<String> = emptyList(),
    val publisher: List<String> = emptyList(),
    val first_publish_year: Int? = null,
    val isbn: List<String> = emptyList(),
    val language: List<String> = emptyList(),
    val subject: List<String> = emptyList(),
    val cover_i: Long? = null,
    val number_of_pages_median: Int? = null
)
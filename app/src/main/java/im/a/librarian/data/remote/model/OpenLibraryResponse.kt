package im.a.librarian.data.remote.model

import kotlinx.serialization.Serializable

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
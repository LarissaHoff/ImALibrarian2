package im.a.librarian.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ReadStatus(val displayName: String) {
    UNREAD("Unread"),
    CURRENTLY_READING("Currently Reading"),
    FINISHED("Finished"),
    DID_NOT_FINISH("DNF")
}
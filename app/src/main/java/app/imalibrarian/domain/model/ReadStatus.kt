package app.imalibrarian.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ReadStatus(val displayName: String) {
    UNREAD("Unread"),
    CURRENTLY_READING("Reading"),
    FINISHED("Finished"),
    DID_NOT_FINISH("DNF")
}
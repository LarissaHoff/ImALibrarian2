package app.imalibrarian.domain.model

import org.junit.Assert.*
import org.junit.Test

class ReadStatusTest {

    @Test
    fun `ReadStatus has four values`() {
        assertEquals(4, ReadStatus.entries.size)
    }

    @Test
    fun `ReadStatus contains DID_NOT_FINISH`() {
        assertTrue(ReadStatus.entries.contains(ReadStatus.DID_NOT_FINISH))
    }

    @Test
    fun `ReadStatus valueOf returns correct enum`() {
        assertEquals(ReadStatus.UNREAD, ReadStatus.valueOf("UNREAD"))
        assertEquals(ReadStatus.CURRENTLY_READING, ReadStatus.valueOf("CURRENTLY_READING"))
        assertEquals(ReadStatus.FINISHED, ReadStatus.valueOf("FINISHED"))
        assertEquals(ReadStatus.DID_NOT_FINISH, ReadStatus.valueOf("DID_NOT_FINISH"))
    }
}
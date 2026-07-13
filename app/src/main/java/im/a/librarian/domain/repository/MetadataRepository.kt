package im.a.librarian.domain.repository

import im.a.librarian.domain.model.ScanResult

interface MetadataRepository {
    suspend fun lookupByIsbn(isbn: String): ScanResult
    suspend fun searchByTitle(query: String): List<ScanResult>
}
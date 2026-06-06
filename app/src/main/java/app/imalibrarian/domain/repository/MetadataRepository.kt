package app.imalibrarian.domain.repository

import app.imalibrarian.domain.model.ScanResult

interface MetadataRepository {
    suspend fun lookupByIsbn(isbn: String): ScanResult
    suspend fun searchByTitle(query: String): List<ScanResult>
}
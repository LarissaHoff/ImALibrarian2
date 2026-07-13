package im.a.librarian.domain.usecase

import im.a.librarian.domain.model.ScanResult
import im.a.librarian.domain.repository.MetadataRepository
import javax.inject.Inject

class ScanBarcodeUseCase @Inject constructor(
    private val metadataRepository: MetadataRepository
) {
    suspend fun lookupBarcode(barcodeValue: String): ScanResult {
        return metadataRepository.lookupByIsbn(barcodeValue)
    }
}
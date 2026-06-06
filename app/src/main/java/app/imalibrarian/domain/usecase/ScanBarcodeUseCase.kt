package app.imalibrarian.domain.usecase

import app.imalibrarian.domain.model.ScanResult
import app.imalibrarian.domain.repository.MetadataRepository
import javax.inject.Inject

class ScanBarcodeUseCase @Inject constructor(
    private val metadataRepository: MetadataRepository
) {
    suspend fun lookupBarcode(barcodeValue: String): ScanResult {
        return metadataRepository.lookupByIsbn(barcodeValue)
    }
}
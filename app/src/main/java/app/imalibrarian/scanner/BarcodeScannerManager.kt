package app.imalibrarian.scanner

import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.asDeferred
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resumeWithException

@Singleton
class BarcodeScannerManager @Inject constructor() {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_CODE_93
        )
        .build()

    private val scanner: BarcodeScanner = BarcodeScanning.getClient(options)

    suspend fun scanImage(inputImage: InputImage): List<ScannedBarcode> {
        return try {
            val barcodes = scanner.process(inputImage).asDeferred().await()
            barcodes.mapNotNull { barcode ->
                val value = barcode.rawValue ?: return@mapNotNull null
                val format = when (barcode.format) {
                    Barcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
                    Barcode.FORMAT_EAN_8 -> BarcodeFormat.EAN_8
                    Barcode.FORMAT_UPC_A -> BarcodeFormat.UPC_A
                    Barcode.FORMAT_UPC_E -> BarcodeFormat.UPC_E
                    Barcode.FORMAT_CODE_128 -> BarcodeFormat.CODE_128
                    else -> BarcodeFormat.UNKNOWN
                }
                ScannedBarcode(value = value, format = format)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun release() {
        scanner.close()
    }
}

data class ScannedBarcode(
    val value: String,
    val format: BarcodeFormat
)

enum class BarcodeFormat {
    EAN_13, EAN_8, UPC_A, UPC_E, CODE_128, UNKNOWN
}
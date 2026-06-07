package app.imalibrarian.scanner

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resumeWithException

@Singleton
class CoverScannerManager @Inject constructor() {

    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun scanCover(inputImage: InputImage): CoverScanResult {
        val labels = try {
            suspendCancellableCoroutine<List<CoverLabel>> { continuation ->
                labeler.process(inputImage)
                    .addOnSuccessListener { imageLabels ->
                        val result = imageLabels
                            .filter { it.confidence > 0.5f }
                            .map { CoverLabel(text = it.text, confidence = it.confidence) }
                        continuation.resume(result) {}
                    }
                    .addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            }
        } catch (_: Exception) {
            emptyList()
        }

        val textLines = try {
            suspendCancellableCoroutine<List<String>> { continuation ->
                textRecognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val lines = visionText.textBlocks
                            .flatMap { block -> block.lines }
                            .map { line -> line.text }
                        continuation.resume(lines) {}
                    }
                    .addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            }
        } catch (_: Exception) {
            emptyList()
        }

        val titleCandidates = textLines
            .filter { it.length in 2..80 }
            .sortedByDescending { it.length }
            .take(5)

        return CoverScanResult(
            labels = labels,
            extractedText = textLines,
            titleCandidates = titleCandidates,
            confidence = if (titleCandidates.isNotEmpty()) 0.6f else 0.3f
        )
    }

    fun release() {
        labeler.close()
        textRecognizer.close()
    }
}

data class CoverScanResult(
    val labels: List<CoverLabel>,
    val extractedText: List<String>,
    val titleCandidates: List<String>,
    val confidence: Float
)

data class CoverLabel(
    val text: String,
    val confidence: Float
)
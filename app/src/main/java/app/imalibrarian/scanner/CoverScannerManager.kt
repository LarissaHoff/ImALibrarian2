package app.imalibrarian.scanner

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerScript
import kotlinx.coroutines.tasks.asDeferred
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoverScannerManager @Inject constructor() {

    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    private val textRecognizer = TextRecognition.getClient(TextRecognizerScript.LATIN)

    suspend fun scanCover(inputImage: InputImage): CoverScanResult {
        val labels = try {
            labeler.process(inputImage).asDeferred().await()
                .filter { it.confidence > 0.5f }
                .map { CoverLabel(text = it.text, confidence = it.confidence) }
        } catch (_: Exception) {
            emptyList()
        }

        val text = try {
            val result = textRecognizer.process(inputImage).asDeferred().await()
            result.textBlocks
                .flatMap { block -> block.lines }
                .map { line -> line.text }
        } catch (_: Exception) {
            emptyList()
        }

        val titleCandidates = text
            .filter { it.length in 2..80 }
            .sortedByDescending { it.length }
            .take(5)

        return CoverScanResult(
            labels = labels,
            extractedText = text,
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
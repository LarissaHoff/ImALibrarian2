package im.a.librarian.scanner

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import im.a.librarian.domain.util.IsbnNormalizer
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resumeWithException

@Singleton
class IsbnTextScannerManager @Inject constructor() {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    suspend fun scanImage(inputImage: InputImage): List<String> {
        val lines = try {
            suspendCancellableCoroutine { continuation ->
                textRecognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val result = visionText.textBlocks
                            .flatMap { block -> block.lines }
                            .map { line -> line.text }
                        continuation.resume(result) {}
                    }
                    .addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            }
        } catch (_: Exception) {
            emptyList()
        }
        return extractIsbnCandidates(lines)
    }

    fun release() {
        textRecognizer.close()
    }

    companion object {
        private val LABELED_ISBN = Regex(
            """ISBN(?:-1[03])?\s*:?\s*([0-9Xx][0-9Xx\- \u00A0]{8,16}[0-9Xx])""",
            RegexOption.IGNORE_CASE
        )

        private val BARE_ISBN = Regex("""(?:^|\D)([0-9Xx][0-9Xx\- \u00A0]{8,16}[0-9Xx])(?!\d)""")

        fun extractIsbnCandidates(lines: List<String>): List<String> {
            val labeled = lines.flatMap { line ->
                LABELED_ISBN.findAll(line).map { it.groupValues[1] }
            }.mapNotNull { candidate ->
                IsbnNormalizer.sanitize(candidate).takeIf { IsbnNormalizer.isValidShape(it) }
            }

            val bare = if (labeled.isEmpty()) {
                lines.flatMap { line ->
                    BARE_ISBN.findAll(line).map { it.groupValues[1] }
                }.mapNotNull { candidate ->
                    IsbnNormalizer.sanitize(candidate).takeIf {
                        IsbnNormalizer.isValidShape(it) && IsbnNormalizer.hasValidChecksum(it)
                    }
                }
            } else {
                emptyList()
            }

            return (labeled + bare).distinct().take(5)
        }
    }
}

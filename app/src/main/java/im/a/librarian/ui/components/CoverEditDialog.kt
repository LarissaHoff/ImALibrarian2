package im.a.librarian.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.exifinterface.media.ExifInterface
import im.a.librarian.ui.theme.Turquoise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min
import kotlin.math.roundToInt

private const val TAG = "CoverEdit"
private const val MAX_WORKING_DIMENSION = 2048
private const val JPEG_QUALITY = 90
private const val MIN_CROP_FRACTION = 0.15f

@Composable
fun CoverEditDialog(
    imagePath: String,
    outputDir: File,
    deleteSourceOnFinish: Boolean,
    onRetake: (() -> Unit)?,
    onConfirmed: (String) -> Unit,
    onDismissed: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loadFailed by remember { mutableStateOf(false) }
    var rotation by remember { mutableStateOf(0) }
    var crop by remember { mutableStateOf(Rect(0f, 0f, 1f, 1f)) }
    var saving by remember { mutableStateOf(false) }
    var sourceFile by remember { mutableStateOf<File?>(null) }
    var disposableSource by remember { mutableStateOf(false) }
    var sourceKept by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            val file = sourceFile
            if (disposableSource && !sourceKept && file != null) {
                file.delete()
            }
            bitmap?.recycle()
        }
    }

    LaunchedEffect(imagePath) {
        val resolved = withContext(Dispatchers.IO) {
            resolveSource(context, imagePath, deleteSourceOnFinish)
        }
        if (resolved == null) {
            loadFailed = true
            return@LaunchedEffect
        }
        sourceFile = resolved.file
        disposableSource = resolved.disposable
        val decoded = withContext(Dispatchers.IO) { decodeWorkingImage(resolved.file) }
        if (decoded != null) {
            bitmap = decoded
        } else {
            loadFailed = true
        }
    }

    fun rotateCrop(clockwise: Boolean) {
        val c = crop
        crop = if (clockwise) {
            Rect(1f - c.bottom, c.left, 1f - c.top, c.right)
        } else {
            Rect(c.top, 1f - c.right, c.bottom, 1f - c.left)
        }
    }

    fun confirm() {
        val bmp = bitmap
        val src = sourceFile
        if (bmp == null || src == null || saving) return
        saving = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { transformAndSave(src, bmp, rotation, crop, outputDir, disposableSource) }
            }
            result.fold(
                onSuccess = { path ->
                    if (path == src.absolutePath) sourceKept = true
                    saving = false
                    onConfirmed(path)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to save edited cover", error)
                    saving = false
                    onDismissed()
                }
            )
        }
    }

    Dialog(
        onDismissRequest = { if (!saving) onDismissed() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (!saving) onDismissed() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel", tint = Color.White)
                    }
                    Text(
                        "Edit Cover",
                        modifier = Modifier.weight(1f),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = { confirm() }, enabled = bitmap != null && !saving) {
                        Icon(Icons.Filled.Check, contentDescription = "Apply", tint = Turquoise)
                    }
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val bmp = bitmap
                    val density = LocalDensity.current
                    when {
                        bmp != null -> {
                            val containerW = constraints.maxWidth.toFloat()
                            val containerH = constraints.maxHeight.toFloat()
                            val swapped = rotation % 180 != 0
                            val dispW = if (swapped) bmp.height.toFloat() else bmp.width.toFloat()
                            val dispH = if (swapped) bmp.width.toFloat() else bmp.height.toFloat()
                            val scale = min(containerW / dispW, containerH / dispH)
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Book cover preview",
                                modifier = Modifier
                                    .size(
                                        with(density) { (bmp.width * scale).toDp() },
                                        with(density) { (bmp.height * scale).toDp() }
                                    )
                                    .graphicsLayer { rotationZ = rotation.toFloat() }
                            )
                            CropOverlay(
                                crop = crop,
                                onCropChange = { crop = it },
                                modifier = Modifier.size(
                                    with(density) { (dispW * scale).toDp() },
                                    with(density) { (dispH * scale).toDp() }
                                )
                            )
                        }
                        loadFailed -> {
                            Text(
                                "Could not load the image for editing.",
                                color = Color.White
                            )
                        }
                        else -> CircularProgressIndicator(color = Turquoise)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            rotation -= 90
                            rotateCrop(clockwise = false)
                        },
                        enabled = bitmap != null && !saving
                    ) {
                        Icon(Icons.AutoMirrored.Filled.RotateLeft, contentDescription = "Rotate left", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(
                        onClick = {
                            rotation += 90
                            rotateCrop(clockwise = true)
                        },
                        enabled = bitmap != null && !saving
                    ) {
                        Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate right", tint = Color.White)
                    }
                    if (onRetake != null) {
                        Spacer(modifier = Modifier.width(16.dp))
                        TextButton(
                            onClick = { if (!saving) onRetake() },
                            enabled = bitmap != null && !saving
                        ) {
                            Icon(
                                Icons.Filled.PhotoCamera,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Take new picture", color = Color.White)
                        }
                    }
                }
            }

            if (saving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) { detectTapGestures { } }
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Turquoise)
                }
            }
        }
    }
}

private data class CoverEditSource(val file: File, val disposable: Boolean)

private fun resolveSource(
    context: Context,
    imagePath: String,
    deleteSourceOnFinish: Boolean
): CoverEditSource? {
    return if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
        downloadImage(imagePath, context.cacheDir)?.let { CoverEditSource(it, disposable = true) }
    } else {
        val file = File(imagePath)
        if (file.exists()) CoverEditSource(file, disposable = deleteSourceOnFinish) else null
    }
}

private fun downloadImage(url: String, cacheDir: File): File? = runCatching {
    val request = Request.Builder().url(url).build()
    OkHttpClient().newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@runCatching null
        val body = response.body ?: return@runCatching null
        val temp = File(cacheDir, "cover_edit_${System.currentTimeMillis()}.jpg")
        body.byteStream().use { input ->
            temp.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        if (temp.length() > 0) temp else null
    }
}.getOrNull()

private enum class CropHandle { TOP_LEFT, TOP, TOP_RIGHT, LEFT, RIGHT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT }

private enum class HandleShape { DOT, BAR_HORIZONTAL, BAR_VERTICAL }

@Composable
private fun CropOverlay(
    crop: Rect,
    onCropChange: (Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    var overlaySize by remember { mutableStateOf(IntSize.Zero) }
    val currentCrop by rememberUpdatedState(crop)
    val currentOnCropChange by rememberUpdatedState(onCropChange)

    Box(modifier = modifier.onSizeChanged { overlaySize = it }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (overlaySize == IntSize.Zero) return@Canvas
            val selection = Rect(
                crop.left * size.width,
                crop.top * size.height,
                crop.right * size.width,
                crop.bottom * size.height
            )
            val mask = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(0f, 0f, size.width, size.height))
                addRect(selection)
            }
            drawPath(mask, Color.Black.copy(alpha = 0.55f))
            val gridColor = Color.White.copy(alpha = 0.35f)
            val lineWidth = 1.dp.toPx()
            for (i in 1..2) {
                val fx = selection.left + (selection.right - selection.left) * i / 3f
                val fy = selection.top + (selection.bottom - selection.top) * i / 3f
                drawRect(
                    color = gridColor,
                    topLeft = Offset(fx - lineWidth / 2f, selection.top),
                    size = Size(lineWidth, selection.height)
                )
                drawRect(
                    color = gridColor,
                    topLeft = Offset(selection.left, fy - lineWidth / 2f),
                    size = Size(selection.width, lineWidth)
                )
            }
            drawRect(
                color = Color.White,
                topLeft = Offset(selection.left, selection.top),
                size = Size(selection.width, selection.height),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        if (overlaySize != IntSize.Zero) {
            val density = LocalDensity.current
            val overlayW = overlaySize.width.coerceAtLeast(1).toFloat()
            val overlayH = overlaySize.height.coerceAtLeast(1).toFloat()

            fun applyHandleDrag(handle: CropHandle, dxPx: Float, dyPx: Float) {
                val c = currentCrop
                val dx = dxPx / overlayW
                val dy = dyPx / overlayH
                val minDim = MIN_CROP_FRACTION
                val next = when (handle) {
                    CropHandle.TOP_LEFT -> Rect(
                        (c.left + dx).coerceIn(0f, c.right - minDim),
                        (c.top + dy).coerceIn(0f, c.bottom - minDim),
                        c.right,
                        c.bottom
                    )
                    CropHandle.TOP -> Rect(c.left, (c.top + dy).coerceIn(0f, c.bottom - minDim), c.right, c.bottom)
                    CropHandle.TOP_RIGHT -> Rect(
                        c.left,
                        (c.top + dy).coerceIn(0f, c.bottom - minDim),
                        (c.right + dx).coerceIn(c.left + minDim, 1f),
                        c.bottom
                    )
                    CropHandle.LEFT -> Rect((c.left + dx).coerceIn(0f, c.right - minDim), c.top, c.right, c.bottom)
                    CropHandle.RIGHT -> Rect(c.left, c.top, (c.right + dx).coerceIn(c.left + minDim, 1f), c.bottom)
                    CropHandle.BOTTOM_LEFT -> Rect(
                        (c.left + dx).coerceIn(0f, c.right - minDim),
                        c.top,
                        c.right,
                        (c.bottom + dy).coerceIn(c.top + minDim, 1f)
                    )
                    CropHandle.BOTTOM -> Rect(c.left, c.top, c.right, (c.bottom + dy).coerceIn(c.top + minDim, 1f))
                    CropHandle.BOTTOM_RIGHT -> Rect(
                        c.left,
                        c.top,
                        (c.right + dx).coerceIn(c.left + minDim, 1f),
                        (c.bottom + dy).coerceIn(c.top + minDim, 1f)
                    )
                }
                currentOnCropChange(next)
            }

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (crop.left * overlayW).roundToInt(),
                            (crop.top * overlayH).roundToInt()
                        )
                    }
                    .size(
                        with(density) { ((crop.right - crop.left) * overlayW).toDp() },
                        with(density) { ((crop.bottom - crop.top) * overlayH).toDp() }
                    )
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val w = overlaySize.width.coerceAtLeast(1).toFloat()
                            val h = overlaySize.height.coerceAtLeast(1).toFloat()
                            val c = currentCrop
                            val dx = dragAmount.x / w
                            val dy = dragAmount.y / h
                            val width = c.right - c.left
                            val height = c.bottom - c.top
                            val newLeft = (c.left + dx).coerceIn(0f, 1f - width)
                            val newTop = (c.top + dy).coerceIn(0f, 1f - height)
                            currentOnCropChange(Rect(newLeft, newTop, newLeft + width, newTop + height))
                        }
                    }
            )

            DragHandle(
                center = Offset(crop.left, crop.top),
                overlaySize = overlaySize,
                shape = HandleShape.DOT
            ) { dx, dy -> applyHandleDrag(CropHandle.TOP_LEFT, dx, dy) }
            DragHandle(
                center = Offset((crop.left + crop.right) / 2f, crop.top),
                overlaySize = overlaySize,
                shape = HandleShape.BAR_HORIZONTAL
            ) { dx, dy -> applyHandleDrag(CropHandle.TOP, dx, dy) }
            DragHandle(
                center = Offset(crop.right, crop.top),
                overlaySize = overlaySize,
                shape = HandleShape.DOT
            ) { dx, dy -> applyHandleDrag(CropHandle.TOP_RIGHT, dx, dy) }
            DragHandle(
                center = Offset(crop.left, (crop.top + crop.bottom) / 2f),
                overlaySize = overlaySize,
                shape = HandleShape.BAR_VERTICAL
            ) { dx, dy -> applyHandleDrag(CropHandle.LEFT, dx, dy) }
            DragHandle(
                center = Offset(crop.right, (crop.top + crop.bottom) / 2f),
                overlaySize = overlaySize,
                shape = HandleShape.BAR_VERTICAL
            ) { dx, dy -> applyHandleDrag(CropHandle.RIGHT, dx, dy) }
            DragHandle(
                center = Offset(crop.left, crop.bottom),
                overlaySize = overlaySize,
                shape = HandleShape.DOT
            ) { dx, dy -> applyHandleDrag(CropHandle.BOTTOM_LEFT, dx, dy) }
            DragHandle(
                center = Offset((crop.left + crop.right) / 2f, crop.bottom),
                overlaySize = overlaySize,
                shape = HandleShape.BAR_HORIZONTAL
            ) { dx, dy -> applyHandleDrag(CropHandle.BOTTOM, dx, dy) }
            DragHandle(
                center = Offset(crop.right, crop.bottom),
                overlaySize = overlaySize,
                shape = HandleShape.DOT
            ) { dx, dy -> applyHandleDrag(CropHandle.BOTTOM_RIGHT, dx, dy) }
        }
    }
}

@Composable
private fun DragHandle(
    center: Offset,
    overlaySize: IntSize,
    shape: HandleShape,
    onDrag: (Float, Float) -> Unit
) {
    if (overlaySize == IntSize.Zero) return
    val currentOnDrag by rememberUpdatedState(onDrag)
    val density = LocalDensity.current
    val touchSize = 34.dp
    val touchPx = with(density) { touchSize.toPx() }
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (center.x * overlaySize.width).roundToInt() - (touchPx / 2f).roundToInt(),
                    (center.y * overlaySize.height).roundToInt() - (touchPx / 2f).roundToInt()
                )
            }
            .size(touchSize)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    currentOnDrag(dragAmount.x, dragAmount.y)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        when (shape) {
            HandleShape.DOT -> Box(
                modifier = Modifier
                    .size(13.dp)
                    .background(Color.White, CircleShape)
                    .border(1.5.dp, Turquoise, CircleShape)
            )
            HandleShape.BAR_HORIZONTAL -> Box(
                modifier = Modifier
                    .size(width = 18.dp, height = 4.dp)
                    .background(Color.White, RoundedCornerShape(2.dp))
            )
            HandleShape.BAR_VERTICAL -> Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 18.dp)
                    .background(Color.White, RoundedCornerShape(2.dp))
            )
        }
    }
}

private fun decodeWorkingImage(file: File): Bitmap? {
    if (!file.exists()) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sampleSize = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= MAX_WORKING_DIMENSION) {
        sampleSize *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val decoded = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null
    val exifDegrees = runCatching {
        when (
            ExifInterface(file).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }.getOrDefault(0)
    return if (exifDegrees == 0) {
        decoded
    } else {
        val rotated = rotateBitmap(decoded, exifDegrees.toFloat())
        decoded.recycle()
        rotated
    }
}

private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

private fun transformAndSave(
    sourceFile: File,
    bitmap: Bitmap,
    rotationDegrees: Int,
    crop: Rect,
    outputDir: File,
    deleteSource: Boolean
): String {
    outputDir.mkdirs()
    val noChanges = ((rotationDegrees % 360) + 360) % 360 == 0 &&
        crop.left <= 0f && crop.top <= 0f && crop.right >= 1f && crop.bottom >= 1f
    if (noChanges && sourceFile.parentFile == outputDir) {
        return sourceFile.absolutePath
    }
    val destFile = File(outputDir, "cover_${System.currentTimeMillis()}.jpg")
    if (noChanges) {
        sourceFile.copyTo(destFile, overwrite = true)
        if (deleteSource) sourceFile.delete()
        return destFile.absolutePath
    }
    val rotated = when (((rotationDegrees % 360) + 360) % 360) {
        90 -> rotateBitmap(bitmap, 90f)
        180 -> rotateBitmap(bitmap, 180f)
        270 -> rotateBitmap(bitmap, 270f)
        else -> bitmap
    }
    val left = (crop.left * rotated.width).roundToInt().coerceIn(0, rotated.width - 1)
    val top = (crop.top * rotated.height).roundToInt().coerceIn(0, rotated.height - 1)
    val width = ((crop.right - crop.left) * rotated.width).roundToInt()
        .coerceIn(1, rotated.width - left)
    val height = ((crop.bottom - crop.top) * rotated.height).roundToInt()
        .coerceIn(1, rotated.height - top)
    val cropped = if (left == 0 && top == 0 && width == rotated.width && height == rotated.height) {
        rotated
    } else {
        Bitmap.createBitmap(rotated, left, top, width, height)
    }
    FileOutputStream(destFile).use { output ->
        cropped.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
    }
    if (cropped !== rotated) cropped.recycle()
    if (rotated !== bitmap) rotated.recycle()
    if (deleteSource) sourceFile.delete()
    return destFile.absolutePath
}

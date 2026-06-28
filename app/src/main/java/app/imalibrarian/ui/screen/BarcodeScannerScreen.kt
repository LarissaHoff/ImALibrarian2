package app.imalibrarian.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import app.imalibrarian.scanner.BarcodeScannerManager
import app.imalibrarian.ui.navigation.Screen
import app.imalibrarian.ui.theme.Turquoise
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

private const val TAG = "BarcodeScanner"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    navController: NavController,
    barcodeScannerManager: BarcodeScannerManager = hiltViewModel<BarcodeScannerViewModel>().scanner
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Barcode") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasCameraPermission) {
                var scannedCode by remember { mutableStateOf<String?>(null) }
                var isLookingUp by remember { mutableStateOf(false) }

                if (scannedCode != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    LaunchedEffect(scannedCode) {
                        if (!isLookingUp) {
                            isLookingUp = true
                            navController.navigate(Screen.AddBook.createRoute(scannedCode!!)) {
                                popUpTo("scan_barcode") { inclusive = true }
                            }
                        }
                    }
                } else {
                    CameraBarcodePreview(
                        modifier = Modifier.fillMaxSize(),
                        lifecycleOwner = lifecycleOwner,
                        barcodeScannerManager = barcodeScannerManager,
                        onBarcodeDetected = { code ->
                            scannedCode = code
                        }
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.VideocamOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                        Text(
                            "Camera permission is required to scan barcodes",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraBarcodePreview(
    modifier: Modifier,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    barcodeScannerManager: BarcodeScannerManager,
    onBarcodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val captureExecutor = remember { Executors.newSingleThreadExecutor() }
    val scanScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isScanning by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val capture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
            .also { imageCapture = it }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                capture
            )
        } catch (e: Exception) {
            Log.e(TAG, "Camera bind failed", e)
        }

        onDispose {
            try {
                cameraProviderFuture.get().unbindAll()
            } catch (_: Exception) {
            }
            captureExecutor.shutdown()
            scanScope.cancel()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        ScanOverlay()

        Button(
            onClick = {
                val capture = imageCapture ?: return@Button
                isScanning = true
                capture.takePicture(
                    captureExecutor,
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(imageProxy: ImageProxy) {
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val inputImage = InputImage.fromMediaImage(
                                    mediaImage, imageProxy.imageInfo.rotationDegrees
                                )
                                scanScope.launch {
                                    try {
                                        val results = barcodeScannerManager.scanImage(inputImage)
                                        if (results.isNotEmpty()) {
                                            Log.d(TAG, "Barcode detected: ${results.first().value}")
                                            withContext(Dispatchers.Main) {
                                                onBarcodeDetected(results.first().value)
                                            }
                                        } else {
                                            withContext(Dispatchers.Main) { isScanning = false }
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Barcode scan error", e)
                                        withContext(Dispatchers.Main) { isScanning = false }
                                    } finally {
                                        imageProxy.close()
                                    }
                                }
                            } else {
                                imageProxy.close()
                                scanScope.launch {
                                    withContext(Dispatchers.Main) { isScanning = false }
                                }
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e(TAG, "Capture failed", exception)
                            scanScope.launch {
                                withContext(Dispatchers.Main) { isScanning = false }
                            }
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .size(64.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Turquoise),
            enabled = !isScanning
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Filled.CameraAlt, contentDescription = "Capture")
            }
        }
    }
}

@Composable
private fun ScanOverlay() {
    val overlayColor = Color.White.copy(alpha = 0.15f)
    val frameColor = Turquoise.copy(alpha = 0.8f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val frameWidth = canvasWidth * 0.7f
        val frameHeight = canvasHeight * 0.25f
        val frameLeft = (canvasWidth - frameWidth) / 2f
        val frameTop = (canvasHeight - frameHeight) / 2f

        drawRect(
            color = overlayColor,
            topLeft = Offset.Zero,
            size = Size(canvasWidth, frameTop)
        )
        drawRect(
            color = overlayColor,
            topLeft = Offset(0f, frameTop + frameHeight),
            size = Size(canvasWidth, canvasHeight - frameTop - frameHeight)
        )
        drawRect(
            color = overlayColor,
            topLeft = Offset(0f, frameTop),
            size = Size(frameLeft, frameHeight)
        )
        drawRect(
            color = overlayColor,
            topLeft = Offset(frameLeft + frameWidth, frameTop),
            size = Size(canvasWidth - frameLeft - frameWidth, frameHeight)
        )

        drawRect(
            color = frameColor,
            topLeft = Offset(frameLeft, frameTop),
            size = Size(frameWidth, frameHeight),
            style = Stroke(width = 3.dp.toPx())
        )

        val cornerLen = 24.dp.toPx()
        drawLine(
            color = frameColor,
            start = Offset(frameLeft, frameTop + cornerLen),
            end = Offset(frameLeft, frameTop),
            strokeWidth = 4.dp.toPx()
        )
        drawLine(
            color = frameColor,
            start = Offset(frameLeft, frameTop),
            end = Offset(frameLeft + cornerLen, frameTop),
            strokeWidth = 4.dp.toPx()
        )

        drawLine(
            color = frameColor,
            start = Offset(frameLeft + frameWidth - cornerLen, frameTop),
            end = Offset(frameLeft + frameWidth, frameTop),
            strokeWidth = 4.dp.toPx()
        )
        drawLine(
            color = frameColor,
            start = Offset(frameLeft + frameWidth, frameTop),
            end = Offset(frameLeft + frameWidth, frameTop + cornerLen),
            strokeWidth = 4.dp.toPx()
        )

        drawLine(
            color = frameColor,
            start = Offset(frameLeft, frameTop + frameHeight - cornerLen),
            end = Offset(frameLeft, frameTop + frameHeight),
            strokeWidth = 4.dp.toPx()
        )
        drawLine(
            color = frameColor,
            start = Offset(frameLeft, frameTop + frameHeight),
            end = Offset(frameLeft + cornerLen, frameTop + frameHeight),
            strokeWidth = 4.dp.toPx()
        )

        drawLine(
            color = frameColor,
            start = Offset(frameLeft + frameWidth - cornerLen, frameTop + frameHeight),
            end = Offset(frameLeft + frameWidth, frameTop + frameHeight),
            strokeWidth = 4.dp.toPx()
        )
        drawLine(
            color = frameColor,
            start = Offset(frameLeft + frameWidth, frameTop + frameHeight),
            end = Offset(frameLeft + frameWidth, frameTop + frameHeight - cornerLen),
            strokeWidth = 4.dp.toPx()
        )
    }
}

@dagger.hilt.android.lifecycle.HiltViewModel
class BarcodeScannerViewModel @javax.inject.Inject constructor(
    val scanner: BarcodeScannerManager
) : androidx.lifecycle.ViewModel()

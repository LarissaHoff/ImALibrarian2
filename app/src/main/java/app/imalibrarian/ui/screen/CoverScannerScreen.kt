package app.imalibrarian.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import app.imalibrarian.scanner.CoverScannerManager
import app.imalibrarian.ui.navigation.Screen
import app.imalibrarian.ui.theme.Turquoise
import com.google.mlkit.vision.common.InputImage
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverScannerScreen(
    navController: NavController,
    coverScannerManager: CoverScannerManager = hiltViewModel<CoverScannerViewModel>().scanner
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
                title = { Text("Scan Cover") },
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
                var isCapturing by remember { mutableStateOf(false) }
                var scanResult by remember { mutableStateOf<String?>(null) }

                Box(modifier = Modifier.fillMaxSize()) {
                    CoverCameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        lifecycleOwner = lifecycleOwner,
                        isCapturing = isCapturing,
                        onCaptureStart = { isCapturing = true },
                        onCaptureComplete = { result ->
                            isCapturing = false
                            scanResult = result
                        },
                        coverScannerManager = coverScannerManager
                    )

                    if (!isCapturing) {
                        Button(
                            onClick = { isCapturing = true },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Capture Cover")
                        }
                    }

                    if (isCapturing) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (scanResult != null) {
                    LaunchedEffect(scanResult) {
                        navController.navigate(Screen.AddBook.createRoute()) {
                            popUpTo("scan_cover") { inclusive = true }
                        }
                    }
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
                            "Camera permission is required to scan covers",
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
private fun CoverCameraPreview(
    modifier: Modifier,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    isCapturing: Boolean,
    onCaptureStart: () -> Unit,
    onCaptureComplete: (String) -> Unit,
    coverScannerManager: CoverScannerManager
) {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val captureExecutor = remember { Executors.newSingleThreadExecutor() }
    val analysisScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

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
        } catch (_: Exception) {
        }

        onDispose {
            cameraProviderFuture.get().unbindAll()
            captureExecutor.shutdown()
            analysisScope.cancel()
        }
    }

    LaunchedEffect(isCapturing) {
        if (isCapturing) {
            val capture = imageCapture ?: return@LaunchedEffect
            onCaptureStart()
            try {
                val photoFile = java.io.File(
                    context.cacheDir,
                    "cover_scan_${System.currentTimeMillis()}.jpg"
                )
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                capture.takePicture(
                    outputOptions,
                    captureExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            analysisScope.launch {
                                try {
                                    val bitmap = android.graphics.BitmapFactory.decodeFile(photoFile.absolutePath)
                                    if (bitmap != null) {
                                        val inputImage = InputImage.fromBitmap(bitmap, 0)
                                        val result = coverScannerManager.scanCover(inputImage)
                                        val bestTitle = result.titleCandidates.firstOrNull() ?: "Unknown Book"
                                        kotlinx.coroutines.withContext(Dispatchers.Main) {
                                            onCaptureComplete(bestTitle)
                                        }
                                    } else {
                                        kotlinx.coroutines.withContext(Dispatchers.Main) {
                                            onCaptureComplete("Unknown Book")
                                        }
                                    }
                                } catch (_: Exception) {
                                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                                        onCaptureComplete("Unknown Book")
                                    }
                                } finally {
                                    photoFile.delete()
                                }
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            analysisScope.launch {
                                kotlinx.coroutines.withContext(Dispatchers.Main) {
                                    onCaptureComplete("Unknown Book")
                                }
                            }
                        }
                    }
                )
            } catch (_: Exception) {
                onCaptureComplete("Unknown Book")
            }
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

@dagger.hilt.android.lifecycle.HiltViewModel
class CoverScannerViewModel @javax.inject.Inject constructor(
    val scanner: CoverScannerManager
) : androidx.lifecycle.ViewModel()

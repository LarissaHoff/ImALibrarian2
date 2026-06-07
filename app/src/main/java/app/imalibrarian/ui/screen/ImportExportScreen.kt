package app.imalibrarian.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.imalibrarian.ui.theme.*
import app.imalibrarian.viewmodel.ImportExportViewModel

private enum class ExportType { JSON, CSV }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(
    navController: NavController,
    viewModel: ImportExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var pendingExportType by remember { mutableStateOf<ExportType?>(null) }
    var fileSaved by remember { mutableStateOf(false) }

    val jsonExportPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(uiState.exportData.toByteArray())
            }
            fileSaved = true
        }
        pendingExportType = null
        viewModel.clearState()
    }

    val csvExportPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(uiState.exportData.toByteArray())
            }
            fileSaved = true
        }
        pendingExportType = null
        viewModel.clearState()
    }

    val jsonImportPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val content = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (content != null) {
                viewModel.importFromJson(String(content))
            }
        }
    }

    val csvImportPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val content = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (content != null) {
                viewModel.importFromCsv(String(content))
            }
        }
    }

    LaunchedEffect(uiState.exportComplete, uiState.exportData) {
        if (uiState.exportComplete && uiState.exportData.isNotBlank() && pendingExportType != null) {
            when (pendingExportType) {
                ExportType.JSON -> jsonExportPicker.launch("library_export.json")
                ExportType.CSV -> csvExportPicker.launch("library_export.csv")
                null -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import / Export") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Import / Export Your Library",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (uiState.errorMessage != null) {
                Card(colors = CardDefaults.cardColors(containerColor = Coral.copy(alpha = 0.15f))) {
                    Text(
                        uiState.errorMessage!!,
                        modifier = Modifier.padding(12.dp),
                        color = Coral
                    )
                }
            }

            if (fileSaved) {
                Card(colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f))) {
                    Text(
                        "File saved successfully!",
                        modifier = Modifier.padding(12.dp),
                        color = SuccessGreen
                    )
                }
            }

            if (uiState.importComplete) {
                Card(colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f))) {
                    Text(
                        "Successfully imported ${uiState.importedCount} books!",
                        modifier = Modifier.padding(12.dp),
                        color = SuccessGreen
                    )
                }
            }

            if (uiState.isExporting || uiState.isImporting) {
                CircularProgressIndicator(color = Turquoise)
            }

            Text("Export", style = MaterialTheme.typography.titleLarge, color = Turquoise)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        fileSaved = false
                        pendingExportType = ExportType.JSON
                        viewModel.exportToJson()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Turquoise),
                    enabled = !uiState.isExporting
                ) {
                    Icon(Icons.Filled.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("JSON")
                }

                Button(
                    onClick = {
                        fileSaved = false
                        pendingExportType = ExportType.CSV
                        viewModel.exportToCsv()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal),
                    enabled = !uiState.isExporting
                ) {
                    Icon(Icons.Filled.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CSV")
                }
            }

            HorizontalDivider()

            Text("Import", style = MaterialTheme.typography.titleLarge, color = Coral)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { jsonImportPicker.launch(arrayOf("application/json", "*/*")) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral),
                    enabled = !uiState.isImporting
                ) {
                    Icon(Icons.Filled.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("JSON")
                }

                Button(
                    onClick = { csvImportPicker.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*")) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange),
                    enabled = !uiState.isImporting
                ) {
                    Icon(Icons.Filled.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CSV")
                }
            }
        }
    }
}
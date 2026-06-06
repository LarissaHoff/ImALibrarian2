package app.imalibrarian.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.imalibrarian.ui.theme.*
import app.imalibrarian.viewmodel.ImportExportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(
    navController: NavController,
    viewModel: ImportExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    onClick = { viewModel.exportToJson() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Turquoise),
                    enabled = !uiState.isExporting
                ) {
                    Icon(Icons.Filled.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("JSON")
                }

                Button(
                    onClick = { viewModel.exportToCsv() },
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
                    onClick = {
                        // In a real app, this would use ActivityResultContracts to pick a file
                        viewModel.importFromJson("{}")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral),
                    enabled = !uiState.isImporting
                ) {
                    Icon(Icons.Filled.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("JSON")
                }

                Button(
                    onClick = {
                        viewModel.importFromCsv("")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange),
                    enabled = !uiState.isImporting
                ) {
                    Icon(Icons.Filled.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CSV")
                }
            }

            if (uiState.exportComplete && uiState.exportData.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Export Data:", style = MaterialTheme.typography.labelMedium)
                        Text(
                            uiState.exportData.take(500) + if (uiState.exportData.length > 500) "..." else "",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
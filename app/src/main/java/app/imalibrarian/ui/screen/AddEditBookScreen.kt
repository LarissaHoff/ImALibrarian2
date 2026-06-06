package app.imalibrarian.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.imalibrarian.domain.model.ReadStatus
import app.imalibrarian.ui.theme.*
import app.imalibrarian.viewmodel.AddEditBookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBookScreen(
    navController: NavController,
    viewModel: AddEditBookViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    if (uiState.saveComplete) {
        LaunchedEffect(Unit) { navController.popBackStack() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "Edit Book" else "Add Book") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveBook() }, enabled = !uiState.isSaving) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.isDuplicate) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DnfOrange.copy(alpha = 0.15f))
                ) {
                    Text(
                        "A book with this ISBN already exists. Save to add another copy.",
                        modifier = Modifier.padding(12.dp),
                        color = DnfOrange
                    )
                }
            }

            Text("Bibliographic Data", style = MaterialTheme.typography.titleMedium, color = Turquoise)

            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.subtitle,
                onValueChange = { viewModel.updateSubtitle(it) },
                label = { Text("Subtitle") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.authorNames,
                onValueChange = { viewModel.updateAuthorNames(it) },
                label = { Text("Author(s)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.isbn10,
                    onValueChange = { viewModel.updateIsbn10(it) },
                    label = { Text("ISBN-10") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.isbn13,
                    onValueChange = { viewModel.updateIsbn13(it) },
                    label = { Text("ISBN-13") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = uiState.publisher,
                onValueChange = { viewModel.updatePublisher(it) },
                label = { Text("Publisher") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.pageCount,
                    onValueChange = { viewModel.updatePageCount(it) },
                    label = { Text("Pages") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.language,
                    onValueChange = { viewModel.updateLanguage(it) },
                    label = { Text("Language") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            HorizontalDivider()

            Text("Publication Data", style = MaterialTheme.typography.titleMedium, color = Turquoise)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.originalPublicationYear,
                    onValueChange = { viewModel.updateOriginalPublicationYear(it) },
                    label = { Text("Original Year") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.editionPublicationYear,
                    onValueChange = { viewModel.updateEditionPublicationYear(it) },
                    label = { Text("Edition Year") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.editionNumber,
                    onValueChange = { viewModel.updateEditionNumber(it) },
                    label = { Text("Edition #") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.printingNumber,
                    onValueChange = { viewModel.updatePrintingNumber(it) },
                    label = { Text("Printing #") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            HorizontalDivider()

            Text("Classification", style = MaterialTheme.typography.titleMedium, color = Turquoise)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.genre,
                    onValueChange = { viewModel.updateGenre(it) },
                    label = { Text("Genre") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.subgenre,
                    onValueChange = { viewModel.updateSubgenre(it) },
                    label = { Text("Subgenre") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.seriesName,
                    onValueChange = { viewModel.updateSeriesName(it) },
                    label = { Text("Series") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.seriesNumber,
                    onValueChange = { viewModel.updateSeriesNumber(it) },
                    label = { Text("Series #") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            HorizontalDivider()

            Text("Personal Library Data", style = MaterialTheme.typography.titleMedium, color = Turquoise)

            Text("Read Status:", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ReadStatus.entries.forEach { status ->
                    FilterChip(
                        selected = uiState.readStatus == status,
                        onClick = { viewModel.updateReadStatus(status) },
                        label = { Text(status.name.replace("_", " "), style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.shelfLocation,
                    onValueChange = { viewModel.updateShelfLocation(it) },
                    label = { Text("Shelf/Location") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.purchasePrice,
                    onValueChange = { viewModel.updatePurchasePrice(it) },
                    label = { Text("Price") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = uiState.sourceOfPurchase,
                onValueChange = { viewModel.updateSourceOfPurchase(it) },
                label = { Text("Source of Purchase") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.personalNotes,
                onValueChange = { viewModel.updatePersonalNotes(it) },
                label = { Text("Personal Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Favourite", modifier = Modifier.weight(1f))
                Switch(checked = uiState.isFavourite, onCheckedChange = { viewModel.updateIsFavourite(it) })
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.saveBook() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Turquoise),
                enabled = uiState.title.isNotBlank() && !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (uiState.isEditing) "Update Book" else "Save Book")
                }
            }
        }
    }
}
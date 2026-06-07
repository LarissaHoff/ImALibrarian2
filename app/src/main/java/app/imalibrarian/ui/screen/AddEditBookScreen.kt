package app.imalibrarian.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.imalibrarian.domain.model.ReadStatus
import app.imalibrarian.ui.theme.*
import app.imalibrarian.viewmodel.AddEditBookViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBookScreen(
    navController: NavController,
    viewModel: AddEditBookViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val fileName = "cover_${System.currentTimeMillis()}.jpg"
            val coversDir = File(context.filesDir, "covers")
            coversDir.mkdirs()
            val destFile = File(coversDir, fileName)
            context.contentResolver.openInputStream(selectedUri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            viewModel.updateCoverImagePath(destFile.absolutePath)
        }
    }

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
                        Icon(Icons.Filled.Save, contentDescription = "Save")
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
            CoverImagePicker(
                coverImagePath = uiState.coverImagePath,
                onPickImage = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            )

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

            if (uiState.lookupFailed) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MustardYellow.copy(alpha = 0.15f))
                ) {
                    Text(
                        "No data found for this ISBN online. Fill in the details manually.",
                        modifier = Modifier.padding(12.dp),
                        color = MustardYellow
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

            if (uiState.isLookingUp) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Turquoise)
            }

            OutlinedTextField(
                value = uiState.publisher,
                onValueChange = { viewModel.updatePublisher(it) },
                label = { Text("Publisher") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.translator,
                onValueChange = { viewModel.updateTranslator(it) },
                label = { Text("Translator") },
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
            }

            Text("Language", style = MaterialTheme.typography.titleMedium, color = Turquoise)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LanguageFlagChip("\uD83C\uDDEC\uD83C\uDDE7", "English", "en", uiState.selectedLanguageCode) {
                    viewModel.selectLanguageFlag(it)
                }
                LanguageFlagChip("\uD83C\uDDEA\uD83C\uDDF8", "Español", "es", uiState.selectedLanguageCode) {
                    viewModel.selectLanguageFlag(it)
                }
                LanguageFlagChip("\uD83C\uDDE9\uD83C\uDDEA", "Deutsch", "de", uiState.selectedLanguageCode) {
                    viewModel.selectLanguageFlag(it)
                }
                LanguageFlagChip("\uD83C\uDF10", "Other", "", uiState.selectedLanguageCode) {
                    viewModel.selectLanguageFlag(it)
                }
            }
            if (uiState.showCustomLanguageField) {
                OutlinedTextField(
                    value = uiState.customLanguageText,
                    onValueChange = { viewModel.updateCustomLanguage(it) },
                    label = { Text("Custom Language") },
                    modifier = Modifier.fillMaxWidth(),
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
                        label = { Text(status.name.replace("_", " ").replace("DID NOT FINISH", "DNF"), style = MaterialTheme.typography.labelSmall) }
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

@Composable
private fun CoverImagePicker(
    coverImagePath: String,
    onPickImage: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onPickImage() },
        contentAlignment = Alignment.Center
    ) {
        if (coverImagePath.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(coverImagePath)
                    .crossfade(true)
                    .build(),
                contentDescription = "Book cover",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Icon(
                Icons.Filled.Edit,
                contentDescription = "Change cover",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.AddAPhoto,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Tap to add cover",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LanguageFlagChip(
    flag: String,
    label: String,
    code: String,
    selectedCode: String,
    onSelect: (String) -> Unit
) {
    val isSelected = selectedCode == code
    FilterChip(
        selected = isSelected,
        onClick = { onSelect(code) },
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(flag, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(4.dp))
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        },
        modifier = Modifier.height(36.dp)
    )
}
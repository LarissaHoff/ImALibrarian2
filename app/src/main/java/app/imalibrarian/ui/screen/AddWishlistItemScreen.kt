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
import app.imalibrarian.domain.model.Priority
import app.imalibrarian.ui.theme.*
import app.imalibrarian.viewmodel.AddWishlistItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWishlistItemScreen(
    navController: NavController,
    viewModel: AddWishlistItemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.saveComplete) {
        LaunchedEffect(Unit) { navController.popBackStack() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add to Wishlist") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveItem() }, enabled = !uiState.isSaving) {
                        Icon(Icons.Filled.Save, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Coral,
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Title *") },
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

            Text("Priority:", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { p ->
                    FilterChip(
                        selected = uiState.priority == p,
                        onClick = { viewModel.updatePriority(p) },
                        label = { Text(p.name) }
                    )
                }
            }

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.saveItem() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Coral),
                enabled = uiState.title.isNotBlank() && !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add to Wishlist")
                }
            }
        }
    }
}
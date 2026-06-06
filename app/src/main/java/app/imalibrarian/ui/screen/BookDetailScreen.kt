package app.imalibrarian.ui.screen

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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.imalibrarian.domain.model.ReadStatus
import app.imalibrarian.ui.components.AtomicCard
import app.imalibrarian.ui.components.ReadStatusBadge
import app.imalibrarian.ui.components.StarburstRating
import app.imalibrarian.ui.theme.*
import app.imalibrarian.viewmodel.BookDetailViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    navController: NavController,
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val book = uiState.book
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Book") },
            text = { Text("Are you sure you want to delete \"${book?.title}\" from your library?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteBook()
                    showDeleteDialog = false
                    navController.popBackStack()
                }) { Text("Delete", color = Coral) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.title ?: "Book Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("edit_book/${book?.id}")
                    }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
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
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Turquoise)
            }
        } else if (book != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (book.coverImagePath.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(book.coverImagePath)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Cover of ${book.title}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = book.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                if (book.subtitle.isNotBlank()) {
                    Text(
                        text = book.subtitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ReadStatusBadge(status = book.readStatus)

                Spacer(modifier = Modifier.height(16.dp))

                StarburstRating(
                    rating = book.rating,
                    onRatingChanged = { viewModel.updateRating(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                AtomicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow("ISBN-10", book.isbn10)
                        DetailRow("ISBN-13", book.isbn13)
                        DetailRow("Publisher", book.publisher)
                        DetailRow("Pages", book.pageCount.toString())
                        DetailRow("Language", languageWithFlag(book.language))
                        DetailRow("First Published", book.originalPublicationYear?.toString() ?: "")
                        DetailRow("Edition Year", book.editionPublicationYear?.toString() ?: "")
                        DetailRow("Genre", book.genre)
                        DetailRow("Series", if (book.seriesName.isNotBlank()) "${book.seriesName} #${book.seriesNumber}" else "")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AtomicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Personal Library Data", style = MaterialTheme.typography.titleMedium, color = Turquoise)
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow("Shelf", book.shelfLocation)
                        DetailRow("Acquired", book.dateAcquired?.toString() ?: "")
                        DetailRow("Price", book.purchasePrice)
                        DetailRow("Source", book.sourceOfPurchase)
                        DetailRow("Favourite", if (book.isFavourite) "Yes" else "No")
                        if (book.personalNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Notes:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(book.personalNotes, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReadStatus.entries.forEach { status ->
                        FilterChip(
                            selected = book.readStatus == status,
                            onClick = { viewModel.updateReadStatus(status) },
                            label = { Text(status.name.replace("_", " "), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                IconButton(onClick = { viewModel.toggleFavourite() }) {
                    Icon(
                        imageVector = if (book.isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Toggle Favourite",
                        tint = if (book.isFavourite) Coral else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun languageWithFlag(code: String): String {
    val flag = when {
        code.equals("en", ignoreCase = true) || code.equals("eng", ignoreCase = true) -> "\uD83C\uDDEC\uD83C\uDDE7 "
        code.equals("es", ignoreCase = true) || code.equals("spa", ignoreCase = true) -> "\uD83C\uDDEA\uD83C\uDDF8 "
        code.equals("de", ignoreCase = true) || code.equals("ger", ignoreCase = true) || code.equals("deu", ignoreCase = true) -> "\uD83C\uDDE9\uD83C\uDDEA "
        else -> ""
    }
    return "$flag$code"
}

@Composable
private fun DetailRow(label: String, value: String) {
    if (value.isNotBlank()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(0.4f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(0.6f)
            )
        }
    }
}
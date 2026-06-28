package app.imalibrarian.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.imalibrarian.domain.model.Book
import app.imalibrarian.domain.model.ReadStatus
import app.imalibrarian.ui.components.AtomicCard
import app.imalibrarian.ui.components.ReadStatusBadge
import app.imalibrarian.ui.theme.*
import androidx.compose.ui.graphics.Color
import app.imalibrarian.viewmodel.LibraryViewModel
import app.imalibrarian.viewmodel.SortOrder
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val headerTitle = buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.titleLarge.fontSize, color = MaterialTheme.colorScheme.onPrimary)) {
            append("I'm a ")
        }
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, fontSize = MaterialTheme.typography.titleLarge.fontSize, color = Coral)) {
            append("Librarian")
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AtomicStarburstHeader(modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = headerTitle)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { navController.navigate("add_book") }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Book", tint = MaterialTheme.colorScheme.onPrimary)
                }

                var sortMenuExpanded by remember { mutableStateOf(false) }
                IconButton(onClick = { sortMenuExpanded = true }) {
                    Icon(Icons.Filled.SwapVert, contentDescription = "Sort", tint = MaterialTheme.colorScheme.onPrimary)
                }
                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {
                    val sortOptions = listOf(
                        SortOrder.DATE_ADDED to "Last Book Added",
                        SortOrder.TITLE to "Title (A-Z)",
                        SortOrder.AUTHOR to "Author (A-Z)",
                        SortOrder.GENRE to "Genre (A-Z)"
                    )
                    sortOptions.forEach { (order, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            leadingIcon = {
                                if (uiState.sortOrder == order) {
                                    Icon(Icons.Filled.Check, contentDescription = null)
                                }
                            },
                            onClick = {
                                viewModel.setSortOrder(order)
                                sortMenuExpanded = false
                            }
                        )
                    }
                }

                var burgerMenuExpanded by remember { mutableStateOf(false) }
                IconButton(onClick = { burgerMenuExpanded = true }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onPrimary)
                }
                DropdownMenu(
                    expanded = burgerMenuExpanded,
                    onDismissRequest = { burgerMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Import / Export") },
                        leadingIcon = { Icon(Icons.Filled.ImportExport, contentDescription = null) },
                        onClick = {
                            navController.navigate("import_export")
                            burgerMenuExpanded = false
                        }
                    )
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            LibrarySearchField(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.searchBooks(it) }
            )

            FilterChipsRow(
                selectedGenre = uiState.selectedGenre,
                selectedStatus = uiState.selectedReadStatus,
                onGenreSelected = { viewModel.filterByGenre(it) },
                onStatusSelected = { viewModel.filterByReadStatus(it) }
            )

            if (uiState.books.isEmpty()) {
                EmptyLibraryState(navController)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.books, key = { it.id }) { book ->
                        BookGridItem(
                            book = book,
                            onClick = { navController.navigate("book_detail/${book.id}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibrarySearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val searchBarColor = Color(0xFFD9D2E9)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        shape = RoundedCornerShape(28.dp),
        color = searchBarColor
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = { Text("Search your library...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = searchBarColor,
                unfocusedContainerColor = searchBarColor,
                disabledContainerColor = searchBarColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
private fun FilterChipsRow(
    selectedGenre: String?,
    selectedStatus: ReadStatus?,
    onGenreSelected: (String?) -> Unit,
    onStatusSelected: (ReadStatus?) -> Unit
) {
    val statuses = listOf<Pair<ReadStatus?, String>>(null to "All") +
        ReadStatus.entries.map { it to it.displayName }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statuses.forEach { (status, label) ->
            FilterStatusChip(
                label = label,
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) }
            )
        }
    }
}

@Composable
private fun FilterStatusChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val foreground = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = background,
        contentColor = foreground,
        border = border
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BookGridItem(
    book: Book,
    onClick: () -> Unit
) {
    AtomicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                if (book.coverImagePath.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(book.coverImagePath)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = book.title.take(1).uppercase(),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                }
            }

            Text(
                text = book.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (book.authorNames.isNotBlank()) {
                Text(
                    text = book.authorNames,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (book.originalPublicationYear != null) {
                    Text(
                        text = book.originalPublicationYear.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Spacer(modifier = Modifier.width(0.dp))
                }
                ReadStatusBadge(status = book.readStatus)
            }
        }
    }
}

@Composable
private fun EmptyLibraryState(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Your library is empty",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { navController.navigate("add_book") },
            colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Your First Book")
        }
    }
}
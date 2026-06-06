package app.imalibrarian.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.imalibrarian.domain.model.Book
import app.imalibrarian.domain.model.ReadStatus
import app.imalibrarian.ui.components.AtomicCard
import app.imalibrarian.ui.components.ReadStatusBadge
import app.imalibrarian.ui.theme.*
import app.imalibrarian.viewmodel.LibraryViewModel
import app.imalibrarian.viewmodel.SortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Library") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("add_book") }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Book")
                    }
                    IconButton(onClick = { navController.navigate("import_export") }) {
                        Icon(Icons.Filled.ImportExport, contentDescription = "Import / Export")
                    }
                    IconButton(onClick = { /* toggle sort menu */ }) {
                        Icon(Icons.Filled.Sort, contentDescription = "Sort")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.searchBooks(it) },
                onSearch = { viewModel.searchBooks(it) },
                active = false,
                onActiveChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search your library...") }
            ) {}

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
private fun FilterChipsRow(
    selectedGenre: String?,
    selectedStatus: ReadStatus?,
    onGenreSelected: (String?) -> Unit,
    onStatusSelected: (ReadStatus?) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = 0,
        modifier = Modifier.fillMaxWidth(),
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        val statuses = listOf(null to "All") + ReadStatus.entries.map { it to it.name }
        statuses.forEach { (status, label) ->
            Tab(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) },
                text = { Text(label.replace("_", " "), style = MaterialTheme.typography.labelSmall) }
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
                    Text(
                        text = book.title.take(1),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
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

            Text(
                text = book.publisher,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            ReadStatusBadge(status = book.readStatus)
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
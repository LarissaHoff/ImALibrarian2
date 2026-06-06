package app.imalibrarian.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.imalibrarian.ui.components.AtomicCard
import app.imalibrarian.ui.theme.*
import app.imalibrarian.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Books") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = {
                    viewModel.updateQuery(searchQuery)
                    viewModel.search()
                },
                active = false,
                onActiveChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by title, author, or ISBN...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
            ) {}

            TabRow(selectedTabIndex = if (uiState.searchOnline) 1 else 0) {
                Tab(
                    selected = !uiState.searchOnline,
                    onClick = {
                        viewModel.updateQuery(searchQuery)
                        viewModel.search()
                    },
                    text = { Text("Local") }
                )
                Tab(
                    selected = uiState.searchOnline,
                    onClick = { viewModel.searchOnline() },
                    text = { Text("Online") }
                )
            }

            if (uiState.isSearching) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Turquoise)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.localResults.isNotEmpty()) {
                        item {
                            Text("Your Library", style = MaterialTheme.typography.titleMedium, color = Turquoise)
                        }
                        items(uiState.localResults) { book ->
                            AtomicCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { navController.navigate("book_detail/${book.id}") }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(book.title, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        book.publisher,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    if (uiState.onlineResults.isNotEmpty()) {
                        item {
                            Text("Online Results", style = MaterialTheme.typography.titleMedium, color = Coral)
                        }
                        items(uiState.onlineResults) { result ->
                            AtomicCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { navController.navigate("add_book") }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(result.title, style = MaterialTheme.typography.titleSmall)
                                    if (result.authors.isNotEmpty()) {
                                        Text(
                                            result.authors.joinToString(", "),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        "${result.publisher} · ${result.originalPublicationYear ?: "Unknown year"}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }

                    if (uiState.localResults.isEmpty() && uiState.onlineResults.isEmpty() && !uiState.isSearching) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No results found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
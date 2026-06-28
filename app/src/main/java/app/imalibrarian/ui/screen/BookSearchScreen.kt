package app.imalibrarian.ui.screen

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.imalibrarian.ui.components.AtomicCard
import app.imalibrarian.ui.theme.*
import app.imalibrarian.viewmodel.SearchSuggestion
import app.imalibrarian.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var userDismissed by remember { mutableStateOf(false) }
    val onlineMode = uiState.searchOnline

    val showDropdown = !onlineMode &&
        uiState.query.isNotBlank() &&
        uiState.suggestions.isNotEmpty() &&
        !userDismissed

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
            ExposedDropdownMenuBox(
                expanded = showDropdown,
                onExpandedChange = { wantsOpen ->
                    if (!wantsOpen) userDismissed = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = {
                        userDismissed = false
                        viewModel.updateQuery(it)
                    },
                    singleLine = true,
                    placeholder = { Text("Search by title, author, or ISBN...") },
                    trailingIcon = {
                        Row {
                            IconButton(onClick = {
                                userDismissed = true
                                viewModel.submitSearch()
                            }) {
                                Icon(Icons.Filled.Search, contentDescription = "Search")
                            }
                            if (uiState.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSearch() }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                                }
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        userDismissed = true
                        viewModel.submitSearch()
                    }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                )

                ExposedDropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { userDismissed = true }
                ) {
                    val titles = uiState.suggestions.filterIsInstance<SearchSuggestion.Title>()
                    val authors = uiState.suggestions.filterIsInstance<SearchSuggestion.Author>()

                    if (titles.isNotEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Titles",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Turquoise
                                )
                            },
                            onClick = {},
                            enabled = false
                        )
                        titles.forEach { suggestion ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            suggestion.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (suggestion.authors.isNotBlank()) {
                                            Text(
                                                suggestion.authors,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    userDismissed = true
                                    navController.navigate("book_detail/${suggestion.id}")
                                }
                            )
                        }
                    }

                    if (authors.isNotEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Authors",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Coral
                                )
                            },
                            onClick = {},
                            enabled = false
                        )
                        authors.forEach { suggestion ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        suggestion.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                onClick = {
                                    viewModel.updateQuery(suggestion.name)
                                    userDismissed = true
                                    viewModel.submitSearch()
                                }
                            )
                        }
                    }
                }
            }

            TabRow(selectedTabIndex = if (onlineMode) 1 else 0) {
                Tab(
                    selected = !onlineMode,
                    onClick = { viewModel.showLocal() },
                    text = { Text("Local") }
                )
                Tab(
                    selected = onlineMode,
                    onClick = { viewModel.showOnline() },
                    text = { Text("Online") }
                )
            }

            val showOnlineSpinner = onlineMode && uiState.isSearchingOnline

            if (showOnlineSpinner) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Turquoise)
                }
            } else {
                if (!onlineMode && uiState.isSearching) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Turquoise
                    )
                }
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!onlineMode && uiState.localResults.isNotEmpty()) {
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
                                    if (book.authorNames.isNotBlank()) {
                                        Text(
                                            book.authorNames,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
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

                    if (onlineMode && uiState.onlineResults.isNotEmpty()) {
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

                    if (onlineMode && uiState.onlineError != null) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Online search failed: ${uiState.onlineError}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.retryOnline() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
                                ) {
                                    Icon(Icons.Filled.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Retry")
                                }
                            }
                        }
                    }

                    if (uiState.query.isNotBlank() &&
                        !uiState.isSearching &&
                        !uiState.isSearchingOnline &&
                        ((!onlineMode && uiState.localResults.isEmpty()) ||
                            (onlineMode && uiState.onlineResults.isEmpty() && uiState.onlineError == null))
                    ) {
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
            if (!uiState.isSearching &&
                !uiState.isSearchingOnline &&
                uiState.query.isNotBlank() &&
                uiState.localResults.isEmpty() &&
                uiState.onlineResults.isEmpty() &&
                uiState.onlineError == null
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Tap the search icon or press Enter to search",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

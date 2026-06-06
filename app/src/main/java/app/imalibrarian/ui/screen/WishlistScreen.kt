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
import app.imalibrarian.domain.model.Priority
import app.imalibrarian.domain.model.WishlistItem
import app.imalibrarian.ui.components.AtomicCard
import app.imalibrarian.ui.components.PriorityBadge
import app.imalibrarian.ui.theme.*
import app.imalibrarian.viewmodel.WishlistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    navController: NavController,
    viewModel: WishlistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wishlist") },
                actions = {
                    IconButton(onClick = { navController.navigate("add_wishlist") }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add to Wishlist")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Coral,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (uiState.items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Your wishlist is empty", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { navController.navigate("add_wishlist") },
                        colors = ButtonDefaults.buttonColors(containerColor = Coral)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add to Wishlist")
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedPriority == null,
                        onClick = { viewModel.filterByPriority(null) },
                        label = { Text("All") }
                    )
                    Priority.entries.forEach { priority ->
                        FilterChip(
                            selected = uiState.selectedPriority == priority,
                            onClick = { viewModel.filterByPriority(priority) },
                            label = { Text(priority.name) }
                        )
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        WishlistItemCard(
                            item = item,
                            onMoveToLibrary = { viewModel.moveToLibrary(item) },
                            onDelete = { viewModel.deleteItem(item) },
                            onClick = { navController.navigate("wishlist_detail/${item.id}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WishlistItemCard(
    item: WishlistItem,
    onMoveToLibrary: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    AtomicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (item.authorNames.isNotBlank()) {
                    Text(item.authorNames, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                PriorityBadge(priority = item.priority)
            }
            Column {
                IconButton(onClick = onMoveToLibrary) {
                    Icon(
                        Icons.Filled.LibraryAdd,
                        contentDescription = "Move to Library",
                        tint = Turquoise
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Coral.copy(alpha = 0.7f))
                }
            }
        }
    }
}
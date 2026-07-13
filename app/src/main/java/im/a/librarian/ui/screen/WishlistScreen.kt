package im.a.librarian.ui.screen

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import im.a.librarian.domain.model.Priority
import im.a.librarian.domain.model.WishlistItem
import im.a.librarian.ui.components.AtomicCard
import im.a.librarian.ui.components.PriorityBadge
import im.a.librarian.ui.theme.*
import im.a.librarian.viewmodel.WishlistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    navController: NavController,
    viewModel: WishlistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showShareMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wishlist") },
                actions = {
                    IconButton(onClick = { navController.navigate("add_wishlist") }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add to Wishlist")
                    }
                    IconButton(onClick = { showShareMenu = true }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share Wishlist")
                    }
                    DropdownMenu(
                        expanded = showShareMenu,
                        onDismissRequest = { showShareMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Share All") },
                            onClick = {
                                showShareMenu = false
                                scope.launch {
                                    val shareText = viewModel.getShareText()
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Wishlist"))
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share High Priority") },
                            onClick = {
                                showShareMenu = false
                                scope.launch {
                                    val shareText = viewModel.getShareText(Priority.HIGH)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share High Priority"))
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share Medium Priority") },
                            onClick = {
                                showShareMenu = false
                                scope.launch {
                                    val shareText = viewModel.getShareText(Priority.MEDIUM)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Medium Priority"))
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share Low Priority") },
                            onClick = {
                                showShareMenu = false
                                scope.launch {
                                    val shareText = viewModel.getShareText(Priority.LOW)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Low Priority"))
                                }
                            }
                        )
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
                            onUpdatePriority = { item, priority -> viewModel.updatePriority(item, priority) }
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
    onUpdatePriority: (WishlistItem, Priority) -> Unit
) {
    var showPriorityDialog by remember { mutableStateOf(false) }

    AtomicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { showPriorityDialog = true }
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

    if (showPriorityDialog) {
        AlertDialog(
            onDismissRequest = { showPriorityDialog = false },
            title = { Text("Set Priority") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Priority.entries.forEach { priority ->
                        FilterChip(
                            selected = item.priority == priority,
                            onClick = {
                                onUpdatePriority(item, priority)
                                showPriorityDialog = false
                            },
                            label = { Text(priority.name) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPriorityDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
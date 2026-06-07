package app.imalibrarian.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.imalibrarian.ui.components.AtomicCard
import app.imalibrarian.ui.components.OrbitalProgress
import app.imalibrarian.ui.components.RetroBarChart
import app.imalibrarian.ui.theme.*
import app.imalibrarian.viewmodel.StatisticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val stats = uiState.statistics

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Turquoise)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Total Books", stats.totalBooks.toString(), Turquoise, Modifier.weight(1f))
                    StatCard("Wishlist", stats.totalWishlistItems.toString(), Coral, Modifier.weight(1f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Read", stats.booksRead.toString(), SuccessGreen, Modifier.weight(1f))
                    StatCard("Reading", stats.booksCurrentlyReading.toString(), ReadingBlue, Modifier.weight(1f))
                    StatCard("Unread", stats.booksUnread.toString(), MustardYellow, Modifier.weight(1f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("DNF", stats.booksDidNotFinish.toString(), DnfOrange, Modifier.weight(1f))
                    StatCard("Progress", "${(stats.readingProgress * 100).toInt()}%", Teal, Modifier.weight(1f))
                }

                AtomicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Reading Progress", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OrbitalProgress(
                                progress = if (stats.totalBooks > 0) stats.booksRead.toFloat() / stats.totalBooks else 0f,
                                color = SuccessGreen,
                                label = "Finished"
                            )
                            OrbitalProgress(
                                progress = stats.readingProgress,
                                color = Turquoise,
                                label = "Overall"
                            )
                        }
                    }
                }

                if (stats.booksByGenre.isNotEmpty()) {
                    AtomicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Books by Genre", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                            RetroBarChart(data = stats.booksByGenre)
                        }
                    }
                }

                if (stats.mostCommonAuthors.isNotEmpty()) {
                    AtomicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Most Common Authors", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            stats.mostCommonAuthors.take(5).forEach { authorCount ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(authorCount.author, style = MaterialTheme.typography.bodyMedium)
                                    Text("${authorCount.count} books", style = MaterialTheme.typography.labelMedium, color = Turquoise)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    AtomicCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineLarge, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
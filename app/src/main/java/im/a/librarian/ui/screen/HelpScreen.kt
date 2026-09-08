package im.a.librarian.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import im.a.librarian.ui.components.AtomicCard
import im.a.librarian.ui.navigation.Screen
import im.a.librarian.ui.theme.Coral
import im.a.librarian.ui.theme.Turquoise

private data class FaqEntry(
    val question: String,
    val answer: String
)

private val faqEntries = listOf(
    FaqEntry(
        question = "How do I add a book?",
        answer = "Tap the + icon at the top of the library and type the ISBN, or tap the scan button in the middle of the bottom bar to read the barcode on the back cover. The title, author, cover and publication details are looked up for you automatically. You can also use the Search tab to find a book by title, author or ISBN and save online results to your wishlist."
    ),
    FaqEntry(
        question = "How do I edit or delete a book?",
        answer = "Tap any book in your library to open its detail page. The pencil icon opens the editor where you can change the title, authors, status, rating, notes and cover photo. The bin icon deletes the book — it always asks for confirmation first."
    ),
    FaqEntry(
        question = "What do the coloured badges mean?",
        answer = "Each book carries a reading status: Unread (grey outline), Currently Reading (blue), Finished (green) and Did Not Finish (orange). You can change the status and add a star rating when editing a book."
    ),
    FaqEntry(
        question = "What is the wishlist for?",
        answer = "The star tab in the bottom bar keeps a list of books you'd like to add to your collection one day, each with a High, Medium or Low priority. When you finally get the book you can move it into your library with all its details intact."
    ),
    FaqEntry(
        question = "How do I back up or move my library?",
        answer = "Open the menu at the top of the library and choose Import / Export. Export writes your whole collection to a JSON or CSV file you can save anywhere; Import reads a file back in. This is also the easiest way to move your library to a new phone."
    ),
    FaqEntry(
        question = "How do I see my reading statistics?",
        answer = "The chart tab in the bottom bar opens the Statistics dashboard with your reading progress, books by genre and your most common authors."
    ),
    FaqEntry(
        question = "Does the app work offline?",
        answer = "Yes. Your catalogue lives entirely on your device — no account needed. An internet connection is only required to look up book details from Google Books and Open Library when adding or searching by ISBN."
    )
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(navController: NavController) {
    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & FAQ") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
            AtomicCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(Screen.Welcome.route) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = null,
                        tint = Coral,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "New around here?",
                            style = MaterialTheme.typography.titleSmall,
                            color = Turquoise
                        )
                        Text(
                            text = "Take the introduction tour again",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = "Frequently Asked Questions",
                style = MaterialTheme.typography.titleMedium,
                color = Turquoise
            )

            faqEntries.forEachIndexed { index, entry ->
                FaqCard(
                    entry = entry,
                    expanded = expandedIndex == index,
                    onClick = {
                        expandedIndex = if (expandedIndex == index) null else index
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FaqCard(
    entry: FaqEntry,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.question,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = entry.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

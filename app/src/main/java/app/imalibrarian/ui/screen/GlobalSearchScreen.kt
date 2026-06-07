package app.imalibrarian.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(navController: NavController) {
    var query by remember { mutableStateOf("") }
    // This screen delegates to BookSearchScreen; included for navigation completeness
    // The actual search logic is in BookSearchScreen
    Text("Global Search - use the Search button on Home or Library")
}
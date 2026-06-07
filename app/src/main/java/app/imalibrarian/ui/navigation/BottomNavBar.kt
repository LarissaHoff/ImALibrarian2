package app.imalibrarian.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.imalibrarian.ui.theme.Cream
import app.imalibrarian.ui.theme.Lavender

private data class Tab(
    val route: String,
    val icon: @Composable () -> Unit,
    val contentDescription: String
)

private val tabs = listOf(
    Tab(Screen.Library.route, { Icon(Icons.Filled.MenuBook, contentDescription = null) }, "Library"),
    Tab(Screen.BookSearch.route, { Icon(Icons.Filled.Search, contentDescription = null) }, "Search"),
    Tab(Screen.Wishlist.route, { Icon(Icons.Filled.Star, contentDescription = null) }, "Wishlist"),
    Tab(Screen.Statistics.route, { Icon(Icons.Filled.BarChart, contentDescription = null) }, "Statistics"),
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
    onScanClick: () -> Unit
) {
    Box {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.primary,
            tonalElevation = 8.dp
        ) {
            tabs.forEach { tab ->
                NavigationBarItem(
                    selected = currentRoute == tab.route,
                    onClick = { onTabSelected(tab.route) },
                    icon = tab.icon,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                        indicatorColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                    )
                )
            }
        }

        FloatingActionButton(
            onClick = onScanClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
                .size(48.dp),
            containerColor = Lavender,
            contentColor = Cream,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp
            )
        ) {
            Icon(
                Icons.Filled.QrCodeScanner,
                contentDescription = "Scan",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

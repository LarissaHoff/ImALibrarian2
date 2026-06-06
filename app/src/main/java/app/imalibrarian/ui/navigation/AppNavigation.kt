package app.imalibrarian.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import app.imalibrarian.ui.screen.*

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.Library.route) {
            LibraryScreen(navController = navController)
        }

        composable(
            route = Screen.BookDetail.route,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) {
            BookDetailScreen(navController = navController)
        }

        composable(Screen.AddBook.route) {
            AddEditBookScreen(navController = navController)
        }

        composable(
            route = Screen.EditBook.route,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) {
            AddEditBookScreen(navController = navController)
        }

        composable(Screen.ScanBarcode.route) {
            BarcodeScannerScreen(navController = navController)
        }

        composable(Screen.ScanCover.route) {
            CoverScannerScreen(navController = navController)
        }

        composable(Screen.BookSearch.route) {
            BookSearchScreen(navController = navController)
        }

        composable(Screen.Wishlist.route) {
            WishlistScreen(navController = navController)
        }

        composable(Screen.AddWishlistItem.route) {
            AddWishlistItemScreen(navController = navController)
        }

        composable(Screen.GlobalSearch.route) {
            BookSearchScreen(navController = navController)
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(navController = navController)
        }

        composable(Screen.ImportExport.route) {
            ImportExportScreen(navController = navController)
        }
    }
}
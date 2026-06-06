package app.imalibrarian.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Library : Screen("library")
    data object BookDetail : Screen("book_detail/{bookId}") {
        fun createRoute(bookId: Long) = "book_detail/$bookId"
    }
    data object AddBook : Screen("add_book")
    data object EditBook : Screen("edit_book/{bookId}") {
        fun createRoute(bookId: Long) = "edit_book/$bookId"
    }
    data object ScanBarcode : Screen("scan_barcode")
    data object ScanCover : Screen("scan_cover")
    data object BookSearch : Screen("search")
    data object Wishlist : Screen("wishlist")
    data object AddWishlistItem : Screen("add_wishlist")
    data object GlobalSearch : Screen("global_search")
    data object Statistics : Screen("statistics")
    data object ImportExport : Screen("import_export")
}
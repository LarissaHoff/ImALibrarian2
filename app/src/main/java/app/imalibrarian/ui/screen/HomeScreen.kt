package app.imalibrarian.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.imalibrarian.ui.components.AtomicCard
import app.imalibrarian.ui.theme.*

@Composable
fun HomeScreen(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            AtomicStarburstHeader()

            Spacer(modifier = Modifier.height(8.dp))

            val titleText = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.displayMedium.fontSize, color = MaterialTheme.colorScheme.primary)) {
                    append("I'm a ")
                }
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, fontSize = MaterialTheme.typography.displayMedium.fontSize, color = Coral)) {
                    append("Librarian")
                }
            }
            Text(text = titleText)

            Text(
                text = "Your Personal Book Catalogue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeButton(
                    icon = Icons.Filled.MenuBook,
                    label = "My Library",
                    onClick = { navController.navigate("library") },
                    modifier = Modifier.weight(1f),
                    color = Turquoise
                )
                HomeButton(
                    icon = Icons.Filled.FavoriteBorder,
                    label = "Wishlist",
                    onClick = { navController.navigate("wishlist") },
                    modifier = Modifier.weight(1f),
                    color = Coral
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeButton(
                    icon = Icons.Filled.QrCodeScanner,
                    label = "Scan Book",
                    onClick = { navController.navigate("scan_barcode") },
                    modifier = Modifier.weight(1f),
                    color = Orange
                )
                HomeButton(
                    icon = Icons.Filled.Search,
                    label = "Search",
                    onClick = { navController.navigate("search") },
                    modifier = Modifier.weight(1f),
                    color = MustardYellow
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HomeButton(
                icon = Icons.Filled.BarChart,
                label = "Statistics",
                onClick = { navController.navigate("statistics") },
                modifier = Modifier.fillMaxWidth(),
                color = Teal
            )

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = { navController.navigate("import_export") }) {
                Icon(Icons.Filled.ImportExport, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Import / Export")
            }
        }
    }
}

@Composable
fun AtomicStarburstHeader(modifier: Modifier = Modifier.size(80.dp)) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val outerRadius = size.width / 2
        val innerRadius = outerRadius * 0.35f
        val points = 12

        val path = Path().apply {
            for (i in 0 until points * 2) {
                val angle = Math.toRadians((i * 360.0 / (points * 2) - 90.0))
                val radius = if (i % 2 == 0) outerRadius else innerRadius
                val x = center.x + (radius * kotlin.math.cos(angle)).toFloat()
                val y = center.y + (radius * kotlin.math.sin(angle)).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }

        drawPath(path = path, color = StarburstGold)
        drawCircle(color = Turquoise, radius = innerRadius * 0.6f, center = center)
    }
}

@Composable
private fun HomeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Turquoise
) {
    AtomicCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
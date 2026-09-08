package im.a.librarian.ui.screen

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import im.a.librarian.ui.navigation.Screen
import im.a.librarian.ui.theme.Coral
import im.a.librarian.ui.theme.StarburstGold
import im.a.librarian.ui.theme.Teal
import im.a.librarian.ui.theme.Turquoise
import kotlinx.coroutines.launch

object OnboardingPrefs {
    const val PREFS_NAME = "imalibrarian_prefs"
    const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

    fun isCompleted(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun markCompleted(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, true)
            .apply()
    }
}

private data class OnboardingPage(
    val icon: ImageVector,
    val iconColor: Color,
    val title: String,
    val body: String
)

private val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.AutoMirrored.Filled.MenuBook,
        iconColor = Turquoise,
        title = "Welcome!",
        body = "This is your personal library card for the books you own, read and love. Everything is stored right here on your device."
    ),
    OnboardingPage(
        icon = Icons.Filled.QrCodeScanner,
        iconColor = Coral,
        title = "Add Books in a Snap",
        body = "Tap + in the library to type an ISBN, or use the scan button in the middle of the bottom bar to read a barcode. Title, author and cover are looked up for you automatically."
    ),
    OnboardingPage(
        icon = Icons.Filled.Star,
        iconColor = StarburstGold,
        title = "Keep a Wishlist",
        body = "The star tab holds books you'd love to own one day, each with its own priority. Search online in the Search tab and save your finds there."
    ),
    OnboardingPage(
        icon = Icons.Filled.BarChart,
        iconColor = Teal,
        title = "Track & Back Up",
        body = "The chart tab shows your reading progress, genres and most common authors. Use the menu at the top of the library to export or import your collection as JSON or CSV."
    )
)

@Composable
fun WelcomeScreen(navController: NavController) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    val finishOnboarding: () -> Unit = {
        OnboardingPrefs.markCompleted(context)
        val cameFromHelp = navController.previousBackStackEntry?.destination?.route == Screen.Help.route
        if (cameFromHelp) {
            navController.popBackStack()
        } else {
            navController.navigate(Screen.Library.route) {
                popUpTo(Screen.Welcome.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        AppNameHeader()

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            OnboardingPageContent(onboardingPages[page])
        }

        PageIndicator(
            pageCount = onboardingPages.size,
            currentPage = pagerState.currentPage
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = finishOnboarding) {
                Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            val isLastPage = pagerState.currentPage == onboardingPages.lastIndex
            Button(
                onClick = {
                    if (isLastPage) {
                        finishOnboarding()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
            ) {
                Text(if (isLastPage) "Get Started" else "Next")
            }
        }
    }
}

@Composable
private fun AppNameHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StarburstDecoration(modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.headlineMedium.fontSize
                    )
                ) {
                    append("I'm a ")
                }
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                        color = Coral
                    )
                ) {
                    append("Librarian")
                }
            }
        )
    }
}

@Composable
private fun StarburstDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
        val outerRadius = size.width / 2
        val innerRadius = outerRadius * 0.4f
        val points = 8

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
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = page.iconColor.copy(alpha = 0.15f),
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = page.iconColor,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall,
            color = Teal,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            Surface(
                shape = RoundedCornerShape(50),
                color = if (selected) Turquoise else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = if (selected) 24.dp else 8.dp, height = 8.dp)
            ) {}
        }
    }
}

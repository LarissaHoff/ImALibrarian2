package app.imalibrarian.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.imalibrarian.ui.theme.*

@Composable
fun AtomicCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        content = content
    )
}

@Composable
fun StarburstRating(
    rating: Int,
    maxRating: Int = 5,
    starSize: Dp = 32.dp,
    onRatingChanged: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxRating) {
            val isFilled = i <= rating
            val starColor = when {
                isFilled -> StarburstGold
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            Canvas(
                modifier = Modifier
                    .size(starSize)
                    .then(
                        if (onRatingChanged != null) {
                            Modifier.clickable { onRatingChanged(i) }
                        } else Modifier
                    )
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val outerRadius = size.width / 2
                val innerRadius = outerRadius * 0.4f
                val points = 5

                val path = Path().apply {
                    for (j in 0 until points * 2) {
                        val angle = Math.toRadians((j * 360.0 / (points * 2) - 90.0))
                        val radius = if (j % 2 == 0) outerRadius else innerRadius
                        val x = center.x + (radius * kotlin.math.cos(angle)).toFloat()
                        val y = center.y + (radius * kotlin.math.sin(angle)).toFloat()
                        if (j == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }

                drawPath(
                    path = path,
                    color = starColor
                )
            }
        }
    }
}

@Composable
fun OrbitalProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = Turquoise,
    trackColor: Color = MaterialTheme.colorScheme.outlineVariant,
    strokeWidth: Dp = 8.dp,
    size: Dp = 120.dp,
    label: String? = null
) {
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            drawCircle(
                color = trackColor,
                radius = (size.toPx() - stroke) / 2,
                style = Stroke(width = stroke)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AtomicHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(modifier = Modifier.size(60.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val outerRadius = size.width / 2
            val innerRadius = outerRadius * 0.3f
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

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )

        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AtomicChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        border = if (!selected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun RetroBarChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier,
    barColor: Color = Turquoise,
    labelColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val textMeasurer = rememberTextMeasurer()
    val maxValue = data.values.maxOrNull() ?: 1

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val barWidth = size.width / (data.size * 1.5f)
            val spacing = barWidth * 0.5f
            data.entries.forEachIndexed { index, entry ->
                val barHeight = (entry.value.toFloat() / maxValue) * size.height * 0.8f
                val x = spacing + index * (barWidth + spacing)
                val y = size.height - barHeight

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 4)
                )

                val labelLayout = textMeasurer.measure(
                    entry.key.take(8),
                    TextStyle(fontSize = 10.sp, color = labelColor)
                )
                drawText(
                    labelLayout,
                    topLeft = Offset(x - 4, size.height + 2.dp.toPx())
                )

                val valueLayout = textMeasurer.measure(
                    entry.value.toString(),
                    TextStyle(fontSize = 10.sp, color = labelColor)
                )
                drawText(
                    valueLayout,
                    topLeft = Offset(x + barWidth / 4, y - 16.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun OrbitRing(
    segments: List<Pair<Float, Color>>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 24.dp
) {
    val textMeasurer = rememberTextMeasurer()

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f
            val stroke = strokeWidth.toPx()
            val radius = (size.toPx() - stroke) / 2

            segments.forEach { (fraction, color) ->
                val sweepAngle = fraction * 360f
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Butt),
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(size.toPx() - stroke, size.toPx() - stroke)
                )
                startAngle += sweepAngle
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            labels.forEachIndexed { index, label ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Canvas(modifier = Modifier.size(8.dp)) {
                        drawCircle(color = segments.getOrElse(index) { Pair(0f, Color.Gray) }.second)
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ReadStatusBadge(
    status: app.imalibrarian.domain.model.ReadStatus,
    modifier: Modifier = Modifier
) {
    val (label, color) = when (status) {
        app.imalibrarian.domain.model.ReadStatus.UNREAD -> "Unread" to MaterialTheme.colorScheme.outline
        app.imalibrarian.domain.model.ReadStatus.CURRENTLY_READING -> "Reading" to ReadingBlue
        app.imalibrarian.domain.model.ReadStatus.FINISHED -> "Finished" to SuccessGreen
        app.imalibrarian.domain.model.ReadStatus.DID_NOT_FINISH -> "DNF" to DnfOrange
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        contentColor = color
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun PriorityBadge(
    priority: app.imalibrarian.domain.model.Priority,
    modifier: Modifier = Modifier
) {
    val (label, color) = when (priority) {
        app.imalibrarian.domain.model.Priority.HIGH -> "High" to Coral
        app.imalibrarian.domain.model.Priority.MEDIUM -> "Medium" to Orange
        app.imalibrarian.domain.model.Priority.LOW -> "Low" to Teal
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        contentColor = color
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
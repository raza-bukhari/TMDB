package com.example.tmdb.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tmdb.core.designsystem.theme.ScoreAmber
import com.example.tmdb.core.designsystem.theme.ScoreGreen
import com.example.tmdb.core.designsystem.theme.ScoreOrange
import com.example.tmdb.core.designsystem.theme.ScoreRed

/** TMDB vote_average (0..10) → traffic-light score colour. */
fun scoreColor(rating: Double): Color = when {
    rating >= 7.5 -> ScoreGreen
    rating >= 6.0 -> ScoreAmber
    rating >= 4.0 -> ScoreOrange
    else -> ScoreRed
}

private val PillAmber = Color(0xFFF5C518) // IMDb-style gold star

/** Compact score pill for poster cards: gold star + number on a dark scrim. */
@Composable
fun RatingBadge(
    rating: Double,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color = Color.Black.copy(alpha = 0.62f), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = PillAmber,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = " %.1f".format(rating),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}

/**
 * Circular percentage ring (TMDB-style) for the detail page: an arc proportional
 * to score/10, colour-coded, with the score centred.
 */
@Composable
fun RatingRing(
    rating: Double,
    modifier: Modifier = Modifier,
    diameter: androidx.compose.ui.unit.Dp = 56.dp,
) {
    val color = scoreColor(rating)
    val track = color.copy(alpha = 0.22f)
    val sweep = (rating.coerceIn(0.0, 10.0) / 10.0 * 360.0).toFloat()
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(diameter)
            .background(color = Color.Black.copy(alpha = 0.55f), shape = androidx.compose.foundation.shape.CircleShape),
    ) {
        Canvas(modifier = Modifier.size(diameter - 8.dp)) {
            val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            val inset = stroke.width / 2
            val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
            drawArc(color = track, startAngle = -90f, sweepAngle = 360f, useCenter = false, topLeft = androidx.compose.ui.geometry.Offset(inset, inset), size = arcSize, style = stroke)
            drawArc(color = color, startAngle = -90f, sweepAngle = sweep, useCenter = false, topLeft = androidx.compose.ui.geometry.Offset(inset, inset), size = arcSize, style = stroke)
        }
        Text(
            text = "%.1f".format(rating),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
    }
}

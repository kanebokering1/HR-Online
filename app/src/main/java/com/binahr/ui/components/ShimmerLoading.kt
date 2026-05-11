package com.binahr.ui.components


import com.binahr.BuildConfig
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp,
    cornerRadius: Dp = 8.dp,
) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_translate",
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim.value - 200f, 0f),
        end = Offset(translateAnim.value, 0f),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(brush, RoundedCornerShape(cornerRadius))
    )
}

@Composable
fun ShimmerCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ShimmerBox(height = 16.dp, modifier = Modifier.fillMaxWidth(0.6f))
        ShimmerBox(height = 12.dp, modifier = Modifier.fillMaxWidth(0.9f))
        ShimmerBox(height = 12.dp, modifier = Modifier.fillMaxWidth(0.7f))
    }
}























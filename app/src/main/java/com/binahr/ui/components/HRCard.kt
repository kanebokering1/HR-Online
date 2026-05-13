package com.binahr.ui.components


import com.binahr.BuildConfig
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.clickable

enum class CardVariant { Default, Elevated, Outline }

@Composable
fun HRCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 2.dp,
    gradientBorder: Brush? = null,
    variant: CardVariant = CardVariant.Default,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val clickMod = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    if (gradientBorder != null) {
        Box(
            modifier = modifier
                .shadow(elevation, shape)
                .clip(shape)
                .background(gradientBorder)
                .padding(2.dp)
                .then(clickMod)
        ) {
            Surface(
                shape = RoundedCornerShape(cornerRadius - 2.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(content = content)
            }
        }
    } else {
        val cardElevation = when (variant) {
            CardVariant.Elevated -> elevation + 2.dp
            else -> elevation
        }
        Card(
            modifier = modifier.shadow(cardElevation, shape).then(clickMod),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            content()
        }
    }
}























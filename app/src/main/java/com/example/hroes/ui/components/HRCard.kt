package com.example.hroes.ui.components


import com.example.hroes.BuildConfig
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

@Composable
fun HRCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 2.dp,
    gradientBorder: Brush? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    if (gradientBorder != null) {
        Box(
            modifier = modifier
                .shadow(elevation, shape)
                .clip(shape)
                .background(gradientBorder)
                .padding(2.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(cornerRadius - 2.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(content = content)
            }
        }
    } else {
        Card(
            modifier = modifier.shadow(elevation, shape),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            content()
        }
    }
}























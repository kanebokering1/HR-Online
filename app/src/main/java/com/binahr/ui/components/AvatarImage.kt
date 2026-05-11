package com.binahr.ui.components


import com.binahr.BuildConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.binahr.ui.theme.*

@Composable
fun AvatarImage(
    imageUrl: String? = null,
    initials: String = "AA",
    size: Dp = 48.dp,
    backgroundColor: Color = GreenPrimary,
    modifier: Modifier = Modifier,
) {
    if (!imageUrl.isNullOrEmpty()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Avatar",
            modifier = modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials.take(2).uppercase(),
                color = Color.White,
                fontSize = (size.value / 2.5).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PlusJakartaSans,
            )
        }
    }
}























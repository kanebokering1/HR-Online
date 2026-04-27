package com.example.hronline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hronline.ui.theme.*

enum class BadgeType {
    SUCCESS, WARNING, ERROR, INFO, PENDING, NEUTRAL
}

@Composable
fun StatusBadge(
    text: String,
    type: BadgeType = BadgeType.NEUTRAL,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor) = when (type) {
        BadgeType.SUCCESS -> Green50 to GreenDark
        BadgeType.WARNING -> AccentOrangeLight to AccentOrangeDark
        BadgeType.ERROR -> AccentRedLight to AccentRedDark
        BadgeType.INFO -> AccentBlueLight to AccentBlueDark
        BadgeType.PENDING -> AccentAmberLight to AccentOrangeDark
        BadgeType.NEUTRAL -> SurfaceLight to TextSecondary
    }

    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = textColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = PlusJakartaSans,
    )
}

package com.binahr.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.binahr.ui.theme.OrangePrimary

/**
 * Standard list row used across all list screens (leave, overtime, reimbursements, etc.).
 *
 * @param icon       Optional leading icon displayed in a 40dp rounded container.
 * @param title      Primary text.
 * @param subtitle   Secondary/meta text below title.
 * @param trailing   Optional trailing composable (badge, chevron, amount, etc.).
 * @param onClick    Row tap handler.
 */
@Composable
fun HRListItem(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: androidx.compose.ui.graphics.Color = OrangePrimary,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val clickMod = if (onClick != null) modifier.clickable(onClick = onClick) else modifier
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = clickMod
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        if (icon != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

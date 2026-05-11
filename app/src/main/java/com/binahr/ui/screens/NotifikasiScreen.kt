package com.binahr.ui.screens


import com.binahr.BuildConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.binahr.ui.components.*
import com.binahr.ui.theme.*
import com.binahr.ui.viewmodel.NotifikasiViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun NotifikasiScreen(onBack: () -> Unit, vm: NotifikasiViewModel = viewModel()) {
    val notifications by vm.notifications.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Notifikasi", onBack = onBack)

        // Unread count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${notifications.count { !it.isRead }} belum dibaca",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            TextButton(onClick = { vm.markAllRead() }) {
                Text("Tandai Semua Dibaca", color = GreenPrimary, style = MaterialTheme.typography.labelMedium)
            }
        }

        if (isLoading && notifications.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = GreenPrimary) }
        } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(notifications) { item ->
                val bgColor = if (!item.isRead) SurfaceGreenTint else Color.Transparent
                val iconColor = GreenPrimary
                val iconBgColor = Green50
                val icon = Icons.Filled.Notifications

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .clickable { vm.markRead(item.id) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(iconBgColor, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                            )
                            Text(item.createdAt ?: "", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            item.message ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (!item.isRead) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(GreenPrimary, CircleShape)
                                .align(Alignment.CenterVertically)
                        )
                    }
                }
                if (notifications.indexOf(item) < notifications.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(start = 52.dp), color = DividerColor)
                }
            }
        }
        }
    }
}























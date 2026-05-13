package com.binahr.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.binahr.ui.theme.OrangePrimary
import kotlinx.coroutines.delay

/**
 * Full-screen success state shown after a form is submitted.
 * Auto-dismisses after [autoDismissMs] milliseconds.
 *
 * @param message      Primary message (e.g., "Pengajuan Cuti Berhasil Dikirim!")
 * @param subMessage   Optional secondary message.
 * @param onDismiss    Called when auto-dismiss timer fires.
 * @param autoDismissMs Delay before [onDismiss] is called. Defaults to 1 800 ms.
 */
@Composable
fun SuccessScreen(
    message: String,
    subMessage: String? = null,
    onDismiss: () -> Unit,
    autoDismissMs: Long = 1_800L,
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "checkmarkScale",
    )

    LaunchedEffect(Unit) {
        delay(autoDismissMs)
        onDismiss()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(OrangePrimary),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .scale(scale),
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(52.dp),
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
            if (subMessage != null) {
                Text(
                    text = subMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 40.dp),
                )
            }
        }
    }
}

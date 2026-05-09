package com.example.hroes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.hroes.ui.theme.Green50
import com.example.hroes.ui.theme.GreenPrimary

enum class CalloutType {
    INFO, ERROR
}

@Composable
fun InfoCallout(
    message: String,
    type: CalloutType = CalloutType.INFO,
    modifier: Modifier = Modifier
) {
    val bgColor = if (type == CalloutType.ERROR) MaterialTheme.colorScheme.errorContainer else Green50
    val contentColor = if (type == CalloutType.ERROR) MaterialTheme.colorScheme.onErrorContainer else GreenPrimary
    val icon = if (type == CalloutType.ERROR) Icons.Default.Error else Icons.Default.Info

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor
        )
    }
}

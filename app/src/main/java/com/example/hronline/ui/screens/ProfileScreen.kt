package com.example.hronline.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hronline.navigation.Screen
import com.example.hronline.ui.components.*
import com.example.hronline.ui.theme.*

@Composable
fun ProfileScreen(
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(GreenPrimary)
                .statusBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AvatarImage(initials = "AA", size = 80.dp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Aries Adityanto", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = PlusJakartaSans)
                Text("Staff IT", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, fontFamily = PlusJakartaSans)
                Text("NIK: 2024001", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontFamily = PlusJakartaSans)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Personal Info
        SectionHeader(title = "Informasi Personal")
        HRCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoRow(Icons.Filled.Email, "Email", "aries.adityanto@xyz.co.id")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerColor)
                ProfileInfoRow(Icons.Filled.Phone, "Telepon", "+62 812-3456-7890")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerColor)
                ProfileInfoRow(Icons.Filled.Business, "Departemen", "IT & Development")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerColor)
                ProfileInfoRow(Icons.Filled.DateRange, "Bergabung", "1 Januari 2024")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Settings
        SectionHeader(title = "Pengaturan")
        HRCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Column {
                ProfileMenuItem(Icons.Filled.Lock, "Ubah Password") { onNavigate(Screen.UbahPassword.route) }
                HorizontalDivider(color = DividerColor)
                ProfileMenuItem(Icons.AutoMirrored.Filled.HelpCenter, "FAQ & Bantuan") { onNavigate(Screen.FAQ.route) }
                HorizontalDivider(color = DividerColor)
                ProfileMenuItem(Icons.Filled.Info, "Tentang Aplikasi") { }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentRedLight,
                contentColor = AccentRed,
            ),
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Keluar", fontWeight = FontWeight.SemiBold, fontFamily = PlusJakartaSans)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Logout confirmation
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Keluar", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin keluar dari aplikasi?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) { Text("Keluar", color = AccentRed) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Batal") }
            },
        )
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ProfileMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextTertiary)
    }
}

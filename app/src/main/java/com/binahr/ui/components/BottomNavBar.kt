package com.binahr.ui.components


import com.binahr.BuildConfig
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.binahr.navigation.Screen
import com.binahr.ui.theme.GreenPrimary

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem("Home",       Screen.Home.route,       Icons.Filled.Home,                 Icons.Outlined.Home),
    BottomNavItem("Waktu",      Screen.History.route,    Icons.Filled.AccessTime,            Icons.Outlined.AccessTime),
    BottomNavItem("Gaji",       Screen.SlipGaji.route,   Icons.Filled.AccountBalanceWallet,  Icons.Outlined.AccountBalanceWallet),
    BottomNavItem("Pengajuan",  Screen.Pengajuan.route,  Icons.Filled.Assignment,            Icons.Outlined.Assignment),
    BottomNavItem("Profil",     Screen.Profile.route,    Icons.Filled.Person,                Icons.Outlined.Person),
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onItemSelected: (BottomNavItem) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GreenPrimary,
                    selectedTextColor = GreenPrimary,
                    indicatorColor = GreenPrimary.copy(alpha = 0.12f),
                ),
            )
        }
    }
}























package com.binahr.navigation


import com.binahr.BuildConfig
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.binahr.data.api.TokenManager
import com.binahr.ui.components.BottomNavBar
import com.binahr.ui.components.BottomNavItem
import com.binahr.ui.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Register the 401 force-logout handler so OkHttp can redirect to Login.
    LaunchedEffect(Unit) {
        TokenManager.onUnauthorized = {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { 100 }) },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { -100 }) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { 100 }) },
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToTenantSetup = {
                    navController.navigate(Screen.TenantSetup.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.TenantSetup.route) {
            TenantSetupScreen(onDomainSaved = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.TenantSetup.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onChangeTenant = {
                    // Clear tenant domain fully so user can re-enter company code
                    com.binahr.data.api.TokenManager.clearAll()
                    navController.navigate(Screen.TenantSetup.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Main.route) {
            MainScreen(rootNavController = navController)
        }

        composable(
            route = Screen.Attendance.route,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "CHECK_IN"
            AttendanceScreen(
                attendanceType = type,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DataDiri.route) {
            DataDiriScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Kalender.route) {
            KalenderScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Cuti.route) {
            CutiScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.SlipGaji.route) {
            SlipGajiScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Pengumuman.route) {
            PengumumanScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Lembur.route) {
            LemburScreen(onBack = { navController.popBackStack() })
        }
        // Izin/Sakit merged into Cuti screen (leave type filter)
        composable(Screen.Notifikasi.route) {
            NotifikasiScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.UbahPassword.route) {
            UbahPasswordScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Reimbursement.route) {
            ReimbursementScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.StrukturOrg.route) {
            StrukturOrgScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Dokumen.route) {
            DokumenScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.FAQ.route) {
            FAQScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Approvals.route) {
            ApprovalsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Performance.route) {
            PerformanceScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.JadwalShift.route) {
            JadwalShiftScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Asset.route) {
            AssetScreen(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun MainScreen(rootNavController: NavHostController) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onItemSelected = { item ->
                    bottomNavController.navigate(item.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigate = { route ->
                        // Tab routes belong to bottomNavController; everything else to root
                        if (route == Screen.History.route || route == Screen.Profile.route || route == Screen.Home.route) {
                            bottomNavController.navigate(route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } else {
                            rootNavController.navigate(route)
                        }
                    },
                    onNotificationClick = { rootNavController.navigate(Screen.Notifikasi.route) }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(onBack = {})
            }
            composable(Screen.Profile.route) {
                val scope = rememberCoroutineScope()
                ProfileScreen(
                    onNavigate = { route -> rootNavController.navigate(route) },
                    onLogout = {
                        scope.launch {
                            com.binahr.data.repository.AuthRepository().logout()
                        }
                        // Navigate back to TenantSetup so the user can switch
                        // companies on logout, or directly to Login if domain is saved.
                        val hasDomain    = !com.binahr.data.api.TokenManager.tenantDomain.isNullOrBlank()
                        val isSingleTenant = com.binahr.BuildConfig.TENANT_DOMAIN.isNotBlank()
                        val destination  = if (!hasDomain && !isSingleTenant) Screen.TenantSetup.route
                                           else Screen.Login.route
                        rootNavController.navigate(destination) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}

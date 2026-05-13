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
import androidx.compose.ui.platform.LocalContext
import com.binahr.data.AttendanceStorage
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

        composable(
            route = Screen.AttendanceMap.route,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "CHECK_IN"
            AttendanceMapScreen(
                attendanceType = type,
                onBack = { navController.popBackStack() },
                onProceed = { lat, lon, addr ->
                    navController.navigate(Screen.AttendanceFace.createRoute(type, lat, lon, addr))
                }
            )
        }

        composable(
            route = Screen.AttendanceFace.route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("lat") { type = NavType.StringType },
                navArgument("lon") { type = NavType.StringType },
                navArgument("address") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "CHECK_IN"
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val lon = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull() ?: 0.0
            val addr = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("address") ?: "", "UTF-8"
            )
            AttendanceFaceScreen(
                attendanceType = type,
                lat = lat,
                lon = lon,
                address = addr,
                onBack = { navController.popBackStack() },
                onSuccess = { record ->
                    val msg = if (type == "CHECK_IN") "Absen Masuk Berhasil!" else "Absen Pulang Berhasil!"
                    navController.navigate(Screen.Success.createRoute(msg, "Data kehadiran telah tercatat")) {
                        popUpTo(Screen.AttendanceMap.createRoute(type)) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.DataDiri.route) {
            DataDiriScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Kalender.route) {
            KalenderScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Cuti.route) {
            CutiScreen(
                onBack = { navController.popBackStack() },
                onAjukanCuti = { navController.navigate(Screen.AjukanCuti.route) },
                onNavigateDetail = { id -> navController.navigate(Screen.CutiDetail.createRoute(id)) },
            )
        }
        composable(Screen.SlipGaji.route) {
            SlipGajiScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Pengumuman.route) {
            PengumumanScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Lembur.route) {
            LemburScreen(
                onBack = { navController.popBackStack() },
                onAjukanLembur = { navController.navigate(Screen.AjukanLembur.route) },
                onNavigateDetail = { id -> navController.navigate(Screen.LemburDetail.createRoute(id)) },
            )
        }
        // Izin/Sakit merged into Cuti screen (leave type filter)
        composable(Screen.Notifikasi.route) {
            NotifikasiScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.UbahPassword.route) {
            UbahPasswordScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Reimbursement.route) {
            ReimbursementScreen(
                onBack = { navController.popBackStack() },
                onAjukanReimbursement = { navController.navigate(Screen.AjukanReimbursement.route) },
                onNavigateDetail = { id -> navController.navigate(Screen.ReimbursementDetail.createRoute(id)) },
            )
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
            ApprovalsScreen(
                onBack = { navController.popBackStack() },
                onNavigateDetail = { id -> navController.navigate(Screen.ApprovalDetail.createRoute(id)) },
            )
        }
        composable(Screen.Performance.route) {
            PerformanceScreen(
                onBack = { navController.popBackStack() },
                onNavigateDetail = { id -> navController.navigate(Screen.PerformanceCycleDetail.createRoute(id)) },
            )
        }
        composable(Screen.JadwalShift.route) {
            JadwalShiftScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Asset.route) {
            AssetScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Pengajuan.route) {
            PengajuanScreen(
                onBack = { navController.popBackStack() },
                onNavigateCuti = { navController.navigate(Screen.AjukanCuti.route) },
                onNavigateLembur = { navController.navigate(Screen.AjukanLembur.route) },
                onNavigateReimbursement = { navController.navigate(Screen.AjukanReimbursement.route) },
            )
        }
        composable(Screen.AjukanCuti.route) {
            AjukanCutiScreen(
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() },
            )
        }
        composable(Screen.AjukanLembur.route) {
            AjukanLemburScreen(
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() },
            )
        }
        composable(Screen.AjukanReimbursement.route) {
            AjukanReimbursementScreen(
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() },
            )
        }
        composable(
            route = Screen.ApprovalDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            ApprovalDetailScreen(
                id = id,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Screen.AttendanceCorrection.route) {
            AttendanceCorrectionScreen(
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(
                        Screen.Success.createRoute("Koreksi kehadiran berhasil dikirim", "Menunggu verifikasi admin")
                    )
                },
            )
        }
        composable(Screen.DataDiriEdit.route) {
            DataDiriEditScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Recruitment.route) {
            RecruitmentScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.CutiDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            CutiDetailScreen(id = id, onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.LemburDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            LemburDetailScreen(id = id, onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.ReimbursementDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            ReimbursementDetailScreen(id = id, onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.PerformanceCycleDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            PerformanceCycleDetailScreen(id = id, onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.Success.route,
            arguments = listOf(
                navArgument("message") { type = NavType.StringType; defaultValue = "Berhasil!" },
                navArgument("sub") { type = NavType.StringType; defaultValue = "" },
            ),
        ) { backStackEntry ->
            val message = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("message") ?: "Berhasil!", "UTF-8"
            )
            val sub = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("sub") ?: "", "UTF-8"
            )
            SuccessScreen(
                message = message,
                subMessage = sub.ifEmpty { null },
                onDismiss = {
                    navController.popBackStack(Screen.Success.route, inclusive = true)
                },
            )
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
                        val bottomTabs = setOf(Screen.Home.route, Screen.History.route, Screen.SlipGaji.route, Screen.Pengajuan.route, Screen.Profile.route)
                        if (route in bottomTabs) {
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
            composable(Screen.SlipGaji.route) {
                SlipGajiScreen(onBack = {})
            }
            composable(Screen.Pengajuan.route) {
                PengajuanScreen(
                    onBack = {},
                    onNavigateCuti = { rootNavController.navigate(Screen.Cuti.route) },
                    onNavigateLembur = { rootNavController.navigate(Screen.Lembur.route) },
                    onNavigateReimbursement = { rootNavController.navigate(Screen.Reimbursement.route) },
                )
            }
            composable(Screen.Profile.route) {
                val scope = rememberCoroutineScope()
                val context = LocalContext.current
                ProfileScreen(
                    onNavigate = { route -> rootNavController.navigate(route) },
                    onLogout = {
                        scope.launch {
                            com.binahr.data.repository.AuthRepository().logout()
                        }
                        AttendanceStorage.clearAll(context)
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

package com.example.botanyhub

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.botanyhub.ui.screens.*
import com.example.botanyhub.ui.theme.BotanyHubTheme
import com.example.botanyhub.viewmodel.ProfileViewModel
import com.example.botanyhub.viewmodel.ScanViewModel
import com.google.gson.Gson

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BotanyHubTheme {
                PlantCareApp()
            }
        }
    }
}

@Composable
fun PlantCareApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val profileVm: ProfileViewModel = viewModel()

    val prefs = context.getSharedPreferences("botanyhub_prefs", Context.MODE_PRIVATE)
    val startDestination = if (prefs.getBoolean("onboarding_done", false)) "home" else "onboarding"

    val currentEntry by navController.currentBackStackEntryAsState()
    val route = currentEntry?.destination?.route
    val showBottom = route in listOf("home", "profile")

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Unspecified,
        bottomBar = {
            if (showBottom) {
                NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
                    listOf(
                        Triple("home", Icons.Rounded.Spa, "Garden"),
                        Triple("profile", Icons.Rounded.Person, "Profile")
                    ).forEach { (r, icon, label) ->
                        NavigationBarItem(
                            selected = route == r,
                            onClick = {
                                navController.navigate(r) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, label) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = com.example.botanyhub.ui.theme.GreenPrimary,
                                selectedTextColor = com.example.botanyhub.ui.theme.GreenPrimary,
                                indicatorColor = com.example.botanyhub.ui.theme.GreenContainer
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {

            // ── Onboarding ────────────────────────────────────
            composable("onboarding") {
                OnboardingScreen(
                    onDone = { name ->
                        profileVm.setNameAndFinishOnboarding(name)
                        navController.navigate("home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }

            // ── Home ──────────────────────────────────────────
            composable("home") {
                HomeScreen(
                    onNavigateToDetail = { id -> navController.navigate("detail/$id") },
                    onNavigateToScan = { navController.navigate("scan") }
                )
            }

            composable("scan") {
                val scanVm: ScanViewModel = viewModel()
                ScanScreen(
                    onNavigateToAdd = { result, uri, careTips ->
                        val gson = Gson()
                        val resultJson = Uri.encode(gson.toJson(result))
                        val imgUri = Uri.encode(uri)

                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("care_tips_json", if (careTips != null) gson.toJson(careTips) else null)

                        navController.navigate("add/$resultJson/$imgUri")
                    },
                    onBack = { navController.popBackStack() },
                    vm = scanVm
                )
            }

            composable(
                route = "add/{result}/{uri}",
                arguments = listOf(
                    navArgument("result") { type = NavType.StringType },
                    navArgument("uri") { type = NavType.StringType }
                )
            ) { backStack ->
                val gson = Gson()
                val resultJson = backStack.arguments?.getString("result") ?: ""
                val uriStr = backStack.arguments?.getString("uri") ?: ""

                val result = gson.fromJson(
                    Uri.decode(resultJson),
                    com.example.botanyhub.data.PlantNetResult::class.java
                )

                val tipsJson = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<String?>("care_tips_json")

                val careTips = try {
                    if (tipsJson.isNullOrEmpty()) null
                    else gson.fromJson(tipsJson, com.example.botanyhub.data.PlantCareTips::class.java)
                } catch (e: Exception) {
                    null
                }

                AddPlantScreen(
                    result = result,
                    imageUri = Uri.decode(uriStr),
                    careTips = careTips,
                    onSaved = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Detail ────────────────────────────────────────
            composable(
                route = "detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStack ->
                PlantDetailScreen(
                    plantId = backStack.arguments?.getInt("id") ?: 0,
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Profile ───────────────────────────────────────
            composable("profile") {
                ProfileScreen(
                    onNavigateToDetail = { id -> navController.navigate("detail/$id") }
                )
            }
        }
    }
}
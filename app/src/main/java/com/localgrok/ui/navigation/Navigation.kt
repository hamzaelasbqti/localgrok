package com.localgrok.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.localgrok.ui.screens.ChatScreen
import com.localgrok.ui.screens.SettingsScreen
import com.localgrok.ui.viewmodel.ChatViewModel
import com.localgrok.ui.viewmodel.SettingsViewModel

/**
 * Navigation routes for the app
 */
object Routes {
    const val CHAT = "chat"
    const val SETTINGS = "settings"
}

/**
 * Main navigation host for the app
 */
@Composable
fun LocalGrokNavHost(
    chatViewModel: ChatViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    // Global navigation debounce state to prevent rapid-clicking issues
    var isNavigationInProgress by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = Routes.CHAT,
        modifier = modifier
    ) {
        composable(Routes.CHAT) {
            // Reset navigation flag when we arrive at chat screen
            isNavigationInProgress = false

            ChatScreen(
                viewModel = chatViewModel,
                onNavigateToSettings = {
                    // Guard against rapid clicks and duplicate navigation
                    if (!isNavigationInProgress) {
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute == Routes.CHAT) {
                            isNavigationInProgress = true
                            navController.navigate(Routes.SETTINGS) {
                                launchSingleTop = true
                            }
                        }
                    }
                }
            )
        }

        composable(Routes.SETTINGS) {
            // Reset navigation flag when we arrive at settings screen
            isNavigationInProgress = false

            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    // Guard against rapid clicks that could empty the stack
                    if (!isNavigationInProgress) {
                        // Check if we have a valid back destination before popping
                        val canPop = navController.previousBackStackEntry != null
                        if (canPop) {
                            isNavigationInProgress = true
                            // Refresh connection when returning from settings
                            chatViewModel.refreshServerConnection()
                            navController.popBackStack()
                        }
                    }
                }
            )
        }
    }
}

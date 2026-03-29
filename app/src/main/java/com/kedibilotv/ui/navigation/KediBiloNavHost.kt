package com.kedibilotv.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kedibilotv.ui.category.CategoryScreen
import com.kedibilotv.ui.content.ContentListScreen
import com.kedibilotv.ui.detail.DetailScreen
import com.kedibilotv.ui.home.HomeScreen
import com.kedibilotv.ui.login.LoginScreen
import com.kedibilotv.ui.player.PlayerScreen
import com.kedibilotv.ui.settings.SettingsScreen

@Composable
fun KediBiloNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(NavRoutes.LOGIN) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(NavRoutes.HOME) {
                    popUpTo(NavRoutes.LOGIN) { inclusive = true }
                }
            })
        }
        composable(NavRoutes.HOME) {
            HomeScreen(
                onNavigateToCategory = { type -> navController.navigate(NavRoutes.category(type)) },
                onNavigateToDetail = { type, id -> navController.navigate(NavRoutes.detail(type, id)) },
                onNavigateToPlayer = { type, id, epId -> navController.navigate(NavRoutes.player(type, id, epId)) },
                onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) }
            )
        }
        composable(
            NavRoutes.CATEGORY,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) {
            CategoryScreen(
                onNavigateToContent = { t, cId -> navController.navigate(NavRoutes.content(t, cId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            NavRoutes.CONTENT,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("categoryId") { type = NavType.StringType }
            )
        ) {
            ContentListScreen(
                onNavigateToDetail = { t, id -> navController.navigate(NavRoutes.detail(t, id)) },
                onNavigateToPlayer = { t, id -> navController.navigate(NavRoutes.player(t, id)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            NavRoutes.DETAIL,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("streamId") { type = NavType.IntType },
                navArgument("name") { type = NavType.StringType; defaultValue = "" },
                navArgument("posterUrl") { type = NavType.StringType; defaultValue = "" }
            )
        ) {
            DetailScreen(
                onPlay = { t, id, epId -> navController.navigate(NavRoutes.player(t, id, epId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            NavRoutes.PLAYER,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("streamId") { type = NavType.IntType },
                navArgument("episodeId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) {
            PlayerScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                onLogout = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

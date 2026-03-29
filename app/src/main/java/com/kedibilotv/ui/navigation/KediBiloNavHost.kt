package com.kedibilotv.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun KediBiloNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(NavRoutes.LOGIN) {
            // LoginScreen — will be added in Task 7
        }
        composable(NavRoutes.HOME) {
            // HomeScreen — will be added in Task 8
        }
        composable(
            NavRoutes.CATEGORY,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) {
            // CategoryScreen — will be added in Task 9
        }
        composable(
            NavRoutes.CONTENT,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("categoryId") { type = NavType.StringType }
            )
        ) {
            // ContentListScreen — will be added in Task 9
        }
        composable(
            NavRoutes.DETAIL,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("streamId") { type = NavType.IntType }
            )
        ) {
            // DetailScreen — will be added in Task 10
        }
        composable(
            NavRoutes.PLAYER,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("streamId") { type = NavType.IntType },
                navArgument("episodeId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) {
            // PlayerScreen — will be added in Task 11
        }
        composable(NavRoutes.SETTINGS) {
            // SettingsScreen — will be added in Task 12
        }
    }
}

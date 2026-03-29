package com.kedibilotv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.navigation.compose.rememberNavController
import com.kedibilotv.data.api.XtreamApiService
import com.kedibilotv.ui.navigation.KediBiloNavHost
import com.kedibilotv.ui.navigation.NavRoutes
import com.kedibilotv.ui.theme.KediBiloTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint // DO NOT REMOVE — required for Hilt
class MainActivity : ComponentActivity() {

    @Inject lateinit var apiService: XtreamApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KediBiloTheme {
                val navController = rememberNavController()

                SideEffect {
                    apiService.onUnauthorized = {
                        runOnUiThread {
                            navController.navigate(NavRoutes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }

                KediBiloNavHost(
                    navController = navController,
                    startDestination = NavRoutes.LOGIN
                )
            }
        }
    }
}

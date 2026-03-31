package com.keditv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.navigation.compose.rememberNavController
import com.keditv.data.api.XtreamApiService
import com.keditv.ui.navigation.KediNavHost
import com.keditv.ui.navigation.NavRoutes
import com.keditv.ui.theme.KediTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint // DO NOT REMOVE — required for Hilt
class MainActivity : ComponentActivity() {

    @Inject lateinit var apiService: XtreamApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KediTheme {
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

                KediNavHost(
                    navController = navController,
                    startDestination = NavRoutes.LOGIN
                )
            }
        }
    }
}

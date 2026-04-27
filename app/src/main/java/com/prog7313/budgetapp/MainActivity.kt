package com.prog7313.budgetapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prog7313.budgetapp.ui.navigation.AppNavHost
import com.prog7313.budgetapp.ui.theme.Application001Theme
import com.prog7313.budgetapp.viewmodel.AppViewModel
import com.prog7313.budgetapp.viewmodel.AuthViewModel


class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val appViewModel:  AppViewModel  by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            authViewModel.uiState.value.isLoading
        }

        setContent {
            Application001Theme {
                val authState by authViewModel.uiState.collectAsStateWithLifecycle()

                AppNavHost(
                    authViewModel = authViewModel,
                    appViewModel  = appViewModel,
                    isLoggedIn    = authState.isLoggedIn
                )
            }
        }
    }
}

/*
Title: Jetpack Compose — Single-Activity architecture
Author(s): Android Developers
Date: 2024
Version: Activity Compose 1.9.0
Type: Documentation
Availability: https://developer.android.com/develop/ui/compose/migrate/activity
*/

/*
Title: ViewModelProvider.AndroidViewModelFactory — Supplying Application to ViewModel
Author(s): Android Developers
Date: 2024
Version: Lifecycle 2.8.2
Type: Documentation
Availability: https://developer.android.com/reference/androidx/lifecycle/ViewModelProvider.AndroidViewModelFactory
*/

/*
Title: Android — SplashScreen API
Author(s): Android Developers
Date: 2024
Version: Core SplashScreen 1.0.1
Type: Documentation
Availability: https://developer.android.com/develop/ui/views/launch/splash-screen
*/
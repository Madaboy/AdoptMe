package com.adoptme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.adoptme.core.di.AppContainer
import com.adoptme.core.navigation.AdoptMeNavHost
import com.adoptme.core.navigation.Screen
import com.adoptme.ui.components.AdoptMeBottomBar
import com.adoptme.ui.components.AdoptMeToast
import com.adoptme.ui.theme.AdoptMeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AdoptMeTheme {
                AdoptMeApp()
            }
        }
    }
}

@Composable
fun AdoptMeApp() {
    val navController = rememberNavController()
    var toastMessage by remember { mutableStateOf<String?>(null) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val interestedDogs by AppContainer.repository.interestedDogs.collectAsState()

    val showBottomBar = currentRoute != Screen.Publish.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                AdoptMeBottomBar(
                    currentRoute = currentRoute,
                    interestCount = interestedDogs.size,
                    onNavigate = { screen ->
                        if (screen == Screen.Publish) {
                            navController.navigate(Screen.Publish.route) {
                                launchSingleTop = true
                            }
                        } else if (currentRoute != screen.route) {
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Adopt.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AdoptMeNavHost(
                navController = navController,
                onShowToast = { message -> toastMessage = message }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                AdoptMeToast(toastMessage) { toastMessage = null }
            }
        }
    }
}

package com.adoptme.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.adoptme.ui.screens.HomeScreen
import com.adoptme.ui.screens.MoreHubScreen
import com.adoptme.ui.screens.MyAdoptionsScreen
import com.adoptme.ui.screens.MyPetScreen
import com.adoptme.ui.screens.PublishScreen
import com.adoptme.ui.screens.ReportsScreen
import com.adoptme.viewmodel.HomeViewModel
import com.adoptme.viewmodel.MyAdoptionsViewModel
import com.adoptme.viewmodel.MyPetViewModel
import com.adoptme.viewmodel.PublishViewModel
import com.adoptme.viewmodel.ReportsViewModel

@Composable
fun AdoptMeNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onShowToast: (String) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Adopt.route,
        modifier = modifier
    ) {
        composable(Screen.Adopt.route) {
            val homeViewModel: HomeViewModel = viewModel()
            HomeScreen(
                viewModel = homeViewModel,
                onShowToast = onShowToast
            )
        }
        composable(Screen.Reports.route) {
            val reportsViewModel: ReportsViewModel = viewModel()
            ReportsScreen(
                viewModel = reportsViewModel,
                onShowToast = onShowToast
            )
        }
        composable(Screen.Interests.route) {
            val myAdoptionsViewModel: MyAdoptionsViewModel = viewModel()
            MyAdoptionsScreen(
                viewModel = myAdoptionsViewModel,
                onExplore = {
                    navController.navigate(Screen.Adopt.route) {
                        popUpTo(Screen.Adopt.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onShowToast = onShowToast
            )
        }
        composable(Screen.MyPet.route) {
            val myPetViewModel: MyPetViewModel = viewModel()
            MyPetScreen(
                viewModel = myPetViewModel,
                onPublish = { navController.navigate(Screen.Publish.route) },
                onOpenMore = { navController.navigate(Screen.More.route) },
                onShowToast = onShowToast
            )
        }
        composable(Screen.Publish.route) {
            val publishViewModel: PublishViewModel = viewModel()
            PublishScreen(
                viewModel = publishViewModel,
                onClose = { navController.popBackStack() },
                onPublished = {
                    navController.popBackStack()
                    navController.navigate(Screen.MyPet.route) {
                        launchSingleTop = true
                    }
                },
                onShowToast = onShowToast
            )
        }
        composable(Screen.More.route) {
            MoreHubScreen(
                onFeatureClick = { feature ->
                    onShowToast("$feature estará disponible pronto")
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

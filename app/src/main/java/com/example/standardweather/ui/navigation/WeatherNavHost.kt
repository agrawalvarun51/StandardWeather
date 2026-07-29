package com.example.standardweather.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.standardweather.ui.screen.SearchScreen
import com.example.standardweather.ui.screen.WeatherScreen
import kotlinx.serialization.Serializable

@Serializable
object SearchRoute

@Serializable
data class WeatherRoute(val cityId: String)

@Composable
fun WeatherNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = SearchRoute) {
        composable<SearchRoute> {
            SearchScreen(
                onCitySelected = { city ->
                    navController.navigate(WeatherRoute(cityId = city.cityId))
                }
            )
        }
        composable<WeatherRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<WeatherRoute>()
            WeatherScreen(
                cityId = route.cityId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

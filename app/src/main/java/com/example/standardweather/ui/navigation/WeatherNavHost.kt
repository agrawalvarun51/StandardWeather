package com.example.standardweather.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.standardweather.domain.model.CitySearchResult
import com.example.standardweather.ui.screen.SearchScreen
import com.example.standardweather.ui.screen.WeatherScreen
import com.google.gson.Gson

sealed class Screen(val route: String) {
    data object Search : Screen("search")
    data object Weather : Screen("weather/{cityJson}") {
        fun createRoute(city: CitySearchResult): String {
            val json = Gson().toJson(city)
            return "weather/${java.net.URLEncoder.encode(json, "UTF-8")}"
        }
    }
}

@Composable
fun WeatherNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Search.route) {
        composable(Screen.Search.route) {
            SearchScreen(
                onCitySelected = { city ->
                    navController.navigate(Screen.Weather.createRoute(city))
                }
            )
        }
        composable(Screen.Weather.route) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("cityJson") ?: return@composable
            val city = remember(encoded) {
                val json = java.net.URLDecoder.decode(encoded, "UTF-8")
                Gson().fromJson(json, CitySearchResult::class.java)
            }
            WeatherScreen(
                city = city,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

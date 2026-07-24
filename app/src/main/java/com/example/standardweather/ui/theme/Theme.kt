package com.example.standardweather.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Weather condition categories
enum class WeatherConditionTheme {
    CLEAR_DAY, CLEAR_NIGHT, CLOUDY, RAINY, STORMY, SNOWY, MISTY, HOT, DEFAULT
}

fun weatherConditionThemeFor(weatherId: Int, isDay: Boolean): WeatherConditionTheme {
    // WeatherAPI.com condition codes
    return when (weatherId) {
        1000 -> if (isDay) WeatherConditionTheme.CLEAR_DAY else WeatherConditionTheme.CLEAR_NIGHT
        1003, 1006, 1009 -> WeatherConditionTheme.CLOUDY
        1030, 1135, 1147 -> WeatherConditionTheme.MISTY
        1063, 1072, in 1150..1201 -> WeatherConditionTheme.RAINY
        in 1204..1237 -> WeatherConditionTheme.SNOWY
        in 1240..1264 -> WeatherConditionTheme.RAINY
        in 1273..1282 -> WeatherConditionTheme.STORMY
        else -> WeatherConditionTheme.DEFAULT
    }
}

data class WeatherColorSet(
    val gradientTop: Color,
    val gradientBottom: Color,
    val onBackground: Color,
    val cardSurface: Color,
    val accent: Color
)

fun weatherColorsFor(theme: WeatherConditionTheme): WeatherColorSet = when (theme) {
    WeatherConditionTheme.CLEAR_DAY -> WeatherColorSet(
        ClearSkyDayTop, ClearSkyDayBottom, OnDarkBg, SurfaceLight, Color(0xFFFFEB3B)
    )
    WeatherConditionTheme.CLEAR_NIGHT -> WeatherColorSet(
        ClearSkyNightTop, ClearSkyNightBottom, OnDarkBg, SurfaceLight, Color(0xFFE3F2FD)
    )
    WeatherConditionTheme.CLOUDY -> WeatherColorSet(
        CloudyTop, CloudyBottom, OnDarkBg, SurfaceLight, Color(0xFFECEFF1)
    )
    WeatherConditionTheme.RAINY -> WeatherColorSet(
        RainyTop, RainyBottom, OnDarkBg, SurfaceLight, Color(0xFF80DEEA)
    )
    WeatherConditionTheme.STORMY -> WeatherColorSet(
        StormyTop, StormyBottom, OnDarkBg, SurfaceLight, StormyAccent
    )
    WeatherConditionTheme.SNOWY -> WeatherColorSet(
        SnowyTop, SnowyBottom, OnLightBg, SurfaceDark, Color(0xFF1565C0)
    )
    WeatherConditionTheme.MISTY -> WeatherColorSet(
        MistyTop, MistyBottom, OnLightBg, SurfaceDark, Color(0xFF455A64)
    )
    WeatherConditionTheme.HOT -> WeatherColorSet(
        HeatTop, HeatBottom, OnDarkBg, SurfaceLight, Color(0xFFFFCC02)
    )
    WeatherConditionTheme.DEFAULT -> WeatherColorSet(
        Purple40, PurpleGrey40, OnDarkBg, SurfaceLight, Pink80
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun StandardWeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

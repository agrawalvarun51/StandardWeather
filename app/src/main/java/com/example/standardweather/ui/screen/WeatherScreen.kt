package com.example.standardweather.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.standardweather.domain.model.DailyWeather
import com.example.standardweather.domain.model.HourlyWeather
import com.example.standardweather.domain.model.WeatherData
import com.example.standardweather.ui.state.WeatherUiState
import com.example.standardweather.ui.theme.WeatherColorSet
import com.example.standardweather.ui.theme.weatherColorsFor
import com.example.standardweather.ui.theme.weatherConditionThemeFor
import com.example.standardweather.ui.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    cityId: String,
    onBack: () -> Unit,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(cityId) {
        viewModel.loadWeather(cityId)
    }

    when (val state = uiState) {
        is WeatherUiState.Loading -> FullScreenLoading()
        is WeatherUiState.Error -> {
            if (state.cachedData != null) {
                WeatherContent(
                    data = state.cachedData,
                    isRefreshing = false,
                    isOffline = true,
                    onBack = onBack,
                    onRefresh = { viewModel.refresh() }
                )
            } else {
                ErrorScreen(message = state.message, onRetry = { viewModel.refresh() }, onBack = onBack)
            }
        }
        is WeatherUiState.Success -> WeatherContent(
            data = state.data,
            isRefreshing = state.isRefreshing,
            isOffline = false,
            onBack = onBack,
            onRefresh = { viewModel.refresh() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeatherContent(
    data: WeatherData,
    isRefreshing: Boolean,
    isOffline: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    val isDay = remember(data.current.dt) {
        val hour = Calendar.getInstance().apply { timeInMillis = data.current.dt * 1000 }.get(Calendar.HOUR_OF_DAY)
        hour in 6..19
    }
    val conditionTheme = weatherConditionThemeFor(data.current.weatherId, isDay)
    val colors = weatherColorsFor(conditionTheme)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(listOf(colors.gradientTop, colors.gradientBottom))
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        @Suppress("DEPRECATION")
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onBackground)
                    }
                    Spacer(Modifier.weight(1f))
                    if (isOffline) {
                        Text(
                            text = "Offline",
                            color = colors.onBackground,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xAA000000))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colors.onBackground, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = colors.onBackground)
                        }
                    }
                }
            }

            item {
                // Hero current weather
                CurrentWeatherHero(data = data, colors = colors)
            }

            // Offline banner
            if (isOffline) {
                item {
                    OfflineBanner(fetchedAt = data.fetchedAt, colors = colors)
                }
            }

            // Active alerts
            if (data.alerts.isNotEmpty()) {
                item {
                    AlertsSection(alerts = data.alerts, colors = colors)
                }
            }

            item {
                // Hourly forecast
                SectionCard(title = "Hourly Forecast", colors = colors) {
                    HourlyForecastRow(hourly = data.hourly.take(24), colors = colors)
                }
            }

            item {
                // Temp graph
                SectionCard(title = "24-Hour Temperature", colors = colors) {
                    TemperatureGraph(
                        hourly = data.hourly.take(24),
                        accentColor = colors.accent
                    )
                }
            }

            item {
                // Daily forecast
                SectionCard(title = "7-Day Forecast", colors = colors) {
                    data.daily.take(7).forEach { day ->
                        DailyForecastRow(day = day, colors = colors)
                        HorizontalDivider(color = colors.onBackground.copy(alpha = 0.1f))
                    }
                }
            }

            item {
                // Extra details
                SectionCard(title = "Today's Details", colors = colors) {
                    DetailGrid(data = data, colors = colors)
                }
            }
        }
    }
}

@Composable
private fun CurrentWeatherHero(data: WeatherData, colors: WeatherColorSet) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${data.cityName}, ${data.country}",
            style = MaterialTheme.typography.titleLarge,
            color = colors.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = data.current.weatherDescription.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onBackground.copy(alpha = 0.8f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "${data.current.temp.roundToInt()}°C",
            fontSize = 80.sp,
            fontWeight = FontWeight.Thin,
            color = colors.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Feels like ${data.current.feelsLike.roundToInt()}°C",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onBackground.copy(alpha = 0.75f)
        )
        Spacer(Modifier.height(12.dp))
        // Quick stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickStat(Icons.Default.WaterDrop, "${data.current.humidity}%", "Humidity", colors.onBackground)
            QuickStat(Icons.Default.Air, "${"%.1f".format(data.current.windSpeed)} m/s", "Wind", colors.onBackground)
            QuickStat(Icons.Default.WbSunny, "${"%.1f".format(data.current.uvi)}", "UV", colors.onBackground)
        }
    }
}

@Composable
private fun QuickStat(icon: ImageVector, value: String, label: String, textColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = textColor, modifier = Modifier.size(20.dp))
        Text(value, color = textColor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(label, color = textColor.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SectionCard(
    title: String,
    colors: WeatherColorSet,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.cardSurface)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = colors.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun HourlyForecastRow(
    hourly: List<HourlyWeather>,
    colors: WeatherColorSet
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(hourly, key = { it.dt }) { h ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(52.dp)
            ) {
                val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                Text(fmt.format(Date(h.dt * 1000)), style = MaterialTheme.typography.labelSmall, color = colors.onBackground.copy(0.7f))
                Spacer(Modifier.height(4.dp))
                Text(
                    weatherIconEmoji(h.weatherId),
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text("${h.temp.roundToInt()}°", style = MaterialTheme.typography.bodyMedium, color = colors.onBackground, fontWeight = FontWeight.SemiBold)
                if (h.pop > 0.1) {
                    Text("${(h.pop * 100).roundToInt()}%", style = MaterialTheme.typography.labelSmall, color = colors.accent)
                }
            }
        }
    }
}

@Composable
private fun TemperatureGraph(hourly: List<HourlyWeather>, accentColor: Color) {
    if (hourly.isEmpty()) return
    val temps = hourly.map { it.temp.toFloat() }
    val minTemp = temps.min()
    val maxTemp = temps.max()
    val range = (maxTemp - minTemp).coerceAtLeast(1f)

    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        val w = size.width
        val h = size.height
        val padV = 12f
        val xStep = w / (temps.size - 1).coerceAtLeast(1)
        val points = temps.mapIndexed { i, t ->
            Offset(
                x = i * xStep,
                y = padV + (1f - (t - minTemp) / range) * (h - 2 * padV)
            )
        }

        // Filled area under the line
        val path = Path().apply {
            moveTo(points.first().x, h)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, h)
            close()
        }
        drawPath(
            path,
            brush = Brush.verticalGradient(
                listOf(accentColor.copy(alpha = 0.4f), Color.Transparent)
            )
        )

        // Line
        for (i in 0 until points.size - 1) {
            drawLine(
                color = accentColor,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }

        // Dots
        points.forEach {
            drawCircle(accentColor, radius = 4f, center = it)
            drawCircle(Color.White, radius = 2f, center = it)
        }
    }

    // Hour labels underneath
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        val fmt = SimpleDateFormat("HH", Locale.getDefault())
        listOf(0, hourly.size / 4, hourly.size / 2, (hourly.size * 3) / 4, hourly.size - 1)
            .distinct()
            .forEach { idx ->
                if (idx < hourly.size) {
                    Text(
                        text = fmt.format(Date(hourly[idx].dt * 1000)),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
    }
}

@Composable
private fun DailyForecastRow(
    day: DailyWeather,
    colors: WeatherColorSet
) {
    val fmt = SimpleDateFormat("EEE", Locale.getDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = fmt.format(Date(day.dt * 1000)).uppercase(),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onBackground,
            modifier = Modifier.width(44.dp)
        )
        Text(weatherIconEmoji(day.weatherId), fontSize = 20.sp, modifier = Modifier.width(32.dp))
        Text(
            text = day.weatherDescription.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodySmall,
            color = colors.onBackground.copy(0.75f),
            modifier = Modifier.weight(1f)
        )
        if (day.pop > 0.1) {
            Text(
                text = "${(day.pop * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = colors.accent,
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.End
            )
        } else {
            Spacer(Modifier.width(32.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "${day.tempMax.roundToInt()}° / ${day.tempMin.roundToInt()}°",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onBackground,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(72.dp)
        )
    }
}

@Composable
private fun DetailGrid(
    data: WeatherData,
    colors: WeatherColorSet
) {
    val items = listOf(
        Triple(Icons.Default.Thermostat, "Feels Like", "${data.current.feelsLike.roundToInt()}°C"),
        Triple(Icons.Default.WaterDrop, "Humidity", "${data.current.humidity}%"),
        Triple(Icons.Default.Air, "Wind Speed", "${"%.1f".format(data.current.windSpeed)} m/s"),
        Triple(Icons.Default.Visibility, "Visibility", "${data.current.visibility / 1000} km"),
        Triple(Icons.Default.WbSunny, "UV Index", "%.1f".format(data.current.uvi)),
        Triple(Icons.Default.WaterDrop, "Today Rain",
            (data.daily.firstOrNull()?.pop ?: 0.0).let { "${(it * 100).roundToInt()}%" })
    )
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        items.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { (icon, label, value) ->
                    DetailCell(icon = icon, label = label, value = value, textColor = colors.onBackground, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DetailCell(
    icon: ImageVector,
    label: String,
    value: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = textColor.copy(0.7f), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = textColor.copy(0.6f))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = textColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AlertsSection(
    alerts: List<com.example.standardweather.domain.model.WeatherAlert>,
    colors: WeatherColorSet
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xCCB71C1C))
            .padding(12.dp)
    ) {
        alerts.forEach { alert ->
            Text(
                text = "⚠️ ${alert.event}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = alert.description.take(120) + if (alert.description.length > 120) "…" else "",
                color = Color.White.copy(0.85f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
            )
        }
    }
}

@Composable
private fun OfflineBanner(
    fetchedAt: Long,
    colors: WeatherColorSet
) {
    val fmt = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xAA000000))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "📡 Offline — last synced ${fmt.format(Date(fetchedAt))}",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun FullScreenLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("😕", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text("Unable to load weather", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) { Text("Retry") }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBack) { Text("Back to Search") }
    }
}

/** Map WeatherAPI.com condition code to a fitting emoji. */
fun weatherIconEmoji(id: Int): String = when (id) {
    1000 -> "☀️"
    1003 -> "🌤️"
    1006 -> "⛅"
    1009 -> "☁️"
    1030, 1135, 1147 -> "🌫️"
    1063, 1072, 1150, 1153, 1168, 1171, 1180, 1183, 1186, 1189, 1192, 1195, 1198, 1201 -> "🌧️"
    1204, 1207, 1210, 1213, 1216, 1219, 1222, 1225, 1237 -> "❄️"
    1240, 1243, 1246, 1249, 1252 -> "🌦️"
    1255, 1258, 1261, 1264 -> "🌨️"
    1273, 1276 -> "⛈️"
    1279, 1282 -> "🌨️"
    else -> "🌡️"
}

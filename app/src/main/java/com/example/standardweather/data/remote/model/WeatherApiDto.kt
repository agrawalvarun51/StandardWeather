package com.example.standardweather.data.remote.model

import com.google.gson.annotations.SerializedName


data class WeatherApiForecastResponse(
    @SerializedName("location") val location: WaLocationDto,
    @SerializedName("current")  val current:  WaCurrentDto,
    @SerializedName("forecast") val forecast: WaForecastDto,
    @SerializedName("alerts")   val alerts:   WaAlertsWrapper?
)

data class WaLocationDto(
    @SerializedName("name")            val name:           String,
    @SerializedName("region")          val region:         String,
    @SerializedName("country")         val country:        String,
    @SerializedName("lat")             val lat:            Double,
    @SerializedName("lon")             val lon:            Double,
    @SerializedName("tz_id")           val tzId:           String,
    @SerializedName("localtime_epoch") val localtimeEpoch: Long,
    @SerializedName("localtime")       val localtime:      String
)

data class WaCurrentDto(
    @SerializedName("last_updated_epoch") val lastUpdatedEpoch: Long,
    @SerializedName("temp_c")             val tempC:            Double,
    @SerializedName("feelslike_c")        val feelslikeC:       Double,
    @SerializedName("humidity")           val humidity:         Int,
    @SerializedName("wind_kph")           val windKph:          Double,
    @SerializedName("vis_km")             val visKm:            Double,
    @SerializedName("uv")                 val uv:               Double,
    @SerializedName("is_day")             val isDay:            Int,
    @SerializedName("condition")          val condition:        WaConditionDto,
    @SerializedName("chance_of_rain")     val chanceOfRain:     Int?,
    @SerializedName("chance_of_snow")     val chanceOfSnow:     Int?
)

data class WaConditionDto(
    @SerializedName("text") val text: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("code") val code: Int
)

data class WaForecastDto(
    @SerializedName("forecastday") val forecastday: List<WaForecastDayDto>
)

data class WaForecastDayDto(
    @SerializedName("date_epoch") val dateEpoch: Long,
    @SerializedName("day")        val day:       WaDayDto,
    @SerializedName("hour")       val hour:      List<WaHourDto>
)

data class WaDayDto(
    @SerializedName("maxtemp_c")          val maxtempC:       Double,
    @SerializedName("mintemp_c")          val mintempC:       Double,
    @SerializedName("avgtemp_c")          val avgtempC:       Double,
    @SerializedName("maxwind_kph")        val maxwindKph:     Double,
    @SerializedName("avghumidity")        val avghumidity:    Int,
    @SerializedName("avgvis_km")          val avgvisKm:       Double,
    @SerializedName("uv")                 val uv:             Double,
    @SerializedName("daily_chance_of_rain") val chanceOfRain: Int,
    @SerializedName("daily_chance_of_snow") val chanceOfSnow: Int,
    @SerializedName("condition")          val condition:      WaConditionDto
)

data class WaHourDto(
    @SerializedName("time_epoch")   val timeEpoch:   Long,
    @SerializedName("temp_c")       val tempC:       Double,
    @SerializedName("feelslike_c")  val feelslikeC:  Double,
    @SerializedName("humidity")     val humidity:    Int,
    @SerializedName("wind_kph")     val windKph:     Double,
    @SerializedName("vis_km")       val visKm:       Double,
    @SerializedName("uv")           val uv:          Double,
    @SerializedName("is_day")       val isDay:       Int,
    @SerializedName("chance_of_rain") val chanceOfRain: Int,
    @SerializedName("condition")    val condition:   WaConditionDto
)

data class WaAlertsWrapper(
    @SerializedName("alert") val alert: List<WaAlertDto>?
)

data class WaAlertDto(
    @SerializedName("headline")    val headline:    String,
    @SerializedName("event")       val event:       String,
    @SerializedName("effective")   val effective:   String?,
    @SerializedName("expires")     val expires:     String?,
    @SerializedName("desc")        val desc:        String,
    @SerializedName("instruction") val instruction: String?,
    @SerializedName("severity")    val severity:    String?,
    @SerializedName("areas")       val areas:       String?
)

data class WaSearchResultDto(
    @SerializedName("id")      val id:      Int,
    @SerializedName("name")    val name:    String,
    @SerializedName("region")  val region:  String,
    @SerializedName("country") val country: String,
    @SerializedName("lat")     val lat:     Double,
    @SerializedName("lon")     val lon:     Double
)

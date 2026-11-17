package app.sadhan.data

import app.sadhan.core.Network
import java.io.IOException
import java.net.URLEncoder

data class City(val name: String, val region: String, val latitude: Double, val longitude: Double)
data class ForecastDay(val date: String, val minimum: Double, val maximum: Double)
data class Weather(val temperature: Double, val condition: String, val humidity: Int, val wind: Double, val days: List<ForecastDay>)

object WeatherRepository {
    suspend fun cities(query: String): List<City> {
        val data = Network.json("https://geocoding-api.open-meteo.com/v1/search?name=${URLEncoder.encode(query.trim(), "UTF-8")}&count=5&language=en&format=json")
        val results = data.optJSONArray("results") ?: return emptyList()
        return List(results.length()) { index ->
            val city = results.getJSONObject(index)
            City(city.getString("name"), listOf(city.optString("admin1"), city.optString("country")).filter { it.isNotBlank() }.joinToString(", "), city.getDouble("latitude"), city.getDouble("longitude"))
        }
    }

    suspend fun forecast(city: City): Weather {
        val data = Network.json("https://api.open-meteo.com/v1/forecast?latitude=${city.latitude}&longitude=${city.longitude}&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&daily=temperature_2m_max,temperature_2m_min&forecast_days=5&timezone=auto")
        val current = data.getJSONObject("current")
        val daily = data.getJSONObject("daily")
        val dates = daily.getJSONArray("time")
        if (dates.length() < 5) throw IOException("Weather provider did not return five forecast days")
        return Weather(
            current.getDouble("temperature_2m"), weatherCondition(current.getInt("weather_code")),
            current.getInt("relative_humidity_2m"), current.getDouble("wind_speed_10m"),
            List(5) { ForecastDay(dates.getString(it), daily.getJSONArray("temperature_2m_min").getDouble(it), daily.getJSONArray("temperature_2m_max").getDouble(it)) },
        )
    }
}

fun weatherCondition(code: Int): String = when (code) {
    0 -> "Clear sky"
    1 -> "Mainly clear"
    2 -> "Partly cloudy"
    3 -> "Overcast"
    45, 48 -> "Fog"
    51, 53, 55 -> "Drizzle"
    56, 57 -> "Freezing drizzle"
    61, 63, 65 -> "Rain"
    66, 67 -> "Freezing rain"
    71, 73, 75 -> "Snow"
    77 -> "Snow grains"
    80, 81, 82 -> "Rain showers"
    85, 86 -> "Snow showers"
    95 -> "Thunderstorm"
    96, 99 -> "Thunderstorm with hail"
    else -> "Weather code $code"
}

package app.sadhan.ui.tools

import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import app.sadhan.core.inputNumber
import app.sadhan.data.City
import app.sadhan.data.Weather
import app.sadhan.data.WeatherRepository
import app.sadhan.ui.ErrorText
import app.sadhan.ui.Input
import app.sadhan.ui.ResultCard
import app.sadhan.ui.ToolColumn
import kotlinx.coroutines.launch
import org.json.JSONException
import java.io.IOException

@Composable
fun WeatherScreen() {
    var query by remember { mutableStateOf("") }
    var cities by remember { mutableStateOf<List<City>>(emptyList()) }
    var selected by remember { mutableStateOf<City?>(null) }
    var weather by remember { mutableStateOf<Weather?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    ToolColumn {
        Input("City name", query, { query = it })
        Button(enabled = query.isNotBlank() && !busy, onClick = {
            scope.launch {
                busy = true
                error = null
                weather = null
                selected = null
                cities = emptyList()
                try {
                    cities = WeatherRepository.cities(query)
                    if (cities.isEmpty()) error = "No matching city. Try another spelling."
                } catch (e: IOException) { error = "Weather search failed: ${e.message}" }
                catch (e: JSONException) { error = "Invalid weather response: ${e.message}" }
                finally { busy = false }
            }
        }) { Text("Find city") }
        if (busy) CircularProgressIndicator()
        cities.forEach { city ->
            OutlinedButton(enabled = !busy, onClick = {
                scope.launch {
                    busy = true
                    error = null
                    weather = null
                    selected = city
                    try { weather = WeatherRepository.forecast(city) }
                    catch (e: IOException) { error = "Forecast failed: ${e.message}" }
                    catch (e: JSONException) { error = "Invalid forecast response: ${e.message}" }
                    finally { busy = false }
                }
            }) { Text("${city.name}, ${city.region}") }
        }
        ErrorText(error)
        weather?.let { forecast ->
            Text("${selected?.name}, ${selected?.region}")
            ResultCard(forecast.condition, "${inputNumber(forecast.temperature)} °C")
            ResultCard("Humidity / wind", "${forecast.humidity}% / ${inputNumber(forecast.wind)} km/h")
            forecast.days.forEach { day -> ResultCard(day.date, "${inputNumber(day.minimum)}° / ${inputNumber(day.maximum)}°") }
        }
        Text("Weather data: Open-Meteo (CC BY 4.0). City search only; no location permission.")
    }
}

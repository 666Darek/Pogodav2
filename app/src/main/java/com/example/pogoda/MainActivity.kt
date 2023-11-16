package com.example.pogoda

import GetWeather
import androidx.compose.ui.platform.LocalContext
import LocationHelper
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pogoda.ui.theme.PogodaTheme
import com.google.android.gms.location.LocationCallback
import kotlinx.coroutines.delay
import androidx.compose.ui.text.TextStyle as TextStyle1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val LOCATION_PERMISSION_REQUEST_CODE = 1234

class MainActivity : ComponentActivity() {

    private var hasLocationPermission by mutableStateOf(false)

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            hasLocationPermission = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            hasLocationPermission = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestLocationPermission()
        val getWeather = GetWeather()

        setContent {
            PogodaTheme {
                // Zmienne stanów
                var temperature by remember { mutableStateOf("Oczekiwanie na dane...") }
                var windInfo by remember { mutableStateOf("Oczekiwanie na dane o wietrze...") }
                var humidity by remember { mutableStateOf("Oczekiwanie na dane o wilgotności...") }
                var pressure by remember { mutableStateOf("Oczekiwanie na dane o ciśnieniu...") }
                var isDataLoaded by remember { mutableStateOf(false) }
                var backgroundType by remember { mutableStateOf("Sunny") }

                val viewModel: LocationViewModel = viewModel()
                val locationName = viewModel.cityName.observeAsState(initial = "Ładowanie...").value

                LaunchedEffect(locationName) {
                    if (locationName != "Ładowanie...") {
                        try {
                            val weatherInfo = getWeather.fetchWeather(locationName)
                            val emoji = extractEmoji(weatherInfo)

                            // Logowanie i aktualizacja stanów
                            Log.d("PogodaEmoji", "Emotka pogody: $emoji")
                            Log.d("PogodaEmoji", "Zapytanie: $weatherInfo")
                            isDataLoaded = true
                            backgroundType = if (emoji == "🌧️") "Rainy" else "Sunny"
                            //backgroundType = if (emoji == "⛅️") "Rainy" else "Sunny"
                            Log.d("PogodaEmoji", "BackGroundType: $backgroundType")
                            // Ekstrakcja danych pogodowych
                            temperature = extractTemperature(weatherInfo)
                            windInfo = extractWindInfo(weatherInfo)
                            humidity = extractHumidity(weatherInfo)
                            pressure = extractPressure(weatherInfo)
                        } catch (e: Exception) {
                            Log.e("PogodaDebug", "Error fetching weather data", e)
                        }
                    }
                }
                LocationPermissionHandler(hasLocationPermission) { location ->
                    location?.let {
                        viewModel.fetchCityName(it.latitude, it.longitude)
                    }
                }
                if (isDataLoaded) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                        if (backgroundType == "Rainy") {
                            RainyBackground {
                                FourTexts(locationName, temperature, windInfo, humidity, pressure)
                            }
                        } else {
                            SunnyBackground {
                                FourTexts(locationName, temperature, windInfo, humidity, pressure)
                            }
                        }
                    }
                } else {
                    // UI wyświetlane podczas ładowania danych
                    CircularProgressIndicator()
                }
            }
        }

    }

    private fun extractTemperature(weatherInfo: String): String {
        // Prosta logika do wyciągnięcia temperatury z odpowiedzi
        val temperatureRegex = Regex("(-?\\d+°C)")
        val matchResult = temperatureRegex.find(weatherInfo)
        return matchResult?.value ?: "Brak danych"
    }
    private fun extractWindInfo(weatherInfo: String): String {
        val windInfoRegex = Regex("\\d+km/h")
        val matchResult = windInfoRegex.find(weatherInfo)
        return matchResult?.value ?: "Brak danych o wietrze"
    }
    fun extractHumidity(weatherInfo: String): String {
        val humidityRegex = Regex("\\d+%")
        val matchResult = humidityRegex.find(weatherInfo)
        return matchResult?.value ?: "Brak danych o wilgotności"
    }

    fun extractPressure(weatherInfo: String): String {
        val pressureRegex = Regex("\\d+hPa")
        val matchResult = pressureRegex.find(weatherInfo)
        return matchResult?.value ?: "Brak danych o ciśnieniu"
    }
    // Funkcja do ekstrakcji emotki z odpowiedzi pogodowej
    private fun extractEmoji(weatherInfo: String): String {
        //val emojiRegex = Regex("\uD83C\uDF26")
        val emojiRegex = Regex("\uD83C\uDF27|⛅️")
        val matchResult = emojiRegex.find(weatherInfo)
        return matchResult?.value ?: ""
    }

}


@Composable
fun RainyBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.backgroundrainy),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        content()
    }
}

@Composable
fun SunnyBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.sunnybackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRainyBackgroundWithTexts() {
    RainyBackground {
       // FourTexts()
    }
}

@Composable
fun FourTexts(locationName: String, temperature: String, windInfo: String, humidity: String, pressure: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicText("Pogoda", style = TextStyle1(
            fontSize = 72.sp,
            color = Color.White
        ))
        Spacer(modifier = Modifier.height(150.dp)) // Dostosuj odstęp według potrzeb
        BasicText(locationName, style = TextStyle1(
            fontSize = 24.sp,
            color = Color.White
        ))
        Spacer(modifier = Modifier.height(20.dp))
        BasicText("Temperatura: $temperature", style = TextStyle1(fontSize = 24.sp, color = Color.White))
        Spacer(modifier = Modifier.height(20.dp))
        BasicText("Wiatr: $windInfo", style = TextStyle1(fontSize = 24.sp, color = Color.White))
        Spacer(modifier = Modifier.height(20.dp))
        BasicText("Wilgotność: $humidity", style = TextStyle1(fontSize = 24.sp, color = Color.White))
        Spacer(modifier = Modifier.height(20.dp))
        BasicText("Ciśnienie: $pressure", style = TextStyle1(fontSize = 24.sp, color = Color.White))
        // ... inne elementy, jeśli są potrzebne ...
    }
}




@Preview(showBackground = true)
@Composable
fun PreviewFourTexts() {
    //FourTexts()
}

@Composable
fun LocationPermissionHandler(
    hasPermission: Boolean,
    onLocationFetched: (Location?) -> Unit // Callback do obsługi pobranej lokalizacji
) {
    val context = LocalContext.current

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            // Pobierz lokalizację tylko wtedy, gdy uprawnienia są przyznane
            val locationHelper = LocationHelper(context as Activity)
            locationHelper.getCurrentLocation { location ->
                if (location != null) { // Sprawdź czy lokalizacja nie jest null
                    onLocationFetched(location) // Wywołanie callback z pobraną lokalizacją
                    // Loguj szerokość i długość geograficzną
                    Log.d("LocationHelper", "Nowa lokalizacja: szerokość=${location.latitude}, długość=${location.longitude}")
                } else {
                    Log.d("LocationHelper", "Lokalizacja nie została znaleziona")
                }
            }
        } else {
            Log.d("LocationHelper", "Brak uprawnień do lokalizacji")
        }
    }
}



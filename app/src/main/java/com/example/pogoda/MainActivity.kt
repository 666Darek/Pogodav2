package com.example.pogoda

import androidx.compose.ui.platform.LocalContext
import LocationHelper
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pogoda.ui.theme.PogodaTheme
import androidx.compose.ui.text.TextStyle as TextStyle1

private const val LOCATION_PERMISSION_REQUEST_CODE = 1234

class MainActivity : ComponentActivity() {
    private var locationName by mutableStateOf("Ładowanie...")
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Pozwolenie zostało udzielone
            } else {
                // Pozwolenie zostało odrzucone
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestLocationPermission()
        setContent {
            PogodaTheme {
                val viewModel: LocationViewModel = viewModel()
                val locationName = viewModel.cityName.observeAsState(initial = "Ładowanie...").value

                val context = LocalContext.current // Przenieś to na zewnątrz LaunchedEffect

//                LaunchedEffect(Unit) {
//                    val locationHelper = LocationHelper(this@MainActivity)
//
//                    if (activity != null) {
//                        val locationHelper = LocationHelper(activity)
//
//                        locationHelper.getCurrentLocation { location ->
//                            location?.let {
//                                viewModel.fetchCityName(51.1005, 17.0229)
//                            }
//                        }
//                    } else {
//                        Log.e("LocationHelper", "Failed to get Activity from context.")
//                    }
//                }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    RainyBackground {
                        FourTexts(locationName = locationName)
                    }
                }
            }
        }

    }
}



@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PogodaTheme {
        Greeting("Android")
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

@Preview(showBackground = true)
@Composable
fun PreviewRainyBackgroundWithTexts() {
    RainyBackground {
       // FourTexts()
    }
}

@Composable
fun FourTexts(locationName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally  // Dodane wyrównanie w poziomie
    ) {
        BasicText(locationName, style = TextStyle1(
            fontSize = 24.sp,
            color = Color.White /* , fontFamily = customFontFamily */
        ))
        Spacer(modifier = Modifier.height(20.dp))
        BasicText("Tekst 2", style = TextStyle1(fontSize = 24.sp, color = Color.White /* , fontFamily = customFontFamily */))
        Spacer(modifier = Modifier.height(20.dp))
        BasicText("Tekst 3", style = TextStyle1(fontSize = 24.sp, color = Color.White /* , fontFamily = customFontFamily */))
        Spacer(modifier = Modifier.height(20.dp))
        BasicText("Tekst 4", style = TextStyle1(fontSize = 24.sp, color = Color.White /* , fontFamily = customFontFamily */))
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewFourTexts() {
    //FourTexts()
}
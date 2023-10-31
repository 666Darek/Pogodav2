package com.example.pogoda

import android.util.Log
import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Locale

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val _cityName = MutableLiveData<String>("Ładowanie...")
    val cityName: LiveData<String> get() = _cityName

    fun fetchCityName(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                Log.d("LocationVM", "Fetching city name for: $latitude, $longitude")

                val geocoder = Geocoder(getApplication(), Locale.getDefault())
                val addressList = geocoder.getFromLocation(latitude, longitude, 1)

                if (addressList != null && addressList.isNotEmpty()) {
                    _cityName.value = addressList[0].locality ?: "Nieznane miasto"
                    Log.d("LocationVM", "City name fetched: ${_cityName.value}")
                } else {
                    _cityName.value = "Nie można uzyskać nazwy miasta"
                }
            } catch (e: Exception) {
                Log.e("LocationVM", "Error fetching city name", e)
                _cityName.value = "Błąd podczas pobierania nazwy miasta"
            }
        }
    }
}

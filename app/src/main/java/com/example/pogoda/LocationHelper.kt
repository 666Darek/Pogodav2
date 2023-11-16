import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class LocationHelper(private val activity: Activity) {
    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
    private var locationCallback: LocationCallback? = null // Referencja do LocationCallback
    private val locationRequest = LocationRequest.create().apply {
        interval = 6 // 10 minut między aktualizacjami
        fastestInterval = 3 // 5 minut jako najszybszy interwał, ale używane tylko w razie potrzeby
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    // Można zmienić strategię związane z otrzymywaniem pierwszej lokalizacji
    // Tutaj przy pierwszym żądaniu użyjemy metody getLastLocation(), a potem cyklicznych aktualizacji
    fun getCurrentLocation(callback: (Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            callback(null)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                callback(location)
                // Po otrzymaniu jednorazowej lokalizacji, uruchom regularne aktualizacje
                startLocationUpdates(callback)
            } else {
                // Jeśli getLastLocation() nie dostarczył lokalizacji, użyj requestLocationUpdates() bezpośrednio
                startLocationUpdates(callback)
            }
        }
    }

    private fun startLocationUpdates(callback: (Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Tworzenie obiektu LocationRequest z odpowiednimi parametrami
            val locationRequest = LocationRequest.create().apply {
                interval = 6 // 10 minut między aktualizacjami
                fastestInterval = 5 // 5 sekund jako najszybszy interwał, dla przypadku gdy inne aplikacje aktualizują lokalizację
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            // Inicjalizacja LocationCallback
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return // Jeśli locationResult jest null, wyjście z funkcji
                    for (location in locationResult.locations) {
                        callback(location) // Wywołanie callback z aktualną lokalizacją
                    }
                }
            }

            // Rejestracja LocationCallback wraz z LocationRequest w FusedLocationProviderClient
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            // Użytkownik nie przyznał uprawnień, możesz zareagować odpowiednio
            callback(null) // Wywołanie callback z null, aby zasygnalizować brak dostępu do lokalizacji
        }
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }
}

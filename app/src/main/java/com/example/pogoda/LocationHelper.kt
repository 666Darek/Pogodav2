import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationHelper(private val activity: Activity) {

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)

    fun getCurrentLocation(callback: (Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback(null)
            return
        }

        Log.d("LocationHelper", "Before attempting to get location")
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                Log.d("LocationHelper", "Location retrieved")
                callback(location)
                if (location != null) {
                    Log.d("LocationHelper", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                } else {
                    Log.d("LocationHelper", "Location is null")

                }
            }
    }
}


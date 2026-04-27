package com.example.hronline.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCurrentLocation(context: Context): LocationResult {
        if (!hasLocationPermission(context)) {
            return LocationResult.PermissionDenied
        }

        return try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val location = getLocation(fusedClient)
            if (location != null) {
                val address = reverseGeocode(context, location.latitude, location.longitude)
                LocationResult.Success(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    address = address ?: "Lat: ${location.latitude}, Lon: ${location.longitude}"
                )
            } else {
                LocationResult.Error("Gagal mendapatkan lokasi")
            }
        } catch (e: SecurityException) {
            LocationResult.PermissionDenied
        } catch (e: Exception) {
            LocationResult.Error(e.message ?: "Gagal mendapatkan lokasi")
        }
    }

    @SuppressWarnings("MissingPermission")
    private suspend fun getLocation(
        fusedClient: FusedLocationProviderClient
    ): android.location.Location? = suspendCancellableCoroutine { cont ->
        try {
            fusedClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(location)
                } else {
                    val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                        .setWaitForAccurateLocation(false)
                        .setMaxUpdates(1)
                        .build()

                    val callback = object : LocationCallback() {
                        override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                            fusedClient.removeLocationUpdates(this)
                            cont.resume(result.lastLocation)
                        }
                    }
                    fusedClient.requestLocationUpdates(request, callback, android.os.Looper.getMainLooper())
                    cont.invokeOnCancellation { fusedClient.removeLocationUpdates(callback) }
                }
            }.addOnFailureListener {
                cont.resume(null)
            }
        } catch (e: SecurityException) {
            cont.resume(null)
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun reverseGeocode(
        context: Context, lat: Double, lon: Double
    ): String? = suspendCancellableCoroutine { cont ->
        try {
            val geocoder = Geocoder(context, Locale.forLanguageTag("id-ID"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(lat, lon, 1) { addresses ->
                    val addr = addresses.firstOrNull()
                    val result = if (addr != null) {
                        listOfNotNull(addr.thoroughfare, addr.subLocality, addr.locality, addr.subAdminArea)
                            .joinToString(", ").ifEmpty { "${addr.latitude}, ${addr.longitude}" }
                    } else null
                    cont.resume(result)
                }
            } else {
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                val addr = addresses?.firstOrNull()
                val result = if (addr != null) {
                    listOfNotNull(addr.thoroughfare, addr.subLocality, addr.locality, addr.subAdminArea)
                        .joinToString(", ").ifEmpty { "${addr.latitude}, ${addr.longitude}" }
                } else null
                cont.resume(result)
            }
        } catch (e: Exception) {
            cont.resume(null)
        }
    }

    sealed class LocationResult {
        data class Success(val latitude: Double, val longitude: Double, val address: String) : LocationResult()
        data object PermissionDenied : LocationResult()
        data class Error(val message: String) : LocationResult()
    }
}

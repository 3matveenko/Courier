package com.example.courier.rest

import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.courier.models.GetSettings
import com.example.courier.models.LocationMy
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson

class SendLocation {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    fun requestLocation(context:Context) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
         if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                ) {
                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        val location = locationResult.lastLocation
                        val latitude = location.latitude
                        val longitude = location.longitude
                        Toast.makeText(context.applicationContext, "Широта: $latitude, Долгота: $longitude", Toast.LENGTH_LONG).show()

                        val gson = Gson()
                        val body = gson.toJson(LocationMy(latitude, longitude))
                        val token = GetSettings(context).load("token").toString()
                        Rabbit(context).sendMessage(token,"location",body)
                    }
                }

                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val locationRequest = LocationRequest()
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                locationRequest.interval = 60000
                locationRequest.smallestDisplacement = 50.0f

                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }
}
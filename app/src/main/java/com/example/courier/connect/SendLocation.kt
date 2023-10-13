package com.example.courier.connect

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.courier.models.GetSettings
import com.example.courier.models.LocationMy
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson

class SendLocation(context: Context) : LocationListener {

    private val _context:Context = context
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onLocationChanged(location: Location) {
    }

     override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    fun request(){
        while (true){
            Thread.sleep(10000)
            checkLocationStatus(_context)
        }
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun checkLocationStatus(context: Context) {
        if (!isLocationEnabled(context)) {
            requestLocationEnabled(context)
        }
    }

    fun requestLocationEnabled(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }
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
                        val token = GetSettings(context).load("token")
                        Log.d("courier_log", "SendLocation передал координаты в Rabbit для отправки")
                        Rabbit(context).sendMessage(token,"location",body)
                    }
                }

             try {
                 val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                 val locationRequest = LocationRequest()
                 locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                 locationRequest.interval = 60000
                 locationRequest.smallestDisplacement = 50.0f
                 fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
             } catch (e: Exception){
                 Log.e("courier_log", "SendLocation Ошибка получения координат")
                 Toast.makeText(context.applicationContext, "Ошибка получения координат", Toast.LENGTH_LONG).show()
             }
        }
    }
}
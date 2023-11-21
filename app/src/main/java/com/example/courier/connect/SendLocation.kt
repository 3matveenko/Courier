package com.example.courier.connect

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
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
import java.util.concurrent.Executors

class SendLocation() : LocationListener , Service() {

//    private val _context:Context = context

    private val executor = Executors.newSingleThreadExecutor()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        executor.submit {
            requestLocation(applicationContext)
        }
//        val backgroundThread = Thread {
//            requestLocation(applicationContext)
//        }
//        backgroundThread.start()



        return START_STICKY
    }


    override fun onLocationChanged(location: Location) {
    }

     override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

//    fun request(){
//        while (true){
//            Thread.sleep(10000)
//            checkLocationStatus(_context)
//        }
//    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkLocationStatus(context: Context) {
        if (!isLocationEnabled(context)) {
            requestLocationEnabled(context)
        }
    }

    fun requestLocationEnabled(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }
    fun requestLocation(context:Context) {
        while (true){

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
                try {
                    // Получение последнего известного местоположения
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            if (location != null) {
                                val latitude = location.latitude
                                val longitude = location.longitude
                                // Отправка координат через RabbitMQ
                                val gson = Gson()
                                val body = gson.toJson(LocationMy(latitude, longitude))
                                val token = GetSettings(context).load("token")
                                Log.d("courier_log", "SendLocation передал координаты в Rabbit для отправки")
                                Rabbit(context).sendMessage(token, "location", body)

                                // Закрытие соединения
                                //fusedLocationClient.removeLocationUpdates(locationCallback)
                            } else {
                                // Местоположение не доступно
                                Log.e("courier_log", "SendLocation Местоположение не доступно")
                                Toast.makeText(context.applicationContext, "Местоположение не доступно", Toast.LENGTH_LONG).show()
                            }
                        }
                } catch (e: Exception) {
                    Log.e("courier_log", "SendLocation Ошибка получения координат")
                    Toast.makeText(context.applicationContext, "Ошибка получения координат", Toast.LENGTH_LONG).show()
                }
            }

            /*
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
                        //Toast.makeText(context.applicationContext, "Широта: $latitude, Долгота: $longitude", Toast.LENGTH_LONG).show()

                        val gson = Gson()
                        val body = gson.toJson(LocationMy(latitude, longitude))
                        val token = GetSettings(context).load("token")
                        Log.d("courier_log", "SendLocation передал координаты в Rabbit для отправки")
                        Rabbit(context).sendMessage(token,"location",body)
                    }
                }

                try {
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                    val locationRequest = LocationRequest.create()
                        .setInterval(60000) // Интервал получения локации в миллисекундах (в данном случае, 1 минута)
                        .setFastestInterval(30000)
                        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                        .setSmallestDisplacement(10.0f)
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                } catch (e: Exception){
                    Log.e("courier_log", "SendLocation Ошибка получения координат")
                    Toast.makeText(context.applicationContext, "Ошибка получения координат", Toast.LENGTH_LONG).show()
                }
            } else{
                Toast.makeText(context.applicationContext, "Ошибка получения координат", Toast.LENGTH_LONG).show()
            }*/
            Thread.sleep(60000)
        }



    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
        return null
    }
}
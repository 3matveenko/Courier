package com.example.courier

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.courier.activity.HomeActivity
import com.example.courier.activity.RegistrActivity
import com.example.courier.models.GetSettings
import com.example.courier.models.LoginDriver
import com.example.courier.models.Setting
import com.example.courier.models.isNotNull
import com.example.courier.rest.Http
import com.example.courier.rest.MyBroadcastReceiver
import com.example.courier.rest.Rabbit
import com.example.courier.rest.SendLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class MainActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_CODE = 101
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.login)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Дайте разрешение выводить приложение поверх других окон!", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + packageName)
            )
            startActivity(intent)
        }

        Log.e("debuggп", "вызвал startListening")
        Rabbit(applicationContext).startListening()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fun isLocationEnabled(context: Context): Boolean {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }

        fun requestLocationEnabled(context: Context) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
        }

        fun checkLocationStatus(context: Context) {
            if (!isLocationEnabled(context)) {
                // Геолокация не включена, предложим включить её
                requestLocationEnabled(context)
            } else {
                // Геолокация включена, продолжаем работу приложения
                // Здесь можно добавить код для вашего приложения
            }
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            SendLocation().requestLocation(this)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        }

        if(!GetSettings(this).isNull("token")){
            startActivity(Intent(this@MainActivity, HomeActivity::class.java))
        }

        findViewById<Button>(R.id.but_registr).setOnClickListener {
            startActivity(Intent(this@MainActivity, RegistrActivity::class.java))
        }

        findViewById<Button>(R.id.but_registr).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.base_color))
        findViewById<Button>(R.id.but_enter).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.base_color))
        findViewById<Button>(R.id.but_qr).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.base_color))

        findViewById<Button>(R.id.but_qr).setOnClickListener {
            startQr()
        }

        findViewById<Button>(R.id.but_enter).setOnClickListener {
            val login = findViewById<EditText>(R.id.editEmail).text.toString()
            val password = findViewById<EditText>(R.id.editPassword).text.toString()
            if(login.isEmpty()||password.isEmpty()){
                Toast.makeText(this, "Поле должно быть заполнено", Toast.LENGTH_SHORT).show()
            } else {
                val rootLayout = findViewById<ConstraintLayout>(R.id.loginLayout)
                val childCount = rootLayout.childCount
                for (i in 0 until childCount) {
                    val child = rootLayout.getChildAt(i)
                    child.visibility = View.GONE
                }

                findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
                Http(this ).login(LoginDriver(login, password))
            }
        }

        if (GetSettings(applicationContext).isNull(GetSettings.SERVER_NAME)) {
            Toast.makeText(this, GetSettings(applicationContext).load(GetSettings.SERVER_NAME),Toast.LENGTH_LONG).show()
            startQr()
        }

        val intentFilter = IntentFilter("no_connection")
        val receiver = MyBroadcastReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)

        val intentFilter2 = IntentFilter("open_new_order")
        val receiver2 = MyBroadcastReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver2, intentFilter2)
    }

    private fun startQr() {
        val integrator = IntentIntegrator(this)
        integrator.setOrientationLocked(false)
        integrator.setPrompt("Отсканируйте QR-код у администратора")
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents == null) {
                // Если сканирование было отменено
                Toast.makeText(this, "Сканирование отменено", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                val gson = Gson()
                val setting:Setting = gson.fromJson(result.contents, Setting::class.java)
                 if(isNotNull(setting)){
                     GetSettings(applicationContext).save(GetSettings.SERVER_NAME,setting.SERVER_NAME)
                     GetSettings(applicationContext).save(GetSettings.RABBIT_SERVER_NAME,setting.RABBIT_SERVER_NAME)
                     Toast.makeText(this, GetSettings(applicationContext).load(GetSettings.SERVER_NAME),Toast.LENGTH_LONG).show()
                 }
                this.recreate()
            }
        }
    }
}
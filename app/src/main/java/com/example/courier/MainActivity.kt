package com.example.courier

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.courier.activity.HomeActivity
import com.example.courier.activity.RegistrActivity
import com.example.courier.connect.Http
import com.example.courier.connect.MyBroadcastReceiver
import com.example.courier.connect.Rabbit
import com.example.courier.connect.SendLocation
import com.example.courier.enums.SettingsValue
import com.example.courier.models.GetSettings
import com.example.courier.models.LoginDriver
import com.example.courier.models.Setting
import com.example.courier.models.isNotNull
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class MainActivity : AppCompatActivity() {

    companion object {
        var connectionFlag: Boolean = false
        const val LOG_TAG: String = "COURIER_LOG"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId", "InvalidWakeLockTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.login)

        ((applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock"))
        .acquire(1200*60*1000L)

        Log.d("courier_log", "MainActivity ***START app Courier***")
        if (!GetSettings(this).isNull(SettingsValue.SERVER_NAME)) {
//            val pingServerServiceIntent = Intent(this, PingServer::class.java)
//            startForegroundService(pingServerServiceIntent)

            val serviceIntent = Intent(this, SendLocation::class.java)
            startForegroundService(serviceIntent)

//            Thread(Runnable {
//                Log.d("courier_log", "(MainActivity Перехожу в PingServer")
//
//            }).start()
        } else {
            Toast.makeText(this,"Отсканируйте настройки у администратора!",Toast.LENGTH_LONG).show()
            Log.e("courier_log", "MainActivity SERVER_NAME не обнаружен")
            startQr()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(
                this,
                "Дайте разрешение выводить приложение поверх других окон!",
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + packageName)
            )
            startActivity(intent)
        }

        broadcastIni()

        var token = GetSettings(this).load(SettingsValue.TOKEN)
        Log.d("courier_log", "MainActivity token = $token")
        if (!GetSettings(this).isNull(SettingsValue.TOKEN)) {
            Rabbit(applicationContext).startListening()





            startActivity(Intent(this@MainActivity, HomeActivity::class.java))
        }

        findViewById<Button>(R.id.but_registr).setOnClickListener {
            Log.d("courier_log", "(MainActivity Перехожу в RegistrActivity")
            startActivity(Intent(this@MainActivity, RegistrActivity::class.java))
        }

        findViewById<Button>(R.id.but_qr).setOnClickListener {
            startQr()
        }

        findViewById<Button>(R.id.but_enter).setOnClickListener {
            if (connectionFlag) {
                if (!GetSettings(this).isNull(SettingsValue.SERVER_NAME)) {
                    val login = findViewById<EditText>(R.id.editEmail).text.toString()
                    val password = findViewById<EditText>(R.id.editPassword).text.toString()
                    if (login.isEmpty() || password.isEmpty()) {
                        Toast.makeText(this, "Поле должно быть заполнено", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        val rootLayout = findViewById<ConstraintLayout>(R.id.loginLayout)
                        val childCount = rootLayout.childCount
                        for (i in 0 until childCount) {
                            val child = rootLayout.getChildAt(i)
                            child.visibility = View.GONE
                        }

                        findViewById<ProgressBar>(R.id.progressBarHomeActivity).visibility =
                            View.VISIBLE
                        Http(this).login(LoginDriver(login, password))
                    }
                } else {
                    Toast.makeText(
                        this,
                        "отсканируйте QR код у администратора!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, "нет интернета!", Toast.LENGTH_SHORT).show()
            }

        }
    }

    fun broadcastIni() {
        val intentFilter = IntentFilter("no_connection")
        val receiver = MyBroadcastReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)

        val intentFilter2 = IntentFilter("open_new_order")
        val receiver2 = MyBroadcastReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver2, intentFilter2)

        val intentFilter3 = IntentFilter("my_orders")
        val receiver3 = MyBroadcastReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver3, intentFilter3)
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
                Toast.makeText(this, "Сканирование отменено", Toast.LENGTH_SHORT).show()
                //finish()
            } else {
                try {
                    val gson = Gson()
                    val setting: Setting = gson.fromJson(result.contents, Setting::class.java)
                    if (isNotNull(setting)) {
                        GetSettings(applicationContext).save(SettingsValue.PROTOCOL, setting.protocol)
                        GetSettings(applicationContext).save(
                            SettingsValue.BACK_QUEUE_NAME,
                            setting.backQueueName
                        )
                        GetSettings(applicationContext).save(SettingsValue.SERVER_NAME, setting.serverName)
                        GetSettings(applicationContext).saveInt(
                            SettingsValue.SERVER_PORT,
                            setting.serverPort
                        )
                        GetSettings(applicationContext).save(
                            SettingsValue.RABBIT_SERVER_NAME,
                            setting.rabbitServerName
                        )
                        GetSettings(applicationContext).saveInt(
                            SettingsValue.RABBIT_SERVER_PORT,
                            setting.rabbitServerPort
                        )
                        GetSettings(applicationContext).save(
                            SettingsValue.RABBIT_USERNAME,
                            setting.rabbitUsername
                        )
                        GetSettings(applicationContext).save(
                            SettingsValue.RABBIT_PASSWORD,
                            setting.rabbitPassword
                        )
                        Toast.makeText(this, "QR-код отсканирован", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("courier_log", "QR-код не распознан $e")
                    Toast.makeText(this, "QR-код не распознан", Toast.LENGTH_SHORT).show()
                }

                this.recreate()
            }
        }
    }
}
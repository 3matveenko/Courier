package com.example.courier.connect

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.util.Log
import android.widget.Switch
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.courier.R
import com.example.courier.activity.HomeActivity
import com.example.courier.models.CreateDriver
import com.example.courier.models.GetSettings
import com.example.courier.models.GetSettings.Companion.TOKEN
import com.example.courier.models.LoginDriver
import com.example.courier.models.Message
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.zxing.integration.android.IntentIntegrator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Modifier
import java.util.Date

class Http(private var activity: AppCompatActivity) {
    private lateinit var api: API

    private var gson: Gson = GsonBuilder()
        .setLenient()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .excludeFieldsWithModifiers(
            Modifier.STATIC,
            Modifier.TRANSIENT,
            Modifier.VOLATILE
        )
        .create()

    init {
        do {
            var flag = false
            if (PingServer(activity).connection()) {
                val server: String =
                    GetSettings(activity).load(GetSettings.PROTOCOL) +
                            GetSettings(activity).load(GetSettings.SERVER_NAME) + ":" +
                            GetSettings(activity).load(GetSettings.SERVER_PORT)
                Log.e("courier_log", "retrofit init $server")
                val retrofit = server.let {
                    Retrofit.Builder()
                        .baseUrl(it)
                        .addConverterFactory(GsonConverterFactory.create(this.gson))
                        .build()
                }
                if (retrofit != null) {
                    this.api = retrofit.create(API::class.java)
                }
            } else {
                Log.e("courier_log", "Http init disconnect")
                Thread.sleep(5000)
                flag = true
            }
        } while (flag)
    }

    fun registr(createDriver: CreateDriver) {
        this.api.createAccount(
            createDriver
        ).enqueue(object : Callback<String> {
            @SuppressLint("CutPasteId", "SetTextI18n", "SimpleDateFormat")
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code() == 200) {
                    val intent = Intent(activity, HomeActivity::class.java)
                    GetSettings(activity).save(TOKEN, response.body().toString())
                    Toast.makeText(activity.applicationContext, "Вы успешно прошли регистрацию", Toast.LENGTH_LONG).show()
                    activity.startActivity(intent)
                }
                if (response.code() == 505) {
                    Toast.makeText(activity.applicationContext, "Такой логин занят", Toast.LENGTH_LONG).show()
                    activity.recreate()
                }
            }

            override fun onFailure(call: Call<String>, throwable: Throwable) {
                throwable.localizedMessage?.let { Log.e("httpConnect", it) }
                Toast.makeText(activity, "Ошибка подключения", Toast.LENGTH_LONG).show()
                val integrator = IntentIntegrator(activity)
                integrator.setOrientationLocked(false)
                integrator.setPrompt("Отсканируйте QR-код у администратора")
                integrator.initiateScan()
            }
        })
    }

    fun login(loginDriver: LoginDriver) {
        this.api.login(
            loginDriver
        ).enqueue(object : Callback<String> {
            @SuppressLint("CutPasteId", "SetTextI18n", "SimpleDateFormat")
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code() == 200) {
                    val intent = Intent(activity, HomeActivity::class.java)
                    GetSettings(activity).save(TOKEN, response.body().toString())
                    activity.startActivity(intent)
                }
                if (response.code() == 403) {
                    Toast.makeText(activity.applicationContext, "Не верные данные авторизации", Toast.LENGTH_LONG).show()
                    activity.recreate()
                }
            }

            override fun onFailure(call: Call<String>, throwable: Throwable) {
                throwable.localizedMessage?.let { Log.e("httpConnect", it) }
                Toast.makeText(activity, "Ошибка подключения", Toast.LENGTH_LONG).show()
                val integrator = IntentIntegrator(activity)
                integrator.setOrientationLocked(false)
                integrator.setPrompt("Отсканируйте QR-код у администратора")
                integrator.initiateScan()
            }
        })
    }

    fun statusDay(message: Message, flag: Boolean) {
        this.api.statusDay(flag,
            message
        ).enqueue(object : Callback<String> {
            @RequiresApi(Build.VERSION_CODES.M)
            @SuppressLint("CutPasteId", "SetTextI18n", "SimpleDateFormat")
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    if (response.body() == "false") {
                        drawSwitch(false)
                    } else if (response.body() == "true") {
                        Log.d("courier_log", "A1 - " + System.currentTimeMillis().toString())
                        drawSwitch(true)
                    } else {
                        //Log.d("courier_log", "Response Body: $response")
                        Log.d("courier_log", "A1 - " + System.currentTimeMillis().toString())
                        Toast.makeText(activity, "Response Body: $response", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.e("courier_log", "Не удалось выполнить запрос. Код ошибки: ${response.code()}")
                    Toast.makeText(activity, "Не удалось выполнить запрос. Код ошибки: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<String>, throwable: Throwable) {
                Log.e("courier_log", "Не удалось выполнить запрос на сервер.")
            }
        })



    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @RequiresApi(Build.VERSION_CODES.M)
    fun drawSwitch(boolean: Boolean){
        val switch = (activity as Activity).findViewById<Switch>(R.id.switchView)
        val colorGreen = ContextCompat.getColor(activity, R.color.green)
        val colorRed = ContextCompat.getColor(activity, R.color.red)
        if(boolean){
            switch.text = "Свободен"
            switch.setTextColor(colorGreen)
            val thumbColor = ColorStateList.valueOf(colorGreen)
            switch.thumbTintList = thumbColor
        } else {
            switch.text = "Занят"
            switch.setTextColor(colorRed)
            val thumbColor = ColorStateList.valueOf(colorRed)
            switch.thumbTintList = thumbColor
        }
    }
}
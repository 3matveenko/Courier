package com.example.courier.rest

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.courier.activity.HomeActivity
import com.example.courier.models.CreateDriver
import com.example.courier.models.GetSettings
import com.example.courier.models.LoginDriver
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.zxing.integration.android.IntentIntegrator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Modifier

class Http(private var activity: AppCompatActivity) {
    private lateinit var api: API
    private lateinit var authorization: String


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
            val retrofit = GetSettings(activity).load(GetSettings.SERVER_NAME)?.let {
                Retrofit.Builder()
                    .baseUrl(it)
                    .addConverterFactory(GsonConverterFactory.create(this.gson))
                    .build()
            }
        if (retrofit != null) {
            this.api = retrofit.create(API::class.java)
        }

        }

    fun registr(createDriver: CreateDriver) {
        this.api.createAccount(
            createDriver
        ).enqueue(object : Callback<String> {
            @SuppressLint("CutPasteId", "SetTextI18n", "SimpleDateFormat")
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code() == 200) {
                    val intent = Intent(activity, HomeActivity::class.java)
                    GetSettings(activity).save("token", response.body().toString())
                    Toast.makeText(activity.applicationContext, "Вы успешно прошли регистрацию", Toast.LENGTH_LONG).show()
                    activity.startActivity(intent)
                }
                if (response.code() == 505) {
                    Toast.makeText(activity.applicationContext, "Такой логин занят", Toast.LENGTH_LONG).show()
                    activity.recreate()
                }
            }

            override fun onFailure(call: Call<String>, throwable: Throwable) {
                Log.e("httpConnect", throwable.localizedMessage)
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
                    GetSettings(activity).save("token", response.body().toString())
                    activity.startActivity(intent)
                }
                if (response.code() == 403) {
                    Toast.makeText(activity.applicationContext, "Не верные данные авторизации", Toast.LENGTH_LONG).show()
                    activity.recreate()
                }
            }

            override fun onFailure(call: Call<String>, throwable: Throwable) {
                Log.e("httpConnect", throwable.localizedMessage)
                Toast.makeText(activity, "Ошибка подключения", Toast.LENGTH_LONG).show()
                val integrator = IntentIntegrator(activity)
                integrator.setOrientationLocked(false)
                integrator.setPrompt("Отсканируйте QR-код у администратора")
                integrator.initiateScan()
            }
        })
    }
}
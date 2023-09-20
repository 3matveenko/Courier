package com.example.courier.rest

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.courier.R
import com.example.courier.activity.HomeActivity
import com.example.courier.models.CreateDriver
import com.example.courier.models.Settings
import com.example.courier.models.Token
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.zxing.integration.android.IntentIntegrator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.await
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Modifier

class Http(private var activity: AppCompatActivity, private var context: Context) {
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
            val retrofit = Settings(context).load(Settings.SERVER_NAME)?.let {
                Retrofit.Builder()
                    .baseUrl(it)
                    .addConverterFactory(GsonConverterFactory.create(this.gson))
                    .build()
            }
        if (retrofit != null) {
            this.api = retrofit.create(API::class.java)
        }

        }


    fun set(createDriver: CreateDriver) {
        //(activity as DocumentActivity).view(true)

        this.api.createAccount(
            createDriver
        ).enqueue(object : Callback<String> {
            @SuppressLint("CutPasteId", "SetTextI18n", "SimpleDateFormat")
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code() == 200) {
                    val intent = Intent(context, HomeActivity::class.java)
                    Settings(activity).save("token", response.body().toString())
                    activity.startActivity(intent)
                }
                //(activity as DocumentActivity).view(false)
            }

            override fun onFailure(call: Call<String>, throwable: Throwable) {
                Log.e("httpConnect", throwable.localizedMessage)
                Toast.makeText(context, "Ошибка подключения", Toast.LENGTH_LONG).show()
                activity.setContentView(R.layout.activity_qr_scanner)
                val integrator = IntentIntegrator(activity)
                integrator.setOrientationLocked(false)
                integrator.setPrompt("Отсканируйте QR-код у администратора")
                integrator.initiateScan()

                //(activity as DocumentActivity).view(false)
            }
        })
    }
}
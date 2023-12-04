package com.example.courier.connect

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.courier.MainActivity
import com.example.courier.R
import com.example.courier.activity.HomeActivity
import com.example.courier.activity.RegistrActivity
import com.example.courier.enums.RabbitCode
import com.example.courier.enums.SettingsValue
import com.example.courier.models.CreateDriver
import com.example.courier.models.GetSettings
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

@SuppressLint("SuspiciousIndentation")
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
        //do {
            var flag = false
            //if (MainActivity.connectionFlag) {
                val server: String =GetSettings(activity).getURI()
                Log.d("courier_log", "retrofit init $server")
                val retrofit = server.let {
                    Retrofit.Builder()
                        .baseUrl(it)
                        .addConverterFactory(GsonConverterFactory.create(this.gson))
                        .build()
                }
                if (retrofit != null) {
                    this.api = retrofit.create(API::class.java)
                }
//            } else {
//                Log.e("courier_log", "Http init disconnect")
//                Thread.sleep(5000)
//                flag = true
//            }
       // } while (flag)
    }

    fun registr(createDriver: CreateDriver) {
        this.api.createAccount(
            createDriver
        ).enqueue(object : Callback<String> {
            @SuppressLint("CutPasteId", "SetTextI18n", "SimpleDateFormat")
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code() == 200) {

                    GetSettings(activity).save(SettingsValue.TOKEN.value, response.body().toString())
                    Rabbit(activity).sendMessage(GetSettings(activity).load(SettingsValue.TOKEN.value),RabbitCode.GET_MY_ORDERS_STATUS_PROGRESSING,"")
                    val intent = Intent(activity, MainActivity::class.java)
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

                val rootLayout = (activity as Activity).findViewById<ConstraintLayout>(R.id.registrActivity)
                val childCount = rootLayout.childCount
                for (i in 0 until childCount) {
                    val child = rootLayout.getChildAt(i)
                    child.visibility = View.VISIBLE

                }
                (activity as Activity).findViewById<ProgressBar>(R.id.progressBarHomeActivity).visibility =
                    View.GONE
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
                    GetSettings(activity).save(SettingsValue.TOKEN.value, response.body().toString())
                    Rabbit(activity).sendMessage(GetSettings(activity).load(SettingsValue.TOKEN.value),RabbitCode.GET_MY_ORDERS_STATUS_PROGRESSING,"")
//                    Thread(Runnable {
//                        SendLocation(activity).requestLocation(activity)
//                    }).start()


                    val intent = Intent(activity, HomeActivity::class.java)
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

                val rootLayout = (activity as Activity).findViewById<ConstraintLayout>(R.id.loginLayout)
                val childCount = rootLayout.childCount
                for (i in 0 until childCount) {
                    val child = rootLayout.getChildAt(i)
                    child.visibility = View.VISIBLE
                }

                (activity as Activity).findViewById<ProgressBar>(R.id.progressBarHomeActivity).visibility =
                    View.GONE
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
                        drawSwitch(true)
                    } else {
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
        val switch = (activity as Activity).findViewById<Button>(R.id.switchView)
        val colorGreen = ContextCompat.getColor(activity, R.color.green)
        val colorRed = ContextCompat.getColor(activity, R.color.red)
        if(boolean){
            switch.text = "Свободен"
            switch.setTextColor(colorGreen)
        } else {
            switch.text = "Занят"
            switch.setTextColor(colorRed)
        }
    }
}
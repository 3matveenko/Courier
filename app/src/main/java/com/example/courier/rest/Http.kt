package com.example.courier.rest

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.courier.models.CreateDriver
import com.example.courier.models.Settings
import com.example.courier.models.Token
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Modifier

class Http(private var activity: AppCompatActivity, private var context: Context) {
    private lateinit var api: API
    private lateinit var authorization: String


    private var gson: Gson = GsonBuilder()
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
        ).enqueue(object : Callback<Token> {
            @SuppressLint("CutPasteId", "SetTextI18n", "SimpleDateFormat")
            override fun onResponse(call: Call<Token>, response: Response<Token>) {
                if (response.code() == 200) {
                    Toast.makeText(context, "okey", Toast.LENGTH_LONG).show()
                    activity.finish()
                }
                //(activity as DocumentActivity).view(false)
            }

            override fun onFailure(call: Call<Token>, throwable: Throwable) {
                Toast.makeText(context, throwable.localizedMessage, Toast.LENGTH_LONG).show()

                //(activity as DocumentActivity).view(false)
            }
        })
    }
}
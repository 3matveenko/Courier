package com.example.courier.connect

import com.example.courier.models.CreateDriver
import com.example.courier.models.GetSettings.Companion.HTTP_TOKEN
import com.example.courier.models.LoginDriver
import com.example.courier.models.Message
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface API {

    @Headers("Content-Type: application/json;charset=UTF-8","Authorization: ${HTTP_TOKEN}")
    @POST("/app/create")
    fun createAccount(@Body query: CreateDriver): Call<String>

    @Headers("Content-Type: application/json;charset=UTF-8","Authorization: $HTTP_TOKEN")
    @POST("/app/authorization")
    fun login(@Body query: LoginDriver): Call<String>

    @Headers("Content-Type: application/json;charset=UTF-8","Authorization: $HTTP_TOKEN")
    @POST("/app/status_day")
    fun statusDay(@Body query: Message): Call<String>
}
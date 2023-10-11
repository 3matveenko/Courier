package com.example.courier.connect

import com.example.courier.models.CreateDriver
import com.example.courier.models.LoginDriver
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface API {

    @Headers("Content-Type: application/json;charset=UTF-8")
    @POST("/app/create")
    fun createAccount(@Body query: CreateDriver): Call<String>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @POST("/app/authorization")
    fun login(@Body query: LoginDriver): Call<String>
}
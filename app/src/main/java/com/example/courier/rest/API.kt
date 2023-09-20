package com.example.courier.rest

import com.example.courier.models.CreateDriver
import com.example.courier.models.Token
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface API {

    @Headers("Content-Type: application/json;charset=UTF-8")
    @POST("/app/create")
    fun createAccount(@Body query: CreateDriver): Call<String>

}
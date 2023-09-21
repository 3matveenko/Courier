package com.example.courier.models

import com.google.gson.annotations.SerializedName

data class LoginDriver (

    @SerializedName(value = "login")
    val login:String,

    @SerializedName(value = "password")
    val password:String
)

data class CreateDriver (

    @SerializedName(value = "login")
    val login:String,

    @SerializedName(value = "password")
    val password:String,

    @SerializedName(value = "name")
    val name:String
)

data class Token(
    @SerializedName(value = "token")
    val token:String
)
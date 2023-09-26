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

data class Setting(
    @SerializedName(value = "server_name")
    val SERVER_NAME:String,

    @SerializedName(value = "rabbit_server_name")
    val RABBIT_SERVER_NAME:String
)
fun isNotNull(setting: Setting): Boolean {
    return setting.SERVER_NAME != null && setting.RABBIT_SERVER_NAME != null
}
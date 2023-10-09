package com.example.courier.models

import com.google.gson.annotations.SerializedName
import java.util.Date

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

data class Message(

    @SerializedName(value = "token")
    val token: String,

    @SerializedName(value = "code")
    val code:String,

    @SerializedName(value = "millisecondsSinceEpoch")
    val millisecondsSinceEpoch:Long,

    @SerializedName(value = "body")
    val body:String
)

data class LocationMy(
    @SerializedName(value = "latitude")
    val latitude: Double,

    @SerializedName(value = "longitude")
    val longitude: Double
)

fun isNotNull(setting: Setting): Boolean {
    return setting.SERVER_NAME != null && setting.RABBIT_SERVER_NAME != null
}


data class Order(

    @SerializedName(value = "id")
    val id: Long,

    @SerializedName(value = "statusDelivery")
    val statusDelivery: Int,

    @SerializedName(value = "guid")
    val guid: String,

    @SerializedName(value = "dateStart")
    val dateStart : Date,

    @SerializedName(value = "dateEnd")
    val dateEnd : Date,

    @SerializedName(value = "address")
    val address:String,

    @SerializedName(value = "phone")
    val phone: String,

    @SerializedName(value = "current")
    val current: String,

    @SerializedName(value = "latitude")
    val latitude: Double,

    @SerializedName(value = "longitude")
    val longitude:Double,
)

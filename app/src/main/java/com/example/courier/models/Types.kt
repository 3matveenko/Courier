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
    var token:String,

    @SerializedName(value = "protocol")
    var protocol:String,

    @SerializedName(value = "serverName")
    var serverName:String,

    @SerializedName(value = "serverPort")
    var serverPort : Int,

    @SerializedName(value = "backQueueName")
    var backQueueName : String,

    @SerializedName(value = "rabbitServerName")
    var rabbitServerName:String,

    @SerializedName(value = "rabbitServerPort")
    var rabbitServerPort : Int,

    @SerializedName(value = "rabbitUsername")
    var rabbitUsername : String,

    @SerializedName(value = "rabbitPassword")
    var rabbitPassword : String,
){
    constructor() : this("", "", "", 0, "", "", 0, "", "")
}

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
    return setting.serverName != null && setting.rabbitServerName != null
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

    @SerializedName(value = "rejectOrder")
    val rejectOrder:RejectOrder,

    @SerializedName(value = "comment")
    val comment:String
)

data class RejectOrder(
    @SerializedName(value = "driver")
    val driver:Driver
)

data class Driver(
    @SerializedName(value = "login")
    val name:String
)

data class SendSms(

    @SerializedName(value = "phone")
    val phone: String,

    @SerializedName(value = "code")
    val code: String,
)


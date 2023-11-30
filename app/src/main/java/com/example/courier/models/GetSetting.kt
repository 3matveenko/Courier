package com.example.courier.models

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.example.courier.enums.SettingsValue
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Modifier

class GetSettings(context: Context) {

    companion object {
        var settings: Setting = Setting()

        val gson: Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .excludeFieldsWithModifiers(
                Modifier.STATIC,
                Modifier.TRANSIENT,
                Modifier.VOLATILE
            )
            .create()

    }
    private var sharedPreferences: SharedPreferences
    init {
        sharedPreferences = context.getSharedPreferences("com.example.courier", Context.MODE_PRIVATE)

        settings.token = load(SettingsValue.TOKEN)
        settings.protocol = load(SettingsValue.PROTOCOL)
        settings.serverName = load(SettingsValue.TOKEN)
        settings.serverPort = loadInt(SettingsValue.SERVER_NAME)
        settings.backQueueName = load(SettingsValue.BACK_QUEUE_NAME)
        settings.rabbitUsername = load(SettingsValue.RABBIT_USERNAME)
        settings.rabbitPassword = load(SettingsValue.RABBIT_PASSWORD)
        settings.rabbitServerName = load(SettingsValue.RABBIT_SERVER_NAME)
        settings.rabbitServerPort = loadInt(SettingsValue.RABBIT_SERVER_PORT)
    }

    fun getURI(): String {
        return settings.protocol + "://" + settings.serverName + ":" + settings.serverPort
    }

    fun load(key: SettingsValue): String {
        var string : String =  sharedPreferences.getString(key.value, "") ?: return ""
        return string

    }

    private fun loadInt(key: SettingsValue): Int {
        return sharedPreferences.getInt(key.value, 0)
    }

    fun load(key: String): String {
        return sharedPreferences.getString(key.toString(), "") ?: return ""
    }

    @SuppressLint("CommitPrefEdits")
    fun save(key: SettingsValue, string: String) {
        val sharedPreferencesEditor = sharedPreferences.edit()
        sharedPreferencesEditor.putString(key.toString(), string)
        sharedPreferencesEditor.apply()
    }

    @SuppressLint("CommitPrefEdits")
    fun save(key: String, value: String) {
        val sharedPreferencesEditor = sharedPreferences.edit()
        sharedPreferencesEditor.putString(key, value)
        sharedPreferencesEditor.apply()
    }

    @SuppressLint("CommitPrefEdits")
    fun saveInt(key: SettingsValue, value: Int) {
        val sharedPreferencesEditor = sharedPreferences.edit()
        sharedPreferencesEditor.putInt(key.value, value)
        sharedPreferencesEditor.apply()
    }

    @SuppressLint("CommitPrefEdits")
    fun remove(key: String) {
        val sharedPreferencesEditor = sharedPreferences.edit()
        sharedPreferencesEditor.remove(key)
        sharedPreferencesEditor.apply()
    }

    fun isNull(key: String): Boolean {
        return sharedPreferences.getString(key, "").toString().isEmpty()
    }
    fun isNull(key: SettingsValue): Boolean {
        return when (key) {
            SettingsValue.TOKEN -> settings.token != ""
            SettingsValue.PROTOCOL -> settings.protocol.isEmpty()
            SettingsValue.SERVER_NAME -> settings.serverName.isEmpty()
            SettingsValue.SERVER_PORT -> settings.serverPort == 0
            SettingsValue.BACK_QUEUE_NAME -> settings.backQueueName.isEmpty()
            SettingsValue.RABBIT_USERNAME -> settings.rabbitUsername.isEmpty()
            SettingsValue.RABBIT_PASSWORD -> settings.rabbitPassword.isEmpty()
            SettingsValue.RABBIT_SERVER_NAME -> settings.rabbitServerName.isEmpty()
            SettingsValue.RABBIT_SERVER_PORT -> settings.rabbitServerPort == 0
        }
    }
}
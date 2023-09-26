package com.example.courier.models

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class GetSettings(context: Context) {

    companion object {

        const val SERVER_NAME:String = "server_name"

        const val RABBIT_SERVER_NAME = "rabbit_server_name"

    }
    private var sharedPreferences: SharedPreferences
    init {
        sharedPreferences = context.getSharedPreferences("courier", Context.MODE_PRIVATE)
    }
    fun load(key: String): String? {
        return  sharedPreferences.getString(key, "")
    }

    @SuppressLint("CommitPrefEdits")
    fun save(key: String, string: String) {
        val sharedPreferencesEditor = sharedPreferences.edit()
        sharedPreferencesEditor.putString(key, string)
        sharedPreferencesEditor.apply()

    }

    fun isNull(key: String): Boolean {
        if (load(key) == "") {
            return true
        }
        return false
    }
}
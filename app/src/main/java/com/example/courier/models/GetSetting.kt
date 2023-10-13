package com.example.courier.models

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class GetSettings(context: Context) {

    companion object {
        //const
        const val TOKEN = "token"

        const val HTTP_TOKEN = "gYIABBFGDkyCwg"
        //var
        const val PROTOCOL = "protocol"

        const val BACK_QUEUE_NAME = "back_queue_name"

        const val SERVER_NAME = "server_name"

        const val SERVER_PORT = "server_port"

        const val RABBIT_SERVER_NAME = "rabbit_server_name"

        const val RABBIT_SERVER_PORT = "rabbit_server_port"

        const val RABBIT_USERNAME = "rabbit_username"

        const val RABBIT_PASSWORD = "rabbit_password"



    }
    private var sharedPreferences: SharedPreferences
    init {
        sharedPreferences = context.getSharedPreferences("courier", Context.MODE_PRIVATE)
    }

    fun load(key: String): String {
        return sharedPreferences.getString(key, "") ?: return ""
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
package com.example.courier

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myTextView = findViewById<TextView>(R.id.mainTextview)

        val sharedPreferences = getSharedPreferences("local_storage", Context.MODE_PRIVATE)
        val serverName = sharedPreferences.getString("server_name", "0")

        if (serverName == "0") {
            myTextView.setText("Локальное хранилище пусто")
        } else {
            myTextView.setText(serverName)
        }

        // Редактирование SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString("server_name", "имя сервера")
        editor.apply()
    }
}
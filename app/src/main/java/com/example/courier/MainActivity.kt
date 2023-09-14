package com.example.courier

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("local_storage", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("server_name", "0000")
        editor.apply()
        setContentView(R.layout.activity_main)
    }
}
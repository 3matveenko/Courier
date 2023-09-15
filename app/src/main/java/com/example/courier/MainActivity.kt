package com.example.courier

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.courier.loginActivity.LoginActivity
import com.example.courier.loginActivity.QRScannerActivity

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myTextView = findViewById<TextView>(R.id.mainTextview)

        val sharedPreferences = getSharedPreferences("local_storage", Context.MODE_PRIVATE)
        val serverName = sharedPreferences.getString("server_name", "0")
        val qrScanner = Intent(this@MainActivity, QRScannerActivity::class.java)
        val login = Intent(this@MainActivity, LoginActivity::class.java)

        if (serverName == "0") {
            startActivity(qrScanner)
        } else {
            startActivity(login)
        }

        // Редактирование SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString("server_name", "имя сервера")
        editor.apply()
    }
}
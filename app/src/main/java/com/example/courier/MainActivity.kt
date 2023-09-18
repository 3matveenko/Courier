package com.example.courier

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.courier.loginActivity.LoginActivity
import com.example.courier.loginActivity.QRScannerActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sharedPreferences = getSharedPreferences("local_storage", Context.MODE_PRIVATE)

        if (sharedPreferences.getString("server_name", null) == null) {
            ///startActivity(Intent(this@MainActivity, QRScannerActivity::class.java))
            val integrator = IntentIntegrator(this)
            integrator.setOrientationLocked(false)
            integrator.setPrompt("Отсканируйте QR-код у администратора")
            integrator.initiateScan()

        } else {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        }
        val editor = sharedPreferences.edit()
        editor.putString("server_name", "имя сервера")
        editor.apply()
        //finish()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        val sharedPreferences = getSharedPreferences("local_storage", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        if (result != null) {
            if (result.contents == null) {
                // Если сканирование было отменено
                Toast.makeText(this, "Сканирование отменено", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                // Если QR-код был успешно прочитан
                editor.putString("server_name", result.contents)
                editor.apply()
                finish()
            }
        }
    }
}
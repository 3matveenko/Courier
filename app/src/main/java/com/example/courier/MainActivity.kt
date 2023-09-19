package com.example.courier

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.courier.loginActivity.RegistrActivity
import com.example.courier.models.Settings
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        findViewById<Button>(R.id.but_registr).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.base_color))
        findViewById<Button>(R.id.but_enter).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.base_color))
        findViewById<Button>(R.id.but_registr).setOnClickListener {
            startActivity(Intent(this@MainActivity, RegistrActivity::class.java))
        }

        if (Settings(applicationContext).isNull(Settings.SERVER_NAME)) {
            Toast.makeText(this, Settings(applicationContext).load(Settings.SERVER_NAME),Toast.LENGTH_LONG).show()
            setContentView(R.layout.activity_qr_scanner)
            val integrator = IntentIntegrator(this)
            integrator.setOrientationLocked(false)
            integrator.setPrompt("Отсканируйте QR-код у администратора")
            integrator.initiateScan()
            Log.e("AAAAAAA", "after")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents == null) {
                // Если сканирование было отменено
                Toast.makeText(this, "Сканирование отменено", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Settings(applicationContext).save(Settings.SERVER_NAME,result.contents)
                Toast.makeText(this, Settings(applicationContext).load(Settings.SERVER_NAME),Toast.LENGTH_LONG).show()
               this.recreate()
            }
        }
        Log.e("AAAAAAA", "scan")
    }
}
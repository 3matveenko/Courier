package com.example.courier.loginActivity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.courier.R
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class QRScannerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)

        val integrator = IntentIntegrator(this)
        integrator.setOrientationLocked(false)
        integrator.setPrompt("Отсканируйте QR-код у администратора")
        integrator.initiateScan()
    }

    // Метод, вызываемый после завершения сканирования
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
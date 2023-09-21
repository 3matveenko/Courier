package com.example.courier

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.courier.activity.HomeActivity
import com.example.courier.activity.RegistrActivity
import com.example.courier.models.LoginDriver
import com.example.courier.models.Settings
import com.example.courier.rest.Http
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        if(!Settings(this).isNull("token")){
            startActivity(Intent(this@MainActivity, HomeActivity::class.java))
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        findViewById<Button>(R.id.but_registr).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.base_color))
        findViewById<Button>(R.id.but_enter).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.base_color))
        findViewById<Button>(R.id.but_qr).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.base_color))

        findViewById<Button>(R.id.but_registr).setOnClickListener {
            startActivity(Intent(this@MainActivity, RegistrActivity::class.java))
        }

        findViewById<Button>(R.id.but_qr).setOnClickListener {
            startQr()
        }

        findViewById<Button>(R.id.but_enter).setOnClickListener {
            val login = findViewById<EditText>(R.id.editEmail).text.toString()
            val password = findViewById<EditText>(R.id.editPassword).text.toString()
            if(login.isEmpty()||password.isEmpty()){
                Toast.makeText(this, "Поле должно быть заполнено", Toast.LENGTH_SHORT).show()
            } else {
                val rootLayout = findViewById<ConstraintLayout>(R.id.loginLayout)
                val childCount = rootLayout.childCount
                for (i in 0 until childCount) {
                    val child = rootLayout.getChildAt(i)
                    child.visibility = View.GONE
                }

                findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
                Http(this ).login(LoginDriver(login, password))
            }
        }

        if (Settings(applicationContext).isNull(Settings.SERVER_NAME)) {
            Toast.makeText(this, Settings(applicationContext).load(Settings.SERVER_NAME),Toast.LENGTH_LONG).show()
            startQr()
        }

    }

    private fun startQr() {
        val integrator = IntentIntegrator(this)
        integrator.setOrientationLocked(false)
        integrator.setPrompt("Отсканируйте QR-код у администратора")
        integrator.initiateScan()
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
    }
}
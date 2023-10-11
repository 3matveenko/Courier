package com.example.courier.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.courier.R
import com.example.courier.models.CreateDriver
import com.example.courier.models.GetSettings
import com.example.courier.connect.Http
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class RegistrActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        if(!GetSettings(this).isNull("token")){
            startActivity(Intent(this@RegistrActivity, HomeActivity::class.java))
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registr)
        findViewById<Button>(R.id.but_enter).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.base_color))




        findViewById<Button>(R.id.but_enter).setOnClickListener {
            val login = findViewById<EditText>(R.id.editEmail).text.toString()
            val password = findViewById<EditText>(R.id.editPassword).text.toString()
            val rePassword = findViewById<EditText>(R.id.editPasswordRetype).text.toString()
            val name = findViewById<EditText>(R.id.editName).text.toString()
            if(password != rePassword){
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            } else if(login.isEmpty()||password.isEmpty()||name.isEmpty()){
                Toast.makeText(this, "Поле должно быть заполнено", Toast.LENGTH_SHORT).show()
            } else{
                val rootLayout = findViewById<ConstraintLayout>(R.id.registrActivity)
                val childCount = rootLayout.childCount
                for (i in 0 until childCount) {
                    val child = rootLayout.getChildAt(i)
                    child.visibility = View.GONE

            }
                findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE

                Http(this@RegistrActivity).registr(CreateDriver(login, password, name))
            }

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
                GetSettings(applicationContext).save(GetSettings.SERVER_NAME,result.contents)
                Toast.makeText(this, GetSettings(applicationContext).load(GetSettings.SERVER_NAME), Toast.LENGTH_LONG).show()
                this.recreate()
            }
        }
    }
}
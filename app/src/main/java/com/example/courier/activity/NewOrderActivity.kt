package com.example.courier.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.courier.R
import com.example.courier.models.GetSettings
import com.example.courier.rest.Rabbit

class NewOrderActivity : AppCompatActivity() {

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_order)

        val mediaPlayer = MediaPlayer.create(applicationContext, R.raw.new_order)
        val vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            // Если разрешение уже есть, устанавливаем флаги и отображаем активность поверх других
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
//            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
//            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            // Вставьте здесь код для отображения содержимого вашей активности


        val imageView = findViewById<ImageView>(R.id.imageView)
        val translationAnimation = TranslateAnimation(
            0f, // начальное положение по горизонтали (слева)
            resources.displayMetrics.widthPixels.toFloat(), // конечное положение по горизонтали (справа)
            0f, // начальное положение по вертикали
            0f // конечное положение по вертикали (остается на месте)
        )
        translationAnimation.duration = 25000 // длительность анимации в миллисекундах (например, 5 секунд)
        translationAnimation.fillAfter = true // оставить картинку в конечной позиции после анимации
        imageView.startAnimation(translationAnimation)

        // Воспроизводим вибрацию
        val vibrationPattern = longArrayOf(0, 100, 1000, 300, 2000)
        vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, 0))

        mediaPlayer.start()

        mediaPlayer.isLooping = true

        findViewById<Button>(R.id.reject).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.red))
        findViewById<Button>(R.id.accept).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.green))




        findViewById<Button>(R.id.accept).setOnClickListener{
            val message = intent.getStringExtra("body")
            val intent = Intent("list_orders")
            intent.putExtra("body", message)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

            val intentMESSAGE = Intent(HomeActivity.MESSAGE)
            intentMESSAGE.putExtra("body", message)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentMESSAGE)

            val token = GetSettings(this).load("token").toString()
            Rabbit(this).sendMessage(token,"accept_order","ok")
            vibrator.cancel()
            mediaPlayer.release()
            finish()
        }



        val timer = object : CountDownTimer(20000, 1) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                vibrator.cancel()
                mediaPlayer.release()
                this@NewOrderActivity.finish()
            }
        }

        timer.start()

        findViewById<Button>(R.id.reject).setOnClickListener{
            vibrator.cancel()
            mediaPlayer.release()
            finish()
        }
    }
}
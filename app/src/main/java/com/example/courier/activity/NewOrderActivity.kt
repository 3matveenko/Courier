package com.example.courier.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.courier.R
import com.example.courier.models.GetSettings
import com.example.courier.connect.Rabbit
import com.example.courier.enums.RabbitCode

class NewOrderActivity : AppCompatActivity() {


    private val channelId = "my_channel"
    private val notificationId = 1

//    init {
//        createNotificationChannel()
//    }

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_order)



        Log.d("courier_log", "(NewOrderActivity открылся активити")
        val mediaPlayer = MediaPlayer.create(applicationContext, R.raw.new_order)
        val vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)

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

        val reject = intent.getStringExtra("reject")
        if(reject == null){
            findViewById<TextView>(R.id.rejectedTextInNewOrder).visibility = View.GONE
        }


        findViewById<Button>(R.id.accept).setOnClickListener{
            Log.d("courier_log", "(NewOrderActivity нажалась кнопка принять заказ")
            val message = intent.getStringExtra("body")

            val intentMESSAGE = Intent(HomeActivity.MESSAGE)
            intentMESSAGE.putExtra("body", message)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentMESSAGE)

            val token = GetSettings(this).load("token")
            if(reject == null){
                Log.d("courier_log", "(NewOrderActivity отправил accept_order:ok")
                Rabbit(this).sendMessage(token,RabbitCode.ACCEPT_ORDER,"ok")
            } else {
                Log.d("courier_log", "(NewOrderActivity отправил accept_rejected_order:ok")
                Rabbit(this).sendMessage(token,RabbitCode.ACCEPT_REJECT_ORDER,"ok")
            }
            vibrator.cancel()
            mediaPlayer.release()
            startActivity(Intent(this@NewOrderActivity, HomeActivity::class.java))
            finish()
        }



        val timer = object : CountDownTimer(20000, 1) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                Log.d("courier_log", "(NewOrderActivity вышло время на ответ о принятии заказа")
                vibrator.cancel()
                mediaPlayer.release()
                this@NewOrderActivity.finish()
            }
        }

        timer.start()

        findViewById<Button>(R.id.reject).setOnClickListener{
            Log.d("courier_log", "(NewOrderActivity нажата кнопка об отказе от заказа")
            vibrator.cancel()
            mediaPlayer.release()
            startActivity(Intent(this@NewOrderActivity, HomeActivity::class.java))
            finish()
        }

    }

//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = "My Channel"
//            val descriptionText = "Description of my channel"
//            val importance = NotificationManager.IMPORTANCE_DEFAULT
//            val channel = NotificationChannel(channelId, name, importance).apply {
//                description = descriptionText
//            }
//
//            val notificationManager =
//                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }

    fun showNotification(title: String, content: String) {
        val intent = Intent(applicationContext, NewOrderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.truck)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                return
            }
            notify(notificationId, builder.build())
        }
    }
}
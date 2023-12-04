package com.example.courier.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import com.example.courier.enums.Action
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.courier.R
import com.example.courier.connect.Rabbit
import com.example.courier.enums.IntentExtra
import com.example.courier.enums.RabbitCode
import com.example.courier.models.GetSettings
import com.example.courier.models.Message
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import java.util.concurrent.Executors

class RabbitService : Service() {

    private val executor = Executors.newSingleThreadExecutor()
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val notification = createNotification()

        notification.visibility = Notification.VISIBILITY_SECRET



        executor.submit {
            startListening()
        }

        startForeground(72018, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)

        return START_REDELIVER_INTENT
    }

    private fun createNotification(): Notification {
        Log.d("courier_log", "(SendLocation создал уведомление")
        val channelId = "72018"
        val channelName = "Courier"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Курьер")
            .setSmallIcon(R.drawable.truck)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        return notificationBuilder.build()
    }


    private fun startListening(){

        Log.d("courier_log", "RabbitListener запустил поток")
        GetSettings(this)



        //Thread {
            val channel: Channel = Rabbit.factory.newConnection()!!.createChannel()
            channel.queueDeclare(GetSettings.settings.token, true, false, false, null)

            while (true) {
                try {
                    //Log.d("courier_log", "RabbitListener после try")
                    val consumer = object : DefaultConsumer(channel) {
                        override fun handleDelivery(
                            consumerTag: String?,
                            envelope: Envelope?,
                            properties: AMQP.BasicProperties?,
                            body: ByteArray?
                        ) {
                           // Log.d("courier_log", "RabbitListener перед message")
                            val message =
                                GetSettings.gson.fromJson(
                                    String(body!!, Charsets.UTF_8),
                                    Message::class.java
                                )
                            Log.d("courier_log", "RabbitListener получил сообщение ${message.body}")
                            when (message.code) {
                                RabbitCode.NEW_ORDER_REJECTED.value -> {
                                    if ((message.millisecondsSinceEpoch + 60000) >= System.currentTimeMillis()) {
                                        val intent = Intent(Action.OPEN_NEW_ORDER.value)

                                        intent.putExtra(IntentExtra.REJECT.value, true)
                                        intent.putExtra(IntentExtra.BODY.value, message.body)

                                        LocalBroadcastManager
                                            .getInstance(applicationContext)
                                            .sendBroadcast(intent)
                                    }
                                }

                                RabbitCode.NEW_ORDER.value -> {
                                    if ((message.millisecondsSinceEpoch + 60000) >= System.currentTimeMillis()) {
                                        Log.d("courier_log", "RabbitListener получил сообщение $applicationContext")
                                        LocalBroadcastManager
                                            .getInstance(applicationContext)
                                            .sendBroadcast(
                                                Intent(Action.OPEN_NEW_ORDER.value).putExtra(
                                                    IntentExtra.BODY.value,
                                                    message.body
                                                )
                                            )
                                    }
                                }

                                RabbitCode.GET_MY_ORDERS_STATUS_PROGRESSING.value -> {
                                    LocalBroadcastManager
                                        .getInstance(applicationContext)
                                        .sendBroadcast(
                                            Intent(Action.MY_ORDER.value).putExtra(
                                                IntentExtra.BODY.value,
                                                message.body
                                            )
                                        )
                                }

                                RabbitCode.SEND_SMS_STATUS.value -> {
                                    Toast.makeText(
                                        applicationContext,
                                        applicationContext.getString(R.string.message_send),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                    Log.d("courier_log", "RabbitListener basicConsume")
                    channel.basicConsume(GetSettings.settings.token, true, consumer)
                } catch (e: InterruptedException) {
                    Log.e("courier_log", "RabbitListener первый кеч - ${e.message}")
                    break
                } catch (e: Exception) {
                    Log.e("courier_log", "RabbitListener второй кеч - ${e.message}")
                    Thread.sleep(5000)
                }
            }
       // }.start()
    }





    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
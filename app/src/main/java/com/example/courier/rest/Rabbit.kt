package com.example.courier.rest

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.courier.models.Message
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import org.json.JSONObject
import java.io.IOException

class Rabbit(private var activity: AppCompatActivity) {
    private lateinit var connection: Connection
    private lateinit var channel: Channel

    fun createConnectionAndChannel() {
        val factory = ConnectionFactory()
        factory.host = "192.168.0.166"
        factory.port = 5672
        factory.username = "guest"
        factory.password = "guest"

        while (true) {
            try {
                connection = factory.newConnection()
                channel = connection.createChannel()
                val queueName = "Driver0"
                channel.queueDeclare(queueName, true, false, false, null)
                break
            } catch (ex: Exception) {
                Thread.sleep(5000)
            }
        }
    }

    fun startListening() {
        Thread(Runnable {
            createConnectionAndChannel()

            val consumer = object : DefaultConsumer(channel) {
                override fun handleDelivery(
                    consumerTag: String?,
                    envelope: Envelope?,
                    properties: AMQP.BasicProperties?,
                    body: ByteArray?
                ) {
                        val stringMessage = String(body!!, Charsets.UTF_8)
//                        NotificationHandler().createNotificationChannel(activity.applicationContext)
//                        NotificationHandler().showNotification(activity.applicationContext, message, "Новый заказ")
                    val intent = Intent("open_new_order")
                    LocalBroadcastManager.getInstance(activity).sendBroadcast(intent)
                    Log.e("debugg", "получил сообщение вызвал бродкаст $stringMessage")

                    val gson = Gson()
                    val message = gson.fromJson(stringMessage, Message::class.java)
                    Log.e("debugg", "код ${message.code}")
                    Log.e("debugg", "миллисекунды ${message.millisecondsSinceEpoch}")
                    Log.e("debugg", "боди ${message.body}")

//                    var j = JsonParser().parse(message) as JSONObject
//                    var a =  j.get("body") as String
//                    var z = JsonParser().parse(a) as JSONObject

                    //Log.e("debugg", "состав сообщения $z")



//                        val handler = Handler(Looper.getMainLooper())
//                        handler.post {
//                            Toast.makeText(activity.applicationContext, message, Toast.LENGTH_LONG)
//                                .show()
//                        }
                }
            }
                channel.basicConsume("Driver0", true, consumer)
        }).start()
    }
    }

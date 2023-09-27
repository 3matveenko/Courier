package com.example.courier.rest

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
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
                        val message = String(body!!, Charsets.UTF_8)
                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            Log.d("loginfo", message)
                            Toast.makeText(activity.applicationContext, message, Toast.LENGTH_LONG)
                                .show()
                            NotificationHandler().createNotificationChannel(activity.applicationContext)
                            NotificationHandler().showNotification(activity.applicationContext, message, "Новый заказ")

                        }
                }
            }
                channel.basicConsume("Driver0", true, consumer)
        }).start()
    }
    }

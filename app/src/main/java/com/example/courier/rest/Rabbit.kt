package com.example.courier.rest

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.courier.models.GetSettings
import com.example.courier.models.Message
import com.example.courier.models.Setting
import com.google.gson.Gson
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import java.io.IOException
import java.util.concurrent.Executors

class Rabbit(private var context: Context) {
    private lateinit var connection: Connection
    private lateinit var channel: Channel

    fun createFactory(): ConnectionFactory {
        val factory = ConnectionFactory()
        factory.host = "192.168.0.166"
        factory.port = 5672
        factory.username = "guest"
        factory.password = "guest"
        return factory
    }
    fun createConnectionAndChannel() {
        val factory = createFactory()

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

                    Log.e("debugg", "получил сообщение вызвал бродкаст $stringMessage")

                    val gson = Gson()
                    val message = gson.fromJson(stringMessage, Message::class.java)
                    Log.e("debugg", "код ${message.code}")
                    Log.e("debugg", "миллисекунды ${message.millisecondsSinceEpoch}")
                    Log.e("debugg", "боди ${message.body}")

                    when(message.code){
                        "new_order" ->
                            if((message.millisecondsSinceEpoch+60000)>=System.currentTimeMillis()){
                                LocalBroadcastManager
                                    .getInstance(context)
                                    .sendBroadcast(Intent("open_new_order").putExtra("body",message.body))
                            }

                    }
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

    fun sendMessage(token:String, code: String, body: String) {
        Thread(Runnable {
                val factory = createFactory()
                val queueName = "back"

                val executorService = Executors.newSingleThreadExecutor()


                val gson = Gson()
                val messageObj = Message(token, code, System.currentTimeMillis(), body)
                val message = gson.toJson(messageObj)

            executorService.execute {
                    val connection = factory.newConnection()
                    val channel = connection.createChannel()

                    // Объявляем очередь, если она еще не создана
                    channel.queueDeclare(queueName, true, false, false, null)

                    // Отправляем сообщение
                    channel.basicPublish("", queueName, null, message.toByteArray())

                    // Закрываем канал и соединение
                    channel.close()
                    connection.close()
                    Log.e("debuggп", "все ок")
            }

            executorService.shutdown()

        }).start()
    }
    }

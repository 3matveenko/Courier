package com.example.courier.connect

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.courier.models.GetSettings
import com.example.courier.models.GetSettings.Companion.TOKEN
import com.example.courier.models.Message
import com.google.gson.Gson
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import java.util.concurrent.Executors

class Rabbit(private var context: Context) {
    companion object {
        private var connection: Connection? = null
        private var channel: Channel? = null
    }

     private val queueName: String? = GetSettings(context).load(TOKEN)

    private fun createFactory(): ConnectionFactory {
        val factory = ConnectionFactory()
        factory.host = GetSettings(context).load(GetSettings.RABBIT_SERVER_NAME)
        factory.port = GetSettings(context).load(GetSettings.RABBIT_SERVER_PORT).toInt()
        factory.username = GetSettings(context).load(GetSettings.RABBIT_USERNAME)
        factory.password = GetSettings(context).load(GetSettings.RABBIT_PASSWORD)
        return factory
    }
    @SuppressLint("SuspiciousIndentation")
    private fun createConnectionAndChannel() {
            try {
                val factory = createFactory()
                connection = factory.newConnection()
                channel = connection!!.createChannel()
                channel!!.queueDeclare(queueName, true, false, false, null)
            } catch (ex: Exception) {
                Thread.sleep(5000)
            }
    }

    fun startListening() {
        Log.d("courier_log", "startListening()")
        if (connection != null) {
            return
        }
        Thread(Runnable {
            do {
                var flag = false
                if(PingServer(context).connection()){
                    Log.d("courier_log", "Rabbit startListening ok")
                    createConnectionAndChannel()
                    val consumer = object : DefaultConsumer(channel) {
                        override fun handleDelivery(
                            consumerTag: String?,
                            envelope: Envelope?,
                            properties: AMQP.BasicProperties?,
                            body: ByteArray?
                        ) {
                            val stringMessage = String(body!!, Charsets.UTF_8)
                            val gson = Gson()
                            val message = gson.fromJson(stringMessage, Message::class.java)

                            when(message.code){
                                "new_order" ->
                                    if((message.millisecondsSinceEpoch+29000)>=System.currentTimeMillis()){
                                        LocalBroadcastManager
                                            .getInstance(context)
                                            .sendBroadcast(Intent("open_new_order").putExtra("body",message.body))

                                    }
                                "get_my_orders_status_progressing" ->
                                    LocalBroadcastManager
                                        .getInstance(context)
                                        .sendBroadcast(Intent("my_orders").putExtra("body",message.body))
                            }
                        }
                    }
                    channel!!.basicConsume(queueName, true, consumer)
                } else {
                    Log.e("courier_log", "Rabbit startListening disconnect")
                    Thread.sleep(5000)
                    flag = true
                }
            } while (flag)
        }).start()
    }

    fun sendMessage(token:String, code: String, body: String) {
        Thread(Runnable {
            do {
                var flag = false
                if(PingServer(context).connection()){
                val factory = createFactory()
                val queueName = GetSettings(context).load(GetSettings.BACK_QUEUE_NAME)

                val executorService = Executors.newSingleThreadExecutor()

                val gson = Gson()
                val messageObj = Message(token, code, System.currentTimeMillis(), body)
                val message = gson.toJson(messageObj)

            executorService.execute {
                    val connection = factory.newConnection()
                    val channel = connection.createChannel()
                    channel.queueDeclare(queueName, true, false, false, null)
                    channel.basicPublish("", queueName, null, message.toByteArray())
                    channel.close()
                    connection.close()
            }
            executorService.shutdown()
                    Log.d("courier_log", "Rabbit sendMessage ok")
                } else {
                    Log.e("courier_log", "Rabbit sendMessage disconnect")
                    Thread.sleep(5000)
                    flag = true
                }
            } while (flag)
        }).start()
    }
}


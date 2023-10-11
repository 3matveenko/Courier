package com.example.courier.connect

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.courier.models.GetSettings
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

     private val queueName: String? = GetSettings(context).load("token")

    private fun createFactory(): ConnectionFactory {
        val factory = ConnectionFactory()
        factory.host = "192.168.0.166"
        factory.port = 5672
        factory.username = "guest"
        factory.password = "guest"
        return factory
    }
    private fun createConnectionAndChannel() {
        val factory = createFactory()

            try {
                connection = factory.newConnection()
                channel = connection!!.createChannel()
                channel!!.queueDeclare(queueName, true, false, false, null)
                //break
            } catch (ex: Exception) {
                Thread.sleep(5000)
            }
    }

    fun startListening() {
        if (connection != null) {
            return
        }
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

                    val gson = Gson()
                    val message = gson.fromJson(stringMessage, Message::class.java)

                    when(message.code){
                        "new_order" ->
                            if((message.millisecondsSinceEpoch+29000)>=System.currentTimeMillis()){
                                LocalBroadcastManager
                                    .getInstance(context)
                                    .sendBroadcast(Intent("open_new_order").putExtra("body",message.body))
                                Log.e("debuggп", Thread.activeCount().toString())
                            }
                        "get_my_orders_status_progressing" ->
                            LocalBroadcastManager
                                .getInstance(context)
                                .sendBroadcast(Intent("my_orders").putExtra("body",message.body))
                    }
                }
            }
                channel!!.basicConsume(queueName, true, consumer)
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
                    channel.queueDeclare(queueName, true, false, false, null)
                    channel.basicPublish("", queueName, null, message.toByteArray())
                    channel.close()
                    connection.close()
            }
            executorService.shutdown()
        }).start()
    }
}

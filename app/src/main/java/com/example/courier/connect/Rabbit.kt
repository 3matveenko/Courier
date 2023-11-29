package com.example.courier.connect

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.courier.MainActivity.Companion.connectionFlag
import com.example.courier.enums.SettingsValue
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
        var connection: Connection? = null
        private var channel: Channel? = null
    }

     private val queueName: String? = GetSettings(context).load(SettingsValue.TOKEN)

    private fun createFactory(): ConnectionFactory {
        val factory = ConnectionFactory()
        factory.host = GetSettings(context).load(SettingsValue.RABBIT_SERVER_NAME)
        factory.port = GetSettings(context).load(SettingsValue.RABBIT_SERVER_PORT).toInt()
        factory.username = GetSettings(context).load(SettingsValue.RABBIT_USERNAME)
        factory.password = GetSettings(context).load(SettingsValue.RABBIT_PASSWORD)
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
        Log.d("courier_log", "Rabbit зашел в метод startListening")
        if (connection != null) {
            return
        }
        Thread(Runnable {
            do {
                var flag = false
                Log.e("courier_log", "Rabbit connectionFlag =  $connectionFlag")
                if(connectionFlag){
                    Log.d("courier_log", "Rabbit startListening успешно запустился")
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
                            val code = message.code
                            val body = message.body
                            val timeNow = System.currentTimeMillis()
                            val timeMessage = message.millisecondsSinceEpoch
                            Log.d("courier_log", "Rabbit получил сообщение в $timeNow время в сообщении $timeMessage с кодом $code и содержанием $body")
                            when(code){
                                "new_order_rejected" ->
                                    if((message.millisecondsSinceEpoch+29000)>=System.currentTimeMillis()){
                                        val intent = Intent("open_new_order")
                                        intent.putExtra("reject","true")
                                        intent.putExtra("body",message.body)
                                        LocalBroadcastManager
                                            .getInstance(context)
                                            .sendBroadcast(intent)
                                    }
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
                                "send_sms_success"->
                                    Toast.makeText(context,"сообщение отправлено!",Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    channel!!.basicConsume(queueName, true, consumer)
                } else {
                    Log.e("courier_log", "Rabbit startListening disconnect")
                    Thread.sleep(1000)
                    flag = true
                }
            } while (flag)
        }).start()
    }

    @SuppressLint("SuspiciousIndentation")
    fun sendMessage(token:String, code: String, body: String) {
        Thread(Runnable {
            do {
                var flag = false
                if(connectionFlag){
                val factory = createFactory()
                val queueName = GetSettings(context).load(SettingsValue.BACK_QUEUE_NAME)

                val executorService = Executors.newSingleThreadExecutor()

                val gson = Gson()
                val messageObj = Message(token, code, System.currentTimeMillis(), body)
                val message = gson.toJson(messageObj)

                    Log.d("courier_log", "Rabbit отправил $message")

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
                    Thread.sleep(1000)
                    Log.e("courier_log", "Rabbit sendMessage disconnect")
                    flag = true
                }
            } while (flag)
        }).start()
    }
}


//package com.example.courier.rest
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.content.Intent
//import android.os.AsyncTask
//import android.os.Build
//import androidx.localbroadcastmanager.content.LocalBroadcastManager
//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
//import com.rabbitmq.client.AMQP
//import com.rabbitmq.client.ConnectionFactory
//import com.rabbitmq.client.DefaultConsumer
//import com.rabbitmq.client.Envelope
//import java.io.IOException
//import java.nio.charset.StandardCharsets
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.Connection;
//import com.rabbitmq.client.Consumer
//import java.util.concurrent.TimeoutException
//import java.lang.reflect.Type;
//
//
///** */
//class ManagerRabbitMQ
///** */(
//    /** */
//    private val context: Context
//) {
//    /** */
//    protected var mChannel: Channel? = null
//    protected var mConnection: Connection? = null
//
//    /** */
//    var userName = "admin"
//    var password = "4217777"
//    var virtualHost = "/"
//    var serverIp = "192.168.0.156"
//    var port = 5672
//
//    /** */
//    protected var running = false
//
//    /** */
//    fun dispose() {
//        running = false
//        try {
//            mConnection?.close()
//            mChannel?.abort()
//        } catch (e: IOException) {
//            throw RuntimeException(e)
//        }
//    }
//
//    /** */
//    @SuppressLint("StaticFieldLeak")
//    fun connectToRabbitMQ() {
//        if (mChannel != null && mChannel!!.isOpen()) {
//            running = true
//        }
//        object : AsyncTask<Void?, Void?, Boolean>() {
//            /** */
//            protected override fun doInBackground(vararg voids: Void): Boolean {
//                val connectionFactory = ConnectionFactory()
//                connectionFactory.username = userName
//                connectionFactory.password = password
//                connectionFactory.virtualHost = virtualHost
//                connectionFactory.host = serverIp
//                connectionFactory.port = port
//                connectionFactory.isAutomaticRecoveryEnabled = true
//                try {
//                    mChannel = connectionFactory.newConnection().createChannel()
//                    registerChanelHost()
//                } catch (e: IOException) {
//                    throw RuntimeException(e)
//                } catch (e: TimeoutException) {
//                    throw RuntimeException(e)
//                }
//                return true
//            }
//
//            /** */
//            override fun onPostExecute(aBoolean: Boolean) {
//                super.onPostExecute(aBoolean)
//                running = aBoolean
//            }
//        }.execute()
//    }
//
//    /** */
//    private fun registerChanelListHost() {
//        try {
//            mChannel?.exchangeDeclare(EXCHANGE_NAME, "direct", true)
//            val queueName: String = mChannel.queueDeclare().getQueue()
//            mChannel?.queueBind(queueName, EXCHANGE_NAME, "topic1")
//            val consumer: Consumer = object : DefaultConsumer(mChannel) {
//                override fun handleDelivery(
//                    consumerTag: String, envelope: Envelope,
//                    properties: AMQP.BasicProperties, body: ByteArray
//                ) {
//                    getHeader(properties)
//                    var message: String? = null
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                        message = kotlin.String(body, StandardCharsets.UTF_8)
//                    }
//                    val gson = Gson()
//                    val type: Type = object : TypeToken<List<Message?>?>() {}.type
//                    val messageList: List<Message> = gson.fromJson<List<Message>>(message, type)
//                }
//            }
//            mChannel.basicConsume(queueName, true, consumer)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    /** */
//    private fun registerChanelHost() {
//        try {
//            mChannel?.exchangeDeclare(EXCHANGE_NAME, "direct", true)
//            val queueName: String = mChannel.queueDeclare().getQueue()
//            mChannel?.queueBind(queueName, EXCHANGE_NAME, "topic1")
//            val consumer: Consumer = object : DefaultConsumer(mChannel) {
//                /** */
//                override fun handleDelivery(
//                    consumerTag: String, envelope: Envelope,
//                    properties: AMQP.BasicProperties, body: ByteArray
//                ) {
//                    var message: String? = null
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                        message = kotlin.String(body, StandardCharsets.UTF_8)
//                    }
//                    sendBroadcast(message)
//                }
//            }
//            mChannel?.basicConsume(queueName, true, consumer)
//        } catch (e: Exception) {
//            throw RuntimeException(e)
//        }
//    }
//
//    /** */
//    private fun sendBroadcast(msg: String?) {
//        val intent = Intent(ACTION_STRING_ACTIVITY)
//        intent.putExtra("message", msg)
//        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
//    }
//
//    /** */
//    private fun getHeader(properties: AMQP.BasicProperties) {
//        val headers = properties.headers
//        val deviceId = headers["extraContent"]
//    }
//
//    /** */
//    @SuppressLint("StaticFieldLeak")
//    fun sendMessage(msg: String) {
//        object : AsyncTask<Void?, Void?, Boolean>() {
//            /** */
//            protected override fun doInBackground(vararg voids: Void): Boolean {
//                try {
//                    mChannel?.basicPublish(EXCHANGE_NAME, "topic1", null, msg.toByteArray())
//                } catch (e: IOException) {
//                    throw RuntimeException(e)
//                }
//                return true
//            }
//
//            /** */
//            override fun onPostExecute(aBoolean: Boolean) {
//                super.onPostExecute(aBoolean)
//                running = aBoolean
//            }
//
//            override fun doInBackground(vararg params: Void?): Boolean {
//                TODO("Not yet implemented")
//            }
//        }.execute()
//    }
//
//    companion object {
//        /** */
//        private const val EXCHANGE_NAME = "amq.direct"
//        private const val ACTION_STRING_ACTIVITY = "broadcast_event"
//    }
//}
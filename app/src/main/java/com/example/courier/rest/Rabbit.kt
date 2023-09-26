package com.example.courier.rest

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope

class Rabbit(private var activity: AppCompatActivity) {

     fun startListening() {
         Thread(Runnable {

             val factory = ConnectionFactory()
             factory.host = "192.168.0.166"
             factory.port = 5672 // Порт по умолчанию для RabbitMQ
             factory.username = "guest"
             factory.password = "guest"

             val connection = factory.newConnection()

             val channel: Channel = connection.createChannel()
             val queueName = "Driver0"

             channel.queueDeclare(queueName, true, false, false, null)

             val consumer = object : DefaultConsumer(channel) {
                 override fun handleDelivery(
                     consumerTag: String?,
                     envelope: Envelope?,
                     properties: AMQP.BasicProperties?,
                     body: ByteArray?
                 ) {
                     val message = String(body!!, Charsets.UTF_8)
                     Log.d("loginfo", message)
                     Toast.makeText(activity.applicationContext, message, Toast.LENGTH_LONG).show()

                     // Обработка нового сообщения
                     // В данном методе можно выполнить действия с полученным сообщением
                 }
             }

             channel.basicConsume("Driver0", true, consumer)
         }).start()
     }
    }

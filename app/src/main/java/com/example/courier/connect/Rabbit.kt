package com.example.courier.connect

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.example.courier.enums.RabbitCode
import com.example.courier.enums.SettingsValue
import com.example.courier.models.GetSettings
import com.example.courier.models.GetSettings.Companion.settings
import com.example.courier.models.Message
import com.rabbitmq.client.ConnectionFactory

class Rabbit(private var context: Context) {
    companion object {

        val factory = createFactory()
        var queueName = settings.backQueueName

        private fun createFactory(): ConnectionFactory {
            val factory = ConnectionFactory()

            factory.host = settings.rabbitServerName
            factory.port = settings.rabbitServerPort.toInt()
            factory.username = settings.rabbitUsername
            factory.password = settings.rabbitPassword

            return factory
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun sendMessage(token: String, code: RabbitCode, body: String = "") {
        Log.d("courier_log", "(Rabbit sendMessage")
        Thread {
            while (true) {
                try {
                    val message =
                        GetSettings.gson.toJson(
                            Message(
                                token,
                                code.value,
                                System.currentTimeMillis(),
                                body
                            )
                        ).toByteArray()

                    val connection = factory.newConnection()

                    val channel = connection.createChannel()

                    channel.queueDeclare(queueName, true, false, false, null)

                    channel.basicPublish("", queueName, null, message)

                    channel.close()

                    connection.close()
                    Log.d("courier_log", "Rabbit дошел до последнего брэйк")
                    break
                } catch (e: InterruptedException) {
                    Log.e("courier_log", "RabbitSendMessage первый кеч ${e}")
                    break
                } catch (e: Exception) {
                    Log.e("courier_log", "RabbitSendMessage второй кеч ${e}")
                    Thread.sleep(5000)
                }
            }
        }.start()
    }
}




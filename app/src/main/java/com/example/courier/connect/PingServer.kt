package com.example.courier.connect

import android.content.Context
import android.util.Log
import com.example.courier.MainActivity.Companion.connectionFlag
import com.example.courier.models.GetSettings
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class PingServer(private var context: Context) {

    fun connection() {
        Log.d("courier_log", "PingServer starting")
        while (true){
            val backServer: String = GetSettings(context).load(GetSettings.SERVER_NAME)
            val backServerPort: String = GetSettings(context).load(GetSettings.SERVER_PORT)
            val rabbitServer: String = GetSettings(context).load(GetSettings.RABBIT_SERVER_NAME)
            val rabbitServerPort: String = GetSettings(context).load(GetSettings.RABBIT_SERVER_PORT)
            val socketServer = Socket()
            val socketRabbit = Socket()
            try {
                socketServer.connect(InetSocketAddress(backServer, backServerPort.toInt()), 5000)
                socketRabbit.connect(InetSocketAddress(rabbitServer, rabbitServerPort.toInt()), 5000)

                connectionFlag = true

            } catch (e: IOException) {
                Log.e("courier_log", "PingServer = false $e")
                connectionFlag = false
            }
            finally {
                socketServer.close()
                socketRabbit.close()
            }
            Thread.sleep(1000)
        }
    }
}
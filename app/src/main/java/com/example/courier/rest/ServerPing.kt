package com.example.courier.rest

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class ServerPing(private var context: Context) {

    fun pingServer(host: String, port: Int, timeoutMillis: Int): Boolean {
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress(host, port), timeoutMillis)
            socket.close()
            return true
        } catch (e: IOException) {
            return false
        }
    }

    fun main() {
        Thread(Runnable {
            while (true){
                val serverHost = "192.168.0.147"
                val serverPort = 80

                val serverPing = ServerPing(context)
                val isServerReachable =
                    serverPing.pingServer(serverHost, serverPort, 60000)

                if (!isServerReachable) {

                    val intent = Intent("no_connection")
                    intent.putExtra("message", "нет связи с сервером")
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
//                    val handler = Handler(Looper.getMainLooper())
//                    handler.post {
//                        Log.d("loginfo", "нет связи с сервером")
//                        Toast.makeText(activity.applicationContext, "нет связи с сервером", Toast.LENGTH_LONG)
//                            .show()
//                    }
                }
                Thread.sleep(5000)
            }
        }).start()
    }
}
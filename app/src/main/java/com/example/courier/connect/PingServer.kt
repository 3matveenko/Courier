package com.example.courier.connect

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.courier.MainActivity.Companion.connectionFlag
import com.example.courier.models.GetSettings
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors

class PingServer(): Service() {

    private val executor = Executors.newSingleThreadExecutor()

    private lateinit var context: Context

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

//        executor.submit {
//            connection()
//        }
        return START_STICKY
    }


    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
        return null
    }
}
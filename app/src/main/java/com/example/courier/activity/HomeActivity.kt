package com.example.courier.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Bundle
import android.os.Process
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.courier.R
import com.example.courier.rest.MyBroadcastReceiver
import com.example.courier.rest.Rabbit
import com.example.courier.rest.ServerPing

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val intentFilter = IntentFilter("no_connection")
        val receiver = MyBroadcastReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        ServerPing(this).main()
        Rabbit(this).startListening()
    }
    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            @SuppressLint("CutPasteId", "UseCompatLoadingForDrawables")
            override fun handleOnBackPressed() {
                try {
                    moveTaskToBack(true)
                    Process.killProcess(Process.myPid())
                    System.exit(1)
                } catch (throwable: Throwable) {
                    throwable.printStackTrace()
                }
            }
        }
}
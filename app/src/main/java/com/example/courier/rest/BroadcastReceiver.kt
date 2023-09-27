package com.example.courier.rest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class MyBroadcastReceiver: BroadcastReceiver() {
     override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "no_connection") {
            val message = intent.getStringExtra("message")
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
package com.example.courier.connect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.courier.activity.HomeActivity
import com.example.courier.activity.NewOrderActivity

class MyBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            if (intent?.action == "no_connection") {
                val message = intent.getStringExtra("message")
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
            if (intent?.action == "open_new_order") {
                val newOrderIntent = Intent(context, NewOrderActivity::class.java)
                val message = intent.getStringExtra("body")
                newOrderIntent.putExtra("body", message)
                newOrderIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(newOrderIntent)
            }
            if(intent?.action=="my_orders"){
                val message = intent.getStringExtra("body")
                val intentMESSAGE = Intent(HomeActivity.MESSAGE)
                intentMESSAGE.putExtra("body", message)
                LocalBroadcastManager.getInstance(context).sendBroadcast(intentMESSAGE)
            }
        }
    }
}
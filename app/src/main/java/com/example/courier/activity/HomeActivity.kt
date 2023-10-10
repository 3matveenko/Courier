package com.example.courier.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Process
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.courier.R
import com.example.courier.models.Order
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Modifier
import java.lang.reflect.Type
import kotlin.system.exitProcess


class HomeActivity : AppCompatActivity() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var orders: List<Order?>? = emptyList()

        val gson: Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .excludeFieldsWithModifiers(
                Modifier.STATIC,
                Modifier.TRANSIENT,
                Modifier.VOLATILE
            )
            .create()

        private val stringOrders: MutableList<String> = mutableListOf()

        const val MESSAGE: String = "MESSAGE"

        val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                val message = intent.getStringExtra("body")
                if (message != null) {
                    val gson = GsonBuilder()
                        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                        .excludeFieldsWithModifiers(
                            Modifier.STATIC,
                            Modifier.TRANSIENT,
                            Modifier.VOLATILE
                        )
                        .create()
                    val deliveryInfoListType: Type =
                        object : TypeToken<List<Order?>?>() {}.type
                    orders = gson.fromJson(message, deliveryInfoListType)
                }
                stringOrders.clear()

                if (orders?.isNotEmpty() == true) {
                    orders?.forEach {
                        if (it != null) {
                            stringOrders.add(it.address)
                        }
                    }
                }
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        LocalBroadcastManager.getInstance(this).registerReceiver(
            broadcastReceiver, IntentFilter(MESSAGE)
        )

        if (stringOrders.isEmpty()) {

        }

        findViewById<ListView>(R.id.recyclerView).adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, stringOrders)

        findViewById<ListView>(R.id.recyclerView).setOnItemClickListener { _, _, position, _ ->

            val jsonOrder:String = gson.toJson(orders?.get(position))

            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtra("order", jsonOrder)
            startActivity(intent)
            Toast.makeText(this,  jsonOrder, Toast.LENGTH_SHORT).show()
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            @SuppressLint("CutPasteId", "UseCompatLoadingForDrawables")
            override fun handleOnBackPressed() {
                try {
                    moveTaskToBack(true)
                    Process.killProcess(Process.myPid())
                    exitProcess(1)
                } catch (throwable: Throwable) {
                    throwable.printStackTrace()
                }
            }
        }
}

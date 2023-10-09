package com.example.courier.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.courier.R
import com.example.courier.models.Order
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Modifier
import java.lang.reflect.Type
import kotlin.system.exitProcess


class HomeActivity : AppCompatActivity() {
    //private var orders: List<Order?>? = emptyList()

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var listView: ListView? = null
        @SuppressLint("StaticFieldLeak")
        private var context_c: Context? = null
        private var orders: List<Order?>? = emptyList()

        const val MESSAGE: String = "MESSAGE"

        val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                val message = intent.getStringExtra("body")
                Log.e("debuggп", "message null")
                if (message != null) {
                    Log.e("debuggп", "message не null")

                    /*val gson = GsonBuilder()
                        .registerTypeAdapter(
                            Date::class.java,
                            JsonDeserializer<Date> { json, _, _ ->
                                try {
                                    val format: DateFormat =
                                        SimpleDateFormat("MMM d, yyyy, h:mm:ss a")
                                    format.parse(json.asString)
                                } catch (e: ParseException) {
                                    null
                                }
                            })
                        .create()*/

                    val gson = GsonBuilder()
                        .setDateFormat("MMM d, yyyy, h:mm:ss a")
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

                val stringOrders = mutableListOf<String>()
                if (orders?.isNotEmpty() == true) {
                    orders?.forEach {
                        stringOrders.add(it!!.current)
                    }

                    if (listView != null) {
                        listView!!.findViewById<ListView>(R.id.recyclerView).adapter =
                            context_c?.let { ArrayAdapter(it, android.R.layout.simple_list_item_1, stringOrders)
                            }
                    }
                }
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        listView = findViewById(R.id.recyclerView)
        context_c = applicationContext

        LocalBroadcastManager.getInstance(this).registerReceiver(
            broadcastReceiver, IntentFilter(MESSAGE)
        )

            val stringOrders = mutableListOf<String>()

            stringOrders+="AAAA"
            stringOrders+="gggg"
            stringOrders+="AAAA"
            stringOrders+="AAAA"
            stringOrders+="AAAA"



        listView?.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, stringOrders)

        /*var reciver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if ("list_orders" == intent?.action) {

                }
            }
        }*/


        //val filter = IntentFilter("list_orders")
        //LocalBroadcastManager.getInstance(this).registerReceiver(reciver, filter)


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

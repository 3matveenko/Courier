package com.example.courier.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.courier.MainActivity
import com.example.courier.R
import com.example.courier.connect.Http
import com.example.courier.connect.Rabbit
import com.example.courier.models.GetSettings
import com.example.courier.models.Message
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
        lateinit var ll:ListView

        @SuppressLint("StaticFieldLeak")
        lateinit var textViewNoOrders:TextView

        lateinit var progressBar:ProgressBar

        @SuppressLint("StaticFieldLeak")
        lateinit var _context: Context

        @SuppressLint("StaticFieldLeak")
        var orders: List<Order?>? = emptyList()

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
                    textViewNoOrders.visibility = View.GONE
                    progressBar.visibility = View.GONE
                    orders?.forEach {
                        if (it != null) {
                            stringOrders.add(it.address)
                        }
                    }
                } else{
                    ll.visibility = View.GONE
                    textViewNoOrders.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }
                var countOrders = orders?.size
                Log.d("courier_log", "перезаписал адаптер(HomeActivity), в массеве $countOrders элементов")
                if (::ll.isInitialized) {
                    ll.adapter =
                        ArrayAdapter(_context, android.R.layout.simple_list_item_1, stringOrders)
                }
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ResourceAsColor", "CutPasteId", "UseSwitchCompatOrMaterialCode",
        "MissingInflatedId"
    )
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        //var switch = toolbar.getChildAt(0) as Switch
        val switch = findViewById<Switch>(R.id.switchView)
        if(!MainActivity.connectionFlag){
            switch.text = "Нет интернета"
        }


        val token:String = GetSettings(this).load("token")

        Thread(Runnable {
            Http(this@HomeActivity).statusDay(Message(token, "", 0, ""),false)
            Log.d("courier_log", "запросил статус")
        }).start()

        switch.setOnCheckedChangeListener { _, _ ->
            Log.e("courier_log",Thread.activeCount().toString())
            Thread(Runnable {
            Http(this@HomeActivity).statusDay(Message(token, "", 0, ""),true)
            }).start()
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(
            broadcastReceiver, IntentFilter(MESSAGE)
        )

        _context = this


        textViewNoOrders = findViewById(R.id.no_orders)
        ll = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBarHomeActivity)

        if (stringOrders.isNotEmpty()) {
        progressBar.visibility = View.GONE
        }
        var countOrders = orders?.size
        Log.d("courier_log", "первый запуск адаптера(HomeActivity), в массеве $countOrders элементов")
        ll.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, stringOrders)

        ll.setOnItemClickListener { _, _, position, _ ->

            val jsonOrder:String = gson.toJson(orders?.get(position))
            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtra("order", jsonOrder)
            startActivity(intent)
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

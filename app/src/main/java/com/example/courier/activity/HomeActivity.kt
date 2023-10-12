package com.example.courier.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Switch
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.courier.R
import com.example.courier.connect.Http
import com.example.courier.models.GetSettings
import com.example.courier.models.Message
import com.example.courier.models.Order
import com.google.android.gms.common.api.internal.StatusCallback
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
        lateinit var _context: Context

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
                if (::ll.isInitialized) {
                    ll.adapter =
                        ArrayAdapter(_context, android.R.layout.simple_list_item_1, stringOrders)
                }
                var a :Int = 5
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ResourceAsColor", "CutPasteId", "UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val switch = findViewById<Switch>(R.id.switchView)
        val colorGreen = resources.getColor(R.color.green, theme)
        val colorRed = resources.getColor(R.color.red,theme)

        val token:String = GetSettings(this).load("token")
        Http(this@HomeActivity).statusDay(Message(token,"",0,""))


        switch.setTextColor(colorRed)

        val thumbColor = ColorStateList.valueOf(colorRed)
        switch.thumbTintList = thumbColor
        LocalBroadcastManager.getInstance(this).registerReceiver(
            broadcastReceiver, IntentFilter(MESSAGE)
        )

        _context = this

        if (stringOrders.isEmpty()) {
            val a : Int = 5
        }
        ll = findViewById<ListView>(R.id.recyclerView)
        ll.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, stringOrders)

        ll.setOnItemClickListener { _, _, position, _ ->

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

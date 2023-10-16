package com.example.courier.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.courier.R
import com.example.courier.models.Order
import com.google.gson.GsonBuilder
import java.lang.reflect.Modifier
import java.text.SimpleDateFormat

class DetailsActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        var ordesNrmber = findViewById<TextView>(R.id.orders_number_text)
        var currentName = findViewById<TextView>(R.id.current_text)
        var phoneNumber = findViewById<TextView>(R.id.phone_number_text)
        var address = findViewById<TextView>(R.id.address_text)
        var time = findViewById<TextView>(R.id.time_text)
        val orderString:String = intent.getStringExtra("order").toString()
        val gson = GsonBuilder()
            .setDateFormat("hh:mm")
            .excludeFieldsWithModifiers(
                Modifier.STATIC,
                Modifier.TRANSIENT,
                Modifier.VOLATILE
            )
            .create()
       var order: Order = gson.fromJson(orderString,Order::class.java)
        ordesNrmber.text = order.guid
        currentName.text = order.current
        phoneNumber.text = order.phone
        address.text = order.address
        time.text = SimpleDateFormat("HH:mm").format(order.dateStart)


    }
}
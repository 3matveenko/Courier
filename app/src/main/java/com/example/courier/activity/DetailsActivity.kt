package com.example.courier.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.courier.R

class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        val text: TextView = findViewById(R.id.textView2)
        val order:String = intent.getStringExtra("order").toString()
        text.text = order


    }
}
package com.example.courier.activity

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.courier.MainActivity
import com.example.courier.R
import com.example.courier.connect.Rabbit
import com.example.courier.models.GetSettings
import com.example.courier.models.Order
import com.example.courier.models.SendSms
import com.google.gson.GsonBuilder
import java.lang.reflect.Modifier
import java.text.SimpleDateFormat
import com.example.courier.activity.HomeActivity.Companion.orders

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
        val rejectButton = findViewById<Button>(R.id.reject_in_details)
        val sendSmsButton = findViewById<Button>(R.id.send_sms)
        val backButton = findViewById<Button>(R.id.back)
        var editCode = findViewById<EditText>(R.id.editCode)
        val checkedButton = findViewById<Button>(R.id.checkButton)

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

        if(GetSettings(this).isNull("id_"+order.id)){
            editCode.visibility = View.GONE
            checkedButton.visibility = View.GONE
        }

        backButton.setOnClickListener {
            finish()
        }
        sendSmsButton.setOnClickListener {
            if(MainActivity.connectionFlag){
            val dialogView = layoutInflater.inflate(R.layout.send_sms_alert, null)

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            dialogView.findViewById<Button>(R.id.alert_sms_no).setOnClickListener {
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.alert_sms_yes).setOnClickListener {
                var randomNumber:String
                if(GetSettings(this).isNull("id_"+order.id)){
                    val random = java.util.Random()
                    randomNumber = String.format("%04d", random.nextInt(10000))
                    GetSettings(this).save("id_"+order.id,randomNumber)
                } else {
                    randomNumber = GetSettings(this).load("id_"+order.id)
                }
                editCode.visibility = View.VISIBLE
                checkedButton.visibility = View.VISIBLE
                Toast.makeText(this, "Cообщение отправлено! $randomNumber",Toast.LENGTH_LONG).show()
                Rabbit(this).sendMessage(GetSettings(this).load(GetSettings.TOKEN),"send_sms",gson.toJson(SendSms(order.phone,randomNumber)))
                dialog.dismiss()
            }
            dialog.show()
            } else {
                Toast.makeText(this,"Нет интернета!",Toast.LENGTH_SHORT).show()
            }
        }

        checkedButton.setOnClickListener {
                editCode = findViewById(R.id.editCode)
                if(GetSettings(this).load("id_"+order.id)==editCode.text.toString()){
                    GetSettings(this).remove("id_"+order.id)
                    Toast.makeText(this,"Заказ успешно доставлен",Toast.LENGTH_LONG).show()
                    var token = GetSettings(this).load(GetSettings.TOKEN)
                    Rabbit(this).sendMessage(token,"order_success",order.id.toString())
                    orders?.toMutableList()?.remove(order)
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this,"Код введен не верно!",Toast.LENGTH_LONG).show()
                    editCode.text.clear()
                }
        }

        phoneNumber.setOnClickListener{
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("", order.phone)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Скопировано", Toast.LENGTH_SHORT).show()
        }
    }
}
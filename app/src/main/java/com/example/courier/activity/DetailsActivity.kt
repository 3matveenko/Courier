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
import com.example.courier.R
import com.example.courier.service.Rabbit
import com.example.courier.enums.RabbitCode
import com.example.courier.enums.SettingsValue
import com.example.courier.models.GetSettings
import com.example.courier.models.Order
import com.example.courier.models.SendSms
import com.google.gson.GsonBuilder
import java.lang.reflect.Modifier
import java.text.SimpleDateFormat

class DetailsActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId", "SimpleDateFormat", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val ordersNumber = findViewById<TextView>(R.id.orders_number_text)
        val currentName = findViewById<TextView>(R.id.current_text)
        val phoneNumber = findViewById<TextView>(R.id.phone_number_text)
        val address = findViewById<TextView>(R.id.address_text)
        val time = findViewById<TextView>(R.id.time_text)
        val rejectButton = findViewById<Button>(R.id.reject_in_details)
        val sendSmsButton = findViewById<Button>(R.id.send_sms)
        val backButton = findViewById<Button>(R.id.back)
        var editCode = findViewById<EditText>(R.id.editCode)
        val checkedButton = findViewById<Button>(R.id.checkButton)
        val rejectedTextView = findViewById<TextView>(R.id.rejectedView)
        val comment = findViewById<TextView>(R.id.comment)
        val token = GetSettings(this).load(SettingsValue.TOKEN.value)

        val orderString: String = intent.getStringExtra("order").toString()
        val gson = GsonBuilder()
            .setDateFormat("hh:mm")
            .excludeFieldsWithModifiers(
                Modifier.STATIC,
                Modifier.TRANSIENT,
                Modifier.VOLATILE
            )
            .create()
        val order: Order = gson.fromJson(orderString, Order::class.java)
        if (order.rejectOrder != null) {
            rejectedTextView.text = "Заказ от " + order.rejectOrder.driver.name
        } else {
            rejectedTextView.visibility = View.GONE
        }
        ordersNumber.text = order.guid
        currentName.text = order.current
        phoneNumber.text = order.phone
        address.text = order.address
        time.text = SimpleDateFormat("HH:mm").format(order.dateStart)
        comment.text = order.comment

        if (GetSettings(this).isNull("id_" + order.id)) {
            editCode.visibility = View.GONE
            checkedButton.visibility = View.GONE
        }

        backButton.setOnClickListener {
            startActivity(Intent(this@DetailsActivity, HomeActivity::class.java))
            finish()
        }
        rejectButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.reject_order_alert, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            dialogView.findViewById<Button>(R.id.button_close_alert_reject_order)
                .setOnClickListener {
                    dialog.dismiss()
                }
            dialogView.findViewById<Button>(R.id.button_send_comment_reject_order)
                .setOnClickListener {
                    val comment = dialogView.findViewById<EditText>(R.id.commentRejectEditText)
                    if (comment.text.toString() != "") {
                        Rabbit(this).sendMessage(
                            GetSettings(this).load(SettingsValue.TOKEN.value),
                            RabbitCode.REJECT_ORDER,
                            comment.text.toString()
                        )
                        dialog.dismiss()
                        startActivity(Intent(this@DetailsActivity, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Добавьте причину отказа!", Toast.LENGTH_LONG).show()
                    }
                }
            dialog.show()
        }
        sendSmsButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.send_sms_alert, null)

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            dialogView.findViewById<Button>(R.id.alert_sms_no).setOnClickListener {
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.alert_sms_yes).setOnClickListener {
                val randomNumber: String
                if (GetSettings(this).isNull("id_" + order.id)) {
                    val random = java.util.Random()
                    randomNumber = String.format("%04d", random.nextInt(10000))
                    GetSettings(this).save("id_" + order.id, randomNumber)
                } else {
                    randomNumber = GetSettings(this).load("id_" + order.id)
                }
                editCode.visibility = View.VISIBLE
                checkedButton.visibility = View.VISIBLE
                Rabbit(this).sendMessage(
                    GetSettings(this).load(SettingsValue.TOKEN.value),
                    RabbitCode.SEND_SMS,
                    gson.toJson(SendSms(order.phone, randomNumber))
                )
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.close_no_sms).setOnClickListener {
                GetSettings(this).remove("id_" + order.id)
                Toast.makeText(this, "Заказ доставлен без подтверждения", Toast.LENGTH_LONG).show()
                Rabbit(this).sendMessage(
                    token,
                    RabbitCode.ORDER_SUCCESS_NOT_SOLD,
                    order.id.toString()
                )
                startActivity(Intent(this@DetailsActivity, HomeActivity::class.java))
                finish()
            }
            dialog.show()
        }

        checkedButton.setOnClickListener {
            editCode = findViewById(R.id.editCode)


            if (GetSettings(this).load("id_" + order.id) == editCode.text.toString()) {
                GetSettings(this).remove("id_" + order.id)
                Toast.makeText(this, "Заказ успешно доставлен", Toast.LENGTH_LONG).show()
                Rabbit(this).sendMessage(token, RabbitCode.ORDER_SUCCESS, order.id.toString())
                startActivity(Intent(this@DetailsActivity, HomeActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Код введен не верно!", Toast.LENGTH_LONG).show()
                editCode.text.clear()
            }
        }

        phoneNumber.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("", order.phone)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Скопировано", Toast.LENGTH_SHORT).show()
        }

        address.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("", order.address)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Скопирован адрес", Toast.LENGTH_SHORT).show()
        }

        address.setOnLongClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(
                "",
                order.latitude.toString() + "," + order.longitude.toString()
            )
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Скопированы координаты", Toast.LENGTH_SHORT).show()
            true
        }
    }
}
package com.example.courier.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.courier.R
import com.example.courier.rest.MyBroadcastReceiver
import com.example.courier.rest.Rabbit
import com.example.courier.rest.ServerPing
import kotlin.system.exitProcess

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val intentFilter = IntentFilter("no_connection")
        val receiver = MyBroadcastReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)

        val intentFilter2 = IntentFilter("open_new_order")
        val receiver2 = MyBroadcastReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver2, intentFilter2)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Дайте разрешение выводить приложение поверх других окон!", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + packageName)
            )
            startActivity(intent)
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        ServerPing(this).main()
        Rabbit(this).startListening()
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
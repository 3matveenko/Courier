package com.example.courier.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Process
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.courier.R

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }
    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            @SuppressLint("CutPasteId", "UseCompatLoadingForDrawables")
            override fun handleOnBackPressed() {
                try {
                    moveTaskToBack(true)
                    Process.killProcess(Process.myPid())
                    System.exit(1)
                } catch (throwable: Throwable) {
                    throwable.printStackTrace()
                }
            }
        }
}
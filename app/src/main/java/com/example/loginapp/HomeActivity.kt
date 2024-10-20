package com.example.loginapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // For now, avoid using ViewCompat until we are sure the layout loads correctly.
        // Comment out any window inset listener code if it exists.
    }
}

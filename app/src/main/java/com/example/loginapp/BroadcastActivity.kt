package com.example.loginapp

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class BroadcastActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broadcast)

        // Find the ImageView for the ripple effect
        val rippleEffect: ImageView = findViewById(R.id.ripple_effect)

        // Use Glide to load and animate the GIF
        Glide.with(this)
            .asGif() // Ensure Glide handles it as a GIF
            .load(R.drawable.ripple) // Reference the GIF file (no extension)
            .into(rippleEffect) // Set it to the ImageView
    }

    // Add any other methods, listeners, or logic here for your BroadcastActivity
}

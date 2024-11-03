package com.example.loginapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide

class BroadcastActivity : AppCompatActivity() {
    private val REQUEST_CODE_BLUETOOTH = 1001


    private var advertiser: BluetoothLeAdvertiser? = null
    private var isAdvertising = false
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            isAdvertising = true
            Log.d("com.example.loginapp.BroadcastActivity", "Advertising started successfully")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            isAdvertising = false
            Log.e("com.example.loginapp.BroadcastActivity", "Advertising failed with error code: $errorCode")
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
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
        // Initialize Bluetooth adapter
        bluetoothAdapter = (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter

        // Find the button and set up an intent to go to PreferencesActivity
        // Get the username from intent extras
        val username = intent.getStringExtra("username") ?: "test"
        val goToPreferencesButton: Button = findViewById(R.id.go_to_preferences_button)

        goToPreferencesButton.setOnClickListener {
            val intent = Intent(this@BroadcastActivity, HomeActivity::class.java)
            intent.putExtra("username", username) // Pass the username here
            Log.d("HomeActivity", "Passing username: $username")
            startActivity(intent)
            finish() // Optional: call finish() to close the current activity

            checkBluetoothPermissions(username) // Check permissions before starting advertising
        }
    }

        @RequiresApi(Build.VERSION_CODES.S)
        fun checkBluetoothPermissions(username: String) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                Log.e("BroadcastActivity", "Bluetooth or location permissions not granted")
                requestBluetoothAdvertisePermission() // Request necessary permissions
            } else {
                startAdvertising(username) // Start advertising if permission is granted
            }
        }

        @RequiresApi(Build.VERSION_CODES.S)
        fun startAdvertising(username: String) {
            // Check if Bluetooth is enabled
            if (!bluetoothAdapter.isEnabled) {
                Log.e("BroadcastActivity", "Bluetooth is disabled")
                return
            }

            // Set up advertising settings
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(false)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .build()

            // Create advertising data with just the username
            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceData(
                    android.os.ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB"),
                    username.toByteArray(Charsets.UTF_8)
                )
                .build()

            // Start advertising
            advertiser = bluetoothAdapter.bluetoothLeAdvertiser
            try {
                advertiser?.startAdvertising(settings, data, advertiseCallback)
            } catch (e: SecurityException) {
                Log.e("BroadcastActivity", "Error starting advertising: ${e.message}")
            }
        }

        fun stopAdvertising() {
            if (isAdvertising) {
                // Check if the necessary Bluetooth permissions are granted
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_ADVERTISE
                    ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {

                    advertiser?.stopAdvertising(advertiseCallback)
                    isAdvertising = false // Update advertising state
                    Log.d("com.example.loginapp.BroadcastActivity", "Advertising stopped")
                } else {
                    // Handle the case where permission is not granted
                    Log.e(
                        "com.example.loginapp.BroadcastActivity",
                        "Bluetooth advertising permission not granted"
                    )
                    // Optionally, inform the user that permissions are required to stop advertising
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.S)
        fun requestBluetoothAdvertisePermission() {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION // Add location permission here
                ),
                REQUEST_CODE_BLUETOOTH
            )
        }

        @RequiresApi(Build.VERSION_CODES.S)
        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == REQUEST_CODE_BLUETOOTH) {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d("BroadcastActivity", "Bluetooth permissions granted")
                    val username = intent.getStringExtra("username") ?: "test" // Get the username
                    startAdvertising(username) // Retry advertising after permission is granted
                } else {
                    Log.e("BroadcastActivity", "Bluetooth permissions denied")
                    // Optionally inform the user about the denial
                }
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            stopAdvertising()
        }
    }
























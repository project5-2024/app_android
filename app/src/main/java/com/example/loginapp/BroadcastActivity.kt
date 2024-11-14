package com.example.loginapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide

class BroadcastActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var advertiser: BluetoothLeAdvertiser? = null
    private var isAdvertising = false

    private val PERMISSION_REQUEST_CODE = 1001
    private val username by lazy { intent.getStringExtra("username") ?: "default_user" }
    private val userId by lazy { intent.getStringExtra("userId") ?: "default_user" }


    // Bluetooth enabling result handler
    private val bluetoothEnablingResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d("BroadcastActivity", "Bluetooth enabled by user, starting advertising.")
            startAdvertising()
        } else {
            Log.d("BroadcastActivity", "Bluetooth not enabled by user, cannot start advertising.")
            promptEnableBluetooth()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broadcast)

        // Find the ImageView for the ripple effect
        val rippleEffect: ImageView = findViewById(R.id.ripple_effect)

        // Use Glide to load and animate the GIF
        Glide.with(this)
            .asGif()
            .load(R.drawable.ripple)
            .into(rippleEffect)

        Log.d("BroadcastActivity", "onCreate called. Initializing Bluetooth adapter.")

        bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        checkPermissionsAndAdvertise()

        // Find the button and set up an intent to go to PreferencesActivity
        val goToPreferencesButton: Button = findViewById(R.id.go_to_preferences_button)
        goToPreferencesButton.setOnClickListener {
            val username = intent.getStringExtra("username")
            val intent = Intent(this@BroadcastActivity, HomeActivity::class.java)
            intent.putExtra("username", username) // Pass the username here
            intent.putExtra("userId", userId)
            Log.d("HomeActivity", "Passing username: $username")
            startActivity(intent)
            finish() // Optional: call finish() to close the current activity
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("BroadcastActivity", "onResume called. Checking if Bluetooth is enabled.")

        if (!bluetoothAdapter.isEnabled) {
            Log.d("BroadcastActivity", "Bluetooth is disabled. Prompting user to enable it.")
            promptEnableBluetooth()
        }
    }

    private fun Context.hasPermission(permissionType: String): Boolean {
        val granted = ContextCompat.checkSelfPermission(this, permissionType) == PackageManager.PERMISSION_GRANTED
        Log.d("BroadcastActivity", "Permission check for $permissionType: $granted")
        return granted
    }

    fun Context.hasRequiredBluetoothPermissions(): Boolean {
        val scanPermissionGranted = hasPermission(Manifest.permission.BLUETOOTH_SCAN)
        val connectPermissionGranted = hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        val advertisePermissionGranted = hasPermission(Manifest.permission.BLUETOOTH_ADVERTISE)

        Log.d("PermissionCheck", "BLUETOOTH_SCAN granted: $scanPermissionGranted")
        Log.d("PermissionCheck", "BLUETOOTH_CONNECT granted: $connectPermissionGranted")
        Log.d("PermissionCheck", "BLUETOOTH_ADVERTISE granted: $advertisePermissionGranted")

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            scanPermissionGranted && connectPermissionGranted && advertisePermissionGranted
        } else {
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    private fun requestRelevantRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d("BroadcastActivity", "Requesting Bluetooth permissions for Android 12+.")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE

                ),
                PERMISSION_REQUEST_CODE
            )
        } else {
            Log.d("BroadcastActivity", "Requesting location permission for Android 11 and below.")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun checkPermissionsAndAdvertise() {
        Log.d("BroadcastActivity", "Checking permissions and advertising if permitted.")
        if (!hasRequiredBluetoothPermissions()) {
            Log.d("BroadcastActivity", "Required Bluetooth permissions are missing.")
            requestRelevantRuntimePermissions()
        } else {
            Log.d("BroadcastActivity", "Permissions granted, starting advertising.")
            startAdvertising()
        }
    }

    private fun startAdvertising() {
        if (!bluetoothAdapter.isEnabled) {
            Log.d("BroadcastActivity", "Bluetooth is disabled, cannot start advertising.")
            promptEnableBluetooth()
            return
        }

        advertiser = bluetoothAdapter.bluetoothLeAdvertiser
        Log.d("BroadcastActivity", "Configuring advertising settings.")

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(false)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        val data = AdvertiseData.Builder()
            .addServiceData(
                android.os.ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB"),
                userId.toByteArray(Charsets.UTF_8)
            )
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("BroadcastActivity", "BLUETOOTH_ADVERTISE permission not granted, cannot start advertising.")
            return
        }

        Log.d("BroadcastActivity", "Starting Bluetooth LE advertising.")
        advertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            isAdvertising = true
            Log.d("BroadcastActivity", "Advertising started successfully with userId: $userId")
        }

        override fun onStartFailure(errorCode: Int) {
            isAdvertising = false
            Log.e("BroadcastActivity", "Advertising failed with error code: $errorCode")
        }
    }

    private fun promptEnableBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        ) return

        if (!bluetoothAdapter.isEnabled) {
            Log.d("BroadcastActivity", "Prompting user to enable Bluetooth.")
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                bluetoothEnablingResult.launch(this)
            }
        }
    }

    private fun requestLocationPermission() {
        Log.d("BroadcastActivity", "Displaying dialog for location permission request.")
        AlertDialog.Builder(this)
            .setTitle("Location permission required")
            .setMessage("Location access is required to scan for BLE devices.")
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                Log.d("BroadcastActivity", "User accepted location permission dialog.")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_CODE
                )
            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestBluetoothPermissions() {
        Log.d("BroadcastActivity", "Displaying dialog for Bluetooth permissions request.")
        AlertDialog.Builder(this)
            .setTitle("Bluetooth permission required")
            .setMessage("Bluetooth access is required to scan and connect to BLE devices.")
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                Log.d("BroadcastActivity", "User accepted Bluetooth permissions dialog.")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_ADVERTISE

                    ),
                    PERMISSION_REQUEST_CODE
                )
            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != PERMISSION_REQUEST_CODE) return

        val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (allGranted) {
            Log.d("BroadcastActivity", "All required permissions granted.")
            startAdvertising()
        } else {
            Log.d("BroadcastActivity", "Some permissions were denied.")
            requestBluetoothPermissions() // Re-request permissions if denied
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d("BroadcastActivity", "onDestroy called. Checking if advertising should be stopped.")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("BroadcastActivity", "BLUETOOTH_ADVERTISE permission not granted, cannot stop advertising.")
            return
        }
        if (isAdvertising) {
            Log.d("BroadcastActivity", "Stopping Bluetooth LE advertising.")
            advertiser?.stopAdvertising(advertiseCallback)
        }
    }
}

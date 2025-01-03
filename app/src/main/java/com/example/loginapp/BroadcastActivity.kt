package com.example.loginapp

import android.Manifest
import android.annotation.SuppressLint
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
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide

class BroadcastActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var advertiser: BluetoothLeAdvertiser? = null
    private var isAdvertising = false
    private var geolocationEnabled = false

    private val PERMISSION_REQUEST_CODE = 1001
    private val username by lazy { intent.getStringExtra("username") ?: "default_user" }
    private val userId by lazy { intent.getStringExtra("userId") ?: "default_user" }

    private val bluetoothEnablingResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d("BroadcastActivity", "Bluetooth enabled by user, starting advertising.")
            startTimedAdvertising()
        } else {
            Log.d("BroadcastActivity", "Bluetooth not enabled by user, cannot start advertising.")
            promptEnableBluetooth()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broadcast)

        val rippleEffect: ImageView = findViewById(R.id.ripple_effect)

        Glide.with(this)
            .asGif()
            .load(R.drawable.ripple)
            .into(rippleEffect)

        Log.d("BroadcastActivity", "onCreate called. Initializing Bluetooth adapter.")

        bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        checkPermissionsAndAdvertise()

        val geolocationToggle: Switch = findViewById(R.id.geolocation_toggle)
        geolocationToggle.setOnCheckedChangeListener { _, isChecked ->
            geolocationEnabled = isChecked
            if (isAdvertising) {
                stopAdvertising()
                startAdvertising()
            } else {
                startAdvertising()
            }
        }

        val goToPreferencesButton: Button = findViewById(R.id.go_to_preferences_button)
        goToPreferencesButton.setOnClickListener {
            val username = intent.getStringExtra("username")
            val intent = Intent(this@BroadcastActivity, HomeActivity::class.java)
            intent.putExtra("username", username) 
            intent.putExtra("userId", userId)
            Log.d("BroadcastActivity", "Passing username: $username")
            startActivity(intent)
            finish() 
        }

        val goToMapButton: Button = findViewById(R.id.go_to_map_button)
        goToMapButton.setOnClickListener {
            val intent = Intent(this@BroadcastActivity, MapActivity::class.java)
            Log.d("BroadcastActivity", "Navigating to MapActivity. Passing username: $username")
            intent.putExtra("username", username) 
            startActivity(intent)
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
            Log.d("BroadcastActivity", "Permissions granted, starting timed advertising.")
            startTimedAdvertising()
        }
    }

    private fun startTimedAdvertising() {
        val handlerThread = HandlerThread("BluetoothAdvertiserThread")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        val advertiseInterval = 5000L 

        val runnable = object : Runnable {
            override fun run() {
                if (!bluetoothAdapter.isEnabled) {
                    Log.d("BroadcastActivity", "Bluetooth is disabled, cannot start advertising.")
                    promptEnableBluetooth()
                    handlerThread.quitSafely() 
                    return
                }

                if (!isAdvertising) {
                    startAdvertising()
                } else {
                    stopAdvertising()
                }

                handler.postDelayed(this, advertiseInterval)
            }
        }

        handler.post(runnable)
    }


    private fun startAdvertising() {
        advertiser = bluetoothAdapter.bluetoothLeAdvertiser
        Log.d("BroadcastActivity", "Configuring advertising settings.")

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(false)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setTimeout(0)
            .build()

        val geolocationState = if (geolocationEnabled) "1" else "0"
        val data = AdvertiseData.Builder()
            .addServiceData(
                android.os.ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB"),
                "$userId|$geolocationState".toByteArray(Charsets.UTF_8)
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

    @SuppressLint("MissingPermission")
    private fun stopAdvertising() {
        if (isAdvertising) {
            Log.d("BroadcastActivity", "Stopping advertising.")
            advertiser?.stopAdvertising(advertiseCallback)
            isAdvertising = false
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            isAdvertising = true
            Log.d("BroadcastActivity", "Advertising started successfully with userId: $userId and geolocation: $geolocationEnabled")
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

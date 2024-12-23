package com.example.loginapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONArray
import org.json.JSONObject
import org.json.*
import java.util.Objects

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mylocation = LatLng(0.0,0.0);
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval: Long = 60000 // 1 minute in milliseconds
    private val distances = mutableMapOf<String, Float>()
    private val preferences = mutableMapOf<String, Float>("music" to 0f, "food" to 0f, "travel" to 0f, "movies" to 0f, "technology" to 0f, "fitness" to 0f, "gaming" to 0f, "books" to 0f, "fashion" to 0f)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Initialize location provider
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Load the map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this) // Call onMapReady when map is loaded
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        getCurrentLocation() // Fetch location after map is ready
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            return
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                googleMap.addMarker(MarkerOptions().position(currentLatLng).title("You are here"))
                mylocation = currentLatLng
                loadNearbyShops(location) // Load nearby places
            } else {
                Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadNearbyShops(location: Location) {
        val radius = 1000 // 1 km radius
        val apiKey = "AIzaSyDMOr-Z3XYTZC8rt9DFopMioNAimNBav5M"

        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=${location.latitude},${location.longitude}&radius=$radius&key=$apiKey"

        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->
                try {
                    val results = JSONObject(response).getJSONArray("results")
                    if (results.length() == 0) {
                        Log.d("MapActivity", "No shops found nearby.")
                        return@Listener
                    }

                    val poiCounter = mutableMapOf<String, Int>()

                    for (i in 0 until results.length()) {
                        val place = results.getJSONObject(i)
                        val name = place.getString("name")
                        val lat = place.getJSONObject("geometry").getJSONObject("location").getDouble("lat")
                        val lng = place.getJSONObject("geometry").getJSONObject("location").getDouble("lng")
                        val latLng = LatLng(lat, lng)

                        // Calculate the distance
                        val myloc = Location("mylocation").apply {
                            latitude = location.latitude
                            longitude = location.longitude
                        }
                        val loc = Location("destination").apply {
                            latitude = latLng.latitude
                            longitude = latLng.longitude
                        }
                        val distance = myloc.distanceTo(loc)

                        googleMap.addMarker(MarkerOptions().position(latLng).title(name))

                        val types = place.getJSONArray("types")
                        for (j in 0 until types.length()) {
                            val type = types.getString(j)

                            val category = when {
                                type.contains("lodging") || type.contains("travel_agency") || type.contains("airport") ||
                                        type.contains("bus_station") || type.contains("train_station") || type.contains("subway_station") ||
                                        type.contains("taxi_stand") || type.contains("transit_station") -> "travel"

                                type.contains("gym") || type.contains("spa") || type.contains("health") ||
                                        type.contains("stadium") || type.contains("park") -> "fitness"

                                type.contains("movie_theater") || type.contains("drive_in_theater") -> "movies"

                                type.contains("restaurant") || type.contains("cafe") || type.contains("bakery") ||
                                        type.contains("bar") || type.contains("food") || type.contains("meal_takeaway") ||
                                        type.contains("grocery_or_supermarket") || type.contains("liquor_store") ||
                                        type.contains("convenience_store") -> "food"

                                type.contains("stadium") || type.contains("sports_complex") || type.contains("bowling_alley") ||
                                        type.contains("ice_skating_rink") || type.contains("golf_course") || type.contains("park") -> "sports"

                                type.contains("shopping_mall") || type.contains("clothing_store") || type.contains("shoe_store") ||
                                        type.contains("jewelry_store") || type.contains("department_store") || type.contains("boutique") -> "fashion"

                                type.contains("electronics_store") || type.contains("hardware_store") || type.contains("computer_store") ||
                                        type.contains("phone_store") || type.contains("home_goods_store") -> "technology"

                                type.contains("amusement_park") || type.contains("arcade") || type.contains("casino") ||
                                        type.contains("escape_room") -> "gaming"

                                type.contains("art_gallery") || type.contains("museum") || type.contains("music_venue") ||
                                        type.contains("night_club") || type.contains("theater") -> "music"

                                type.contains("library") || type.contains("book_store") || type.contains("university") || type.contains("school") -> "books"

                                else -> null
                            }

                            if (category != null) {
                                poiCounter[category] = poiCounter.getOrDefault(category, 0) + 1
                            }
                        }
                    }

                    // Update preferences
                    for ((category, count) in poiCounter) {
                        val multiplier = count.toFloat() / radius
                        preferences[category] = multiplier
                        Log.d("type count", "Category: $category, Count: $count, Multiplier: $multiplier")
                    }
                    Log.d("Preferences multiplier", preferences.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                Log.e("MapActivity", "Error: ${error.message}")
            })

        requestQueue.add(stringRequest)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
    }
}
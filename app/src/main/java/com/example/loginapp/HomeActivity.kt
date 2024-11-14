package com.example.loginapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.android.material.slider.Slider
import androidx.appcompat.app.AppCompatActivity
import com.example.loginapp.apiUsage.EmptyRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.loginapp.apiUsage.SetPreferencesRequest
import com.example.loginapp.apiUsage.GetPreferencesResponse  // Response class for GET preferences

class HomeActivity : AppCompatActivity() {

    private lateinit var slider1: Slider
    private lateinit var slider2: Slider
    private lateinit var slider3: Slider
    private lateinit var slider4: Slider
    private lateinit var slider5: Slider
    private lateinit var slider6: Slider
    private lateinit var slider7: Slider
    private lateinit var slider8: Slider
    private lateinit var slider9: Slider
    private lateinit var slider10: Slider

    private lateinit var doneButton: Button
    private lateinit var apiService: ApiService
    private val username by lazy { intent.getStringExtra("username") ?: "default_user" }
    private val userId by lazy { intent.getStringExtra("userId") ?: "default_user" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize the sliders and button
        slider1 = findViewById(R.id.slider_1)
        slider2 = findViewById(R.id.slider_2)
        slider3 = findViewById(R.id.slider_3)
        slider4 = findViewById(R.id.slider_4)
        slider5 = findViewById(R.id.slider_5)
        slider6 = findViewById(R.id.slider_6)
        slider7 = findViewById(R.id.slider_7)
        slider8 = findViewById(R.id.slider_8)
        slider9 = findViewById(R.id.slider_9)
        slider10 = findViewById(R.id.slider_10)
        doneButton = findViewById(R.id.done_btn)

        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://185.94.45.58:7832/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)




        if (username.isNotEmpty()) {
            Log.d("GetPreferences user id",  userId)
            Log.d("GetPreferences username",  username)
            getPreferences(userId)  // Load preferences when activity opens
        } else {
            Log.e("HomeActivity", "Username is null or empty")
        }

        // Set click listener for the Done button
        doneButton.setOnClickListener {
            val preferences = getSliderValues()
            makeSetPreferencesRequest(preferences, username)
        }
    }

    // Function to retrieve preferences when activity is loaded
    private fun getPreferences(userId: String) {
        val call = apiService.getPreferences(userId)
        call.enqueue(object : Callback<GetPreferencesResponse> {
            override fun onResponse(call: Call<GetPreferencesResponse>, response: Response<GetPreferencesResponse>) {
                if (response.isSuccessful) {
                    Log.d("GetPreferences", "Raw response body: ${response.body()}")
                    val preferences = response.body()
                    if (preferences != null) {
                        // Directly set the sliders based on the response
                        val prefsMap = mapOf(
                            "sports" to preferences.sports,
                            "music" to preferences.music,
                            "food" to preferences.food,
                            "travel" to preferences.travel,
                            "movies" to preferences.movies,
                            "technology" to preferences.technology,
                            "fitness" to preferences.fitness,
                            "gaming" to preferences.gaming,
                            "books" to preferences.books,
                            "fashion" to preferences.fashion
                        )
                        setSliderValues(prefsMap)
                    } else {
                        Log.e("GetPreferences", "Preferences not found in response")
                    }
                } else {
                    Log.e("GetPreferences", "Failed to get preferences: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<GetPreferencesResponse>, t: Throwable) {
                Log.e("GetPreferences", "Error fetching preferences: ${t.message}")
            }
        })
    }



    // Set sliders based on fetched preferences
    private fun setSliderValues(preferences: Map<String, Int>) {
        slider1.value = preferences["sports"]?.toFloat() ?: 0f
        slider2.value = preferences["music"]?.toFloat() ?: 0f
        slider3.value = preferences["food"]?.toFloat() ?: 0f
        slider4.value = preferences["travel"]?.toFloat() ?: 0f
        slider5.value = preferences["movies"]?.toFloat() ?: 0f
        slider6.value = preferences["technology"]?.toFloat() ?: 0f
        slider7.value = preferences["fitness"]?.toFloat() ?: 0f
        slider8.value = preferences["gaming"]?.toFloat() ?: 0f
        slider9.value = preferences["books"]?.toFloat() ?: 0f
        slider10.value = preferences["fashion"]?.toFloat() ?: 0f
    }

    private fun getSliderValues(): Map<String, Int> {
        return mapOf(
            "sports" to slider1.value.toInt(),
            "music" to slider2.value.toInt(),
            "food" to slider3.value.toInt(),
            "travel" to slider4.value.toInt(),
            "movies" to slider5.value.toInt(),
            "technology" to slider6.value.toInt(),
            "fitness" to slider7.value.toInt(),
            "gaming" to slider8.value.toInt(),
            "books" to slider9.value.toInt(),
            "fashion" to slider10.value.toInt()
        )
    }

    private fun makeSetPreferencesRequest(preferences: Map<String, Int>, username: String) {
        val setPreferencesRequest = SetPreferencesRequest(preferences)
        Log.d("SetPreferences", "Sending preferences: $preferences")
        val call = apiService.setPreferences(username, setPreferencesRequest)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.i("SetPreferences", "Preferences set successfully!")
                    val intent = Intent(this@HomeActivity, BroadcastActivity::class.java)
                    intent.putExtra("username", username)
                    intent.putExtra("userId", userId)
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("SetPreferences", "Failed to set preferences: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("SetPreferences", "Error setting preferences: ${t.message}")
            }
        })
    }
}

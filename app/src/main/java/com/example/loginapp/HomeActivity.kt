package com.example.loginapp;
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.android.material.slider.Slider
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.loginapp.apiUsage.SetPreferencesRequest

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

        // Set click listener for the Done button
        doneButton.setOnClickListener {
            val preferences = getSliderValues()
            val username = "jeff" // Replace with the actual username variable if available
            makeSetPreferencesRequest(preferences, username)
        }

    }

    // Function to retrieve the slider values
    private fun getSliderValues(): Map<String, Int> {
        val preferencesMap = mutableMapOf<String, Int>()
        preferencesMap["sports"] = slider1.value.toInt()
        preferencesMap["music"] = slider2.value.toInt()
        preferencesMap["food"] = slider3.value.toInt()
        preferencesMap["travel"] = slider4.value.toInt()
        preferencesMap["movies"] = slider5.value.toInt()
        preferencesMap["technology"] = slider6.value.toInt()
        preferencesMap["fitness"] = slider7.value.toInt()
        preferencesMap["gaming"] = slider8.value.toInt()
        preferencesMap["books"] = slider9.value.toInt()
        preferencesMap["fashion"] = slider10.value.toInt()


        return preferencesMap
    }


    // Function to make the API call to set preferences
    private fun makeSetPreferencesRequest(preferences: Map<String, Int>, username: String) {
        val setPreferencesRequest = SetPreferencesRequest(preferences)

        val call = apiService.setPreferences(username, setPreferencesRequest)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.i("SetPreferences", "Preferences set successfully!")
                } else {
                    Log.e("SetPreferences", "Failed to set preferences: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("SetPreferences", "Error: ${t.message}")
            }
        })
    }


}

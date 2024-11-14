package com.example.loginapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.loginapp.apiUsage.LoginRequest
import com.example.loginapp.apiUsage.LoginResponse
import com.example.loginapp.apiUsage.RegisterRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    lateinit var usernameInput: EditText
    lateinit var passwordInput: EditText
    lateinit var loginBtn: Button
    lateinit var registerBtn: Button
    lateinit var apiService: ApiService
    lateinit var username: String
    lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        loginBtn = findViewById(R.id.login_btn)
        registerBtn = findViewById(R.id.register_btn)

        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://185.94.45.58:7832/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        // Handle Login Button Click
        loginBtn.setOnClickListener {
             username = usernameInput.text.toString()
            val password = passwordInput.text.toString()
            makeLoginRequest(username, password)
        }

        // Handle Register Button Click
        registerBtn.setOnClickListener {
             username = usernameInput.text.toString()
            val password = passwordInput.text.toString()
            makeRegisterRequest(username, password)
        }
    }

    private fun makeLoginRequest(username: String, password: String) {
        val loginRequest = LoginRequest(username, password)
        val call = apiService.login(loginRequest)

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    Log.i("Login", "Login successful! Response: $loginResponse")

                    // You can also log individual fields from the response if needed
                    Log.d("Login", "User ID: ${loginResponse?.userId}")
                    Log.d("Login", "Token: ${loginResponse?.token}")

                    // Switch to HomeActivity on successful login
                    val intent = Intent(this@MainActivity, BroadcastActivity::class.java)
                    intent.putExtra("username", username)
                    intent.putExtra("userId", loginResponse?.userId) // Pass the username here
                    Log.d("MainActivity", "Passing username: $username")
                    startActivity(intent)
                    finish() // Optional: call finish() to close the current activity
                } else {
                    // Log the error response details
                    Log.e("Login", "Login failed: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("Login", "Error: ${t.message}")
            }
        })
    }

    private fun makeRegisterRequest(username: String, password: String) {
        val registerRequest = RegisterRequest(username, password, is_admin = 1)

        // Log the registerRequest to see what is being sent
        Log.d("RegisterRequest", "Payload: $registerRequest")

        val call = apiService.register(registerRequest)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.i("Register", "Registration successful!: ${response.code()} - ${response.message()}")

                    // Assuming you get the 'userId' from the Login response after registration
                    // You can pass the userId here if you know it is available after registration
                    // For now, we'll just pass a placeholder userId (you can replace this with the actual ID)

                    val placeholderUserId = "newUserId"  // Replace with actual logic to get userId if available

                    // Switch to HomeActivity on successful registration
                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                    intent.putExtra("username", username) // Pass the username here
                    intent.putExtra("userId", placeholderUserId) // Pass the userId here
                    Log.d("MainActivity", "Passing username: $username, userId: $placeholderUserId")
                    startActivity(intent)
                    finish() // Optional: call finish() to close the current activity
                } else {
                    Log.e("Register", "Registration failed: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Register", "Error: ${t.message}")
            }
        })
    }



}

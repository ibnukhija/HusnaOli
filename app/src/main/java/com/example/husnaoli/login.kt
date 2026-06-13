package com.example.husnaoli

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.husnaoli.databinding.ActivityLoginBinding
import com.example.husnaoli.network.LoginResponse
import com.example.husnaoli.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val user = binding.etUsername.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Isi dulu bosku username & pass-nya!", Toast.LENGTH_SHORT).show()
            } else {
                checkLogin(user, pass)
            }
        }
    }

    private fun checkLogin(user: String, pass: String) {
        RetrofitClient.instance.login(user, pass).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null && loginResponse.status == "success") {
                        val userData = loginResponse.data

                        // Simpan Session Lengkap ke SharedPreferences
                        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putInt("USER_ID", userData?.userId ?: 0) // Simpan ID User
                        editor.putString("USER_NAMA", userData?.nama)
                        editor.putString("USER_ROLE", userData?.role)
                        editor.apply()

                        val role = userData?.role ?: ""
                        if (role.lowercase() == "admin") {
                            startActivity(Intent(this@login, DashboardActivity::class.java))
                        } else {
                            startActivity(Intent(this@login, DashboardKasirActivity::class.java))
                        }
                        finish()
                    } else {
                        Toast.makeText(this@login, loginResponse?.message ?: "Login Gagal", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@login, "Error Server: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("API_ERROR", t.message ?: "Unknown Error")
                Toast.makeText(this@login, "Koneksi Gagal! Pastikan Laragon aktif & IP benar.", Toast.LENGTH_LONG).show()
            }
        })
    }
}

package com.example.husnaoli

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.husnaoli.databinding.ActivityTambahUserBinding
import com.example.husnaoli.network.LoginResponse
import com.example.husnaoli.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TambahUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTambahUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHeader()
        setupSpinner()

        binding.btnSimpanUser.setOnClickListener {
            saveUser()
        }
    }

    private fun setupHeader() {
        binding.btnBackHeader.setOnClickListener { finish() }
        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupSpinner() {
        val roles = arrayOf("-- Pilih Jabatan --", "Admin", "Kasir")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRole.adapter = adapter
    }

    private fun saveUser() {
        val nama = binding.etNamaLengkap.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val role = binding.spinnerRole.selectedItem.toString()

        if (nama.isEmpty() || username.isEmpty() || password.isEmpty() || role == "-- Pilih Jabatan --") {
            Toast.makeText(this, "Harap lengkapi semua data!", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show()
            return
        }

        // Memanggil API tambah_user.php melalui Retrofit
        RetrofitClient.instance.tambahUser(nama, username, password, role.lowercase())
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res != null && res.status == "success") {
                            Toast.makeText(this@TambahUserActivity, res.message, Toast.LENGTH_SHORT).show()
                            finish() // Kembali ke UserFragment
                        } else {
                            Toast.makeText(this@TambahUserActivity, res?.message ?: "Gagal simpan", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@TambahUserActivity, "Error Server: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.e("API_ERROR", t.message ?: "Unknown Error")
                    Toast.makeText(this@TambahUserActivity, "Koneksi Gagal: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
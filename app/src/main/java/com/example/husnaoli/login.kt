package com.example.husnaoli

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.husnaoli.databinding.ActivityLoginBinding

class login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbHelper: DBHusnaOli

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHusnaOli(this)

        binding.btnLogin.setOnClickListener {
            val user = binding.etUsername.text.toString()
            val pass = binding.etPassword.text.toString()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Isi dulu bosku username & pass-nya!", Toast.LENGTH_SHORT).show()
            } else {
                checkLogin(user, pass)
            }
        }
    }

    private fun checkLogin(user: String, pass: String) {
        val db = dbHelper.readableDatabase
        // Cek username, password, dan ambil role serta nama
        val cursor = db.rawQuery(
            "SELECT role, nama FROM user WHERE username=? AND password=?",
            arrayOf(user, pass)
        )

        if (cursor.moveToFirst()) {
            val role = cursor.getString(0)
            val nama = cursor.getString(1)

            Toast.makeText(this, "Login Berhasil! Halo $nama", Toast.LENGTH_SHORT).show()

            if (role == "admin") {
                // Jika Admin ke Dashboard Utama
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
            } else {
                // Jika Kasir ke Dashboard Kasir yang baru kita buat
                val intent = Intent(this, DashboardKasirActivity::class.java)
                intent.putExtra("USER_NAMA", nama) // Kirim nama ke Dashboard
                startActivity(intent)
            }
            finish() // Tutup activity login supaya gak bisa di-back
        } else {
            Toast.makeText(this, "Username atau Password salah, Fi!", Toast.LENGTH_LONG).show()
        }
        cursor.close()
    }
}